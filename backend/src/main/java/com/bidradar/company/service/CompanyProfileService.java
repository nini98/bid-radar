package com.bidradar.company.service;

import com.bidradar.auth.repository.UserRepository;
import com.bidradar.code.domain.BusinessArea;
import com.bidradar.code.domain.TechTag;
import com.bidradar.code.dto.response.CodeResponse;
import com.bidradar.code.repository.BusinessAreaRepository;
import com.bidradar.code.repository.TechTagRepository;
import com.bidradar.code.service.CodeMapper;
import com.bidradar.common.exception.ApiException;
import com.bidradar.common.response.ResultCode;
import com.bidradar.company.domain.Company;
import com.bidradar.company.domain.CompanyBidPreference;
import com.bidradar.company.domain.CompanyBusinessArea;
import com.bidradar.company.domain.CompanyCertificate;
import com.bidradar.company.domain.CompanyProjectExperience;
import com.bidradar.company.domain.CompanyTechTag;
import com.bidradar.company.dto.request.CompanyProfileRequest;
import com.bidradar.company.dto.request.CompanyProfileRequest.BidPreferenceRequest;
import com.bidradar.company.dto.request.CompanyProfileRequest.ProjectExperienceRequest;
import com.bidradar.company.dto.response.CompanyProfileResponse;
import com.bidradar.company.dto.response.CompanyProfileResponse.BidPreferenceResponse;
import com.bidradar.company.dto.response.CompanyProfileResponse.ProjectExperienceResponse;
import com.bidradar.company.repository.CompanyBidPreferenceRepository;
import com.bidradar.company.repository.CompanyBusinessAreaRepository;
import com.bidradar.company.repository.CompanyCertificateRepository;
import com.bidradar.company.repository.CompanyProjectExperienceRepository;
import com.bidradar.company.repository.CompanyRepository;
import com.bidradar.company.repository.CompanyTechTagRepository;
import com.bidradar.match.domain.MatchCalculationStatus;
import com.bidradar.match.event.MatchRecalculationRequestedEvent;
import com.bidradar.match.repository.MatchCalculationStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyProfileService {

    private static final long CALCULATION_LOCK_STALE_MINUTES = 5;

    private final CompanyRepository companyRepository;
    private final CompanyTechTagRepository companyTechTagRepository;
    private final CompanyBusinessAreaRepository companyBusinessAreaRepository;
    private final CompanyCertificateRepository companyCertificateRepository;
    private final CompanyProjectExperienceRepository companyProjectExperienceRepository;
    private final CompanyBidPreferenceRepository companyBidPreferenceRepository;
    private final TechTagRepository techTagRepository;
    private final BusinessAreaRepository businessAreaRepository;
    private final UserRepository userRepository;
    private final CodeMapper codeMapper;
    private final CompanyProfileMapper companyProfileMapper;
    private final MatchCalculationStatusRepository matchCalculationStatusRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public CompanyProfileResponse getProfile(Long userId) {
        return companyRepository.findByUserId(userId)
                .map(this::buildResponse)
                .orElse(null);
    }

    @Transactional
    public CompanyProfileResponse saveProfile(Long userId, CompanyProfileRequest request) {
        List<TechTag> techTags = validateTechTags(request.techTagIds());
        List<BusinessArea> businessAreas = validateBusinessAreas(request.businessAreaIds());
        validateBidPreference(request.bidPreference());

        Company company = companyRepository.findByUserId(userId)
                .orElseGet(() -> Company.create(userRepository.getReferenceById(userId), request.companyName()));
        company.update(
                request.companyName(), request.businessNumber(), request.industry(), request.foundedYear(),
                request.companySize(), request.region(), request.address(), request.website(), request.strengths(),
                request.managerName(), request.managerEmail(), request.managerPhone()
        );
        company = companyRepository.save(company);

        String lockToken = acquireCalculationLock(company);

        replaceTechTags(company, techTags);
        replaceBusinessAreas(company, businessAreas);
        replaceCertificates(company, request.certificates());
        replaceProjectExperiences(company, request.projectExperiences());
        replaceBidPreference(company, request.bidPreference());

        eventPublisher.publishEvent(new MatchRecalculationRequestedEvent(company.getId(), lockToken));

        return buildResponse(company);
    }

    private String acquireCalculationLock(Company company) {
        String newToken = UUID.randomUUID().toString();
        Optional<MatchCalculationStatus> existing = matchCalculationStatusRepository.findByCompanyId(company.getId());
        if (existing.isEmpty()) {
            matchCalculationStatusRepository.save(MatchCalculationStatus.start(company, newToken));
            return newToken;
        }

        LocalDateTime staleBefore = LocalDateTime.now().minusMinutes(CALCULATION_LOCK_STALE_MINUTES);
        int acquired = matchCalculationStatusRepository.acquireLock(existing.get().getId(), staleBefore, newToken);
        if (acquired == 0) {
            throw new ApiException(ResultCode.MATCH_CALCULATION_IN_PROGRESS);
        }
        return newToken;
    }

    private List<TechTag> validateTechTags(List<Long> techTagIds) {
        if (techTagIds == null || techTagIds.isEmpty()) {
            return List.of();
        }
        List<TechTag> techTags = techTagRepository.findAllById(techTagIds);
        if (techTags.size() != Set.copyOf(techTagIds).size()) {
            throw new ApiException(ResultCode.VALIDATION_ERROR);
        }
        return techTags;
    }

    private List<BusinessArea> validateBusinessAreas(List<Long> businessAreaIds) {
        if (businessAreaIds == null || businessAreaIds.isEmpty()) {
            return List.of();
        }
        List<BusinessArea> businessAreas = businessAreaRepository.findAllById(businessAreaIds);
        if (businessAreas.size() != Set.copyOf(businessAreaIds).size()) {
            throw new ApiException(ResultCode.VALIDATION_ERROR);
        }
        return businessAreas;
    }

    private void validateBidPreference(BidPreferenceRequest bidPreference) {
        if (bidPreference == null) {
            return;
        }
        Long budgetMin = bidPreference.budgetMin();
        Long budgetMax = bidPreference.budgetMax();
        if (budgetMin != null && budgetMax != null && budgetMin > budgetMax) {
            throw new ApiException(ResultCode.VALIDATION_ERROR);
        }
    }

    private void replaceTechTags(Company company, List<TechTag> techTags) {
        companyTechTagRepository.deleteAllByCompanyId(company.getId());
        companyTechTagRepository.flush();
        List<CompanyTechTag> entities = techTags.stream()
                .map(techTag -> CompanyTechTag.create(company, techTag))
                .toList();
        companyTechTagRepository.saveAll(entities);
    }

    private void replaceBusinessAreas(Company company, List<BusinessArea> businessAreas) {
        companyBusinessAreaRepository.deleteAllByCompanyId(company.getId());
        companyBusinessAreaRepository.flush();
        List<CompanyBusinessArea> entities = businessAreas.stream()
                .map(businessArea -> CompanyBusinessArea.create(company, businessArea))
                .toList();
        companyBusinessAreaRepository.saveAll(entities);
    }

    private void replaceCertificates(Company company, List<String> certificateNames) {
        companyCertificateRepository.deleteAllByCompanyId(company.getId());
        companyCertificateRepository.flush();
        if (certificateNames == null) {
            return;
        }
        List<CompanyCertificate> entities = certificateNames.stream()
                .map(name -> CompanyCertificate.create(company, name))
                .toList();
        companyCertificateRepository.saveAll(entities);
    }

    private void replaceProjectExperiences(Company company, List<ProjectExperienceRequest> experiences) {
        companyProjectExperienceRepository.deleteAllByCompanyId(company.getId());
        companyProjectExperienceRepository.flush();
        if (experiences == null) {
            return;
        }
        List<CompanyProjectExperience> entities = experiences.stream()
                .map(experience -> CompanyProjectExperience.create(company, experience.projectType(), experience.description()))
                .toList();
        companyProjectExperienceRepository.saveAll(entities);
    }

    private void replaceBidPreference(Company company, BidPreferenceRequest bidPreference) {
        companyBidPreferenceRepository.deleteByCompanyId(company.getId());
        companyBidPreferenceRepository.flush();
        if (bidPreference == null) {
            return;
        }
        CompanyBidPreference entity = CompanyBidPreference.create(
                company,
                bidPreference.preferredRegions(),
                bidPreference.budgetMin(),
                bidPreference.budgetMax(),
                bidPreference.deadlineMinDays(),
                bidPreference.preferredBidTypes(),
                bidPreference.preferredContractTypes()
        );
        companyBidPreferenceRepository.save(entity);
    }

    private CompanyProfileResponse buildResponse(Company company) {
        List<CodeResponse> techTags = companyTechTagRepository.findByCompanyId(company.getId()).stream()
                .map(companyTechTag -> codeMapper.toResponse(companyTechTag.getTechTag()))
                .toList();
        List<CodeResponse> businessAreas = companyBusinessAreaRepository.findByCompanyId(company.getId()).stream()
                .map(companyBusinessArea -> codeMapper.toResponse(companyBusinessArea.getBusinessArea()))
                .toList();
        List<String> certificates = companyCertificateRepository.findByCompanyId(company.getId()).stream()
                .map(CompanyCertificate::getCertificateName)
                .toList();
        List<ProjectExperienceResponse> projectExperiences = companyProjectExperienceRepository.findByCompanyId(company.getId()).stream()
                .map(companyProfileMapper::toProjectExperienceResponse)
                .toList();
        BidPreferenceResponse bidPreference = companyBidPreferenceRepository.findByCompanyId(company.getId())
                .map(companyProfileMapper::toBidPreferenceResponse)
                .orElse(null);

        return companyProfileMapper.toResponse(company, techTags, businessAreas, certificates, projectExperiences, bidPreference);
    }
}
