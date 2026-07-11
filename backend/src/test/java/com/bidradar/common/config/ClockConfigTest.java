package com.bidradar.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class ClockConfigTest {

    private final ClockConfig config = new ClockConfig();

    @Test
    @DisplayName("clock 빈은 실행 환경(JVM 기본 타임존)과 무관하게 항상 Asia/Seoul을 기준으로 한다")
    void clock_빈은_항상_KST() {
        // when
        Clock clock = config.clock();

        // then
        assertThat(clock.getZone()).isEqualTo(ZoneId.of("Asia/Seoul"));
    }
}
