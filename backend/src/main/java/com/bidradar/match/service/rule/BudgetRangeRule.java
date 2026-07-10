package com.bidradar.match.service.rule;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.company.domain.Company;
import com.bidradar.match.service.CompanyProfileContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BudgetRangeRule implements ScoreRule {

    private static final String RULE_NAME = "budgetRange";
    private static final int MAX_SCORE = 15;
    private static final int PARTIAL_SCORE = 7;
    private static final double PARTIAL_OVERAGE_RATIO = 0.5;

    @Override
    public ScoreResult calculate(BidNotice bid, Company company, CompanyProfileContext profile) {
        Long budgetMin = profile.budgetMin();
        Long budgetMax = profile.budgetMax();
        Long budget = bid.getBudget();

        if (budgetMin == null && budgetMax == null) {
            return new ScoreResult(RULE_NAME, MAX_SCORE, MAX_SCORE, "선호 예산 범위 미설정", List.of());
        }
        if (budget == null) {
            return new ScoreResult(RULE_NAME, MAX_SCORE, MAX_SCORE, "공고 예산 정보 없음", List.of());
        }

        boolean belowMin = budgetMin != null && budget < budgetMin;
        boolean aboveMax = budgetMax != null && budget > budgetMax;

        if (!belowMin && !aboveMax) {
            return new ScoreResult(RULE_NAME, MAX_SCORE, MAX_SCORE, "예산 범위 내 적합", List.of());
        }

        Long bound = belowMin ? budgetMin : budgetMax;
        if (bound == null || bound == 0) {
            return new ScoreResult(RULE_NAME, 0, MAX_SCORE, "예산 범위와 크게 벗어남", List.of());
        }

        double overageRatio = Math.abs(budget - bound) / (double) bound;
        if (overageRatio <= PARTIAL_OVERAGE_RATIO) {
            return new ScoreResult(RULE_NAME, PARTIAL_SCORE, MAX_SCORE, "예산 범위를 일부 초과", List.of());
        }
        return new ScoreResult(RULE_NAME, 0, MAX_SCORE, "예산 범위와 크게 벗어남", List.of());
    }
}
