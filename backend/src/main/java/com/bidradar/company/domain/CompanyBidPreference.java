package com.bidradar.company.domain;

import com.bidradar.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Getter
@Entity
@Table(name = "company_bid_preferences")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyBidPreference extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_company_bid_preferences_company"))
    private Company company;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "preferred_regions", columnDefinition = "varchar[]")
    private List<String> preferredRegions;

    @Column(name = "budget_min")
    private Long budgetMin;

    @Column(name = "budget_max")
    private Long budgetMax;

    @Column(name = "deadline_min_days")
    private Integer deadlineMinDays;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "preferred_bid_types", columnDefinition = "varchar[]")
    private List<String> preferredBidTypes;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "preferred_contract_types", columnDefinition = "varchar[]")
    private List<String> preferredContractTypes;

    public static CompanyBidPreference create(Company company,
                                               List<String> preferredRegions,
                                               Long budgetMin,
                                               Long budgetMax,
                                               Integer deadlineMinDays,
                                               List<String> preferredBidTypes,
                                               List<String> preferredContractTypes) {
        CompanyBidPreference preference = new CompanyBidPreference();
        preference.company = company;
        preference.preferredRegions = preferredRegions;
        preference.budgetMin = budgetMin;
        preference.budgetMax = budgetMax;
        preference.deadlineMinDays = deadlineMinDays;
        preference.preferredBidTypes = preferredBidTypes;
        preference.preferredContractTypes = preferredContractTypes;
        return preference;
    }
}
