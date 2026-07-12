package com.bidradar.match.domain;

public enum MatchGrade {
    STRONG_REVIEW("적극 검토"),
    RECOMMENDED("추천"),
    NEED_REVIEW("검토 필요");

    private final String displayText;

    MatchGrade(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }
}
