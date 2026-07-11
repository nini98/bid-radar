package com.bidradar.match.service.rule;

import java.util.List;

public record ScoreResult(
        String ruleName,
        int score,
        int maxScore,
        String reason,
        List<String> matched
) {}
