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
        LocalDateTime publishedAt
) {}
