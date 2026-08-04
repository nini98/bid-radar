package com.bidradar.match.controller;

import com.bidradar.common.exception.ApiException;
import com.bidradar.common.response.ResultCode;
import com.bidradar.match.domain.MatchCalculationStatusType;
import com.bidradar.match.dto.response.MatchCalculationStatusResponse;
import com.bidradar.match.service.MatchCalculationStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchCalculationStatusController.class)
class MatchCalculationStatusControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MatchCalculationStatusService matchCalculationStatusService;

    private UsernamePasswordAuthenticationToken authOf(Long userId) {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("GET /api/companies/me/match-status 요청 시 현재 계산 상태가 반환된다")
    void getStatus_현재_상태를_반환한다() throws Exception {
        // given
        Instant updatedAt = Instant.parse("2026-08-01T12:00:00Z");
        given(matchCalculationStatusService.getStatus(1L))
                .willReturn(new MatchCalculationStatusResponse(MatchCalculationStatusType.DONE, updatedAt));

        // when // then
        mockMvc.perform(get("/api/companies/me/match-status").with(authentication(authOf(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header.resultCode").value("200"))
                .andExpect(jsonPath("$.data.status").value("DONE"))
                .andExpect(jsonPath("$.data.updatedAt").value("2026-08-01T12:00:00Z"));
    }

    @Test
    @DisplayName("GET /api/companies/me/match-status 요청 시 계산 이력이 없으면 status가 null로 반환된다")
    void getStatus_이력없으면_status_null로_반환된다() throws Exception {
        // given
        given(matchCalculationStatusService.getStatus(1L))
                .willReturn(new MatchCalculationStatusResponse(null, null));

        // when // then
        mockMvc.perform(get("/api/companies/me/match-status").with(authentication(authOf(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header.resultCode").value("200"))
                .andExpect(jsonPath("$.data.status").value(nullValue()));
    }

    @Test
    @DisplayName("GET /api/companies/me/match-status 요청 시 회사 프로필이 없으면 404가 반환된다")
    void getStatus_회사프로필없으면_404를_반환한다() throws Exception {
        // given
        willThrow(new ApiException(ResultCode.NOT_FOUND)).given(matchCalculationStatusService).getStatus(1L);

        // when // then
        mockMvc.perform(get("/api/companies/me/match-status").with(authentication(authOf(1L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.header.resultCode").value("404"));
    }

    @Test
    @DisplayName("POST /api/companies/me/match-status/retry 요청 시 재시도가 성공하면 200이 반환된다")
    void retry_성공하면_200을_반환한다() throws Exception {
        // when // then
        mockMvc.perform(post("/api/companies/me/match-status/retry")
                        .with(authentication(authOf(1L)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header.resultCode").value("200"));
    }

    @Test
    @DisplayName("POST /api/companies/me/match-status/retry 요청 시 FAILED 상태가 아니면 409가 반환된다")
    void retry_FAILED가_아니면_409를_반환한다() throws Exception {
        // given
        willThrow(new ApiException(ResultCode.MATCH_CALCULATION_RETRY_NOT_ALLOWED))
                .given(matchCalculationStatusService).retry(1L);

        // when // then
        mockMvc.perform(post("/api/companies/me/match-status/retry")
                        .with(authentication(authOf(1L)))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.header.resultCode").value("409"));
    }
}
