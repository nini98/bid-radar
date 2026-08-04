package com.bidradar.match.dto.response;

import com.bidradar.match.domain.MatchCalculationStatusType;

import java.time.Instant;

public record MatchCalculationStatusResponse(MatchCalculationStatusType status, Instant updatedAt) {
}
