package com.bidradar.match.service.rule;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.company.domain.Company;
import com.bidradar.match.service.CompanyProfileContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TechTagMatchRuleTest {

    private final TechTagMatchRule rule = new TechTagMatchRule();

    @Test
    @DisplayName("공고명에 보유 기술 태그가 포함되면 만점을 받는다")
    void 공고명에_기술태그포함시_만점() {
        // given
        BidNotice bid = bidNotice("Spring Boot 기반 시스템 구축", null);
        CompanyProfileContext profile = profile(Set.of("Spring Boot"));

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(20);
        assertThat(result.matched()).containsExactly("Spring Boot");
    }

    @Test
    @DisplayName("자격요건 요약에 보유 기술 태그가 포함되면 만점을 받는다")
    void 자격요건요약에_기술태그포함시_만점() {
        // given
        BidNotice bid = bidNotice("시스템 구축 사업", "React 개발 경험 필요");
        CompanyProfileContext profile = profile(Set.of("React"));

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(20);
    }

    @Test
    @DisplayName("보유 기술 태그와 일치하는 내용이 없으면 0점을 받는다")
    void 기술태그_불일치시_0점() {
        // given
        BidNotice bid = bidNotice("행사 대행 용역", null);
        CompanyProfileContext profile = profile(Set.of("Spring Boot"));

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(0);
        assertThat(result.matched()).isEmpty();
    }

    private BidNotice bidNotice(String title, String qualificationSummary) {
        return BidNotice.create(new BidNoticeCreateCommand(
                "EXT-1", "G2B", title, null, null, null, null, null, null, null,
                null, null, null, null, null, qualificationSummary));
    }

    private CompanyProfileContext profile(Set<String> techTagNames) {
        return new CompanyProfileContext(techTagNames, Set.of(), List.of(), null, null, null, List.of(), List.of());
    }
}
