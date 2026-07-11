package com.bidradar.match.service.rule;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.company.domain.Company;
import com.bidradar.match.service.CompanyProfileContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TechTagMatchRule implements ScoreRule {

    private static final String RULE_NAME = "techTagMatch";
    private static final int MAX_SCORE = 20;

    @Override
    public ScoreResult calculate(BidNotice bid, Company company, CompanyProfileContext profile) {
        String searchText = buildSearchText(bid);
        List<String> matched = profile.techTagNames().stream()
                .filter(searchText::contains)
                .sorted()
                .toList();

        if (matched.isEmpty()) {
            return new ScoreResult(RULE_NAME, 0, MAX_SCORE, "보유 기술 태그와 일치하는 내용 없음", matched);
        }
        return new ScoreResult(RULE_NAME, MAX_SCORE, MAX_SCORE,
                "보유 기술 태그 일치: " + String.join(", ", matched), matched);
    }

    private String buildSearchText(BidNotice bid) {
        String title = bid.getTitle() == null ? "" : bid.getTitle();
        String qualification = bid.getQualificationSummary() == null ? "" : bid.getQualificationSummary();
        return title + " " + qualification;
    }
}
