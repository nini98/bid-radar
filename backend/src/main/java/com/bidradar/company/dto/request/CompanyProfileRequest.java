package com.bidradar.company.dto.request;

import com.bidradar.company.dto.validation.AllowedValues;
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
            // "전자시담(2인 이상)"은 나라장터 명세 원문("전자시담(다자간)")과 다른 프론트 오타값이다.
            // 프론트 라벨 정정(Issue #19 후속 Task) 머지 전까지 기존 저장값 호환을 위해 임시로 함께 허용한다.
            // 그 Task가 머지되면 이 값은 제거한다.
            List<@NotBlank @AllowedValues(values = {
                    "직찰", "전자입찰", "전자입찰/직찰", "전자/직찰/우편/상시", "직찰/우편/상시",
                    "우편/상시", "전자시담", "복수견적(역경매)", "직찰/우편", "전자시담(다자간)",
                    "전자시담(2인 이상)"
            }) String> preferredBidTypes,
            List<@NotBlank @AllowedValues(values = {
                    "일반경쟁", "제한경쟁", "지명경쟁", "수의계약"
            }) String> preferredContractTypes
    ) {}
}
