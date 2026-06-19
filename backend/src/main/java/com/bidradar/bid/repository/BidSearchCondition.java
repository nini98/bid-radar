package com.bidradar.bid.repository;

import com.bidradar.bid.dto.request.BidSortType;

public record BidSearchCondition(
        String keyword,
        String region,
        Long budgetMin,
        Long budgetMax,
        Integer deadlineDays,
        BidSortType sort
) {}
