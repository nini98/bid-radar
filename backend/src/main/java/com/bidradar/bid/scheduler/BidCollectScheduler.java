package com.bidradar.bid.scheduler;

import com.bidradar.bid.service.BidCollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class BidCollectScheduler {

    private final BidCollectorService bidCollectorService;

    @Scheduled(cron = "0 0 7 * * *")
    public void collect() {
        LocalDate today = LocalDate.now();
        log.info("G2B 공고 수집 시작: date={}", today);
        try {
            bidCollectorService.collect(today);
        } catch (Exception e) {
            log.error("G2B 공고 수집 실패: date={}", today, e);
        }
    }
}
