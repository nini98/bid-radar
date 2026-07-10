package com.bidradar.match.service.rule;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.company.domain.Company;
import com.bidradar.match.service.CompanyProfileContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BidTypeRule implements ScoreRule {

    private static final String RULE_NAME = "bidType";
    private static final int MAX_SCORE = 10;
    private static final int HALF_SCORE = 5;

    @Override
    public ScoreResult calculate(BidNotice bid, Company company, CompanyProfileContext profile) {
        List<String> matched = new ArrayList<>();
        List<String> reasons = new ArrayList<>();

        int bidTypeScore = evaluate(profile.preferredBidTypes(), bid.getBidType(), "입찰 방식", matched, reasons);
        int contractTypeScore = evaluate(profile.preferredContractTypes(), bid.getContractType(), "계약 방식", matched, reasons);

        int score = bidTypeScore + contractTypeScore;
        return new ScoreResult(RULE_NAME, score, MAX_SCORE, String.join(", ", reasons), matched);
    }

    private int evaluate(List<String> preferred, String actual, String label, List<String> matched, List<String> reasons) {
        if (preferred == null || preferred.isEmpty()) {
            reasons.add(label + " 선호 미설정");
            return HALF_SCORE;
        }
        if (actual == null) {
            reasons.add(label + " 정보 없음");
            return HALF_SCORE;
        }
        if (preferred.contains(actual)) {
            matched.add(actual);
            reasons.add(label + " 선호 일치: " + actual);
            return HALF_SCORE;
        }
        reasons.add(label + " 선호와 불일치");
        return 0;
    }
}
