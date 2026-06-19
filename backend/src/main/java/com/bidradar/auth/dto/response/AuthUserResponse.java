package com.bidradar.auth.dto.response;

import com.bidradar.auth.domain.UserRole;

public record AuthUserResponse(
        Long id,
        String email,
        String name,
        UserRole role
) {}
