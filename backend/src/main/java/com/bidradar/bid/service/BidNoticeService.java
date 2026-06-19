package com.bidradar.bid.service;

import com.bidradar.bid.dto.request.BidNoticeSearchRequest;
import com.bidradar.bid.dto.response.BidAttachmentResponse;
import com.bidradar.bid.dto.response.BidListResponse;
import com.bidradar.bid.dto.response.BidNoticeDetailResponse;
import com.bidradar.bid.dto.response.BidNoticeSummaryResponse;
import com.bidradar.bid.repository.BidAttachmentRepository;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.bid.repository.BidSearchCondition;
import com.bidradar.common.exception.ApiException;
import com.bidradar.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BidNoticeService {

    private final BidNoticeRepository bidNoticeRepository;
    private final BidAttachmentRepository bidAttachmentRepository;
    private final BidNoticeMapper bidNoticeMapper;

    @Transactional(readOnly = true)
    public BidListResponse getList(BidNoticeSearchRequest request) {
        BidSearchCondition condition = new BidSearchCondition(
                request.keyword(),
                request.region(),
                request.budgetMin(),
                request.budgetMax(),
                request.deadlineDays(),
                request.sort()
        );

        PageRequest pageable = PageRequest.of(request.page(), request.size());
        Page<BidNoticeSummaryResponse> page = bidNoticeRepository.search(condition, pageable);

        long todayNewCount = bidNoticeRepository.countTodayNew();
        long todayDeadlineCount = bidNoticeRepository.countTodayDeadline();

        BidListResponse.Summary summary = new BidListResponse.Summary(todayNewCount, 0L, todayDeadlineCount);
        BidListResponse.PageMeta pageMeta = new BidListResponse.PageMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );

        return new BidListResponse(summary, page.getContent(), pageMeta);
    }

    @Transactional(readOnly = true)
    public BidNoticeDetailResponse getDetail(Long bidId) {
        var notice = bidNoticeRepository.findById(bidId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND));

        List<BidAttachmentResponse> attachments = bidAttachmentRepository.findByBidNoticeId(bidId)
                .stream()
                .map(bidNoticeMapper::toAttachmentResponse)
                .toList();

        return bidNoticeMapper.toDetailResponse(notice, attachments);
    }
}
