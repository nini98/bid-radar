package com.bidradar.match.service.rule;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.company.domain.Company;
import com.bidradar.match.service.CompanyProfileContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DeadlineRule implements ScoreRule {

    private static final String RULE_NAME = "deadline";
    private static final int MAX_SCORE = 10;
    private static final int HALF_SCORE = 5;

    private final Clock clock;

    @Override
    public ScoreResult calculate(BidNotice bid, Company company, CompanyProfileContext profile) {
        LocalDateTime deadline = bid.getBidDeadline();
        Integer deadlineMinDays = profile.deadlineMinDays();

        if (deadline == null) {
            return new ScoreResult(RULE_NAME, MAX_SCORE, MAX_SCORE, "입찰 마감일 정보 없음", List.of());
        }
        if (deadlineMinDays == null) {
            return new ScoreResult(RULE_NAME, MAX_SCORE, MAX_SCORE, "마감 여유 선호 미설정", List.of());
        }

        long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(clock), deadline);
        if (daysRemaining >= deadlineMinDays) {
            return new ScoreResult(RULE_NAME, MAX_SCORE, MAX_SCORE, "마감 여유 충분 (D-" + daysRemaining + ")", List.of());
        }
        if (daysRemaining >= deadlineMinDays / 2.0) {
            return new ScoreResult(RULE_NAME, HALF_SCORE, MAX_SCORE, "마감 여유 부족 (D-" + daysRemaining + ")", List.of());
        }
        return new ScoreResult(RULE_NAME, 0, MAX_SCORE, "마감 임박 (D-" + daysRemaining + ")", List.of());
    }
}
