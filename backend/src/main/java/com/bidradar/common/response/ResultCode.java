package com.bidradar.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResultCode {
    SUCCESS("200", "성공"),
    VALIDATION_ERROR("400", "입력한 값이 유효하지 않습니다."),
    UNAUTHORIZED("401", "인증이 필요합니다."),
    FORBIDDEN("403", "권한이 없습니다."),
    NOT_FOUND("404", "리소스를 찾을 수 없습니다."),
    CONFLICT("409", "이미 존재합니다."),
    INTERNAL_ERROR("500", "서버 오류가 발생했습니다.");

    private final String code;
    private final String message;
}
