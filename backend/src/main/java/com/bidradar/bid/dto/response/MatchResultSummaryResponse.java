package com.bidradar.bid.dto.response;

import com.bidradar.match.domain.BidMatchResultStatus;
import com.bidradar.match.domain.MatchGrade;

import java.math.BigDecimal;

public record MatchResultSummaryResponse(
        BidMatchResultStatus status,
        BigDecimal totalScore,
        MatchGrade grade,
        String displayText
) {
    public MatchResultSummaryResponse(BidMatchResultStatus status, BigDecimal totalScore, MatchGrade grade) {
        this(status, totalScore, grade, grade != null ? grade.getDisplayText() : null);
    }
}
