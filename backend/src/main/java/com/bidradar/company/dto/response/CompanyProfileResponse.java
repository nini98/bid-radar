package com.bidradar.company.dto.response;

import com.bidradar.code.dto.response.CodeResponse;

import java.time.LocalDateTime;
import java.util.List;

public record CompanyProfileResponse(
        Long id,
        String companyName,
        String businessNumber,
        String industry,
        Integer foundedYear,
        String companySize,
        String region,
        String address,
        String website,
        String strengths,
        List<CodeResponse> techTags,
        List<CodeResponse> businessAreas,
        List<String> certificates,
        List<ProjectExperienceResponse> projectExperiences,
        BidPreferenceResponse bidPreference,
        String managerName,
        String managerEmail,
        String managerPhone,
        LocalDateTime updatedAt
) {

    public record ProjectExperienceResponse(
            String projectType,
            String description
    ) {}

    public record BidPreferenceResponse(
            List<String> preferredRegions,
            Long budgetMin,
            Long budgetMax,
            Integer deadlineMinDays,
            List<String> preferredBidTypes,
            List<String> preferredContractTypes
    ) {}
}
