package com.bidradar.code.service;

import com.bidradar.code.dto.response.CodeResponse;
import com.bidradar.code.repository.BusinessAreaRepository;
import com.bidradar.code.repository.TechTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeService {

    private static final Sort ID_ASC = Sort.by(Sort.Direction.ASC, "id");

    private final TechTagRepository techTagRepository;
    private final BusinessAreaRepository businessAreaRepository;
    private final CodeMapper codeMapper;

    public List<CodeResponse> getTechTags() {
        return techTagRepository.findAll(ID_ASC).stream()
                .map(codeMapper::toResponse)
                .toList();
    }

    public List<CodeResponse> getBusinessAreas() {
        return businessAreaRepository.findAll(ID_ASC).stream()
                .map(codeMapper::toResponse)
                .toList();
    }
}
