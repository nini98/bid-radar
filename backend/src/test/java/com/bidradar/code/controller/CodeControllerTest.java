package com.bidradar.code.controller;

import com.bidradar.code.dto.response.CodeResponse;
import com.bidradar.code.service.CodeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CodeController.class)
class CodeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CodeService codeService;

    @Test
    @WithMockUser
    @DisplayName("GET /api/codes/tech-tags 요청 시 공통 Wrapper 구조로 목록이 반환된다")
    void getTechTags_공통Wrapper로_목록을_반환한다() throws Exception {
        given(codeService.getTechTags())
                .willReturn(List.of(new CodeResponse(1L, "Spring"), new CodeResponse(2L, "React")));

        mockMvc.perform(get("/api/codes/tech-tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header.resultCode").value("200"))
                .andExpect(jsonPath("$.data[0].name").value("Spring"))
                .andExpect(jsonPath("$.data[1].name").value("React"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/codes/business-areas 요청 시 공통 Wrapper 구조로 목록이 반환된다")
    void getBusinessAreas_공통Wrapper로_목록을_반환한다() throws Exception {
        given(codeService.getBusinessAreas())
                .willReturn(List.of(new CodeResponse(1L, "SI"), new CodeResponse(2L, "클라우드")));

        mockMvc.perform(get("/api/codes/business-areas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header.resultCode").value("200"))
                .andExpect(jsonPath("$.data[0].name").value("SI"))
                .andExpect(jsonPath("$.data[1].name").value("클라우드"));
    }
}
