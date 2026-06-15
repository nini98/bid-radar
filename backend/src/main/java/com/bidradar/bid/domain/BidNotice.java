package com.bidradar.bid.domain;

import com.bidradar.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "bid_notices")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BidNotice extends BaseEntity {

    @Column(name = "external_notice_id", nullable = false, length = 100, unique = true)
    private String externalNoticeId;

    @Column(name = "source", nullable = false, length = 50)
    private String source;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "agency", length = 200)
    private String agency;

    @Column(name = "budget")
    private Long budget;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "bid_type", length = 50)
    private String bidType;

    @Column(name = "contract_type", length = 50)
    private String contractType;

    @Column(name = "industry_restriction", length = 100)
    private String industryRestriction;

    @Column(name = "region_restriction", length = 100)
    private String regionRestriction;

    @Column(name = "qualification_summary", columnDefinition = "TEXT")
    private String qualificationSummary;

    @Column(name = "notice_url", columnDefinition = "TEXT")
    private String noticeUrl;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "question_deadline")
    private LocalDateTime questionDeadline;

    @Column(name = "document_deadline")
    private LocalDateTime documentDeadline;

    @Column(name = "bid_deadline")
    private LocalDateTime bidDeadline;

    @Column(name = "open_at")
    private LocalDateTime openAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BidStatus status;

    @Column(name = "raw_data", columnDefinition = "jsonb")
    private String rawData;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    public static BidNotice create(String externalNoticeId, String source, String title, BidStatus status) {
        BidNotice notice = new BidNotice();
        notice.externalNoticeId = externalNoticeId;
        notice.source = source;
        notice.title = title;
        notice.status = status;
        notice.collectedAt = LocalDateTime.now();
        return notice;
    }

    public void close() {
        if (this.status == BidStatus.CLOSED) {
            throw new IllegalStateException("이미 마감된 공고입니다.");
        }
        this.status = BidStatus.CLOSED;
    }

    public void cancel() {
        if (this.status == BidStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 공고입니다.");
        }
        this.status = BidStatus.CANCELED;
    }
}
