package com.bidradar.match.domain;

import com.bidradar.auth.domain.User;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.domain.BidStatus;
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
@Import(JpaConfig.class)
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
}
