package com.bidradar.match.repository;

import com.bidradar.auth.domain.User;
import com.bidradar.company.domain.Company;
import com.bidradar.config.JpaConfig;
import com.bidradar.match.domain.MatchCalculationStatus;
import com.bidradar.match.domain.MatchCalculationStatusType;
import com.bidradar.support.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class MatchCalculationStatusRepositoryTest extends IntegrationTestBase {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    MatchCalculationStatusRepository matchCalculationStatusRepository;

    private Company newCompany(String email) {
        User user = entityManager.persistAndFlush(User.create(email, "hash", "홍길동"));
        return entityManager.persistAndFlush(Company.create(user, "테스트 회사"));
    }

    @Test
    @DisplayName("상태가 DONE이면 락 선점에 성공해 1건이 갱신되고 상태가 IN_PROGRESS로 바뀐다")
    void 상태가_DONE이면_락선점에_성공한다() {
        // given
        Company company = newCompany("a@bidradar.com");
        MatchCalculationStatus status = MatchCalculationStatus.start(company);
        status.markDone();
        entityManager.persistAndFlush(status);
        entityManager.clear();

        // when
        int updated = matchCalculationStatusRepository.acquireLock(status.getId(), LocalDateTime.now().minusMinutes(5));

        // then
        assertThat(updated).isEqualTo(1);
        MatchCalculationStatus found = entityManager.find(MatchCalculationStatus.class, status.getId());
        assertThat(found.getStatus()).isEqualTo(MatchCalculationStatusType.IN_PROGRESS);
    }

    @Test
    @DisplayName("상태가 신선한 IN_PROGRESS면 락 선점에 실패해 0건이 갱신된다")
    void 상태가_신선한_IN_PROGRESS면_락선점에_실패한다() {
        // given
        Company company = newCompany("b@bidradar.com");
        MatchCalculationStatus status = entityManager.persistAndFlush(MatchCalculationStatus.start(company));
        entityManager.clear();

        // when
        int updated = matchCalculationStatusRepository.acquireLock(status.getId(), LocalDateTime.now().minusMinutes(5));

        // then
        assertThat(updated).isEqualTo(0);
    }

    @Test
    @DisplayName("상태가 오래된 IN_PROGRESS(5분 초과)면 락 선점에 성공한다")
    void 상태가_오래된_IN_PROGRESS면_락선점에_성공한다() throws InterruptedException {
        // given
        Company company = newCompany("c@bidradar.com");
        MatchCalculationStatus status = entityManager.persistAndFlush(MatchCalculationStatus.start(company));
        entityManager.clear();

        // when
        // staleBefore를 현재 시각 이후로 두어, 방금 만든 updatedAt도 "그 이전"으로 취급되게 한다 (죽은 락 시나리오 재현).
        int updated = matchCalculationStatusRepository.acquireLock(status.getId(), LocalDateTime.now().plusMinutes(1));

        // then
        assertThat(updated).isEqualTo(1);
    }
}
