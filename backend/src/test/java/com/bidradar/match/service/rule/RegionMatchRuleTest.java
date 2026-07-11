package com.bidradar.match.service.rule;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.match.service.CompanyProfileContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegionMatchRuleTest {

    private final RegionMatchRule rule = new RegionMatchRule();

    @Test
    @DisplayName("선호 지역이 미설정이면 만점을 받는다")
    void 선호지역_미설정시_만점() {
        // given
        BidNotice bid = bidNotice("서울특별시");
        CompanyProfileContext profile = profile(List.of());

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(10);
    }

    @Test
    @DisplayName("공고 지역 정보가 없으면 만점을 받는다")
    void 공고지역없으면_만점() {
        // given
        BidNotice bid = bidNotice(null);
        CompanyProfileContext profile = profile(List.of("서울특별시"));

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(10);
    }

    @Test
    @DisplayName("공고 지역이 선호 지역 목록에 포함되면 만점을 받는다")
    void 선호지역포함시_만점() {
        // given
        BidNotice bid = bidNotice("서울특별시");
        CompanyProfileContext profile = profile(List.of("서울특별시", "경기도"));

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(10);
    }

    @Test
    @DisplayName("공고 지역이 선호 지역 목록에 없으면 0점을 받는다")
    void 선호지역불일치시_0점() {
        // given
        BidNotice bid = bidNotice("부산광역시");
        CompanyProfileContext profile = profile(List.of("서울특별시"));

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(0);
    }

    private BidNotice bidNotice(String region) {
        return BidNotice.create(new BidNoticeCreateCommand(
                "EXT-1", "G2B", "테스트 공고", null, null, region, null, null, null, null,
                null, null, null, null, null));
    }

    private CompanyProfileContext profile(List<String> preferredRegions) {
        return new CompanyProfileContext(Set.of(), Set.of(), preferredRegions, null, null, null, List.of(), List.of());
    }
}
