package com.bidradar.company.controller;

import com.bidradar.company.dto.request.CompanyProfileRequest;
import com.bidradar.company.dto.response.CompanyProfileResponse;
import com.bidradar.company.service.CompanyProfileService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        mockMvc.perform(get("/api/companies/me").with(authentication(authOf(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header.resultCode").value("200"))
                .andExpect(jsonPath("$.data").doesNotExist());
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
}
