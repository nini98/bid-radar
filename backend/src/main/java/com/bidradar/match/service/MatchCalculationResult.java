package com.bidradar.match.service;

import com.bidradar.match.domain.MatchGrade;

import java.math.BigDecimal;

public record MatchCalculationResult(
        BigDecimal totalScore,
        MatchGrade grade,
        BigDecimal scoreTech,
        BigDecimal scoreRegion,
        BigDecimal scoreBudget,
        BigDecimal scoreBusiness,
        String matchedKeywords,
        String scoreReason
) {}
