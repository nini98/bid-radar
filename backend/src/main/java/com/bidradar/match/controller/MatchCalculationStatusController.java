package com.bidradar.match.controller;

import com.bidradar.common.response.Response;
import com.bidradar.match.dto.response.MatchCalculationStatusResponse;
import com.bidradar.match.service.MatchCalculationStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies/me/match-status")
@RequiredArgsConstructor
public class MatchCalculationStatusController {

    private final MatchCalculationStatusService matchCalculationStatusService;

    @GetMapping
    public Response<MatchCalculationStatusResponse> getStatus(@AuthenticationPrincipal Long userId) {
        return Response.success(matchCalculationStatusService.getStatus(userId));
    }

    @PostMapping("/retry")
    public Response<Void> retry(@AuthenticationPrincipal Long userId) {
        matchCalculationStatusService.retry(userId);
        return Response.success();
    }
}
