package com.bidradar.match.service.rule;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.match.service.CompanyProfileContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BidTypeRuleTest {

    private final BidTypeRule rule = new BidTypeRule();

    @Test
    @DisplayName("입찰 방식과 계약 방식이 모두 일치하면 만점을 받는다")
    void 둘다일치시_만점() {
        // given
        BidNotice bid = bidNotice("전자입찰", "일반경쟁");
        CompanyProfileContext profile = profile(List.of("전자입찰"), List.of("일반경쟁"));

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(10);
    }

    @Test
    @DisplayName("입찰 방식만 일치하면 절반 점수를 받는다")
    void 하나만일치시_절반점수() {
        // given
        BidNotice bid = bidNotice("전자입찰", "수의계약");
        CompanyProfileContext profile = profile(List.of("전자입찰"), List.of("일반경쟁"));

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(5);
    }

    @Test
    @DisplayName("선호 조건이 미설정이면 해당 항목은 만점 처리된다")
    void 선호미설정시_만점처리() {
        // given
        BidNotice bid = bidNotice("전자입찰", "일반경쟁");
        CompanyProfileContext profile = profile(List.of(), List.of());

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(10);
    }

    @Test
    @DisplayName("둘 다 불일치하면 0점을 받는다")
    void 둘다불일치시_0점() {
        // given
        BidNotice bid = bidNotice("직찰", "지명경쟁");
        CompanyProfileContext profile = profile(List.of("전자입찰"), List.of("일반경쟁"));

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(0);
    }

    private BidNotice bidNotice(String bidType, String contractType) {
        return BidNotice.create(new BidNoticeCreateCommand(
                "EXT-1", "G2B", "테스트 공고", null, null, null, bidType, contractType, null, null,
                null, null, null, null, null));
    }

    private CompanyProfileContext profile(List<String> preferredBidTypes, List<String> preferredContractTypes) {
        return new CompanyProfileContext(Set.of(), Set.of(), List.of(), null, null, null,
                preferredBidTypes, preferredContractTypes);
    }
}
