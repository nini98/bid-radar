package com.bidradar.bid.service.command;

import java.time.LocalDateTime;

public record BidNoticeCreateCommand(
        String externalNoticeId,
        String source,
        String title,
        String agency,
        Long budget,
        String region,
        String bidType,
        String contractType,
        String industryRestriction,
        String noticeUrl,
        LocalDateTime publishedAt,
        LocalDateTime documentDeadline,
        LocalDateTime bidDeadline,
        LocalDateTime openAt,
        String rawData
) {}
