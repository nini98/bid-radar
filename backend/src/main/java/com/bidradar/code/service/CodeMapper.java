package com.bidradar.code.service;

import com.bidradar.code.domain.BusinessArea;
import com.bidradar.code.domain.TechTag;
import com.bidradar.code.dto.response.CodeResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CodeMapper {

    CodeResponse toResponse(TechTag techTag);

    CodeResponse toResponse(BusinessArea businessArea);
}
