package com.bidradar.match.service;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.company.domain.Company;
import com.bidradar.match.domain.MatchCalculationStatusType;
import com.bidradar.match.repository.MatchCalculationStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchCalculationStatusCoordinator {

    private final MatchCalculationStatusRepository matchCalculationStatusRepository;
    private final MatchCalculationService matchCalculationService;

    /**
     * 락 토큰 확인(heartbeat)과 결과 저장을 하나의 트랜잭션으로 묶는다.
     * 토큰 확인 UPDATE가 그 row에 잡는 락이 트랜잭션이 끝날 때까지 유지되므로, 다른 작업이
     * 같은 순간 재선점을 시도해도 이 트랜잭션이 끝날 때까지 기다리게 된다 — 그 덕에 "확인 시점엔
     * 유효했는데 저장 시점엔 이미 밀렸다"는 불일치가 생길 수 없다.
     *
     * @return 여전히 유효한 소유자여서 계산/저장까지 마쳤으면 true, 이미 다른 작업에 밀려 건너뛰었으면 false
     */
    @Transactional
    public boolean calculateAndSaveIfOwner(BidNotice bid, Company company, String lockToken) {
        if (matchCalculationStatusRepository.heartbeat(company.getId(), lockToken) == 0) {
            return false;
        }
        matchCalculationService.calculateAndSave(bid, company);
        return true;
    }

    /**
     * 공고 단위 매칭 계산 실패를 기록한다. {@link #calculateAndSaveIfOwner}와 동일하게 heartbeat로
     * 락 토큰을 같은 트랜잭션에서 검증한 뒤에만 저장한다 — 계산 도중 락이 죽은 락 복구(5분 유효기간)로
     * 이미 다른 작업에 넘어갔다면, 소유권을 잃은 이 작업이 그 새 작업의 최신 SUCCESS를 FAILED로
     * 덮어써버리면 안 되기 때문이다 (Codex 리뷰, Issue #40).
     *
     * @return 여전히 유효한 소유자여서 실패 기록까지 마쳤으면 true, 이미 다른 작업에 밀려 건너뛰었으면 false
     */
    @Transactional
    public boolean markFailedIfOwner(BidNotice bid, Company company, String lockToken, String errorMessage) {
        if (matchCalculationStatusRepository.heartbeat(company.getId(), lockToken) == 0) {
            return false;
        }
        matchCalculationService.markFailed(bid, company, errorMessage);
        return true;
    }

    /**
     * REQUIRES_NEW: 이 메서드는 원래 트랜잭션이 이미 커밋된 AFTER_COMMIT 콜백 스레드에서도 호출될 수 있다.
     * 그 시점엔 원래 트랜잭션의 리소스가 아직 스레드에 바인딩되어 있을 수 있어, 기본 REQUIRED로는
     * 이 메서드의 변경이 독립적으로 커밋된다는 보장이 없다. REQUIRES_NEW로 항상 새 트랜잭션을 강제한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finish(Long companyId, String lockToken, MatchCalculationStatusType status) {
        int updated = matchCalculationStatusRepository.finish(companyId, lockToken, status);
        if (updated == 0) {
            log.warn("이미 다른 재계산 작업에 밀려 상태 반영을 건너뜀: companyId={}, status={}", companyId, status);
        }
    }
}
