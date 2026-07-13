package com.bidradar.bid.dto.query;

import com.bidradar.match.domain.MatchGrade;

public record BidNoticeSearchCondition(
        String keyword,
        String region,
        Long budgetMin,
        Long budgetMax,
        Integer deadlineDays,
        MatchGrade grade,
        BidSortType sort,
        Integer page,
        Integer size
) {
    public BidNoticeSearchCondition {
        if (sort == null) sort = BidSortType.LATEST;
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 20;
    }
}
