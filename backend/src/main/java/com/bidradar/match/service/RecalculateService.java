package com.bidradar.match.service;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.company.domain.Company;
import com.bidradar.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecalculateService {

    private final CompanyRepository companyRepository;
    private final BidNoticeRepository bidNoticeRepository;
    private final MatchCalculationService matchCalculationService;

    @Async("matchTaskExecutor")
    public void recalculate(Long userId) {
        Company company = companyRepository.findByUserId(userId).orElse(null);
        if (company == null) {
            log.warn("재계산 대상 회사 프로필을 찾을 수 없음: userId={}", userId);
            return;
        }

        List<BidNotice> bids = bidNoticeRepository.findAll();
        for (BidNotice bid : bids) {
            try {
                matchCalculationService.calculateAndSave(bid, company);
            } catch (Exception e) {
                log.error("재계산 실패: bidNoticeId={}, companyId={}", bid.getId(), company.getId(), e);
            }
        }
    }
}
