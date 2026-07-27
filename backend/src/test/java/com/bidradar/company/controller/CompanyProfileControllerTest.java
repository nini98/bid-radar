package com.bidradar.company.controller;

import com.bidradar.common.exception.ApiException;
import com.bidradar.common.response.ResultCode;
import com.bidradar.company.dto.request.CompanyProfileRequest;
import com.bidradar.company.dto.response.CompanyProfileResponse;
import com.bidradar.company.service.CompanyProfileService;
import com.bidradar.match.service.RecalculateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyProfileController.class)
class CompanyProfileControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CompanyProfileService companyProfileService;

    @MockitoBean
    RecalculateService recalculateService;

    private UsernamePasswordAuthenticationToken authOf(Long userId) {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private CompanyProfileRequest validRequest() {
        return new CompanyProfileRequest(
                "델타소프트", null, null, null, null, null, null, null, null,
                List.of(), List.of(), List.of(), List.of(), null,
                null, null, null
        );
    }

    @Test
    @DisplayName("GET /api/companies/me 요청 시 공통 Wrapper 구조로 프로필이 반환된다")
    void getMyProfile_공통Wrapper로_프로필을_반환한다() throws Exception {
        // given
        CompanyProfileResponse response = new CompanyProfileResponse(
                1L, "델타소프트", null, null, null, null, null, null, null, null,
                List.of(), List.of(), List.of(), List.of(), null,
                null, null, null, null
        );
        given(companyProfileService.getProfile(1L)).willReturn(response);

        // when // then
        mockMvc.perform(get("/api/companies/me").with(authentication(authOf(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header.resultCode").value("200"))
                .andExpect(jsonPath("$.data.companyName").value("델타소프트"));
    }

    @Test
    @DisplayName("GET /api/companies/me 요청 시 프로필이 없으면 data: null로 응답된다")
    void getMyProfile_프로필없으면_data_null로_응답된다() throws Exception {
        // given
        given(companyProfileService.getProfile(1L)).willReturn(null);

        // when // then
        // "$.data"가 아예 없는 응답과 구별하기 위해 값이 JSON null임을 명시적으로 검증한다.
        // (evaluateJsonPath는 경로 자체가 없으면 예외를 던지므로 value(nullValue())는 "존재하되 null"만 통과시킨다)
        mockMvc.perform(get("/api/companies/me").with(authentication(authOf(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header.resultCode").value("200"))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(content().string(containsString("\"data\":null")));
    }

    @Test
    @DisplayName("PUT /api/companies/me 정상 요청 시 200으로 저장된 프로필이 반환된다")
    void saveMyProfile_정상요청시_200을_반환한다() throws Exception {
        // given
        CompanyProfileResponse response = new CompanyProfileResponse(
                1L, "델타소프트", null, null, null, null, null, null, null, null,
                List.of(), List.of(), List.of(), List.of(), null,
                null, null, null, null
        );
        given(companyProfileService.saveProfile(eq(1L), any())).willReturn(response);

        // when // then
        mockMvc.perform(put("/api/companies/me")
                        .with(authentication(authOf(1L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header.resultCode").value("200"))
                .andExpect(jsonPath("$.data.companyName").value("델타소프트"));
    }

    @Test
    @DisplayName("PUT /api/companies/me 요청 시 companyName이 없으면 400이 반환된다")
    void saveMyProfile_companyName_누락시_400을_반환한다() throws Exception {
        // when // then
        mockMvc.perform(put("/api/companies/me")
                        .with(authentication(authOf(1L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.header.resultCode").value("400"));
    }

    @Test
    @DisplayName("PUT /api/companies/me 요청 시 projectExperiences의 projectType이 비어 있으면 400이 반환된다")
    void saveMyProfile_projectType_공백이면_400을_반환한다() throws Exception {
        // given
        CompanyProfileRequest request = new CompanyProfileRequest(
                "델타소프트", null, null, null, null, null, null, null, null,
                List.of(), List.of(), List.of(),
                List.of(new CompanyProfileRequest.ProjectExperienceRequest(" ", "설명")),
                null, null, null, null
        );

        // when // then
        mockMvc.perform(put("/api/companies/me")
                        .with(authentication(authOf(1L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.header.resultCode").value("400"));
    }

    @Test
    @DisplayName("PUT /api/companies/me 요청 시 bidPreference.deadlineMinDays가 음수이면 400이 반환된다")
    void saveMyProfile_deadlineMinDays_음수이면_400을_반환한다() throws Exception {
        // given
        CompanyProfileRequest request = new CompanyProfileRequest(
                "델타소프트", null, null, null, null, null, null, null, null,
                List.of(), List.of(), List.of(), List.of(),
                new CompanyProfileRequest.BidPreferenceRequest(List.of(), null, null, -1, List.of(), List.of()),
                null, null, null
        );

        // when // then
        mockMvc.perform(put("/api/companies/me")
                        .with(authentication(authOf(1L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.header.resultCode").value("400"));
    }

    @Test
    @DisplayName("PUT /api/companies/me 요청 시 preferredBidTypes에 허용되지 않은 값이 있으면 400이 반환된다")
    void saveMyProfile_preferredBidTypes_허용되지않은값이면_400을_반환한다() throws Exception {
        // given
        CompanyProfileRequest request = new CompanyProfileRequest(
                "델타소프트", null, null, null, null, null, null, null, null,
                List.of(), List.of(), List.of(), List.of(),
                new CompanyProfileRequest.BidPreferenceRequest(List.of(), null, null, null, List.of("존재하지않는값"), List.of()),
                null, null, null
        );

        // when // then
        mockMvc.perform(put("/api/companies/me")
                        .with(authentication(authOf(1L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.header.resultCode").value("400"));
    }

    @Test
    @DisplayName("PUT /api/companies/me 요청 시 preferredBidTypes에 임시 호환값 '전자시담(2인 이상)'을 보내면 200이 반환된다")
    void saveMyProfile_전자시담_2인이상_임시호환값이면_200을_반환한다() throws Exception {
        // given
        CompanyProfileResponse response = new CompanyProfileResponse(
                1L, "델타소프트", null, null, null, null, null, null, null, null,
                List.of(), List.of(), List.of(), List.of(), null,
                null, null, null, null
        );
        given(companyProfileService.saveProfile(eq(1L), any())).willReturn(response);

        CompanyProfileRequest request = new CompanyProfileRequest(
                "델타소프트", null, null, null, null, null, null, null, null,
                List.of(), List.of(), List.of(), List.of(),
                new CompanyProfileRequest.BidPreferenceRequest(List.of(), null, null, null, List.of("전자시담(2인 이상)"), List.of()),
                null, null, null
        );

        // when // then
        mockMvc.perform(put("/api/companies/me")
                        .with(authentication(authOf(1L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header.resultCode").value("200"));
    }

    @Test
    @DisplayName("POST /api/companies/me/recalculate 요청 시 200을 반환하고 비동기 재계산을 트리거한다")
    void recalculate_정상요청시_200을_반환한다() throws Exception {
        // when // then
        mockMvc.perform(post("/api/companies/me/recalculate")
                        .with(authentication(authOf(1L)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header.resultCode").value("200"));

        verify(recalculateService).recalculate(1L);
    }

    @Test
    @DisplayName("POST /api/companies/me/recalculate 요청 시 회사 프로필이 없으면 404를 반환한다")
    void recalculate_프로필없으면_404를_반환한다() throws Exception {
        // given
        willThrow(new ApiException(ResultCode.NOT_FOUND))
                .given(companyProfileService).validateProfileExists(1L);

        // when // then
        mockMvc.perform(post("/api/companies/me/recalculate")
                        .with(authentication(authOf(1L)))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.header.resultCode").value("404"));

        verify(recalculateService, never()).recalculate(any());
    }
}
