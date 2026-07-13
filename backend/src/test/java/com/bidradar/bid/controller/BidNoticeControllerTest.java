package com.bidradar.bid.controller;

import com.bidradar.bid.service.BidNoticeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BidNoticeController.class)
class BidNoticeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BidNoticeService bidNoticeService;

    private UsernamePasswordAuthenticationToken authOf(Long userId) {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("GET /api/bids 요청 시 grade에 존재하지 않는 값이 오면 500이 아닌 400이 반환된다")
    void getList_잘못된_grade_값이면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/api/bids")
                        .param("grade", "존재하지않는등급")
                        .with(authentication(authOf(1L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.header.resultCode").value("400"));
    }
}
