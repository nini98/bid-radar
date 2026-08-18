package com.bidradar.match.event;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.event.BidNoticeCollectedEvent;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.company.domain.Company;
import com.bidradar.company.repository.CompanyRepository;
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
public class BidMatchEventListener {

    private final BidNoticeRepository bidNoticeRepository;
    private final CompanyRepository companyRepository;
    private final MatchCalculationService matchCalculationService;

    @Async("matchTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BidNoticeCollectedEvent event) {
        BidNotice bid = bidNoticeRepository.findById(event.bidNoticeId()).orElse(null);
        if (bid == null) {
            log.warn("매칭 계산 대상 공고를 찾을 수 없음: bidNoticeId={}", event.bidNoticeId());
            return;
        }

        List<Company> companies = companyRepository.findAll();
        for (Company company : companies) {
            try {
                matchCalculationService.calculateAndSave(bid, company);
            } catch (Exception e) {
                log.error("매칭 계산 실패: bidNoticeId={}, companyId={}", bid.getId(), company.getId(), e);
                // 실패 기록 저장 자체가 또 실패해도 이 예외가 for문을 끊고 나머지 회사 처리를
                // 막아버리면 안 되므로 별도로 잡는다 (Issue #40).
                try {
                    matchCalculationService.markFailed(bid, company, buildErrorMessage(e));
                } catch (Exception saveException) {
                    log.error("매칭 실패 상태 저장도 실패: bidNoticeId={}, companyId={}, 원본예외={}",
                            bid.getId(), company.getId(), e.toString(), saveException);
                }
            }
        }
    }

    private String buildErrorMessage(Exception e) {
        return e.getMessage() != null
                ? e.getClass().getSimpleName() + ": " + e.getMessage()
                : e.getClass().getSimpleName();
    }
}
