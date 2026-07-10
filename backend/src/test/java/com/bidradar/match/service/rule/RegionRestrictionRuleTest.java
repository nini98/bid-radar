package com.bidradar.match.service.rule;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.match.service.CompanyProfileContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegionRestrictionRuleTest {

    private final RegionRestrictionRule rule = new RegionRestrictionRule();
    private final CompanyProfileContext profile =
            new CompanyProfileContext(Set.of(), Set.of(), List.of(), null, null, null, List.of(), List.of());

    @Test
    @DisplayName("지역 제한이 없으면 만점을 받는다")
    void 지역제한없으면_만점() {
        // given
        BidNotice bid = bidNotice(null);

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(5);
    }

    @Test
    @DisplayName("지역 제한이 있으면 0점을 받는다")
    void 지역제한있으면_0점() {
        // given
        BidNotice bid = bidNotice("서울특별시");

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(0);
    }

    private BidNotice bidNotice(String regionRestriction) {
        BidNotice notice = BidNotice.create(new BidNoticeCreateCommand(
                "EXT-1", "G2B", "테스트 공고", null, null, null, null, null, null, null,
                null, null, null, null, null));
        ReflectionTestUtils.setField(notice, "regionRestriction", regionRestriction);
        return notice;
    }
}
