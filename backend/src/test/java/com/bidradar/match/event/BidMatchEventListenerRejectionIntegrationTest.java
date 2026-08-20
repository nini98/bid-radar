package com.bidradar.match.event;

import com.bidradar.auth.domain.User;
import com.bidradar.auth.repository.UserRepository;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.event.BidNoticeCollectedEvent;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.company.domain.Company;
import com.bidradar.company.repository.CompanyRepository;
import com.bidradar.match.domain.BidMatchResult;
import com.bidradar.match.domain.BidMatchResultStatus;
import com.bidradar.match.repository.BidMatchResultRepository;
import com.bidradar.support.IntegrationTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * {@code matchTaskExecutor} 제출이 거부됐을 때, 실패 기록이 AFTER_COMMIT 콜백을 호출한 스레드에서
 * 실제로 독립 커밋되는지 검증한다 (Issue #51 Codex 리뷰). 이 스레드엔 원래 트랜잭션의 리소스가 아직
 * 바인딩돼 있을 수 있어, {@code markFailed()}(REQUIRED)를 그대로 썼다면 그 stale한 리소스에 참여해
 * DB엔 반영되지 않을 수 있었다 — Mockito로 {@code MatchCalculationService}를 통째로 대체하는 단위
 * 테스트로는 이 문제를 잡아낼 수 없어, 실제 커밋 여부를 확인하는 별도 통합 테스트가 필요하다.
 *
 * {@code matchTaskExecutor}를 이 테스트 클래스 전용으로 "항상 거부"하도록 Mock하기 때문에, 실제
 * 스레드풀에서 정상 처리되는 흐름을 검증하는 {@link BidMatchEventListenerIntegrationTest}와는
 * 별도 클래스로 분리했다 (같은 클래스에 두면 그쪽 테스트들의 정상 실행 흐름이 깨진다).
 */
@SpringBootTest
class BidMatchEventListenerRejectionIntegrationTest extends IntegrationTestBase {

    @Autowired
    BidNoticeRepository bidNoticeRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BidMatchResultRepository bidMatchResultRepository;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    PlatformTransactionManager transactionManager;

    @MockitoBean(name = "matchTaskExecutor")
    TaskExecutor matchTaskExecutor;

    private final List<Long> createdCompanyIds = new ArrayList<>();
    private final List<Long> createdUserIds = new ArrayList<>();
    private final List<Long> createdBidNoticeIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        doThrow(new TaskRejectedException("풀 포화")).when(matchTaskExecutor).execute(any());
    }

    @AfterEach
    void cleanUp() {
        // FK 순서: bid_match_results → companies → users, bid_notices는 독립.
        createdBidNoticeIds.forEach(bidNoticeId ->
                createdCompanyIds.forEach(companyId ->
                        bidMatchResultRepository.findByBidNoticeIdAndCompanyId(bidNoticeId, companyId)
                                .ifPresent(bidMatchResultRepository::delete)));
        createdCompanyIds.forEach(companyRepository::deleteById);
        createdUserIds.forEach(userRepository::deleteById);
        createdBidNoticeIds.forEach(bidNoticeRepository::deleteById);
    }

    private BidNotice saveBidNotice(String externalId) {
        BidNotice notice = bidNoticeRepository.save(BidNotice.create(new BidNoticeCreateCommand(
                externalId, "G2B", "테스트 공고", null, null, null, null, null, null, null,
                null, null, null, null, null, null)));
        createdBidNoticeIds.add(notice.getId());
        return notice;
    }

    private Company saveCompany(String name) {
        User user = userRepository.save(User.create(
                "rejection-test-" + System.nanoTime() + "@bidradar.com", "hash", name));
        createdUserIds.add(user.getId());
        Company company = companyRepository.save(Company.create(user, name));
        createdCompanyIds.add(company.getId());
        return company;
    }

    @Test
    @DisplayName("제출이 거부되면 AFTER_COMMIT 콜백 스레드에서도 전체 회사가 실제로 FAILED로 커밋된다")
    void 제출거부시_AFTER_COMMIT_스레드에서도_전체회사가_FAILED로_커밋된다() {
        // given
        BidNotice bidNotice = saveBidNotice("REJECTION-TEST-" + System.nanoTime());
        Company companyA = saveCompany("거부테스트회사A" + System.nanoTime());
        Company companyB = saveCompany("거부테스트회사B" + System.nanoTime());

        // when: handle()이 AFTER_COMMIT 콜백에서 동기 실행되므로, committingTx.execute()가
        // 반환된 시점엔 이미 거부 처리(실패 기록)까지 끝나 있다.
        TransactionTemplate committingTx = new TransactionTemplate(transactionManager);
        committingTx.execute(status -> {
            eventPublisher.publishEvent(new BidNoticeCollectedEvent(bidNotice.getId()));
            return null;
        });

        // then
        BidMatchResult resultA = bidMatchResultRepository
                .findByBidNoticeIdAndCompanyId(bidNotice.getId(), companyA.getId())
                .orElseThrow();
        assertThat(resultA.getStatus()).isEqualTo(BidMatchResultStatus.FAILED);
        assertThat(resultA.getTotalScore()).isNull();

        BidMatchResult resultB = bidMatchResultRepository
                .findByBidNoticeIdAndCompanyId(bidNotice.getId(), companyB.getId())
                .orElseThrow();
        assertThat(resultB.getStatus()).isEqualTo(BidMatchResultStatus.FAILED);
    }
}
