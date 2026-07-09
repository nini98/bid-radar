package com.bidradar.company.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CompanyProfileRequest(
        @NotBlank String companyName,
        String businessNumber,
        String industry,
        Integer foundedYear,
        String companySize,
        String region,
        String address,
        String website,
        String strengths,
        List<Long> techTagIds,
        List<Long> businessAreaIds,
        List<String> certificates,
        List<ProjectExperienceRequest> projectExperiences,
        BidPreferenceRequest bidPreference,
        String managerName,
        @Email String managerEmail,
        String managerPhone
) {

    public record ProjectExperienceRequest(
            String projectType,
            String description
    ) {}

    public record BidPreferenceRequest(
            List<String> preferredRegions,
            Long budgetMin,
            Long budgetMax,
            Integer deadlineMinDays,
            List<String> preferredBidTypes,
            List<String> preferredContractTypes
    ) {}
}
