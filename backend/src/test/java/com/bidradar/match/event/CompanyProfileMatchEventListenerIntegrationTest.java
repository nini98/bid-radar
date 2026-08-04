package com.bidradar.match.event;

import com.bidradar.auth.domain.User;
import com.bidradar.auth.repository.UserRepository;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.company.domain.Company;
import com.bidradar.company.dto.request.CompanyProfileRequest;
import com.bidradar.company.dto.response.CompanyProfileResponse;
import com.bidradar.company.repository.CompanyRepository;
import com.bidradar.company.service.CompanyProfileService;
import com.bidradar.match.domain.MatchCalculationStatus;
import com.bidradar.match.domain.MatchCalculationStatusType;
import com.bidradar.match.repository.MatchCalculationStatusRepository;
import com.bidradar.match.service.MatchCalculationService;
import com.bidradar.support.IntegrationTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * {@code @TransactionalEventListener(AFTER_COMMIT)} + 수동 스레드풀 제출 조합이 실제로 커밋 후에만,
 * 별도 스레드에서 동작하는지 검증한다 (transaction-rule.md §12). 실제 트랜잭션 커밋/롤백이 필요해
 * 순수 단위 테스트로는 검증할 수 없다.
 *
 * {@code @SpringBootTest}는 {@code @DataJpaTest}와 달리 테스트 종료 시 롤백되지 않고 실제로 커밋되므로
 * (AFTER_COMMIT 자체를 검증해야 해서 불가피함), 모든 정적 Testcontainers 컨테이너를 공유하는 다른
 * 테스트 클래스가 오염되지 않도록 각 테스트가 만든 데이터를 {@link #cleanUp()}에서 직접 정리한다.
 */
@SpringBootTest
class CompanyProfileMatchEventListenerIntegrationTest extends IntegrationTestBase {

    @Autowired
    CompanyProfileService companyProfileService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    BidNoticeRepository bidNoticeRepository;

    @Autowired
    MatchCalculationStatusRepository matchCalculationStatusRepository;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @MockitoBean
    MatchCalculationService matchCalculationService;

    private final List<Long> createdCompanyIds = new ArrayList<>();
    private final List<Long> createdUserIds = new ArrayList<>();
    private final List<Long> createdBidNoticeIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        // FK 순서: match_calculation_status → companies → users, bid_notices는 독립.
        createdCompanyIds.forEach(id -> matchCalculationStatusRepository.findByCompanyId(id)
                .ifPresent(matchCalculationStatusRepository::delete));
        createdCompanyIds.forEach(companyRepository::deleteById);
        createdUserIds.forEach(userRepository::deleteById);
        createdBidNoticeIds.forEach(bidNoticeRepository::deleteById);
    }

    private CompanyProfileRequest emptyRequest(String companyName) {
        return new CompanyProfileRequest(
                companyName, null, null, null, null, null, null, null, null,
                List.of(), List.of(), List.of(), List.of(), null,
                null, null, null
        );
    }

    private MatchCalculationStatusType pollStatus(Long companyId, Duration timeout) {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            MatchCalculationStatusType current = matchCalculationStatusRepository.findByCompanyId(companyId)
                    .map(MatchCalculationStatus::getStatus)
                    .orElse(null);
            if (current == MatchCalculationStatusType.DONE || current == MatchCalculationStatusType.FAILED) {
                return current;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("대기 중 인터럽트 발생", e);
            }
        }
        return fail("제한 시간(" + timeout + ") 안에 상태가 종료 상태로 바뀌지 않았습니다.");
    }

    @Test
    @DisplayName("saveProfile 커밋 직후에는 IN_PROGRESS이고, 이후 비동기로 DONE까지 전이된다")
    void saveProfile_커밋후_비동기로_상태가_DONE까지_전이된다() throws InterruptedException {
        // given: 리스너가 실제로 실행 중임을 관찰할 수 있도록, calculateAndSave를 래치로 막아둔다.
        // (bidNoticeRepository가 비어있으면 saveProfile() 직후 리스너가 이미 끝나버려
        //  "커밋 직후 IN_PROGRESS" 단언이 비동기 완료와 경쟁하는 간헐적 실패가 날 수 있다 — Codex 리뷰 반영)
        CountDownLatch releaseLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            releaseLatch.await(5, TimeUnit.SECONDS);
            return null;
        }).when(matchCalculationService).calculateAndSave(any(), any());

        User user = userRepository.save(User.create("commit-" + System.nanoTime() + "@bidradar.com", "hash", "홍길동"));
        createdUserIds.add(user.getId());
        BidNotice bidNotice = bidNoticeRepository.save(BidNotice.create(new BidNoticeCreateCommand(
                "COMMIT-TEST-" + System.nanoTime(), "G2B", "테스트 공고", null, null, null, null, null, null, null,
                null, null, null, null, null)));
        createdBidNoticeIds.add(bidNotice.getId());
        CompanyProfileRequest request = emptyRequest("커밋테스트회사");

        // when
        CompanyProfileResponse response = companyProfileService.saveProfile(user.getId(), request);
        createdCompanyIds.add(response.id());

        // then: 리스너가 래치에 막혀 있는 동안은 확정적으로 IN_PROGRESS다.
        MatchCalculationStatusType whileBlocked = matchCalculationStatusRepository
                .findByCompanyId(response.id())
                .orElseThrow()
                .getStatus();
        assertThat(whileBlocked).isEqualTo(MatchCalculationStatusType.IN_PROGRESS);

        // 래치를 풀어주면 리스너가 마저 끝나 DONE으로 전이된다.
        releaseLatch.countDown();
        MatchCalculationStatusType finalStatus = pollStatus(response.id(), Duration.ofSeconds(5));
        assertThat(finalStatus).isEqualTo(MatchCalculationStatusType.DONE);
    }

    @Test
    @DisplayName("이벤트를 발행한 트랜잭션이 롤백되면 리스너가 실행되지 않는다")
    void 이벤트발행_트랜잭션이_롤백되면_리스너가_실행되지_않는다() throws InterruptedException {
        // given: 별도의 커밋되는 트랜잭션으로 회사와 FAILED 상태를 미리 만들어둔다.
        //        (실행되면 정상 완료 시 DONE으로 바뀌므로, FAILED로 시작해야 "리스너가 안 돌았다"를 구분할 수 있다)
        TransactionTemplate committingTx = new TransactionTemplate(transactionManager);
        Long companyId = committingTx.execute(status -> {
            User user = userRepository.save(User.create("rollback-" + System.nanoTime() + "@bidradar.com", "hash", "홍길동"));
            createdUserIds.add(user.getId());
            Company company = companyRepository.save(Company.create(user, "롤백테스트회사"));
            createdCompanyIds.add(company.getId());
            MatchCalculationStatus calcStatus = MatchCalculationStatus.start(company, "rollback-token");
            calcStatus.markFailed();
            matchCalculationStatusRepository.save(calcStatus);
            return company.getId();
        });

        // when: 이벤트만 발행하고 트랜잭션은 롤백한다.
        TransactionTemplate rollingBackTx = new TransactionTemplate(transactionManager);
        rollingBackTx.execute(status -> {
            eventPublisher.publishEvent(new MatchRecalculationRequestedEvent(companyId, "rollback-token"));
            status.setRollbackOnly();
            return null;
        });

        // then: 비동기 실행 유예 시간이 지나도 상태가 여전히 FAILED다 (리스너가 실행되지 않았다).
        Thread.sleep(1000);
        MatchCalculationStatusType status = matchCalculationStatusRepository.findByCompanyId(companyId)
                .orElseThrow()
                .getStatus();
        assertThat(status).isEqualTo(MatchCalculationStatusType.FAILED);
    }

    @Test
    @DisplayName("락을 재선점당한 낡은 작업이 뒤늦게 완료를 시도해도 최신 작업의 결과를 덮어쓰지 못한다")
    void 낡은_작업의_뒤늦은_완료가_최신_작업_결과를_덮어쓰지_못한다() {
        // given: 5분 넘게 응답 없는(죽은 것으로 간주되는) 낡은 IN_PROGRESS 락을 미리 만들어둔다.
        User user = userRepository.save(User.create("stale-" + System.nanoTime() + "@bidradar.com", "hash", "홍길동"));
        createdUserIds.add(user.getId());
        Company company = companyRepository.save(Company.create(user, "낡은락테스트회사"));
        createdCompanyIds.add(company.getId());
        matchCalculationStatusRepository.save(MatchCalculationStatus.start(company, "old-token"));
        jdbcTemplate.update(
                "UPDATE match_calculation_status SET updated_at = ? WHERE company_id = ?",
                OffsetDateTime.ofInstant(Instant.now().minus(Duration.ofMinutes(6)), ZoneOffset.UTC), company.getId());

        // when: 새로 프로필을 저장하면 낡은 락을 재선점해 새 토큰으로 진행한다 (calculateAndSave는 즉시 반환되도록 둠).
        CompanyProfileResponse response = companyProfileService.saveProfile(user.getId(), emptyRequest("낡은락테스트회사"));
        String newToken = matchCalculationStatusRepository.findByCompanyId(response.id()).orElseThrow().getLockToken();
        assertThat(newToken).isNotEqualTo("old-token");

        // 낡은 작업(old-token)이 뒤늦게 실행을 마치고 완료 처리를 시도한다고 가정한다.
        int updated = matchCalculationStatusRepository.finish(company.getId(), "old-token", MatchCalculationStatusType.FAILED);
        assertThat(updated).isEqualTo(0);

        // then: 실제 최신 작업(new-token)은 정상적으로 DONE까지 전이되고, 낡은 작업의 시도는 반영되지 않는다.
        MatchCalculationStatusType finalStatus = pollStatus(response.id(), Duration.ofSeconds(5));
        assertThat(finalStatus).isEqualTo(MatchCalculationStatusType.DONE);
    }
}
