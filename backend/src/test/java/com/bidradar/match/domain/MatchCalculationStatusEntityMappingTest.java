package com.bidradar.match.domain;

import com.bidradar.auth.domain.User;
import com.bidradar.company.domain.Company;
import com.bidradar.config.JpaConfig;
import com.bidradar.support.IntegrationTestBase;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class MatchCalculationStatusEntityMappingTest extends IntegrationTestBase {

    @Autowired
    TestEntityManager entityManager;

    @Test
    @DisplayName("MatchCalculationStatus가 Company와 함께 저장되고 상태값이 보존된다")
    void MatchCalculationStatus가_저장되고_상태값이_보존된다() {
        // given
        User user = entityManager.persistAndFlush(User.create("owner@bidradar.com", "hash", "홍길동"));
        Company company = entityManager.persistAndFlush(Company.create(user, "테스트 회사"));

        MatchCalculationStatus status = entityManager.persistAndFlush(MatchCalculationStatus.start(company));

        // when
        entityManager.clear();
        MatchCalculationStatus found = entityManager.find(MatchCalculationStatus.class, status.getId());

        // then
        assertThat(found.getCompany().getId()).isEqualTo(company.getId());
        assertThat(found.getStatus()).isEqualTo(MatchCalculationStatusType.IN_PROGRESS);
    }

    @Test
    @DisplayName("같은 회사로 두 번 저장하면 UNIQUE 제약 위반이 발생한다")
    void MatchCalculationStatus_UNIQUE_제약이_동작한다() {
        // given
        User user = entityManager.persistAndFlush(User.create("owner2@bidradar.com", "hash", "홍길동"));
        Company company = entityManager.persistAndFlush(Company.create(user, "테스트 회사2"));
        entityManager.persistAndFlush(MatchCalculationStatus.start(company));

        // when // then
        assertThatThrownBy(() -> entityManager.persistAndFlush(MatchCalculationStatus.start(company)))
                .isInstanceOf(PersistenceException.class);
    }
}
