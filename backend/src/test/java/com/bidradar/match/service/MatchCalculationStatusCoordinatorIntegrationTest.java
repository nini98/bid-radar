package com.bidradar.match.service;

import com.bidradar.auth.domain.User;
import com.bidradar.auth.repository.UserRepository;
import com.bidradar.company.domain.Company;
import com.bidradar.company.repository.CompanyRepository;
import com.bidradar.match.domain.MatchCalculationStatus;
import com.bidradar.match.domain.MatchCalculationStatusType;
import com.bidradar.match.repository.MatchCalculationStatusRepository;
import com.bidradar.support.IntegrationTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code finish()}가 {@code REQUIRES_NEW}로 선언된 이유(transaction-rule.md §12 관련 논의)를 검증한다.
 * AFTER_COMMIT 콜백처럼 "이미 어떤 트랜잭션 컨텍스트가 스레드에 남아있는" 상황을 흉내내기 위해,
 * 바깥 트랜잭션을 일부러 롤백시키면서 finish()의 효과가 그와 무관하게 독립적으로 커밋되는지 확인한다.
 */
@SpringBootTest
class MatchCalculationStatusCoordinatorIntegrationTest extends IntegrationTestBase {

    @Autowired
    MatchCalculationStatusCoordinator coordinator;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    MatchCalculationStatusRepository matchCalculationStatusRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    private Long companyId;

    @AfterEach
    void cleanUp() {
        if (companyId != null) {
            matchCalculationStatusRepository.findByCompanyId(companyId)
                    .ifPresent(matchCalculationStatusRepository::delete);
            companyRepository.deleteById(companyId);
        }
    }

    @Test
    @DisplayName("바깥 트랜잭션이 롤백돼도 REQUIRES_NEW인 finish()의 커밋은 영향을 받지 않는다")
    void 바깥트랜잭션이_롤백돼도_finish는_독립적으로_커밋된다() {
        // given
        User user = userRepository.save(User.create("coordinator-" + System.nanoTime() + "@bidradar.com", "hash", "홍길동"));
        Company company = companyRepository.save(Company.create(user, "코디네이터테스트회사"));
        companyId = company.getId();
        matchCalculationStatusRepository.save(MatchCalculationStatus.start(company, "token"));

        // when: 바깥 트랜잭션 안에서 finish()를 호출한 뒤 바깥 트랜잭션은 롤백시킨다.
        TransactionTemplate outerTx = new TransactionTemplate(transactionManager);
        outerTx.execute(status -> {
            coordinator.finish(companyId, "token", MatchCalculationStatusType.DONE);
            status.setRollbackOnly();
            return null;
        });

        // then: REQUIRES_NEW가 독립적인 트랜잭션에서 커밋했으므로, 바깥 트랜잭션의 롤백과 무관하게 DONE으로 남아있다.
        MatchCalculationStatusType finalStatus = matchCalculationStatusRepository.findByCompanyId(companyId)
                .orElseThrow()
                .getStatus();
        assertThat(finalStatus).isEqualTo(MatchCalculationStatusType.DONE);
    }
}
