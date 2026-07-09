package com.bidradar.code.service;

import com.bidradar.code.dto.response.CodeResponse;
import com.bidradar.code.repository.BusinessAreaRepository;
import com.bidradar.code.repository.TechTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeService {

    private final TechTagRepository techTagRepository;
    private final BusinessAreaRepository businessAreaRepository;
    private final CodeMapper codeMapper;

    public List<CodeResponse> getTechTags() {
        return techTagRepository.findAll().stream()
                .map(codeMapper::toResponse)
                .toList();
    }

    public List<CodeResponse> getBusinessAreas() {
        return businessAreaRepository.findAll().stream()
                .map(codeMapper::toResponse)
                .toList();
    }
}
