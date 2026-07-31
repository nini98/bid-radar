package com.bidradar.match.event;

import com.bidradar.auth.domain.User;
import com.bidradar.auth.repository.UserRepository;
import com.bidradar.company.domain.Company;
import com.bidradar.company.dto.request.CompanyProfileRequest;
import com.bidradar.company.dto.response.CompanyProfileResponse;
import com.bidradar.company.event.CompanyProfileSavedEvent;
import com.bidradar.company.repository.CompanyRepository;
import com.bidradar.company.service.CompanyProfileService;
import com.bidradar.match.domain.MatchCalculationStatus;
import com.bidradar.match.domain.MatchCalculationStatusType;
import com.bidradar.match.repository.MatchCalculationStatusRepository;
import com.bidradar.support.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * {@code @Async + @TransactionalEventListener(AFTER_COMMIT)} 조합이 실제로 커밋 후에만,
 * 그리고 실제로 별도 스레드에서 동작하는지 검증한다 (transaction-rule.md §12).
 * 이 조합을 검증하려면 실제 트랜잭션 커밋/롤백이 필요해 순수 단위 테스트로는 검증할 수 없다.
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
    MatchCalculationStatusRepository matchCalculationStatusRepository;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    PlatformTransactionManager transactionManager;

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
    void saveProfile_커밋후_비동기로_상태가_DONE까지_전이된다() {
        // given
        User user = userRepository.save(User.create("commit-" + System.nanoTime() + "@bidradar.com", "hash", "홍길동"));
        CompanyProfileRequest request = emptyRequest("커밋테스트회사");

        // when
        CompanyProfileResponse response = companyProfileService.saveProfile(user.getId(), request);

        // then: saveProfile()이 반환된 시점엔 리스너가 아직 실행되지 않아 IN_PROGRESS여야 한다 (동기적으로 즉시 도는 게 아니라는 근거).
        MatchCalculationStatusType immediatelyAfterReturn = matchCalculationStatusRepository
                .findByCompanyId(response.id())
                .orElseThrow()
                .getStatus();
        assertThat(immediatelyAfterReturn).isEqualTo(MatchCalculationStatusType.IN_PROGRESS);

        // 이후 AFTER_COMMIT + @Async 리스너가 별도 스레드에서 실행되어 DONE으로 전이된다.
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
            Company company = companyRepository.save(Company.create(user, "롤백테스트회사"));
            MatchCalculationStatus calcStatus = MatchCalculationStatus.start(company);
            calcStatus.markFailed();
            matchCalculationStatusRepository.save(calcStatus);
            return company.getId();
        });

        // when: 이벤트만 발행하고 트랜잭션은 롤백한다.
        TransactionTemplate rollingBackTx = new TransactionTemplate(transactionManager);
        rollingBackTx.execute(status -> {
            eventPublisher.publishEvent(new CompanyProfileSavedEvent(companyId));
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
}
