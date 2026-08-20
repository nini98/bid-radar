package com.bidradar.match.service.rule;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.match.service.CompanyProfileContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BudgetRangeRuleTest {

    private final BudgetRangeRule rule = new BudgetRangeRule();

    @Test
    @DisplayName("선호 예산 범위가 미설정이면 만점을 받는다")
    void 선호예산_미설정시_만점() {
        // given
        BidNotice bid = bidNotice(50_000_000L);
        CompanyProfileContext profile = profile(null, null);

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(15);
    }

    @Test
    @DisplayName("공고 예산 정보가 없으면 만점을 받는다")
    void 공고예산없으면_만점() {
        // given
        BidNotice bid = bidNotice(null);
        CompanyProfileContext profile = profile(10_000_000L, 100_000_000L);

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(15);
    }

    @Test
    @DisplayName("예산이 선호 범위 내에 있으면 만점을 받는다")
    void 예산범위내_만점() {
        // given
        BidNotice bid = bidNotice(50_000_000L);
        CompanyProfileContext profile = profile(10_000_000L, 100_000_000L);

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(15);
    }

    @Test
    @DisplayName("예산이 선호 범위를 50% 이내로 초과하면 부분 점수를 받는다")
    void 예산범위_일부초과시_부분점수() {
        // given
        BidNotice bid = bidNotice(140_000_000L);
        CompanyProfileContext profile = profile(10_000_000L, 100_000_000L);

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(7);
    }

    @Test
    @DisplayName("예산이 선호 범위를 크게 벗어나면 0점을 받는다")
    void 예산범위_크게벗어나면_0점() {
        // given
        BidNotice bid = bidNotice(300_000_000L);
        CompanyProfileContext profile = profile(10_000_000L, 100_000_000L);

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(0);
    }

    private BidNotice bidNotice(Long budget) {
        return BidNotice.create(new BidNoticeCreateCommand(
                "EXT-1", "G2B", "테스트 공고", null, budget, null, null, null, null, null,
                null, null, null, null, null, null));
    }

    private CompanyProfileContext profile(Long budgetMin, Long budgetMax) {
        return new CompanyProfileContext(Set.of(), Set.of(), List.of(), budgetMin, budgetMax, null, List.of(), List.of());
    }
}
