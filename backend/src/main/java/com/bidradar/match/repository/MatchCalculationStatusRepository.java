package com.bidradar.match.repository;

import com.bidradar.match.domain.MatchCalculationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MatchCalculationStatusRepository extends JpaRepository<MatchCalculationStatus, Long> {

    Optional<MatchCalculationStatus> findByCompanyId(Long companyId);

    @Modifying
    @Query("""
            UPDATE MatchCalculationStatus s
            SET s.status = com.bidradar.match.domain.MatchCalculationStatusType.IN_PROGRESS,
                s.updatedAt = CURRENT_TIMESTAMP
            WHERE s.id = :id
              AND (s.status <> com.bidradar.match.domain.MatchCalculationStatusType.IN_PROGRESS
                   OR s.updatedAt < :staleBefore)
            """)
    int acquireLock(@Param("id") Long id, @Param("staleBefore") LocalDateTime staleBefore);
}
