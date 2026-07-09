package com.bidradar.code.controller;

import com.bidradar.code.dto.response.CodeResponse;
import com.bidradar.code.service.CodeService;
import com.bidradar.common.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;

    @GetMapping("/tech-tags")
    public Response<List<CodeResponse>> getTechTags() {
        return Response.success(codeService.getTechTags());
    }

    @GetMapping("/business-areas")
    public Response<List<CodeResponse>> getBusinessAreas() {
        return Response.success(codeService.getBusinessAreas());
    }
}
