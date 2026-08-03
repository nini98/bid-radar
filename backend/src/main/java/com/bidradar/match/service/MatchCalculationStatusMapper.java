package com.bidradar.match.service;

import com.bidradar.match.domain.MatchCalculationStatus;
import com.bidradar.match.dto.response.MatchCalculationStatusResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MatchCalculationStatusMapper {

    MatchCalculationStatusResponse toResponse(MatchCalculationStatus status);
}
