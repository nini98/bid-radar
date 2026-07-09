package com.bidradar.company.repository;

import com.bidradar.company.domain.CompanyTechTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyTechTagRepository extends JpaRepository<CompanyTechTag, Long> {

    List<CompanyTechTag> findByCompanyId(Long companyId);

    void deleteAllByCompanyId(Long companyId);
}
