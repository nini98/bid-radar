package com.bidradar.company.repository;

import com.bidradar.company.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
