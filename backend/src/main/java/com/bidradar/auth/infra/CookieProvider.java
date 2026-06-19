package com.bidradar.auth.infra;

import com.bidradar.config.CookieProperties;
import com.bidradar.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieProvider {

    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String REFRESH_TOKEN = "REFRESH_TOKEN";

    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;

    public void addAccessTokenCookie(HttpServletResponse response, String token) {
        response.addCookie(httpOnlyCookie(ACCESS_TOKEN, token,
                (int) (jwtProperties.getAccessTokenExpirationMinutes() * 60)));
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        response.addCookie(httpOnlyCookie(REFRESH_TOKEN, token,
                (int) (jwtProperties.getRefreshTokenExpirationDays() * 24 * 60 * 60)));
    }

    public void clearAuthCookies(HttpServletResponse response) {
        response.addCookie(httpOnlyCookie(ACCESS_TOKEN, "", 0));
        response.addCookie(httpOnlyCookie(REFRESH_TOKEN, "", 0));
    }

    private Cookie httpOnlyCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieProperties.isSecure());
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}
