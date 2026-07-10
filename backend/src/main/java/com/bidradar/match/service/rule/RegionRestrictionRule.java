package com.bidradar.match.service.rule;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.company.domain.Company;
import com.bidradar.match.service.CompanyProfileContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegionRestrictionRule implements ScoreRule {

    private static final String RULE_NAME = "regionRestriction";
    private static final int MAX_SCORE = 5;

    @Override
    public ScoreResult calculate(BidNotice bid, Company company, CompanyProfileContext profile) {
        String restriction = bid.getRegionRestriction();

        if (restriction == null || restriction.isBlank()) {
            return new ScoreResult(RULE_NAME, MAX_SCORE, MAX_SCORE, "지역 제한 없음", List.of());
        }
        return new ScoreResult(RULE_NAME, 0, MAX_SCORE, "지역 제한 있음: " + restriction, List.of());
    }
}
