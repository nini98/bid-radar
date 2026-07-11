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
