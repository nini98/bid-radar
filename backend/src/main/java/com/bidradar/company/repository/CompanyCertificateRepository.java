package com.bidradar.company.repository;

import com.bidradar.company.domain.CompanyCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyCertificateRepository extends JpaRepository<CompanyCertificate, Long> {

    List<CompanyCertificate> findByCompanyId(Long companyId);

    void deleteAllByCompanyId(Long companyId);
}
