package com.bidradar.auth.service;

import com.bidradar.auth.domain.User;
import com.bidradar.auth.dto.request.LoginRequest;
import com.bidradar.auth.dto.request.SignupRequest;
import com.bidradar.auth.infra.JwtProvider;
import com.bidradar.auth.repository.UserRepository;
import com.bidradar.common.exception.ApiException;
import com.bidradar.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(ResultCode.CONFLICT);
        }
        User user = User.create(request.email(), passwordEncoder.encode(request.password()), request.name());
        userRepository.save(user);
    }

    @Transactional
    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(ResultCode.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(ResultCode.UNAUTHORIZED);
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        user.updateRefreshToken(refreshToken, jwtProvider.getRefreshTokenExpiry());
        user.recordLogin();

        return new LoginResult(accessToken, refreshToken, user);
    }

    @Transactional
    public String refresh(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ApiException(ResultCode.UNAUTHORIZED));

        if (user.getRefreshTokenExpiresAt() == null
                || user.getRefreshTokenExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new ApiException(ResultCode.UNAUTHORIZED);
        }

        return jwtProvider.createAccessToken(user.getId(), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND));
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND));
        user.revokeRefreshToken();
    }

    public record LoginResult(String accessToken, String refreshToken, User user) {}
}
