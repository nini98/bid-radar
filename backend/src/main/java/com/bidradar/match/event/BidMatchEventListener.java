package com.bidradar.match.event;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.event.BidNoticeCollectedEvent;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.company.domain.Company;
import com.bidradar.company.repository.CompanyRepository;
import com.bidradar.match.service.MatchCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
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
    private final TaskExecutor matchTaskExecutor;

    /**
     * {@code @Async}는 스레드풀 제출을 프록시 뒤에서 대신 해줘, 큐 포화로 제출 자체가 거부되면
     * 우리 코드가 그 실패를 잡을 방법이 없다(제출 거부 예외가 이 메서드 본문이 시작되기도 전에
     * 프록시 계층에서 발생함). 여기서는 직접 제출해 거부를 감지하고, 회사 목록조차 순회하지
     * 못한 이 공고의 전체 회사를 즉시 FAILED로 기록한다 (Issue #51).
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BidNoticeCollectedEvent event) {
        try {
            matchTaskExecutor.execute(() -> process(event));
        } catch (TaskRejectedException e) {
            log.error("매칭 계산 작업 제출이 거부됨(스레드풀 포화): bidNoticeId={}", event.bidNoticeId(), e);
            recordRejectionFailure(event.bidNoticeId(), e);
        }
    }

    private void process(BidNoticeCollectedEvent event) {
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

    private void recordRejectionFailure(Long bidNoticeId, Exception rejection) {
        BidNotice bid = bidNoticeRepository.findById(bidNoticeId).orElse(null);
        if (bid == null) {
            log.warn("매칭 계산 대상 공고를 찾을 수 없음: bidNoticeId={}", bidNoticeId);
            return;
        }

        List<Company> companies = companyRepository.findAll();
        for (Company company : companies) {
            try {
                // AFTER_COMMIT 콜백 스레드에서 직접 호출되므로 REQUIRES_NEW가 필요하다 (클래스 상단 Javadoc 참고).
                matchCalculationService.markFailedInNewTransaction(bid, company, buildErrorMessage(rejection));
            } catch (Exception saveException) {
                log.error("제출 거부 실패 상태 저장도 실패: bidNoticeId={}, companyId={}",
                        bid.getId(), company.getId(), saveException);
            }
        }
    }

    private String buildErrorMessage(Exception e) {
        return e.getMessage() != null
                ? e.getClass().getSimpleName() + ": " + e.getMessage()
                : e.getClass().getSimpleName();
    }
}
