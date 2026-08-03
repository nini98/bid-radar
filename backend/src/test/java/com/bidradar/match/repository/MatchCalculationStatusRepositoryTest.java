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
    @DisplayName("상태가 DONE이면 락 선점에 성공해 1건이 갱신되고 상태/토큰이 바뀐다")
    void 상태가_DONE이면_락선점에_성공한다() {
        // given
        Company company = newCompany("a@bidradar.com");
        MatchCalculationStatus status = MatchCalculationStatus.start(company, "old-token");
        status.markDone();
        entityManager.persistAndFlush(status);
        entityManager.clear();

        // when
        int updated = matchCalculationStatusRepository.acquireLock(status.getId(), LocalDateTime.now().minusMinutes(5), "new-token");

        // then
        assertThat(updated).isEqualTo(1);
        MatchCalculationStatus found = entityManager.find(MatchCalculationStatus.class, status.getId());
        assertThat(found.getStatus()).isEqualTo(MatchCalculationStatusType.IN_PROGRESS);
        assertThat(found.getLockToken()).isEqualTo("new-token");
    }

    @Test
    @DisplayName("상태가 신선한 IN_PROGRESS면 락 선점에 실패해 0건이 갱신되고 토큰도 바뀌지 않는다")
    void 상태가_신선한_IN_PROGRESS면_락선점에_실패한다() {
        // given
        Company company = newCompany("b@bidradar.com");
        MatchCalculationStatus status = entityManager.persistAndFlush(MatchCalculationStatus.start(company, "old-token"));
        entityManager.clear();

        // when
        int updated = matchCalculationStatusRepository.acquireLock(status.getId(), LocalDateTime.now().minusMinutes(5), "new-token");

        // then
        assertThat(updated).isEqualTo(0);
        MatchCalculationStatus found = entityManager.find(MatchCalculationStatus.class, status.getId());
        assertThat(found.getLockToken()).isEqualTo("old-token");
    }

    @Test
    @DisplayName("상태가 오래된 IN_PROGRESS(5분 초과)면 락 선점에 성공한다")
    void 상태가_오래된_IN_PROGRESS면_락선점에_성공한다() {
        // given
        Company company = newCompany("c@bidradar.com");
        MatchCalculationStatus status = entityManager.persistAndFlush(MatchCalculationStatus.start(company, "old-token"));
        entityManager.clear();

        // when
        // staleBefore를 현재 시각 이후로 두어, 방금 만든 updatedAt도 "그 이전"으로 취급되게 한다 (죽은 락 시나리오 재현).
        int updated = matchCalculationStatusRepository.acquireLock(status.getId(), LocalDateTime.now().plusMinutes(1), "new-token");

        // then
        assertThat(updated).isEqualTo(1);
    }

    @Test
    @DisplayName("상태가 FAILED면 재시도 락 선점에 성공한다")
    void 상태가_FAILED면_재시도락_선점에_성공한다() {
        // given
        Company company = newCompany("h@bidradar.com");
        MatchCalculationStatus status = MatchCalculationStatus.start(company, "old-token");
        status.markFailed();
        entityManager.persistAndFlush(status);
        entityManager.clear();

        // when
        int updated = matchCalculationStatusRepository.acquireRetryLock(status.getId(), LocalDateTime.now().minusMinutes(5), "new-token");

        // then
        assertThat(updated).isEqualTo(1);
        MatchCalculationStatus found = entityManager.find(MatchCalculationStatus.class, status.getId());
        assertThat(found.getStatus()).isEqualTo(MatchCalculationStatusType.IN_PROGRESS);
        assertThat(found.getLockToken()).isEqualTo("new-token");
    }

    @Test
    @DisplayName("상태가 DONE이면 재시도 락 선점에 실패한다 (일반 acquireLock과 달리 재시도는 DONE을 허용하지 않는다)")
    void 상태가_DONE이면_재시도락_선점에_실패한다() {
        // given
        Company company = newCompany("i@bidradar.com");
        MatchCalculationStatus status = MatchCalculationStatus.start(company, "old-token");
        status.markDone();
        entityManager.persistAndFlush(status);
        entityManager.clear();

        // when
        int updated = matchCalculationStatusRepository.acquireRetryLock(status.getId(), LocalDateTime.now().minusMinutes(5), "new-token");

        // then
        assertThat(updated).isEqualTo(0);
        MatchCalculationStatus found = entityManager.find(MatchCalculationStatus.class, status.getId());
        assertThat(found.getLockToken()).isEqualTo("old-token");
    }

    @Test
    @DisplayName("상태가 신선한 IN_PROGRESS면 재시도 락 선점에 실패한다")
    void 상태가_신선한_IN_PROGRESS면_재시도락_선점에_실패한다() {
        // given
        Company company = newCompany("j@bidradar.com");
        MatchCalculationStatus status = entityManager.persistAndFlush(MatchCalculationStatus.start(company, "old-token"));
        entityManager.clear();

        // when
        int updated = matchCalculationStatusRepository.acquireRetryLock(status.getId(), LocalDateTime.now().minusMinutes(5), "new-token");

        // then
        assertThat(updated).isEqualTo(0);
    }

    @Test
    @DisplayName("상태가 오래된 IN_PROGRESS(5분 초과)면 재시도 락 선점에 성공한다")
    void 상태가_오래된_IN_PROGRESS면_재시도락_선점에_성공한다() {
        // given
        Company company = newCompany("k@bidradar.com");
        MatchCalculationStatus status = entityManager.persistAndFlush(MatchCalculationStatus.start(company, "old-token"));
        entityManager.clear();

        // when
        // staleBefore를 현재 시각 이후로 두어, 방금 만든 updatedAt도 "그 이전"으로 취급되게 한다 (죽은 락 시나리오 재현).
        int updated = matchCalculationStatusRepository.acquireRetryLock(status.getId(), LocalDateTime.now().plusMinutes(1), "new-token");

        // then
        assertThat(updated).isEqualTo(1);
        MatchCalculationStatus found = entityManager.find(MatchCalculationStatus.class, status.getId());
        assertThat(found.getLockToken()).isEqualTo("new-token");
    }

    @Test
    @DisplayName("토큰이 일치하고 IN_PROGRESS면 heartbeat가 성공해 updated_at이 갱신된다")
    void 토큰이_일치하면_heartbeat가_성공한다() {
        // given
        Company company = newCompany("d@bidradar.com");
        Long companyId = company.getId();
        entityManager.persistAndFlush(MatchCalculationStatus.start(company, "my-token"));
        entityManager.clear();

        // when
        int updated = matchCalculationStatusRepository.heartbeat(companyId, "my-token");

        // then
        assertThat(updated).isEqualTo(1);
    }

    @Test
    @DisplayName("다른 작업이 이미 락을 재선점해 토큰이 바뀌었으면 heartbeat가 실패한다")
    void 토큰이_불일치하면_heartbeat가_실패한다() {
        // given
        Company company = newCompany("e@bidradar.com");
        Long companyId = company.getId();
        entityManager.persistAndFlush(MatchCalculationStatus.start(company, "someone-elses-token"));
        entityManager.clear();

        // when
        int updated = matchCalculationStatusRepository.heartbeat(companyId, "my-old-token");

        // then
        assertThat(updated).isEqualTo(0);
    }

    @Test
    @DisplayName("토큰이 일치하면 최종 상태(DONE/FAILED)가 반영된다")
    void 토큰이_일치하면_finish가_성공한다() {
        // given
        Company company = newCompany("f@bidradar.com");
        Long companyId = company.getId();
        entityManager.persistAndFlush(MatchCalculationStatus.start(company, "my-token"));
        entityManager.clear();

        // when
        int updated = matchCalculationStatusRepository.finish(companyId, "my-token", MatchCalculationStatusType.DONE);

        // then
        assertThat(updated).isEqualTo(1);
        MatchCalculationStatus found = matchCalculationStatusRepository.findByCompanyId(companyId).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(MatchCalculationStatusType.DONE);
    }

    @Test
    @DisplayName("이미 다른 작업에 밀려 토큰이 바뀌었으면 늦게 끝난 작업의 finish는 무시된다")
    void 토큰이_불일치하면_finish가_무시된다() {
        // given: 새 작업이 재선점해 토큰이 new-token으로 바뀐 상태를 재현
        Company company = newCompany("g@bidradar.com");
        Long companyId = company.getId();
        MatchCalculationStatus status = entityManager.persistAndFlush(MatchCalculationStatus.start(company, "old-token"));
        entityManager.clear();
        matchCalculationStatusRepository.acquireLock(status.getId(), LocalDateTime.now().plusMinutes(1), "new-token");

        // when: 밀려난 옛 작업(old-token)이 뒤늦게 완료 처리를 시도
        int updated = matchCalculationStatusRepository.finish(companyId, "old-token", MatchCalculationStatusType.FAILED);

        // then: 반영되지 않고, 새 작업이 이미 IN_PROGRESS로 세팅해둔 상태가 그대로 유지된다
        assertThat(updated).isEqualTo(0);
        MatchCalculationStatus found = matchCalculationStatusRepository.findByCompanyId(companyId).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(MatchCalculationStatusType.IN_PROGRESS);
        assertThat(found.getLockToken()).isEqualTo("new-token");
    }
}
