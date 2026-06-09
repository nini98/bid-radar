package com.bidradar.common.exception;

import com.bidradar.common.response.ResultCode;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final ResultCode resultCode;

    public ApiException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }
}
