package com.bidradar.bid.infra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record G2bNoticeItem(
        String bidNtceNo,
        String bidNtceOrd,
        String bidNtceNm,
        String ntceInsttNm,
        String bidMethdNm,
        String cntrctCnclsMthdNm,
        String bdgtAmt,
        String cnstrtsiteRgnNm,
        String bidNtceDt,
        String bidQlfctRgstDt,
        String bidClseDt,
        String opengDt,
        String bidNtceDtlUrl,
        String indstrytyLmtYn,
        String ntceKindNm,
        String rgstDt,
        String ntceSpecDocUrl1,
        String ntceSpecDocUrl2,
        String ntceSpecDocUrl3,
        String ntceSpecDocUrl4,
        String ntceSpecDocUrl5,
        String ntceSpecDocUrl6,
        String ntceSpecDocUrl7,
        String ntceSpecDocUrl8,
        String ntceSpecDocUrl9,
        String ntceSpecDocUrl10,
        String ntceSpecFileNm1,
        String ntceSpecFileNm2,
        String ntceSpecFileNm3,
        String ntceSpecFileNm4,
        String ntceSpecFileNm5,
        String ntceSpecFileNm6,
        String ntceSpecFileNm7,
        String ntceSpecFileNm8,
        String ntceSpecFileNm9,
        String ntceSpecFileNm10
) {
    public List<AttachmentEntry> attachments() {
        List<AttachmentEntry> result = new ArrayList<>();
        String[] urls = {ntceSpecDocUrl1, ntceSpecDocUrl2, ntceSpecDocUrl3, ntceSpecDocUrl4, ntceSpecDocUrl5,
                ntceSpecDocUrl6, ntceSpecDocUrl7, ntceSpecDocUrl8, ntceSpecDocUrl9, ntceSpecDocUrl10};
        String[] names = {ntceSpecFileNm1, ntceSpecFileNm2, ntceSpecFileNm3, ntceSpecFileNm4, ntceSpecFileNm5,
                ntceSpecFileNm6, ntceSpecFileNm7, ntceSpecFileNm8, ntceSpecFileNm9, ntceSpecFileNm10};
        for (int i = 0; i < urls.length; i++) {
            if (urls[i] != null && !urls[i].isBlank() && names[i] != null && !names[i].isBlank()) {
                result.add(new AttachmentEntry(names[i], urls[i]));
            }
        }
        return result;
    }

    public record AttachmentEntry(String fileName, String downloadUrl) {}
}
