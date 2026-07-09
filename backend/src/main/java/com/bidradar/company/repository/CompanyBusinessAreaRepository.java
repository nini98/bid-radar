package com.bidradar.company.repository;

import com.bidradar.company.domain.CompanyBusinessArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyBusinessAreaRepository extends JpaRepository<CompanyBusinessArea, Long> {

    List<CompanyBusinessArea> findByCompanyId(Long companyId);

    void deleteAllByCompanyId(Long companyId);
}
