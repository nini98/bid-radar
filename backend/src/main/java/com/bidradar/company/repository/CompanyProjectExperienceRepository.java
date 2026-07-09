package com.bidradar.company.repository;

import com.bidradar.company.domain.CompanyProjectExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyProjectExperienceRepository extends JpaRepository<CompanyProjectExperience, Long> {

    List<CompanyProjectExperience> findByCompanyId(Long companyId);

    void deleteAllByCompanyId(Long companyId);
}
