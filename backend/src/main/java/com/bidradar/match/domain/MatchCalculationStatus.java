package com.bidradar.match.domain;

import com.bidradar.common.domain.BaseEntity;
import com.bidradar.company.domain.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "match_calculation_status",
        uniqueConstraints = @UniqueConstraint(name = "uk_match_calculation_status_company", columnNames = "company_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchCalculationStatus extends BaseEntity {

    /**
     * IN_PROGRESS 상태가 이 시간(분)을 넘도록 갱신되지 않으면 죽은 작업으로 간주해
     * 새 시도(프로필 저장/재시도)가 락을 재선점할 수 있다.
     */
    public static final long LOCK_STALE_MINUTES = 5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_match_calculation_status_company"))
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MatchCalculationStatusType status;

    @Column(name = "lock_token", nullable = false, length = 36)
    private String lockToken;

    public static MatchCalculationStatus start(Company company, String lockToken) {
        MatchCalculationStatus status = new MatchCalculationStatus();
        status.company = company;
        status.status = MatchCalculationStatusType.IN_PROGRESS;
        status.lockToken = lockToken;
        return status;
    }

    public void markDone() {
        this.status = MatchCalculationStatusType.DONE;
    }

    public void markFailed() {
        this.status = MatchCalculationStatusType.FAILED;
    }
}
