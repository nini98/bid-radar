package com.bidradar.bid.dto.response;

import java.util.List;

public record BidListResponse(
        Summary summary,
        List<BidNoticeSummaryResponse> content,
        PageMeta page
) {
    public record Summary(long todayNewCount, long todayRecommendedCount, long todayDeadlineCount) {}

    public record PageMeta(int number, int size, long totalElements, int totalPages) {}
}
