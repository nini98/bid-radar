package com.bidradar.bid.service;

import com.bidradar.bid.domain.BidAttachment;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.infra.dto.G2bNoticeItem;
import com.bidradar.bid.repository.BidAttachmentRepository;
import com.bidradar.bid.repository.BidNoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidNoticeProcessor {

    private final BidNoticeRepository bidNoticeRepository;
    private final BidAttachmentRepository bidAttachmentRepository;
    private final G2bNoticeMapper g2bNoticeMapper;

    @Transactional
    public boolean process(G2bNoticeItem item) {
        if (bidNoticeRepository.existsByExternalNoticeId(item.bidNtceNo())) {
            return false;
        }

        BidNotice notice = BidNotice.create(g2bNoticeMapper.toCommand(item));
        bidNoticeRepository.save(notice);

        List<BidAttachment> attachments = g2bNoticeMapper.toAttachments(item, notice);
        if (!attachments.isEmpty()) {
            bidAttachmentRepository.saveAll(attachments);
        }

        return true;
    }
}
