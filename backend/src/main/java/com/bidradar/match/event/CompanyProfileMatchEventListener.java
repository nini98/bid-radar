package com.bidradar.match.event;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.company.domain.Company;
import com.bidradar.company.event.CompanyProfileSavedEvent;
import com.bidradar.company.repository.CompanyRepository;
import com.bidradar.match.domain.MatchCalculationStatusType;
import com.bidradar.match.service.MatchCalculationStatusCoordinator;
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

    private final CompanyRepository companyRepository;
    private final BidNoticeRepository bidNoticeRepository;
    private final MatchCalculationStatusCoordinator matchCalculationStatusCoordinator;
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
            matchCalculationStatusCoordinator.finish(event.companyId(), event.lockToken(), MatchCalculationStatusType.FAILED);
        }
    }

    private void process(CompanyProfileSavedEvent event) {
        try {
            Company company = companyRepository.findById(event.companyId()).orElse(null);
            if (company == null) {
                log.warn("재계산 대상 회사를 찾을 수 없음: companyId={}", event.companyId());
                matchCalculationStatusCoordinator.finish(event.companyId(), event.lockToken(), MatchCalculationStatusType.FAILED);
                return;
            }

            List<BidNotice> bids = bidNoticeRepository.findAll();
            for (BidNotice bid : bids) {
                boolean stillOwner;
                try {
                    stillOwner = matchCalculationStatusCoordinator.calculateAndSaveIfOwner(bid, company, event.lockToken());
                } catch (Exception e) {
                    log.error("재계산 실패: bidNoticeId={}, companyId={}", bid.getId(), company.getId(), e);
                    continue;
                }
                if (!stillOwner) {
                    log.warn("다른 재계산 작업에 락을 넘겨줘 실행을 중단함: companyId={}", event.companyId());
                    return;
                }
            }
            matchCalculationStatusCoordinator.finish(event.companyId(), event.lockToken(), MatchCalculationStatusType.DONE);
        } catch (Exception e) {
            log.error("회사 매칭 재계산 배치 실패: companyId={}", event.companyId(), e);
            matchCalculationStatusCoordinator.finish(event.companyId(), event.lockToken(), MatchCalculationStatusType.FAILED);
        }
    }
}
