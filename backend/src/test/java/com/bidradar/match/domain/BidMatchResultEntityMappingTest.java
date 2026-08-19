package com.bidradar.match.domain;

import com.bidradar.auth.domain.User;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.domain.BidStatus;
import com.bidradar.common.config.ClockConfig;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, ClockConfig.class})
class BidMatchResultEntityMappingTest extends IntegrationTestBase {

    @Autowired
    TestEntityManager entityManager;

    @Test
    @DisplayName("BidMatchResult가 BidNotice/Company 연관관계와 함께 저장되고 matched_keywords(JSONB)가 보존된다")
    void BidMatchResult가_저장되고_matchedKeywords가_보존된다() {
        // given
        BidNotice bidNotice = entityManager.persistAndFlush(
                BidNotice.create("20250001", "G2B", "테스트 공고", BidStatus.OPEN));
        User user = entityManager.persistAndFlush(User.create("owner@bidradar.com", "hash", "홍길동"));
        Company company = entityManager.persistAndFlush(Company.create(user, "테스트 회사"));

        BidMatchResult result = entityManager.persistAndFlush(BidMatchResult.create(
                bidNotice, company,
                new BigDecimal("85.50"), MatchGrade.STRONG_REVIEW,
                new BigDecimal("40.00"), new BigDecimal("20.00"),
                new BigDecimal("15.00"), new BigDecimal("10.50"),
                "[\"백엔드 개발\", \"서울\"]", "기술 태그와 지역 조건이 일치합니다."));

        // when
        entityManager.clear();
        BidMatchResult found = entityManager.find(BidMatchResult.class, result.getId());

        // then
        assertThat(found.getBidNotice().getId()).isEqualTo(bidNotice.getId());
        assertThat(found.getCompany().getId()).isEqualTo(company.getId());
        assertThat(found.getTotalScore()).isEqualByComparingTo("85.50");
        assertThat(found.getGrade()).isEqualTo(MatchGrade.STRONG_REVIEW);
        assertThat(found.getMatchedKeywords()).isEqualTo("[\"백엔드 개발\", \"서울\"]");
    }

    @Test
    @DisplayName("같은 BidNotice-Company 조합으로 두 번 저장하면 UNIQUE 제약 위반이 발생한다")
    void BidMatchResult_UNIQUE_제약이_동작한다() {
        // given
        BidNotice bidNotice = entityManager.persistAndFlush(
                BidNotice.create("20250002", "G2B", "테스트 공고2", BidStatus.OPEN));
        User user = entityManager.persistAndFlush(User.create("owner2@bidradar.com", "hash", "홍길동"));
        Company company = entityManager.persistAndFlush(Company.create(user, "테스트 회사2"));
        entityManager.persistAndFlush(BidMatchResult.create(
                bidNotice, company,
                new BigDecimal("70.00"), MatchGrade.RECOMMENDED,
                null, null, null, null, null, null));

        // when // then
        assertThatThrownBy(() -> entityManager.persistAndFlush(BidMatchResult.create(
                bidNotice, company,
                new BigDecimal("72.00"), MatchGrade.RECOMMENDED,
                null, null, null, null, null, null)))
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("FAILED 결과가 점수 필드 전부 null인 채로 저장되고 조회된다")
    void FAILED_결과가_점수없이_저장되고_조회된다() {
        // given
        BidNotice bidNotice = entityManager.persistAndFlush(
                BidNotice.create("20250003", "G2B", "테스트 공고3", BidStatus.OPEN));
        User user = entityManager.persistAndFlush(User.create("owner3@bidradar.com", "hash", "홍길동"));
        Company company = entityManager.persistAndFlush(Company.create(user, "테스트 회사3"));

        BidMatchResult result = entityManager.persistAndFlush(
                BidMatchResult.createFailed(bidNotice, company, "RuntimeException: 계산 중 오류"));

        // when
        entityManager.clear();
        BidMatchResult found = entityManager.find(BidMatchResult.class, result.getId());

        // then
        assertThat(found.getStatus()).isEqualTo(BidMatchResultStatus.FAILED);
        assertThat(found.getErrorMessage()).isEqualTo("RuntimeException: 계산 중 오류");
        assertThat(found.getTotalScore()).isNull();
        assertThat(found.getGrade()).isNull();
    }

    @Test
    @DisplayName("status와 점수 조합이 CHECK 제약을 위반하면 저장이 거부된다")
    void status와_점수조합이_CHECK_제약을_위반하면_저장이_거부된다() {
        // given: 엔티티의 create()/createFailed()/markFailed()는 항상 일관된 조합만 만들어내므로,
        // DB 레벨 방어를 직접 검증하려면 네이티브 SQL로 불일치 조합을 강제로 넣어야 한다.
        BidNotice bidNotice = entityManager.persistAndFlush(
                BidNotice.create("20250004", "G2B", "테스트 공고4", BidStatus.OPEN));
        User user = entityManager.persistAndFlush(User.create("owner4@bidradar.com", "hash", "홍길동"));
        Company company = entityManager.persistAndFlush(Company.create(user, "테스트 회사4"));

        // when // then: status=SUCCESS인데 total_score/grade가 없는 모순된 조합
        assertThatThrownBy(() -> {
            entityManager.getEntityManager().createNativeQuery(
                            "INSERT INTO bid_match_results (bid_notice_id, company_id, status, calculated_at, created_at) "
                                    + "VALUES (:bidNoticeId, :companyId, 'SUCCESS', now(), now())")
                    .setParameter("bidNoticeId", bidNotice.getId())
                    .setParameter("companyId", company.getId())
                    .executeUpdate();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("FAILED인데 세부 점수 컬럼이 남아있는 조합도 CHECK 제약이 거부한다")
    void FAILED인데_세부점수가_남아있으면_CHECK_제약이_거부한다() {
        // given: total_score/grade는 비웠지만 score_tech만 남겨둔 모순된 조합 — V16에서
        // 넓힌 제약이 아니면 이 조합은 잡히지 않는다 (Codex 리뷰, Issue #40).
        BidNotice bidNotice = entityManager.persistAndFlush(
                BidNotice.create("20250005", "G2B", "테스트 공고5", BidStatus.OPEN));
        User user = entityManager.persistAndFlush(User.create("owner5@bidradar.com", "hash", "홍길동"));
        Company company = entityManager.persistAndFlush(Company.create(user, "테스트 회사5"));

        // when // then
        assertThatThrownBy(() -> {
            entityManager.getEntityManager().createNativeQuery(
                            "INSERT INTO bid_match_results "
                                    + "(bid_notice_id, company_id, status, score_tech, calculated_at, created_at) "
                                    + "VALUES (:bidNoticeId, :companyId, 'FAILED', 20.00, now(), now())")
                    .setParameter("bidNoticeId", bidNotice.getId())
                    .setParameter("companyId", company.getId())
                    .executeUpdate();
        }).isInstanceOf(PersistenceException.class);
    }
}
