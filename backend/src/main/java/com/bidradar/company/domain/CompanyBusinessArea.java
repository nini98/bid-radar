package com.bidradar.company.domain;

import com.bidradar.code.domain.BusinessArea;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "company_business_areas",
        uniqueConstraints = @UniqueConstraint(name = "uk_company_business_areas_company_area",
                columnNames = {"company_id", "business_area_id"})
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyBusinessArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_company_business_areas_company"))
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_area_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_company_business_areas_business_area"))
    private BusinessArea businessArea;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static CompanyBusinessArea create(Company company, BusinessArea businessArea) {
        CompanyBusinessArea companyBusinessArea = new CompanyBusinessArea();
        companyBusinessArea.company = company;
        companyBusinessArea.businessArea = businessArea;
        return companyBusinessArea;
    }
}
