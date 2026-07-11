package com.bidradar.company.controller;

import com.bidradar.common.response.Response;
import com.bidradar.company.dto.request.CompanyProfileRequest;
import com.bidradar.company.dto.response.CompanyProfileResponse;
import com.bidradar.company.service.CompanyProfileService;
import com.bidradar.match.service.RecalculateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyProfileController {

    private final CompanyProfileService companyProfileService;
    private final RecalculateService recalculateService;

    @GetMapping("/me")
    public Response<CompanyProfileResponse> getMyProfile(@AuthenticationPrincipal Long userId) {
        return Response.success(companyProfileService.getProfile(userId));
    }

    @PutMapping("/me")
    public Response<CompanyProfileResponse> saveMyProfile(@AuthenticationPrincipal Long userId,
                                                            @RequestBody @Valid CompanyProfileRequest request) {
        return Response.success(companyProfileService.saveProfile(userId, request));
    }

    @PostMapping("/me/recalculate")
    public Response<Void> recalculate(@AuthenticationPrincipal Long userId) {
        companyProfileService.validateProfileExists(userId);
        recalculateService.recalculate(userId);
        return Response.success();
    }
}
