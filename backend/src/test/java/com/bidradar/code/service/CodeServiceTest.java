package com.bidradar.code.service;

import com.bidradar.code.domain.BusinessArea;
import com.bidradar.code.domain.TechTag;
import com.bidradar.code.dto.response.CodeResponse;
import com.bidradar.code.repository.BusinessAreaRepository;
import com.bidradar.code.repository.TechTagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CodeServiceTest {

    @Mock
    TechTagRepository techTagRepository;

    @Mock
    BusinessAreaRepository businessAreaRepository;

    final CodeMapper codeMapper = new CodeMapperImpl();

    CodeService codeService;

    @BeforeEach
    void setUp() {
        codeService = new CodeService(techTagRepository, businessAreaRepository, codeMapper);
    }

    @Test
    @DisplayName("getTechTags는 id 오름차순 정렬 조건으로 조회하고 순서를 그대로 유지한다")
    void getTechTags_id_오름차순으로_조회한다() {
        // given
        TechTag first = TechTag.create("Java");
        TechTag second = TechTag.create("Spring");
        given(techTagRepository.findAll(any(Sort.class)))
                .willReturn(List.of(first, second));

        // when
        List<CodeResponse> result = codeService.getTechTags();

        // then
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(techTagRepository).findAll(sortCaptor.capture());
        assertThat(sortCaptor.getValue()).isEqualTo(Sort.by(Sort.Direction.ASC, "id"));
        assertThat(result).extracting(CodeResponse::name).containsExactly("Java", "Spring");
    }

    @Test
    @DisplayName("getBusinessAreas는 id 오름차순 정렬 조건으로 조회하고 순서를 그대로 유지한다")
    void getBusinessAreas_id_오름차순으로_조회한다() {
        // given
        BusinessArea first = BusinessArea.create("SI");
        BusinessArea second = BusinessArea.create("클라우드");
        given(businessAreaRepository.findAll(any(Sort.class)))
                .willReturn(List.of(first, second));

        // when
        List<CodeResponse> result = codeService.getBusinessAreas();

        // then
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(businessAreaRepository).findAll(sortCaptor.capture());
        assertThat(sortCaptor.getValue()).isEqualTo(Sort.by(Sort.Direction.ASC, "id"));
        assertThat(result).extracting(CodeResponse::name).containsExactly("SI", "클라우드");
    }
}
