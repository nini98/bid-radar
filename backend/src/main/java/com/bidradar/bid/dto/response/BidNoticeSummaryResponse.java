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
        MatchResultSummaryResponse matchResult
) {
    public BidNoticeSummaryResponse {
        // QueryDSL의 nested constructor projection은 LEFT JOIN 미스여도 필드가 전부 null인
        // MatchResultSummaryResponse를 그대로 만든다 — 이 경우 status도 null이라, "매칭 row 자체가
        // 없음(미계산)"과 "row는 있지만 status=FAILED"를 status 유무로 구분할 수 있다(Issue #40).
        // status가 null(=row 없음)일 때만 미계산으로 정규화하고, FAILED는 그대로 내려보낸다.
        if (matchResult != null && matchResult.status() == null) {
            matchResult = null;
        }
    }
}
