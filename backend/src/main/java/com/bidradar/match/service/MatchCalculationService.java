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
     * 계산이 실패했을 때 실패 사실을 기록한다. 항상 이전 {@code @Transactional} 호출
     * (예: {@link #calculateAndSave}, {@code MatchCalculationStatusCoordinator.calculateAndSaveIfOwner})이
     * 이미 커밋/롤백을 마치고 반환된 뒤 비동기 풀 스레드에서 호출되므로, 이 시점엔 활성 트랜잭션이
     * 없다. 그래서 {@code REQUIRES_NEW}가 아니라 기본 REQUIRED로도 독립적인 커밋이 보장된다.
     */
    @Transactional
    public void markFailed(BidNotice bid, Company company, String errorMessage) {
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
