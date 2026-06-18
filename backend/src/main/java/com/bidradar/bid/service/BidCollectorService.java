package com.bidradar.bid.service;

import com.bidradar.bid.infra.G2bApiClient;
import com.bidradar.bid.infra.dto.G2bNoticeItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidCollectorService {

    private final G2bApiClient g2bApiClient;
    private final BidNoticeProcessor processor;

    public void collect(LocalDate date) {
        int saved = 0;
        int total = 0;

        List<G2bNoticeItem> items = new ArrayList<>();
        items.addAll(g2bApiClient.fetchConstructionNotices(date));
        items.addAll(g2bApiClient.fetchServiceNotices(date));
        items.addAll(g2bApiClient.fetchGoodsNotices(date));

        for (G2bNoticeItem item : items) {
            total++;
            try {
                if (processor.process(item)) saved++;
            } catch (Exception e) {
                log.error("공고 저장 실패: bidNtceNo={}", item.bidNtceNo(), e);
            }
        }

        log.info("G2B 공고 수집 완료: date={}, 신규저장={}, 중복스킵={}", date, saved, total - saved);
    }
}
