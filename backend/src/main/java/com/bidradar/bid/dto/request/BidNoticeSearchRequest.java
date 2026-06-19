package com.bidradar.bid.dto.request;

public record BidNoticeSearchRequest(
        String keyword,
        String region,
        Long budgetMin,
        Long budgetMax,
        Integer deadlineDays,
        BidSortType sort,
        Integer page,
        Integer size
) {
    public BidNoticeSearchRequest {
        if (sort == null) sort = BidSortType.LATEST;
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 20;
    }
}
