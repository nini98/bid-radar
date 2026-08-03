package com.bidradar.match.event;

public record MatchRecalculationRequestedEvent(Long companyId, String lockToken) {
}
