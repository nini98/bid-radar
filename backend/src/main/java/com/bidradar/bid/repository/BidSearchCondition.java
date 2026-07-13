package com.bidradar.bid.repository;

import com.bidradar.bid.dto.query.BidSortType;
import com.bidradar.match.domain.MatchGrade;

public record BidSearchCondition(
        String keyword,
        String region,
        Long budgetMin,
        Long budgetMax,
        Integer deadlineDays,
        MatchGrade grade,
        Long companyId,
        BidSortType sort
) {}
