package com.bidradar.match.service;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.match.domain.MatchGrade;
import com.bidradar.match.service.rule.ScoreResult;
import com.bidradar.match.service.rule.ScoreRule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MatchingEngineTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    ScoreRule techTagMatch;
    @Mock
    ScoreRule businessAreaMatch;
    @Mock
    ScoreRule budgetRange;
    @Mock
    ScoreRule regionMatch;
    @Mock
    ScoreRule industryRestriction;
    @Mock
    ScoreRule bidType;
    @Mock
    ScoreRule deadline;
    @Mock
    ScoreRule regionRestriction;
    @Mock
    ScoreRule extraRule;

    @Test
    @DisplayName("8개 룰의 점수를 합산해 총점을 계산한다")
    void 룰점수를_합산해서_총점계산() {
        // given
        stub(20, 20, 15, 10, 10, 10, 10, 5); // 합계 100
        MatchingEngine engine = engine();

        // when
        MatchCalculationResult result = engine.calculate(bid(), null, profile());

        // then
        assertThat(result.totalScore().intValue()).isEqualTo(100);
    }

    @Test
    @DisplayName("총점이 80점 이상이면 STRONG_REVIEW 등급을 받는다")
    void 총점80점이상_STRONG_REVIEW() {
        // given
        stub(20, 20, 15, 10, 10, 5, 0, 0); // 합계 80
        MatchingEngine engine = engine();

        // when
        MatchCalculationResult result = engine.calculate(bid(), null, profile());

        // then
        assertThat(result.grade()).isEqualTo(MatchGrade.STRONG_REVIEW);
    }

    @Test
    @DisplayName("총점이 60~79점이면 RECOMMENDED 등급을 받는다")
    void 총점60에서79점_RECOMMENDED() {
        // given
        stub(20, 20, 15, 10, 0, 0, 0, 0); // 합계 65
        MatchingEngine engine = engine();

        // when
        MatchCalculationResult result = engine.calculate(bid(), null, profile());

        // then
        assertThat(result.grade()).isEqualTo(MatchGrade.RECOMMENDED);
    }

    @Test
    @DisplayName("총점이 60점 미만이면 NEED_REVIEW 등급을 받는다")
    void 총점60점미만_NEED_REVIEW() {
        // given
        stub(20, 20, 0, 0, 0, 0, 0, 0); // 합계 40
        MatchingEngine engine = engine();

        // when
        MatchCalculationResult result = engine.calculate(bid(), null, profile());

        // then
        assertThat(result.grade()).isEqualTo(MatchGrade.NEED_REVIEW);
    }

    @Test
    @DisplayName("세부 컬럼에는 기술/사업분야/예산/지역 4개 룰의 점수만 매핑된다")
    void 세부컬럼에는_4개룰만_매핑() {
        // given
        stub(20, 18, 15, 10, 10, 10, 10, 5);
        MatchingEngine engine = engine();

        // when
        MatchCalculationResult result = engine.calculate(bid(), null, profile());

        // then
        assertThat(result.scoreTech().intValue()).isEqualTo(20);
        assertThat(result.scoreBusiness().intValue()).isEqualTo(18);
        assertThat(result.scoreBudget().intValue()).isEqualTo(15);
        assertThat(result.scoreRegion().intValue()).isEqualTo(10);
    }

    @Test
    @DisplayName("matched_keywords에 8개 룰 결과가 구조화된 JSON으로 저장된다")
    void matchedKeywords에_8개룰_구조화저장() throws Exception {
        // given
        stub(20, 20, 15, 10, 10, 10, 10, 5);
        MatchingEngine engine = engine();

        // when
        MatchCalculationResult result = engine.calculate(bid(), null, profile());

        // then
        JsonNode node = objectMapper.readTree(result.matchedKeywords());
        assertThat(node).hasSize(8);
        assertThat(node.get(0).get("ruleName").asText()).isEqualTo("techTagMatch");
        assertThat(node.get(0).get("score").asInt()).isEqualTo(20);
    }

    @Test
    @DisplayName("동일한 입력으로 반복 계산해도 항상 동일한 결과가 나온다")
    void 동일입력_반복계산시_결정론성() {
        // given
        stub(20, 20, 15, 10, 10, 10, 10, 5);
        MatchingEngine engine = engine();
        BidNotice bid = bid();
        CompanyProfileContext profile = profile();

        // when
        MatchCalculationResult first = engine.calculate(bid, null, profile);
        MatchCalculationResult second = engine.calculate(bid, null, profile);

        // then
        assertThat(first.totalScore()).isEqualByComparingTo(second.totalScore());
        assertThat(first.grade()).isEqualTo(second.grade());
        assertThat(first.matchedKeywords()).isEqualTo(second.matchedKeywords());
        assertThat(first.scoreReason()).isEqualTo(second.scoreReason());
    }

    @Test
    @DisplayName("등록된 룰이 늘어나 만점 합계가 100을 넘어도 총점은 0~100 범위로 정규화된다")
    void 룰추가로_만점합계가100초과해도_정규화된다() {
        // given
        stub(20, 20, 15, 10, 10, 10, 10, 5); // 기존 8개 룰 만점(100/100)
        given(extraRule.calculate(any(), any(), any()))
                .willReturn(new ScoreResult("extraRule", 0, 20, "extra reason", List.of())); // 새 룰은 0/20
        MatchingEngine engine = new MatchingEngine(
                List.of(techTagMatch, businessAreaMatch, budgetRange, regionMatch,
                        industryRestriction, bidType, deadline, regionRestriction, extraRule),
                objectMapper
        );

        // when
        MatchCalculationResult result = engine.calculate(bid(), null, profile());

        // then
        assertThat(result.totalScore().intValue()).isBetween(0, 100);
        assertThat(result.totalScore().intValue()).isEqualTo(83); // 100/120 * 100 반올림
    }

    private void stub(int tech, int business, int budget, int region,
                       int industry, int type, int deadlineScore, int regionRestrict) {
        given(techTagMatch.calculate(any(), any(), any()))
                .willReturn(new ScoreResult("techTagMatch", tech, 20, "tech reason", List.of()));
        given(businessAreaMatch.calculate(any(), any(), any()))
                .willReturn(new ScoreResult("businessAreaMatch", business, 20, "business reason", List.of()));
        given(budgetRange.calculate(any(), any(), any()))
                .willReturn(new ScoreResult("budgetRange", budget, 15, "budget reason", List.of()));
        given(regionMatch.calculate(any(), any(), any()))
                .willReturn(new ScoreResult("regionMatch", region, 10, "region reason", List.of()));
        given(industryRestriction.calculate(any(), any(), any()))
                .willReturn(new ScoreResult("industryRestriction", industry, 10, "industry reason", List.of()));
        given(bidType.calculate(any(), any(), any()))
                .willReturn(new ScoreResult("bidType", type, 10, "bidType reason", List.of()));
        given(deadline.calculate(any(), any(), any()))
                .willReturn(new ScoreResult("deadline", deadlineScore, 10, "deadline reason", List.of()));
        given(regionRestriction.calculate(any(), any(), any()))
                .willReturn(new ScoreResult("regionRestriction", regionRestrict, 5, "regionRestriction reason", List.of()));
    }

    private MatchingEngine engine() {
        return new MatchingEngine(
                List.of(techTagMatch, businessAreaMatch, budgetRange, regionMatch,
                        industryRestriction, bidType, deadline, regionRestriction),
                objectMapper
        );
    }

    private BidNotice bid() {
        return BidNotice.create(new BidNoticeCreateCommand(
                "EXT-1", "G2B", "테스트 공고", null, null, null, null, null, null, null,
                null, null, null, null, null, null));
    }

    private CompanyProfileContext profile() {
        return new CompanyProfileContext(Set.of(), Set.of(), List.of(), null, null, null, List.of(), List.of());
    }
}
