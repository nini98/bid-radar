package com.bidradar.match.service;

import com.bidradar.common.exception.ApiException;
import com.bidradar.common.response.ResultCode;
import com.bidradar.company.domain.Company;
import com.bidradar.company.repository.CompanyRepository;
import com.bidradar.match.domain.MatchCalculationStatus;
import com.bidradar.match.domain.MatchCalculationStatusType;
import com.bidradar.match.dto.response.MatchCalculationStatusResponse;
import com.bidradar.match.event.MatchRecalculationRequestedEvent;
import com.bidradar.match.repository.MatchCalculationStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchCalculationStatusService {

    private final CompanyRepository companyRepository;
    private final MatchCalculationStatusRepository matchCalculationStatusRepository;
    private final MatchCalculationStatusMapper matchCalculationStatusMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public MatchCalculationStatusResponse getStatus(Long userId) {
        Company company = companyRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND));
        return matchCalculationStatusRepository.findByCompanyId(company.getId())
                .map(matchCalculationStatusMapper::toResponse)
                .orElse(new MatchCalculationStatusResponse(null, null));
    }

    @Transactional
    public void retry(Long userId) {
        Company company = companyRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND));
        MatchCalculationStatus status = matchCalculationStatusRepository.findByCompanyId(company.getId())
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND));
        if (status.getStatus() != MatchCalculationStatusType.FAILED) {
            throw new ApiException(ResultCode.MATCH_CALCULATION_RETRY_NOT_ALLOWED);
        }

        String newToken = UUID.randomUUID().toString();
        // staleBefore는 IN_PROGRESS 락 재선점 여부를 가릴 때만 쓰인다. 이미 FAILED임을 확인했으므로
        // acquireLock의 "status <> IN_PROGRESS" 조건이 항상 참이라 이 값은 결과에 영향을 주지 않는다.
        int acquired = matchCalculationStatusRepository.acquireLock(status.getId(), LocalDateTime.now(), newToken);
        if (acquired == 0) {
            throw new ApiException(ResultCode.MATCH_CALCULATION_RETRY_NOT_ALLOWED);
        }

        eventPublisher.publishEvent(new MatchRecalculationRequestedEvent(company.getId(), newToken));
    }
}
