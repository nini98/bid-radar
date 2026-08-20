package com.bidradar.bid.service;

import com.bidradar.bid.domain.BidAttachment;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.infra.dto.G2bNoticeItem;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class G2bNoticeMapper {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DT_SHORT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ObjectMapper objectMapper;

    public BidNoticeCreateCommand toCommand(G2bNoticeItem item) {
        return new BidNoticeCreateCommand(
                item.bidNtceNo(),
                "G2B",
                truncate(item.bidNtceNm(), 500),
                truncate(item.ntceInsttNm(), 200),
                parseAmount(item.bdgtAmt()),
                truncate(item.cnstrtsiteRgnNm(), 100),
                truncate(item.bidMethdNm(), 50),
                truncate(item.cntrctCnclsMthdNm(), 50),
                truncate(item.indstrytyLmtYn(), 100),
                item.bidNtceDtlUrl(),
                parseDateTime(item.bidNtceDt()),
                parseDateTime(item.bidQlfctRgstDt()),
                parseDateTime(item.bidClseDt()),
                parseDateTime(item.opengDt()),
                toJson(item),
                resolveQualificationSummary(item)
        );
    }

    /**
     * 건설(mainCnsttyNm)/용역(pubPrcrmntClsfcNm)/물품(dtilPrdctClsfcNoNm) 목록 API는
     * 서로 다른 분류 필드를 응답에 담아 보내고, 자기 유형이 아닌 필드는 응답 자체에 없어
     * 자동으로 null이 된다. 어떤 오퍼레이션에서 온 아이템인지 별도로 추적하지 않고도
     * 이 중 채워진 값 하나를 그대로 채택한다.
     */
    private String resolveQualificationSummary(G2bNoticeItem item) {
        String[] candidates = {item.mainCnsttyNm(), item.pubPrcrmntClsfcNm(), item.dtilPrdctClsfcNoNm()};
        String resolved = null;
        int nonNullCount = 0;
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                nonNullCount++;
                if (resolved == null) resolved = candidate;
            }
        }
        if (nonNullCount > 1) {
            log.warn("공고 분류 필드가 2개 이상 동시에 존재: bidNtceNo={}, mainCnsttyNm={}, pubPrcrmntClsfcNm={}, dtilPrdctClsfcNoNm={}",
                    item.bidNtceNo(), item.mainCnsttyNm(), item.pubPrcrmntClsfcNm(), item.dtilPrdctClsfcNoNm());
        }
        return resolved;
    }

    public List<BidAttachment> toAttachments(G2bNoticeItem item, BidNotice notice) {
        String[] urls = {
                item.ntceSpecDocUrl1(), item.ntceSpecDocUrl2(), item.ntceSpecDocUrl3(),
                item.ntceSpecDocUrl4(), item.ntceSpecDocUrl5(), item.ntceSpecDocUrl6(),
                item.ntceSpecDocUrl7(), item.ntceSpecDocUrl8(), item.ntceSpecDocUrl9(),
                item.ntceSpecDocUrl10()
        };
        String[] names = {
                item.ntceSpecFileNm1(), item.ntceSpecFileNm2(), item.ntceSpecFileNm3(),
                item.ntceSpecFileNm4(), item.ntceSpecFileNm5(), item.ntceSpecFileNm6(),
                item.ntceSpecFileNm7(), item.ntceSpecFileNm8(), item.ntceSpecFileNm9(),
                item.ntceSpecFileNm10()
        };

        List<BidAttachment> result = new ArrayList<>();
        for (int i = 0; i < urls.length; i++) {
            if (urls[i] != null && !urls[i].isBlank() && names[i] != null && !names[i].isBlank()) {
                result.add(BidAttachment.create(notice, names[i], urls[i]));
            }
        }
        return result;
    }

    private static String truncate(String s, int maxLength) {
        if (s == null) return null;
        return s.length() <= maxLength ? s : s.substring(0, maxLength);
    }

    private static Long parseAmount(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDateTime.parse(s.trim(), DT_FORMAT);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(s.trim(), DT_SHORT_FORMAT);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    private String toJson(G2bNoticeItem item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
