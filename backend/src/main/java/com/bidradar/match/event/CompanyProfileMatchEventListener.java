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
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyProfileMatchEventListener {

    private static final int HEARTBEAT_INTERVAL = 20;

    private final CompanyRepository companyRepository;
    private final BidNoticeRepository bidNoticeRepository;
    private final MatchCalculationService matchCalculationService;
    private final MatchCalculationStatusRepository matchCalculationStatusRepository;
    private final TaskExecutor matchTaskExecutor;

    /**
     * {@code @Async}는 스레드풀 제출을 프록시 뒤에서 대신 해줘, 큐 포화로 제출 자체가 거부되면
     * 우리 코드가 그 실패를 잡을 방법이 없다. 여기서는 직접 제출해 거부를 감지하고 즉시 FAILED로 기록한다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CompanyProfileSavedEvent event) {
        try {
            matchTaskExecutor.execute(() -> process(event));
        } catch (TaskRejectedException e) {
            log.error("재계산 작업 제출이 거부됨(스레드풀 포화): companyId={}", event.companyId(), e);
            finish(event, MatchCalculationStatusType.FAILED);
        }
    }

    private void process(CompanyProfileSavedEvent event) {
        Company company = companyRepository.findById(event.companyId()).orElse(null);
        if (company == null) {
            log.warn("재계산 대상 회사를 찾을 수 없음: companyId={}", event.companyId());
            return;
        }

        try {
            List<BidNotice> bids = bidNoticeRepository.findAll();
            int processed = 0;
            for (BidNotice bid : bids) {
                try {
                    matchCalculationService.calculateAndSave(bid, company);
                } catch (Exception e) {
                    log.error("재계산 실패: bidNoticeId={}, companyId={}", bid.getId(), company.getId(), e);
                }
                processed++;
                if (processed % HEARTBEAT_INTERVAL == 0 && !heartbeat(event)) {
                    log.warn("다른 재계산 작업에 락을 넘겨줘 실행을 중단함: companyId={}", event.companyId());
                    return;
                }
            }
            finish(event, MatchCalculationStatusType.DONE);
        } catch (Exception e) {
            log.error("회사 매칭 재계산 배치 실패: companyId={}", company.getId(), e);
            finish(event, MatchCalculationStatusType.FAILED);
        }
    }

    /**
     * 이 작업이 여전히 유효한 락 소유자임을 알리는 생존 신고.
     * 다른 작업이 이미 재선점해 lockToken이 바뀌었으면 false를 반환해 호출자가 실행을 중단하게 한다.
     */
    private boolean heartbeat(CompanyProfileSavedEvent event) {
        return matchCalculationStatusRepository.heartbeat(event.companyId(), event.lockToken()) > 0;
    }

    /**
     * 락 소유자가 여전히 자신일 때만 최종 상태를 반영한다. 이미 다른 작업에 밀렸으면 조용히 무시된다.
     */
    private void finish(CompanyProfileSavedEvent event, MatchCalculationStatusType status) {
        int updated = matchCalculationStatusRepository.finish(event.companyId(), event.lockToken(), status);
        if (updated == 0) {
            log.warn("이미 다른 재계산 작업에 밀려 상태 반영을 건너뜀: companyId={}, status={}", event.companyId(), status);
        }
    }
}
