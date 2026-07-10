package com.bidradar.match.repository;

import com.bidradar.match.domain.BidMatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BidMatchResultRepository extends JpaRepository<BidMatchResult, Long> {

    Optional<BidMatchResult> findByBidNoticeIdAndCompanyId(Long bidNoticeId, Long companyId);
}
