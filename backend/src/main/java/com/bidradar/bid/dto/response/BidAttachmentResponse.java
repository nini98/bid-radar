package com.bidradar.bid.dto.response;

public record BidAttachmentResponse(
        Long id,
        String fileName,
        String fileType,
        Long fileSize,
        String downloadUrl,
        boolean isDownloaded,
        boolean isAnalyzed
) {}
