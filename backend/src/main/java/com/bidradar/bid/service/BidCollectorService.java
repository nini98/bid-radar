package com.bidradar.bid.service;

import com.bidradar.bid.infra.G2bApiClient;
import com.bidradar.bid.infra.dto.G2bNoticeItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidCollectorService {

    private final G2bApiClient g2bApiClient;
    private final BidNoticeProcessor processor;

    public void collect(LocalDate date) {
        int[] result = {0, 0}; // [saved, total]

        accumulate(g2bApiClient.fetchConstructionNotices(date), result);
        accumulate(g2bApiClient.fetchServiceNotices(date), result);
        accumulate(g2bApiClient.fetchGoodsNotices(date), result);

        log.info("G2B 공고 수집 완료: date={}, 신규저장={}, 중복스킵={}", date, result[0], result[1] - result[0]);
    }

    private void accumulate(List<G2bNoticeItem> items, int[] result) {
        for (G2bNoticeItem item : items) {
            result[1]++;
            try {
                if (processor.process(item)) result[0]++;
            } catch (Exception e) {
                log.error("공고 저장 실패: bidNtceNo={}", item.bidNtceNo(), e);
            }
        }
    }
}
