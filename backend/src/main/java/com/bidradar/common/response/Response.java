package com.bidradar.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Response<T> {
    private final Header header;
    private final T data;

    public static <T> Response<T> success(T data) {
        return new Response<>(Header.success(), data);
    }

    public static Response<Void> success() {
        return new Response<>(Header.success(), null);
    }

    public static <T> Response<T> fail(ResultCode resultCode, T data) {
        return new Response<>(Header.of(resultCode), data);
    }

    public static Response<Void> fail(ResultCode resultCode) {
        return new Response<>(Header.of(resultCode), null);
    }
}
