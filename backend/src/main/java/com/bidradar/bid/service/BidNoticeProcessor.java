package com.bidradar.bid.service;

import com.bidradar.bid.domain.BidAttachment;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.infra.dto.G2bNoticeItem;
import com.bidradar.bid.repository.BidAttachmentRepository;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidNoticeProcessor {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DT_SHORT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final BidNoticeRepository bidNoticeRepository;
    private final BidAttachmentRepository bidAttachmentRepository;
    private final ObjectMapper objectMapper;
    private final G2bNoticeMapper g2bNoticeMapper;

    @Transactional
    public boolean process(G2bNoticeItem item) {
        if (bidNoticeRepository.existsByExternalNoticeId(item.bidNtceNo())) {
            return false;
        }

        BidNotice notice = BidNotice.create(toCommand(item));
        bidNoticeRepository.save(notice);

        List<BidAttachment> attachments = g2bNoticeMapper.toAttachments(item, notice);
        if (!attachments.isEmpty()) {
            bidAttachmentRepository.saveAll(attachments);
        }

        return true;
    }

    private BidNoticeCreateCommand toCommand(G2bNoticeItem item) {
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
                toJson(item)
        );
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
