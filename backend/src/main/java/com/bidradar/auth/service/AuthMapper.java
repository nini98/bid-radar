package com.bidradar.auth.service;

import com.bidradar.auth.domain.User;
import com.bidradar.auth.dto.response.AuthUserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    AuthUserResponse toAuthUserResponse(User user);
}
