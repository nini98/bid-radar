package com.bidradar.bid.dto.response;

import com.bidradar.match.domain.MatchGrade;

import java.math.BigDecimal;

public record MatchResultResponse(
        BigDecimal totalScore,
        MatchGrade grade,
        String displayText,
        BigDecimal scoreTech,
        BigDecimal scoreBusiness,
        BigDecimal scoreBudget,
        BigDecimal scoreRegion,
        String matchedKeywords,
        String scoreReason
) {
    public MatchResultResponse(BigDecimal totalScore,
                                MatchGrade grade,
                                BigDecimal scoreTech,
                                BigDecimal scoreBusiness,
                                BigDecimal scoreBudget,
                                BigDecimal scoreRegion,
                                String matchedKeywords,
                                String scoreReason) {
        this(totalScore, grade, grade != null ? grade.getDisplayText() : null,
                scoreTech, scoreBusiness, scoreBudget, scoreRegion, matchedKeywords, scoreReason);
    }
}
