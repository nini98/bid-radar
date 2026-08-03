package com.bidradar.match.dto.response;

import com.bidradar.match.domain.MatchCalculationStatusType;

import java.time.LocalDateTime;

public record MatchCalculationStatusResponse(MatchCalculationStatusType status, LocalDateTime updatedAt) {
}
