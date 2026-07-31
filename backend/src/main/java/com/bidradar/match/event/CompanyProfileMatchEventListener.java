package com.bidradar.match.event;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.company.domain.Company;
import com.bidradar.company.event.CompanyProfileSavedEvent;
import com.bidradar.company.repository.CompanyRepository;
import com.bidradar.match.domain.MatchCalculationStatusType;
import com.bidradar.match.repository.MatchCalculationStatusRepository;
import com.bidradar.match.service.MatchCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyProfileMatchEventListener {

    private final CompanyRepository companyRepository;
    private final BidNoticeRepository bidNoticeRepository;
    private final MatchCalculationService matchCalculationService;
    private final MatchCalculationStatusRepository matchCalculationStatusRepository;

    @Async("matchTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CompanyProfileSavedEvent event) {
        Company company = companyRepository.findById(event.companyId()).orElse(null);
        if (company == null) {
            log.warn("재계산 대상 회사를 찾을 수 없음: companyId={}", event.companyId());
            return;
        }

        try {
            List<BidNotice> bids = bidNoticeRepository.findAll();
            for (BidNotice bid : bids) {
                try {
                    matchCalculationService.calculateAndSave(bid, company);
                } catch (Exception e) {
                    log.error("재계산 실패: bidNoticeId={}, companyId={}", bid.getId(), company.getId(), e);
                }
            }
            markStatus(company.getId(), MatchCalculationStatusType.DONE);
        } catch (Exception e) {
            log.error("회사 매칭 재계산 배치 실패: companyId={}", company.getId(), e);
            markStatus(company.getId(), MatchCalculationStatusType.FAILED);
        }
    }

    private void markStatus(Long companyId, MatchCalculationStatusType type) {
        matchCalculationStatusRepository.findByCompanyId(companyId).ifPresentOrElse(status -> {
            if (type == MatchCalculationStatusType.DONE) {
                status.markDone();
            } else {
                status.markFailed();
            }
            matchCalculationStatusRepository.save(status);
        }, () -> log.error("재계산 상태 row를 찾을 수 없음: companyId={}", companyId));
    }
}
