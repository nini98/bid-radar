package com.bidradar.bid.service;

import com.bidradar.bid.dto.query.BidNoticeSearchCondition;
import com.bidradar.bid.dto.response.BidAttachmentResponse;
import com.bidradar.bid.dto.response.BidListResponse;
import com.bidradar.bid.dto.response.BidNoticeDetailResponse;
import com.bidradar.bid.dto.response.BidNoticeSummaryResponse;
import com.bidradar.bid.repository.BidAttachmentRepository;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.bid.repository.BidSearchCondition;
import com.bidradar.common.exception.ApiException;
import com.bidradar.common.response.ResultCode;
import com.bidradar.company.domain.Company;
import com.bidradar.company.repository.CompanyRepository;
import com.bidradar.match.domain.BidMatchResult;
import com.bidradar.match.repository.BidMatchResultRepository;
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
    private final CompanyRepository companyRepository;
    private final BidMatchResultRepository bidMatchResultRepository;
    private final BidNoticeMapper bidNoticeMapper;

    @Transactional(readOnly = true)
    public BidListResponse getList(BidNoticeSearchCondition condition, Long userId) {
        Long companyId = companyRepository.findByUserId(userId)
                .map(Company::getId)
                .orElse(null);

        BidSearchCondition searchCondition = new BidSearchCondition(
                condition.keyword(),
                condition.region(),
                condition.budgetMin(),
                condition.budgetMax(),
                condition.deadlineDays(),
                condition.grade(),
                companyId,
                condition.sort()
        );

        PageRequest pageable = PageRequest.of(condition.page(), condition.size());
        Page<BidNoticeSummaryResponse> page = bidNoticeRepository.search(searchCondition, pageable);

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
    public BidNoticeDetailResponse getDetail(Long bidId, Long userId) {
        var notice = bidNoticeRepository.findById(bidId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND));

        List<BidAttachmentResponse> attachments = bidAttachmentRepository.findByBidNoticeId(bidId)
                .stream()
                .map(bidNoticeMapper::toAttachmentResponse)
                .toList();

        BidMatchResult matchResult = companyRepository.findByUserId(userId)
                .flatMap(company -> bidMatchResultRepository.findByBidNoticeIdAndCompanyId(bidId, company.getId()))
                .orElse(null);

        return bidNoticeMapper.toDetailResponse(notice, attachments, matchResult);
    }
}
