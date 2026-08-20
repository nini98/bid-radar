package com.bidradar.match.service.rule;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.match.service.CompanyProfileContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DeadlineRuleTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 10, 0, 0);
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final DeadlineRule rule = new DeadlineRule(Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE));

    @Test
    @DisplayName("입찰 마감일이 없으면 만점을 받는다")
    void 마감일없으면_만점() {
        // given
        BidNotice bid = bidNotice(null);
        CompanyProfileContext profile = profile(10);

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(10);
    }

    @Test
    @DisplayName("마감 여유 선호가 미설정이면 만점을 받는다")
    void 마감여유선호_미설정시_만점() {
        // given
        BidNotice bid = bidNotice(NOW.plusDays(1));
        CompanyProfileContext profile = profile(null);

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(10);
    }

    @Test
    @DisplayName("여유일수가 선호 마감 여유일 이상이면 만점을 받는다")
    void 여유충분시_만점() {
        // given
        BidNotice bid = bidNotice(NOW.plusDays(15));
        CompanyProfileContext profile = profile(10);

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(10);
    }

    @Test
    @DisplayName("여유일수가 선호 마감 여유일의 절반 이상이면 절반 점수를 받는다")
    void 여유부족시_절반점수() {
        // given
        BidNotice bid = bidNotice(NOW.plusDays(7));
        CompanyProfileContext profile = profile(10);

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(5);
    }

    @Test
    @DisplayName("여유일수가 선호 마감 여유일의 절반 미만이면 0점을 받는다")
    void 마감임박시_0점() {
        // given
        BidNotice bid = bidNotice(NOW.plusDays(2));
        CompanyProfileContext profile = profile(10);

        // when
        ScoreResult result = rule.calculate(bid, RuleTestFixtures.company(), profile);

        // then
        assertThat(result.score()).isEqualTo(0);
    }

    private BidNotice bidNotice(LocalDateTime bidDeadline) {
        return BidNotice.create(new BidNoticeCreateCommand(
                "EXT-1", "G2B", "테스트 공고", null, null, null, null, null, null, null,
                null, null, bidDeadline, null, null, null));
    }

    private CompanyProfileContext profile(Integer deadlineMinDays) {
        return new CompanyProfileContext(Set.of(), Set.of(), List.of(), null, null, deadlineMinDays, List.of(), List.of());
    }
}
