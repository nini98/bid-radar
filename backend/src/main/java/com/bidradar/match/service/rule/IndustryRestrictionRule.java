package com.bidradar.match.service.rule;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.company.domain.Company;
import com.bidradar.match.service.CompanyProfileContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IndustryRestrictionRule implements ScoreRule {

    private static final String RULE_NAME = "industryRestriction";
    private static final int MAX_SCORE = 10;

    @Override
    public ScoreResult calculate(BidNotice bid, Company company, CompanyProfileContext profile) {
        String restriction = bid.getIndustryRestriction();

        if (restriction == null || !"Y".equalsIgnoreCase(restriction.trim())) {
            return new ScoreResult(RULE_NAME, MAX_SCORE, MAX_SCORE, "업종 제한 없음", List.of());
        }
        return new ScoreResult(RULE_NAME, 0, MAX_SCORE, "업종 제한 있음 - 회사 자격 확인 필요", List.of());
    }
}
