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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_match_calculation_status_company"))
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MatchCalculationStatusType status;

    public static MatchCalculationStatus start(Company company) {
        MatchCalculationStatus status = new MatchCalculationStatus();
        status.company = company;
        status.status = MatchCalculationStatusType.IN_PROGRESS;
        return status;
    }

    public void markDone() {
        this.status = MatchCalculationStatusType.DONE;
    }

    public void markFailed() {
        this.status = MatchCalculationStatusType.FAILED;
    }
}
