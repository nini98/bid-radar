package com.bidradar.match.service.rule;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.match.service.CompanyProfileContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class IndustryRestrictionRuleTest {

    private final IndustryRestrictionRule rule = new IndustryRestrictionRule();
    private final CompanyProfileContext profile =
            new CompanyProfileContext(Set.of(), Set.of(), List.of(), null, null, null, List.of(), List.of());

    @Test
    @DisplayName("업종 제한 플래그가 없으면 만점을 받는다")
    void 업종제한없으면_만점() {
        // given
        BidNotice bid = bidNotice(null);

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(10);
    }

    @Test
    @DisplayName("업종 제한 플래그가 N이면 만점을 받는다")
    void 업종제한N이면_만점() {
        // given
        BidNotice bid = bidNotice("N");

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(10);
    }

    @Test
    @DisplayName("업종 제한 플래그가 Y이면 0점을 받는다")
    void 업종제한Y이면_0점() {
        // given
        BidNotice bid = bidNotice("Y");

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(0);
    }

    private BidNotice bidNotice(String industryRestriction) {
        return BidNotice.create(new BidNoticeCreateCommand(
                "EXT-1", "G2B", "테스트 공고", null, null, null, null, null, industryRestriction, null,
                null, null, null, null, null, null));
    }
}
