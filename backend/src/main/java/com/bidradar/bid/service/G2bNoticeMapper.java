package com.bidradar.bid.service;

import com.bidradar.bid.domain.BidAttachment;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.infra.dto.G2bNoticeItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class G2bNoticeMapper {

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
}
