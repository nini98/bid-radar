package com.bidradar.bid.repository;

import com.bidradar.bid.domain.BidAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BidAttachmentRepository extends JpaRepository<BidAttachment, Long> {
    List<BidAttachment> findByBidNoticeId(Long bidNoticeId);
}
