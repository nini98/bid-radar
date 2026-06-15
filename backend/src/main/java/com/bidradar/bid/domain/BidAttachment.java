package com.bidradar.bid.domain;

import com.bidradar.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "bid_attachments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BidAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_notice_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_bid_attachments_bid_notice"))
    private BidNotice bidNotice;

    @Column(name = "file_name", nullable = false, length = 500)
    private String fileName;

    @Column(name = "file_type", length = 20)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "s3_key", columnDefinition = "TEXT")
    private String s3Key;

    @Column(name = "download_url", columnDefinition = "TEXT")
    private String downloadUrl;

    @Column(name = "is_downloaded", nullable = false)
    private boolean isDownloaded;

    @Column(name = "is_analyzed", nullable = false)
    private boolean isAnalyzed;

    public static BidAttachment create(BidNotice bidNotice, String fileName, String downloadUrl) {
        BidAttachment attachment = new BidAttachment();
        attachment.bidNotice = bidNotice;
        attachment.fileName = fileName;
        attachment.downloadUrl = downloadUrl;
        attachment.isDownloaded = false;
        attachment.isAnalyzed = false;
        return attachment;
    }

    public void markAsDownloaded(String s3Key) {
        if (this.isDownloaded) {
            throw new IllegalStateException("이미 다운로드된 파일입니다.");
        }
        this.isDownloaded = true;
        this.s3Key = s3Key;
    }

    public void markAsAnalyzed() {
        if (this.isAnalyzed) {
            throw new IllegalStateException("이미 분석된 파일입니다.");
        }
        this.isAnalyzed = true;
    }
}
