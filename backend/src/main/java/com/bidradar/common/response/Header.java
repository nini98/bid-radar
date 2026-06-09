package com.bidradar.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Header {
    private final String resultCode;
    private final String resultMessage;

    public static Header success() {
        return new Header(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    public static Header of(ResultCode resultCode) {
        return new Header(resultCode.getCode(), resultCode.getMessage());
    }
}
