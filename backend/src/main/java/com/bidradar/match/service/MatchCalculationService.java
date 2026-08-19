package com.bidradar.match.service;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.company.domain.Company;
import com.bidradar.company.domain.CompanyBidPreference;
import com.bidradar.company.repository.CompanyBidPreferenceRepository;
import com.bidradar.company.repository.CompanyBusinessAreaRepository;
import com.bidradar.company.repository.CompanyTechTagRepository;
import com.bidradar.match.domain.BidMatchResult;
import com.bidradar.match.repository.BidMatchResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchCalculationService {

    private final CompanyTechTagRepository companyTechTagRepository;
    private final CompanyBusinessAreaRepository companyBusinessAreaRepository;
    private final CompanyBidPreferenceRepository companyBidPreferenceRepository;
    private final BidMatchResultRepository bidMatchResultRepository;
    private final MatchingEngine matchingEngine;

    /**
     * 계산이 실패했을 때 실패 사실을 기록한다. {@code MatchCalculationStatusCoordinator.markFailedIfOwner()}가
     * heartbeat(락 확인)와 이 저장을 하나의 트랜잭션으로 묶기 위해 REQUIRED로 참여하는 것에 의존하므로,
     * 이 메서드의 propagation을 임의로 REQUIRES_NEW로 바꾸면 그 결합이 깨진다. AFTER_COMMIT 콜백
     * 스레드에서 직접 호출해야 하는 경우는 {@link #markFailedInNewTransaction} 사용.
     */
    @Transactional
    public void markFailed(BidNotice bid, Company company, String errorMessage) {
        markFailedInternal(bid, company, errorMessage);
    }

    /**
     * {@code BidMatchEventListener}가 스레드풀 제출 거부를 처리할 때처럼, AFTER_COMMIT 콜백을
     * 호출한 스레드에서 직접 호출되는 경우 전용이다. 그 시점엔 원래 트랜잭션이 이미 커밋됐지만
     * 트랜잭션 리소스가 아직 스레드에 바인딩되어 있을 수 있어(Spring이 afterCommit 콜백을
     * cleanupAfterCompletion()보다 먼저 호출함), 기본 REQUIRED로는 그 stale한 리소스에 참여해버려
     * 이 저장이 독립적으로 커밋된다는 보장이 없다. REQUIRES_NEW로 항상 새 트랜잭션을 강제한다
     * (MatchCalculationStatusCoordinator.finish()와 동일한 이유, Issue #51 Codex 리뷰).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailedInNewTransaction(BidNotice bid, Company company, String errorMessage) {
        markFailedInternal(bid, company, errorMessage);
    }

    private void markFailedInternal(BidNotice bid, Company company, String errorMessage) {
        BidMatchResult matchResult = bidMatchResultRepository
                .findByBidNoticeIdAndCompanyId(bid.getId(), company.getId())
                .map(existing -> {
                    existing.markFailed(errorMessage);
                    return existing;
                })
                .orElseGet(() -> BidMatchResult.createFailed(bid, company, errorMessage));
        bidMatchResultRepository.save(matchResult);
    }

    @Transactional
    public void calculateAndSave(BidNotice bid, Company company) {
        CompanyProfileContext profile = buildProfile(company);
        MatchCalculationResult result = matchingEngine.calculate(bid, company, profile);

        BidMatchResult matchResult = bidMatchResultRepository
                .findByBidNoticeIdAndCompanyId(bid.getId(), company.getId())
                .map(existing -> {
                    existing.update(
                            result.totalScore(), result.grade(),
                            result.scoreTech(), result.scoreRegion(), result.scoreBudget(), result.scoreBusiness(),
                            result.matchedKeywords(), result.scoreReason()
                    );
                    return existing;
                })
                .orElseGet(() -> BidMatchResult.create(
                        bid, company,
                        result.totalScore(), result.grade(),
                        result.scoreTech(), result.scoreRegion(), result.scoreBudget(), result.scoreBusiness(),
                        result.matchedKeywords(), result.scoreReason()
                ));
        bidMatchResultRepository.save(matchResult);
    }

    private CompanyProfileContext buildProfile(Company company) {
        Set<String> techTagNames = companyTechTagRepository.findByCompanyId(company.getId()).stream()
                .map(companyTechTag -> companyTechTag.getTechTag().getName())
                .collect(Collectors.toSet());
        Set<String> businessAreaNames = companyBusinessAreaRepository.findByCompanyId(company.getId()).stream()
                .map(companyBusinessArea -> companyBusinessArea.getBusinessArea().getName())
                .collect(Collectors.toSet());
        CompanyBidPreference preference = companyBidPreferenceRepository.findByCompanyId(company.getId()).orElse(null);

        return new CompanyProfileContext(
                techTagNames,
                businessAreaNames,
                preference == null ? null : preference.getPreferredRegions(),
                preference == null ? null : preference.getBudgetMin(),
                preference == null ? null : preference.getBudgetMax(),
                preference == null ? null : preference.getDeadlineMinDays(),
                preference == null ? null : preference.getPreferredBidTypes(),
                preference == null ? null : preference.getPreferredContractTypes()
        );
    }
}
