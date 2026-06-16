package com.bidradar.bid.repository;

import com.bidradar.bid.domain.BidAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidAttachmentRepository extends JpaRepository<BidAttachment, Long> {
}
