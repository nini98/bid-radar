package com.bidradar.match.service.rule;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.company.domain.Company;
import com.bidradar.match.service.CompanyProfileContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegionMatchRule implements ScoreRule {

    private static final String RULE_NAME = "regionMatch";
    private static final int MAX_SCORE = 10;

    @Override
    public ScoreResult calculate(BidNotice bid, Company company, CompanyProfileContext profile) {
        List<String> preferredRegions = profile.preferredRegions();

        if (preferredRegions == null || preferredRegions.isEmpty()) {
            return new ScoreResult(RULE_NAME, MAX_SCORE, MAX_SCORE, "선호 지역 미설정", List.of());
        }
        if (bid.getRegion() == null) {
            return new ScoreResult(RULE_NAME, MAX_SCORE, MAX_SCORE, "공고 지역 정보 없음", List.of());
        }
        if (preferredRegions.contains(bid.getRegion())) {
            return new ScoreResult(RULE_NAME, MAX_SCORE, MAX_SCORE, "선호 지역 일치: " + bid.getRegion(), List.of(bid.getRegion()));
        }
        return new ScoreResult(RULE_NAME, 0, MAX_SCORE, "선호 지역과 불일치", List.of());
    }
}
