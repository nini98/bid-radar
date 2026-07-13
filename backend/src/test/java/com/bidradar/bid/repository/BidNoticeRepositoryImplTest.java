package com.bidradar.bid.repository;

import com.bidradar.auth.domain.User;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.dto.query.BidSortType;
import com.bidradar.bid.dto.response.BidNoticeSummaryResponse;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.company.domain.Company;
import com.bidradar.config.JpaConfig;
import com.bidradar.match.domain.BidMatchResult;
import com.bidradar.match.domain.MatchGrade;
import com.bidradar.support.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class BidNoticeRepositoryImplTest extends IntegrationTestBase {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BidNoticeRepository bidNoticeRepository;

    @Test
    @DisplayName("companyId가 일치하는 매치 결과가 있으면 목록에 matchResult가 채워진다")
    void companyId가_일치하면_matchResult가_채워진다() {
        // given
        BidNotice bid = entityManager.persistAndFlush(createBid("20250101"));
        Company company = createCompany("owner1@bidradar.com");
        entityManager.persistAndFlush(BidMatchResult.create(
                bid, company, new BigDecimal("85.00"), MatchGrade.STRONG_REVIEW,
                new BigDecimal("40.00"), new BigDecimal("20.00"), new BigDecimal("15.00"), new BigDecimal("10.00"),
                "[\"백엔드\"]", "기술 태그 일치"));
        entityManager.clear();

        BidSearchCondition condition = new BidSearchCondition(
                null, null, null, null, null, null, company.getId(), BidSortType.LATEST);

        // when
        Page<BidNoticeSummaryResponse> page = bidNoticeRepository.search(condition, PageRequest.of(0, 20));

        // then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).matchResult()).isNotNull();
        assertThat(page.getContent().get(0).matchResult().totalScore()).isEqualByComparingTo("85.00");
        assertThat(page.getContent().get(0).matchResult().grade()).isEqualTo(MatchGrade.STRONG_REVIEW);
        assertThat(page.getContent().get(0).matchResult().displayText()).isEqualTo("적극 검토");
    }

    @Test
    @DisplayName("companyId가 null이면 매치 결과가 존재해도 matchResult가 null이다")
    void companyId가_null이면_matchResult가_null이다() {
        // given
        BidNotice bid = entityManager.persistAndFlush(createBid("20250102"));
        Company company = createCompany("owner2@bidradar.com");
        entityManager.persistAndFlush(BidMatchResult.create(
                bid, company, new BigDecimal("85.00"), MatchGrade.STRONG_REVIEW,
                null, null, null, null, null, null));
        entityManager.clear();

        BidSearchCondition condition = new BidSearchCondition(
                null, null, null, null, null, null, null, BidSortType.LATEST);

        // when
        Page<BidNoticeSummaryResponse> page = bidNoticeRepository.search(condition, PageRequest.of(0, 20));

        // then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).matchResult()).isNull();
    }

    @Test
    @DisplayName("grade 필터 적용 시 해당 등급의 매치 결과를 가진 공고만 반환된다")
    void grade_필터가_동작한다() {
        // given
        BidNotice strongBid = entityManager.persistAndFlush(createBid("20250103"));
        BidNotice recommendedBid = entityManager.persistAndFlush(createBid("20250104"));
        Company company = createCompany("owner3@bidradar.com");
        entityManager.persistAndFlush(BidMatchResult.create(
                strongBid, company, new BigDecimal("90.00"), MatchGrade.STRONG_REVIEW,
                null, null, null, null, null, null));
        entityManager.persistAndFlush(BidMatchResult.create(
                recommendedBid, company, new BigDecimal("65.00"), MatchGrade.RECOMMENDED,
                null, null, null, null, null, null));
        entityManager.clear();

        BidSearchCondition condition = new BidSearchCondition(
                null, null, null, null, null, MatchGrade.STRONG_REVIEW, company.getId(), BidSortType.LATEST);

        // when
        Page<BidNoticeSummaryResponse> page = bidNoticeRepository.search(condition, PageRequest.of(0, 20));

        // then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).id()).isEqualTo(strongBid.getId());
    }

    @Test
    @DisplayName("sort=score 정렬이 total_score 내림차순으로 동작한다")
    void sort_score가_total_score_기준으로_동작한다() {
        // given
        BidNotice lowScoreBid = entityManager.persistAndFlush(createBid("20250105"));
        BidNotice highScoreBid = entityManager.persistAndFlush(createBid("20250106"));
        Company company = createCompany("owner4@bidradar.com");
        entityManager.persistAndFlush(BidMatchResult.create(
                lowScoreBid, company, new BigDecimal("50.00"), MatchGrade.NEED_REVIEW,
                null, null, null, null, null, null));
        entityManager.persistAndFlush(BidMatchResult.create(
                highScoreBid, company, new BigDecimal("95.00"), MatchGrade.STRONG_REVIEW,
                null, null, null, null, null, null));
        entityManager.clear();

        BidSearchCondition condition = new BidSearchCondition(
                null, null, null, null, null, null, company.getId(), BidSortType.SCORE);

        // when
        Page<BidNoticeSummaryResponse> page = bidNoticeRepository.search(condition, PageRequest.of(0, 20));

        // then
        assertThat(page.getContent()).extracting(BidNoticeSummaryResponse::id)
                .containsExactly(highScoreBid.getId(), lowScoreBid.getId());
    }

    @Test
    @DisplayName("sort=score 정렬 시 매치 결과가 없는 공고는 점수가 있는 공고보다 뒤로 밀린다")
    void sort_score가_매치결과_없는_공고를_뒤로_보낸다() {
        // given
        BidNotice unmatchedBid = entityManager.persistAndFlush(createBid("20250107"));
        BidNotice matchedBid = entityManager.persistAndFlush(createBid("20250108"));
        Company company = createCompany("owner5@bidradar.com");
        entityManager.persistAndFlush(BidMatchResult.create(
                matchedBid, company, new BigDecimal("60.00"), MatchGrade.RECOMMENDED,
                null, null, null, null, null, null));
        entityManager.clear();

        BidSearchCondition condition = new BidSearchCondition(
                null, null, null, null, null, null, company.getId(), BidSortType.SCORE);

        // when
        Page<BidNoticeSummaryResponse> page = bidNoticeRepository.search(condition, PageRequest.of(0, 20));

        // then
        assertThat(page.getContent()).extracting(BidNoticeSummaryResponse::id)
                .containsExactly(matchedBid.getId(), unmatchedBid.getId());
    }

    @Test
    @DisplayName("sort=score 정렬 시 점수가 같으면 publishedAt 내림차순으로 안정적으로 정렬된다")
    void sort_score가_동점일때_publishedAt으로_보조정렬된다() {
        // given
        BidNotice olderBid = entityManager.persistAndFlush(createBid("20250109"));
        BidNotice newerBid = entityManager.persistAndFlush(createBid("20250110"));
        Company company = createCompany("owner6@bidradar.com");
        entityManager.persistAndFlush(BidMatchResult.create(
                olderBid, company, new BigDecimal("70.00"), MatchGrade.RECOMMENDED,
                null, null, null, null, null, null));
        entityManager.persistAndFlush(BidMatchResult.create(
                newerBid, company, new BigDecimal("70.00"), MatchGrade.RECOMMENDED,
                null, null, null, null, null, null));
        entityManager.clear();

        BidSearchCondition condition = new BidSearchCondition(
                null, null, null, null, null, null, company.getId(), BidSortType.SCORE);

        // when
        Page<BidNoticeSummaryResponse> page = bidNoticeRepository.search(condition, PageRequest.of(0, 20));

        // then
        assertThat(page.getContent()).extracting(BidNoticeSummaryResponse::id)
                .containsExactly(newerBid.getId(), olderBid.getId());
    }

    private BidNotice createBid(String externalNoticeId) {
        return BidNotice.create(new BidNoticeCreateCommand(
                externalNoticeId, "G2B", "테스트 공고 " + externalNoticeId, "국토교통부",
                50_000_000L, "서울특별시", "일반경쟁", "일반계약", null, null,
                LocalDateTime.now(), null, LocalDateTime.now().plusDays(10), null, null));
    }

    private Company createCompany(String email) {
        User user = entityManager.persistAndFlush(User.create(email, "hash", "홍길동"));
        return entityManager.persistAndFlush(Company.create(user, "테스트 회사"));
    }
}
