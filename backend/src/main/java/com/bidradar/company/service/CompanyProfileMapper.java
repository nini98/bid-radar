package com.bidradar.company.service;

import com.bidradar.code.dto.response.CodeResponse;
import com.bidradar.company.domain.Company;
import com.bidradar.company.domain.CompanyBidPreference;
import com.bidradar.company.domain.CompanyProjectExperience;
import com.bidradar.company.dto.response.CompanyProfileResponse;
import com.bidradar.company.dto.response.CompanyProfileResponse.BidPreferenceResponse;
import com.bidradar.company.dto.response.CompanyProfileResponse.ProjectExperienceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyProfileMapper {

    ProjectExperienceResponse toProjectExperienceResponse(CompanyProjectExperience experience);

    BidPreferenceResponse toBidPreferenceResponse(CompanyBidPreference bidPreference);

    @Mapping(source = "company.id", target = "id")
    @Mapping(source = "company.companyName", target = "companyName")
    @Mapping(source = "company.businessNumber", target = "businessNumber")
    @Mapping(source = "company.industry", target = "industry")
    @Mapping(source = "company.foundedYear", target = "foundedYear")
    @Mapping(source = "company.companySize", target = "companySize")
    @Mapping(source = "company.region", target = "region")
    @Mapping(source = "company.address", target = "address")
    @Mapping(source = "company.website", target = "website")
    @Mapping(source = "company.strengths", target = "strengths")
    @Mapping(source = "techTags", target = "techTags")
    @Mapping(source = "businessAreas", target = "businessAreas")
    @Mapping(source = "certificates", target = "certificates")
    @Mapping(source = "projectExperiences", target = "projectExperiences")
    @Mapping(source = "bidPreference", target = "bidPreference")
    @Mapping(source = "company.managerName", target = "managerName")
    @Mapping(source = "company.managerEmail", target = "managerEmail")
    @Mapping(source = "company.managerPhone", target = "managerPhone")
    @Mapping(source = "company.updatedAt", target = "updatedAt")
    CompanyProfileResponse toResponse(Company company,
                                       List<CodeResponse> techTags,
                                       List<CodeResponse> businessAreas,
                                       List<String> certificates,
                                       List<ProjectExperienceResponse> projectExperiences,
                                       BidPreferenceResponse bidPreference);
}
