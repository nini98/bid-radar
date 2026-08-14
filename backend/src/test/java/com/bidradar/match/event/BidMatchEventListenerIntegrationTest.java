package com.bidradar.match.event;

import com.bidradar.auth.domain.User;
import com.bidradar.auth.repository.UserRepository;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.event.BidNoticeCollectedEvent;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.company.domain.Company;
import com.bidradar.company.repository.CompanyRepository;
import com.bidradar.match.service.MatchCalculationService;
import com.bidradar.support.IntegrationTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * {@code @TransactionalEventListener(phase = AFTER_COMMIT)} + {@code @Async} 조합이 실제로
 * 커밋 후에만, 별도 스레드에서 동작하는지 검증한다 (transaction-rule.md §12, Issue #43).
 * 순수 단위 테스트({@code listener.handle()} 직접 호출)로는 트랜잭션 커밋/롤백에 따른
 * 실행 여부를 검증할 수 없어 실제 커밋/롤백이 필요하다.
 *
 * {@code companyRepository.findAll()}은 공유 Testcontainers 컨테이너에 남아있는 다른 테스트의
 * 데이터까지 모두 반환할 수 있으므로, 전체 호출 횟수가 아니라 이 테스트가 만든 특정
 * bid/company의 ID로 호출됐는지를 기준으로 검증한다.
 *
 * {@code @SpringBootTest}는 {@code @DataJpaTest}와 달리 테스트 종료 시 롤백되지 않고 실제로
 * 커밋되므로(AFTER_COMMIT 자체를 검증해야 해서 불가피함), 각 테스트가 만든 데이터를
 * {@link #cleanUp()}에서 직접 정리한다.
 */
@SpringBootTest
class BidMatchEventListenerIntegrationTest extends IntegrationTestBase {

    @Autowired
    BidNoticeRepository bidNoticeRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    PlatformTransactionManager transactionManager;

    @MockitoBean
    MatchCalculationService matchCalculationService;

    private final List<Long> createdCompanyIds = new ArrayList<>();
    private final List<Long> createdUserIds = new ArrayList<>();
    private final List<Long> createdBidNoticeIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        // FK 순서: companies → users, bid_notices는 독립.
        createdCompanyIds.forEach(companyRepository::deleteById);
        createdUserIds.forEach(userRepository::deleteById);
        createdBidNoticeIds.forEach(bidNoticeRepository::deleteById);
    }

    private BidNotice saveBidNotice(String externalId) {
        BidNotice notice = bidNoticeRepository.save(BidNotice.create(new BidNoticeCreateCommand(
                externalId, "G2B", "테스트 공고", null, null, null, null, null, null, null,
                null, null, null, null, null)));
        createdBidNoticeIds.add(notice.getId());
        return notice;
    }

    private Company saveCompany(String name) {
        User user = userRepository.save(User.create(
                "listener-test-" + System.nanoTime() + "@bidradar.com", "hash", name));
        createdUserIds.add(user.getId());
        Company company = companyRepository.save(Company.create(user, name));
        createdCompanyIds.add(company.getId());
        return company;
    }

    @Test
    @DisplayName("커밋 전에는 호출되지 않고, 커밋 후 별도 스레드에서 대상 회사 전체에 대해 호출된다")
    void 커밋후_별도스레드에서_매칭계산이_호출된다() {
        // given
        BidNotice bidNotice = saveBidNotice("COMMIT-TEST-" + System.nanoTime());
        Company companyA = saveCompany("스레드테스트회사A" + System.nanoTime());
        Company companyB = saveCompany("스레드테스트회사B" + System.nanoTime());

        List<String> capturedThreadNames = new CopyOnWriteArrayList<>();
        doAnswer(invocation -> {
            capturedThreadNames.add(Thread.currentThread().getName());
            return null;
        }).when(matchCalculationService).calculateAndSave(any(), any());

        // when: 트랜잭션 콜백 안(=커밋 전)에서 이벤트를 발행한다.
        TransactionTemplate committingTx = new TransactionTemplate(transactionManager);
        committingTx.execute(status -> {
            eventPublisher.publishEvent(new BidNoticeCollectedEvent(bidNotice.getId()));
            // then: 콜백이 아직 반환되지 않아 커밋 전이므로, 리스너가 실행됐을 리 없다.
            verify(matchCalculationService, never()).calculateAndSave(any(), any());
            return null;
        });

        // then: 콜백이 끝나 커밋이 완료되면, 결국 두 회사 모두에 대해 별도 스레드에서 호출된다.
        // (Codex 리뷰 반영: bid 인자도 any() 대신 ID로 좁혀서, 다른 테스트/스케줄러가 만든
        //  엉뚱한 공고로 호출된 걸 이 공고로 호출된 것으로 착각하지 않도록 함)
        verify(matchCalculationService, timeout(5000)).calculateAndSave(
                argThat(bn -> bn.getId().equals(bidNotice.getId())),
                argThat(c -> c.getId().equals(companyA.getId())));
        verify(matchCalculationService, timeout(5000)).calculateAndSave(
                argThat(bn -> bn.getId().equals(bidNotice.getId())),
                argThat(c -> c.getId().equals(companyB.getId())));
        assertThat(capturedThreadNames).isNotEmpty();
        assertThat(capturedThreadNames).allMatch(name -> name.startsWith("match-exec-"));
    }

    @Test
    @DisplayName("이벤트를 발행한 트랜잭션이 롤백되면 리스너가 실행되지 않는다")
    void 트랜잭션이_롤백되면_리스너가_실행되지_않는다() throws InterruptedException {
        // given: 별도의 커밋되는 트랜잭션으로 공고를 미리 만들어둔다.
        BidNotice bidNotice = saveBidNotice("ROLLBACK-TEST-" + System.nanoTime());

        // when: 이벤트만 발행하고 트랜잭션은 롤백한다.
        TransactionTemplate rollingBackTx = new TransactionTemplate(transactionManager);
        rollingBackTx.execute(status -> {
            eventPublisher.publishEvent(new BidNoticeCollectedEvent(bidNotice.getId()));
            status.setRollbackOnly();
            return null;
        });

        // then: 비동기 실행 유예 시간이 지나도 호출되지 않는다(AFTER_COMMIT 콜백 자체가 발동하지 않음).
        Thread.sleep(1000);
        verify(matchCalculationService, never()).calculateAndSave(any(), any());
    }
}
