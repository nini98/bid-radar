package com.bidradar.match.repository;

import com.bidradar.match.domain.MatchCalculationStatus;
import com.bidradar.match.domain.MatchCalculationStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MatchCalculationStatusRepository extends JpaRepository<MatchCalculationStatus, Long> {

    Optional<MatchCalculationStatus> findByCompanyId(Long companyId);

    /**
     * {@code @Modifying} 커스텀 쿼리는 SimpleJpaRepository의 CRUD 메서드(save 등)와 달리
     * 리포지토리 프록시가 자동으로 트랜잭션을 걸어주지 않는다. 이 메서드는 트랜잭션이 없는
     * 컨텍스트(비동기 리스너 스레드 등)에서도 호출되므로 직접 {@code @Transactional}을 명시한다.
     */
    @Transactional
    @Modifying
    @Query("""
            UPDATE MatchCalculationStatus s
            SET s.status = com.bidradar.match.domain.MatchCalculationStatusType.IN_PROGRESS,
                s.updatedAt = CURRENT_TIMESTAMP,
                s.lockToken = :newToken
            WHERE s.id = :id
              AND (s.status <> com.bidradar.match.domain.MatchCalculationStatusType.IN_PROGRESS
                   OR s.updatedAt < :staleBefore)
            """)
    int acquireLock(@Param("id") Long id, @Param("staleBefore") LocalDateTime staleBefore, @Param("newToken") String newToken);

    /**
     * 재시도 전용 CAS: FAILED이거나, IN_PROGRESS인데 죽은 것으로 간주되는 낡은 락(updatedAt이 staleBefore 이전)일 때만
     * 락을 재선점한다. {@link #acquireLock}과 달리 DONE은 통과시키지 않는다 — "정상 완료된 결과를 재시도로
     * 덮어쓸 수 없다"는 재시도 API 고유의 계약이라, 상태 확인과 락 획득을 이 쿼리 하나로 원자적으로 묶어
     * 조회 시점과 갱신 시점 사이에 상태가 바뀌는 경쟁 상태(TOCTOU)를 없앤다.
     */
    @Transactional
    @Modifying
    @Query("""
            UPDATE MatchCalculationStatus s
            SET s.status = com.bidradar.match.domain.MatchCalculationStatusType.IN_PROGRESS,
                s.updatedAt = CURRENT_TIMESTAMP,
                s.lockToken = :newToken
            WHERE s.id = :id
              AND (s.status = com.bidradar.match.domain.MatchCalculationStatusType.FAILED
                   OR (s.status = com.bidradar.match.domain.MatchCalculationStatusType.IN_PROGRESS
                       AND s.updatedAt < :staleBefore))
            """)
    int acquireRetryLock(@Param("id") Long id, @Param("staleBefore") LocalDateTime staleBefore, @Param("newToken") String newToken);

    /**
     * 진행 중인 재계산 작업이 자신이 여전히 유효한 락 소유자임을 알리는 생존 신고.
     * 다른 작업이 이미 락을 재선점해 lockToken이 바뀌었으면 0건 갱신되어 실패로 감지된다.
     */
    @Transactional
    @Modifying
    @Query("""
            UPDATE MatchCalculationStatus s
            SET s.updatedAt = CURRENT_TIMESTAMP
            WHERE s.company.id = :companyId
              AND s.lockToken = :token
              AND s.status = com.bidradar.match.domain.MatchCalculationStatusType.IN_PROGRESS
            """)
    int heartbeat(@Param("companyId") Long companyId, @Param("token") String token);

    /**
     * 락 소유자가 여전히 자신일 때만 최종 상태(DONE/FAILED)를 반영한다.
     * 이미 다른 작업에 밀렸으면(lockToken 불일치) 0건 갱신되어 조용히 무시된다.
     */
    @Transactional
    @Modifying
    @Query("""
            UPDATE MatchCalculationStatus s
            SET s.status = :status,
                s.updatedAt = CURRENT_TIMESTAMP
            WHERE s.company.id = :companyId
              AND s.lockToken = :token
            """)
    int finish(@Param("companyId") Long companyId, @Param("token") String token, @Param("status") MatchCalculationStatusType status);
}
