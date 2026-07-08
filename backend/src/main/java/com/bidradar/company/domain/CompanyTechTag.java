package com.bidradar.company.domain;

import com.bidradar.code.domain.TechTag;
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
        name = "company_tech_tags",
        uniqueConstraints = @UniqueConstraint(name = "uk_company_tech_tags_company_tech",
                columnNames = {"company_id", "tech_tag_id"})
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyTechTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_company_tech_tags_company"))
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_tag_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_company_tech_tags_tech_tag"))
    private TechTag techTag;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static CompanyTechTag create(Company company, TechTag techTag) {
        CompanyTechTag companyTechTag = new CompanyTechTag();
        companyTechTag.company = company;
        companyTechTag.techTag = techTag;
        return companyTechTag;
    }
}
