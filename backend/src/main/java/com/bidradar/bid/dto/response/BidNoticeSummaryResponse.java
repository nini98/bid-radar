package com.bidradar.bid.dto.response;

import com.bidradar.bid.domain.BidStatus;

import java.time.LocalDateTime;

public record BidNoticeSummaryResponse(
        Long id,
        String title,
        String agency,
        Long budget,
        String region,
        String bidType,
        BidStatus status,
        LocalDateTime bidDeadline,
        LocalDateTime publishedAt,
        MatchResultResponse matchResult
) {
    public BidNoticeSummaryResponse {
        // QueryDSL의 nested constructor projection은 LEFT JOIN 미스여도 필드가 전부 null인
        // MatchResultResponse를 그대로 만든다. total_score는 DB NOT NULL이라 null이면 매치 없음을 뜻한다.
        if (matchResult != null && matchResult.totalScore() == null) {
            matchResult = null;
        }
    }
}
