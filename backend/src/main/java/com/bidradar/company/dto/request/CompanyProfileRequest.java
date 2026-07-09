package com.bidradar.company.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

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
        List<@NotBlank String> certificates,
        @Valid List<ProjectExperienceRequest> projectExperiences,
        @Valid BidPreferenceRequest bidPreference,
        String managerName,
        @Email String managerEmail,
        String managerPhone
) {

    public record ProjectExperienceRequest(
            @NotBlank String projectType,
            @NotBlank String description
    ) {}

    public record BidPreferenceRequest(
            List<@NotBlank String> preferredRegions,
            @PositiveOrZero Long budgetMin,
            @PositiveOrZero Long budgetMax,
            @PositiveOrZero Integer deadlineMinDays,
            List<@NotBlank String> preferredBidTypes,
            List<@NotBlank String> preferredContractTypes
    ) {}
}
