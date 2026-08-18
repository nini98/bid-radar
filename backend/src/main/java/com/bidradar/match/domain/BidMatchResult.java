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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BidMatchResultStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

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
        result.status = BidMatchResultStatus.SUCCESS;
        result.errorMessage = null;
        return result;
    }

    /**
     * 계산이 처음부터 실패해 기존 row가 없는 경우 FAILED row를 생성한다.
     */
    public static BidMatchResult createFailed(BidNotice bidNotice, Company company, String errorMessage) {
        BidMatchResult result = new BidMatchResult();
        result.bidNotice = bidNotice;
        result.company = company;
        result.calculatedAt = LocalDateTime.now();
        result.status = BidMatchResultStatus.FAILED;
        result.errorMessage = errorMessage;
        return result;
    }

    public void update(BigDecimal totalScore,
                        MatchGrade grade,
                        BigDecimal scoreTech,
                        BigDecimal scoreRegion,
                        BigDecimal scoreBudget,
                        BigDecimal scoreBusiness,
                        String matchedKeywords,
                        String scoreReason) {
        this.totalScore = totalScore;
        this.grade = grade;
        this.scoreTech = scoreTech;
        this.scoreRegion = scoreRegion;
        this.scoreBudget = scoreBudget;
        this.scoreBusiness = scoreBusiness;
        this.matchedKeywords = matchedKeywords;
        this.scoreReason = scoreReason;
        this.calculatedAt = LocalDateTime.now();
        this.status = BidMatchResultStatus.SUCCESS;
        this.errorMessage = null;
    }

    /**
     * 재계산 실패 시 이전 성공 점수가 있어도 유지하지 않고 전부 비운다 — 재계산 트리거 자체가
     * 프로필/공고 데이터 변경을 의미하므로 이전 점수는 stale하다고 판단했기 때문이다 (Issue #40).
     */
    public void markFailed(String errorMessage) {
        this.totalScore = null;
        this.grade = null;
        this.scoreTech = null;
        this.scoreRegion = null;
        this.scoreBudget = null;
        this.scoreBusiness = null;
        this.matchedKeywords = null;
        this.scoreReason = null;
        this.calculatedAt = LocalDateTime.now();
        this.status = BidMatchResultStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
