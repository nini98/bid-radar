package com.bidradar.bid.repository;

import com.bidradar.bid.domain.BidNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidNoticeRepository extends JpaRepository<BidNotice, Long> {
    boolean existsByExternalNoticeId(String externalNoticeId);
}
