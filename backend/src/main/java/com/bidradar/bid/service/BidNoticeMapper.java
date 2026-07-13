package com.bidradar.bid.service;

import com.bidradar.bid.domain.BidAttachment;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.dto.response.BidAttachmentResponse;
import com.bidradar.bid.dto.response.BidNoticeDetailResponse;
import com.bidradar.bid.dto.response.MatchResultResponse;
import com.bidradar.match.domain.BidMatchResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BidNoticeMapper {

    @Mapping(source = "downloaded", target = "isDownloaded")
    @Mapping(source = "analyzed", target = "isAnalyzed")
    BidAttachmentResponse toAttachmentResponse(BidAttachment attachment);

    @Mapping(source = "grade.displayText", target = "displayText")
    MatchResultResponse toMatchResultResponse(BidMatchResult matchResult);

    @Mapping(source = "notice.id", target = "id")
    @Mapping(source = "notice.externalNoticeId", target = "externalNoticeId")
    @Mapping(source = "notice.title", target = "title")
    @Mapping(source = "notice.agency", target = "agency")
    @Mapping(source = "notice.budget", target = "budget")
    @Mapping(source = "notice.region", target = "region")
    @Mapping(source = "notice.bidType", target = "bidType")
    @Mapping(source = "notice.contractType", target = "contractType")
    @Mapping(source = "notice.industryRestriction", target = "industryRestriction")
    @Mapping(source = "notice.regionRestriction", target = "regionRestriction")
    @Mapping(source = "notice.qualificationSummary", target = "qualificationSummary")
    @Mapping(source = "notice.noticeUrl", target = "noticeUrl")
    @Mapping(source = "notice.publishedAt", target = "publishedAt")
    @Mapping(source = "notice.questionDeadline", target = "questionDeadline")
    @Mapping(source = "notice.documentDeadline", target = "documentDeadline")
    @Mapping(source = "notice.bidDeadline", target = "bidDeadline")
    @Mapping(source = "notice.openAt", target = "openAt")
    @Mapping(source = "notice.status", target = "status")
    @Mapping(source = "notice.collectedAt", target = "collectedAt")
    @Mapping(source = "attachmentResponses", target = "attachments")
    @Mapping(source = "matchResult", target = "matchResult")
    BidNoticeDetailResponse toDetailResponse(BidNotice notice, List<BidAttachmentResponse> attachmentResponses, BidMatchResult matchResult);
}
