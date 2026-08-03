package com.bidradar.match.service;

import com.bidradar.common.exception.ApiException;
import com.bidradar.common.response.ResultCode;
import com.bidradar.company.domain.Company;
import com.bidradar.company.repository.CompanyRepository;
import com.bidradar.match.domain.MatchCalculationStatus;
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

        String newToken = UUID.randomUUID().toString();
        LocalDateTime staleBefore = LocalDateTime.now().minusMinutes(MatchCalculationStatus.LOCK_STALE_MINUTES);
        int acquired = matchCalculationStatusRepository.acquireRetryLock(status.getId(), staleBefore, newToken);
        if (acquired == 0) {
            throw new ApiException(ResultCode.MATCH_CALCULATION_RETRY_NOT_ALLOWED);
        }

        eventPublisher.publishEvent(new MatchRecalculationRequestedEvent(company.getId(), newToken));
    }
}
