package com.bidradar.auth.controller;

import com.bidradar.auth.dto.request.LoginRequest;
import com.bidradar.auth.dto.request.SignupRequest;
import com.bidradar.auth.dto.response.AuthUserResponse;
import com.bidradar.auth.infra.CookieProvider;
import com.bidradar.auth.service.AuthMapper;
import com.bidradar.auth.service.AuthService;
import com.bidradar.auth.service.AuthService.LoginResult;
import com.bidradar.common.exception.ApiException;
import com.bidradar.common.response.Response;
import com.bidradar.common.response.ResultCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthMapper authMapper;
    private final CookieProvider cookieProvider;

    @PostMapping("/signup")
    public Response<Void> signup(@RequestBody @Valid SignupRequest request) {
        authService.signup(request);
        return Response.success();
    }

    @PostMapping("/login")
    public Response<AuthUserResponse> login(@RequestBody @Valid LoginRequest request,
                                            HttpServletResponse response) {
        LoginResult result = authService.login(request);
        cookieProvider.addAccessTokenCookie(response, result.accessToken());
        cookieProvider.addRefreshTokenCookie(response, result.refreshToken());
        return Response.success(authMapper.toAuthUserResponse(result.user()));
    }

    @PostMapping("/refresh")
    public Response<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "REFRESH_TOKEN");
        String newAccessToken = authService.refresh(refreshToken);
        cookieProvider.addAccessTokenCookie(response, newAccessToken);
        return Response.success();
    }

    @GetMapping("/me")
    public Response<AuthUserResponse> me(@AuthenticationPrincipal Long userId) {
        return Response.success(authMapper.toAuthUserResponse(
                authService.getUser(userId)
        ));
    }

    @PostMapping("/logout")
    public Response<Void> logout(@AuthenticationPrincipal Long userId,
                                  HttpServletResponse response) {
        authService.logout(userId);
        cookieProvider.clearAuthCookies(response);
        return Response.success();
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) throw new ApiException(ResultCode.UNAUTHORIZED);
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new ApiException(ResultCode.UNAUTHORIZED));
    }
}
