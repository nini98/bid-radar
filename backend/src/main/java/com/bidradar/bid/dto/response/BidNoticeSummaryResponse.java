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
        // MatchResultResponse를 그대로 만든다. total_score가 null인 경우는 두 가지다 —
        // (1) LEFT JOIN 미스(매치 없음/미계산), (2) 실제 row는 있지만 status=FAILED(Issue #40,
        // total_score가 nullable로 바뀌면서 가능해짐). 둘 다 실패 상태를 아직 노출하지 않는
        // 현재 시점엔 동일하게 "미설정"으로 취급하는 게 맞아 null로 정규화한다. Issue #40의
        // 프론트 노출 Task에서 이 정규화를 다시 검토해야 한다.
        if (matchResult != null && matchResult.totalScore() == null) {
            matchResult = null;
        }
    }
}
