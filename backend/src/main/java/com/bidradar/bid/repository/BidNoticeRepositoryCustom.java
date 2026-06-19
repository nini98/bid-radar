package com.bidradar.bid.repository;

import com.bidradar.bid.dto.response.BidNoticeSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BidNoticeRepositoryCustom {

    Page<BidNoticeSummaryResponse> search(BidSearchCondition condition, Pageable pageable);

    long countTodayNew();

    long countTodayDeadline();
}
