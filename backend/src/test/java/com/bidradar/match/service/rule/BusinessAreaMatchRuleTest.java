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

class BusinessAreaMatchRuleTest {

    private final BusinessAreaMatchRule rule = new BusinessAreaMatchRule();

    @Test
    @DisplayName("공고명에 보유 사업 분야가 포함되면 만점을 받는다")
    void 공고명에_사업분야포함시_만점() {
        // given
        BidNotice bid = bidNotice("스마트팩토리 관제 시스템 구축", null);
        CompanyProfileContext profile = profile(Set.of("스마트팩토리"));

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(20);
        assertThat(result.matched()).containsExactly("스마트팩토리");
    }

    @Test
    @DisplayName("보유 사업 분야와 일치하는 내용이 없으면 0점을 받는다")
    void 사업분야_불일치시_0점() {
        // given
        BidNotice bid = bidNotice("행사 대행 용역", null);
        CompanyProfileContext profile = profile(Set.of("스마트팩토리"));

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(0);
    }

    private BidNotice bidNotice(String title, String qualificationSummary) {
        BidNotice notice = BidNotice.create(new BidNoticeCreateCommand(
                "EXT-1", "G2B", title, null, null, null, null, null, null, null,
                null, null, null, null, null));
        ReflectionTestUtils.setField(notice, "qualificationSummary", qualificationSummary);
        return notice;
    }

    private CompanyProfileContext profile(Set<String> businessAreaNames) {
        return new CompanyProfileContext(Set.of(), businessAreaNames, List.of(), null, null, null, List.of(), List.of());
    }
}
