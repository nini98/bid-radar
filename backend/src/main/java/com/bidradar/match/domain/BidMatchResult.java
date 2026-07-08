package com.bidradar.match.domain;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.company.domain.Company;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "bid_match_results",
        uniqueConstraints = @UniqueConstraint(name = "uk_bid_match_results_notice_company",
                columnNames = {"bid_notice_id", "company_id"})
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BidMatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_notice_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_bid_match_results_bid_notice"))
    private BidNotice bidNotice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_bid_match_results_company"))
    private Company company;

    @Column(name = "total_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal totalScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade", nullable = false, length = 20)
    private MatchGrade grade;

    @Column(name = "score_tech", precision = 5, scale = 2)
    private BigDecimal scoreTech;

    @Column(name = "score_region", precision = 5, scale = 2)
    private BigDecimal scoreRegion;

    @Column(name = "score_budget", precision = 5, scale = 2)
    private BigDecimal scoreBudget;

    @Column(name = "score_business", precision = 5, scale = 2)
    private BigDecimal scoreBusiness;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matched_keywords", columnDefinition = "jsonb")
    private String matchedKeywords;

    @Column(name = "score_reason", columnDefinition = "TEXT")
    private String scoreReason;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static BidMatchResult create(BidNotice bidNotice,
                                         Company company,
                                         BigDecimal totalScore,
                                         MatchGrade grade,
                                         BigDecimal scoreTech,
                                         BigDecimal scoreRegion,
                                         BigDecimal scoreBudget,
                                         BigDecimal scoreBusiness,
                                         String matchedKeywords,
                                         String scoreReason) {
        BidMatchResult result = new BidMatchResult();
        result.bidNotice = bidNotice;
        result.company = company;
        result.totalScore = totalScore;
        result.grade = grade;
        result.scoreTech = scoreTech;
        result.scoreRegion = scoreRegion;
        result.scoreBudget = scoreBudget;
        result.scoreBusiness = scoreBusiness;
        result.matchedKeywords = matchedKeywords;
        result.scoreReason = scoreReason;
        result.calculatedAt = LocalDateTime.now();
        return result;
    }
}
