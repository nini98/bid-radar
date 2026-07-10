package com.bidradar.match.service;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.company.domain.Company;
import com.bidradar.match.domain.MatchGrade;
import com.bidradar.match.service.rule.ScoreResult;
import com.bidradar.match.service.rule.ScoreRule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MatchingEngine {

    private static final List<String> RULE_ORDER = List.of(
            "techTagMatch", "businessAreaMatch", "budgetRange", "regionMatch",
            "industryRestriction", "bidType", "deadline", "regionRestriction"
    );
    private static final int STRONG_REVIEW_THRESHOLD = 80;
    private static final int RECOMMENDED_THRESHOLD = 60;

    private final List<ScoreRule> scoreRules;
    private final ObjectMapper objectMapper;

    public MatchCalculationResult calculate(BidNotice bid, Company company, CompanyProfileContext profile) {
        List<ScoreResult> results = scoreRules.stream()
                .map(rule -> rule.calculate(bid, company, profile))
                .sorted(Comparator.comparingInt(result -> RULE_ORDER.indexOf(result.ruleName())))
                .toList();

        int totalScore = results.stream().mapToInt(ScoreResult::score).sum();
        MatchGrade grade = resolveGrade(totalScore);

        Map<String, ScoreResult> byRule = results.stream()
                .collect(Collectors.toMap(ScoreResult::ruleName, result -> result));

        String scoreReason = results.stream()
                .map(ScoreResult::reason)
                .collect(Collectors.joining(" / "));

        return new MatchCalculationResult(
                BigDecimal.valueOf(totalScore),
                grade,
                scoreOf(byRule, "techTagMatch"),
                scoreOf(byRule, "regionMatch"),
                scoreOf(byRule, "budgetRange"),
                scoreOf(byRule, "businessAreaMatch"),
                toJson(results),
                scoreReason
        );
    }

    private MatchGrade resolveGrade(int totalScore) {
        if (totalScore >= STRONG_REVIEW_THRESHOLD) {
            return MatchGrade.STRONG_REVIEW;
        }
        if (totalScore >= RECOMMENDED_THRESHOLD) {
            return MatchGrade.RECOMMENDED;
        }
        return MatchGrade.NEED_REVIEW;
    }

    private BigDecimal scoreOf(Map<String, ScoreResult> byRule, String ruleName) {
        return BigDecimal.valueOf(byRule.get(ruleName).score());
    }

    private String toJson(List<ScoreResult> results) {
        try {
            return objectMapper.writeValueAsString(results);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
