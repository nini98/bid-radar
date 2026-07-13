package com.bidradar.bid.dto.response;

import com.bidradar.match.domain.MatchGrade;

import java.math.BigDecimal;

public record MatchResultSummaryResponse(
        BigDecimal totalScore,
        MatchGrade grade,
        String displayText
) {
    public MatchResultSummaryResponse(BigDecimal totalScore, MatchGrade grade) {
        this(totalScore, grade, grade != null ? grade.getDisplayText() : null);
    }
}
