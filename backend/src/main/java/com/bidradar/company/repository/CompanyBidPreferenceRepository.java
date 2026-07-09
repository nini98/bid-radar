package com.bidradar.company.repository;

import com.bidradar.company.domain.CompanyBidPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyBidPreferenceRepository extends JpaRepository<CompanyBidPreference, Long> {

    Optional<CompanyBidPreference> findByCompanyId(Long companyId);

    void deleteByCompanyId(Long companyId);
}
