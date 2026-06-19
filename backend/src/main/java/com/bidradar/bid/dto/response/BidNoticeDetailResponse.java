package com.bidradar.bid.dto.response;

import com.bidradar.bid.domain.BidStatus;

import java.time.LocalDateTime;
import java.util.List;

public record BidNoticeDetailResponse(
        Long id,
        String externalNoticeId,
        String title,
        String agency,
        Long budget,
        String region,
        String bidType,
        String contractType,
        String industryRestriction,
        String regionRestriction,
        String qualificationSummary,
        String noticeUrl,
        LocalDateTime publishedAt,
        LocalDateTime questionDeadline,
        LocalDateTime documentDeadline,
        LocalDateTime bidDeadline,
        LocalDateTime openAt,
        BidStatus status,
        LocalDateTime collectedAt,
        List<BidAttachmentResponse> attachments
) {}
