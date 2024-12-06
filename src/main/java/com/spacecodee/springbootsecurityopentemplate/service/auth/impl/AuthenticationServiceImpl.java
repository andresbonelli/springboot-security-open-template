package com.spacecodee.springbootsecurityopentemplate.service.auth.impl;

import com.spacecodee.springbootsecurityopentemplate.data.common.auth.AuthenticationResponsePojo;
import com.spacecodee.springbootsecurityopentemplate.data.dto.user.details.UserDetailsDTO;
import com.spacecodee.springbootsecurityopentemplate.data.vo.auth.LoginUserVO;
import com.spacecodee.springbootsecurityopentemplate.exceptions.auth.TokenExpiredException;
import com.spacecodee.springbootsecurityopentemplate.exceptions.auth.TokenNotFoundException;
import com.spacecodee.springbootsecurityopentemplate.exceptions.util.ExceptionShortComponent;
import com.spacecodee.springbootsecurityopentemplate.mappers.basic.IJwtTokenMapper;
import com.spacecodee.springbootsecurityopentemplate.service.auth.IAuthenticationService;
import com.spacecodee.springbootsecurityopentemplate.service.core.user.details.IUserDetailsService;
import com.spacecodee.springbootsecurityopentemplate.service.security.IJwtService;
import com.spacecodee.springbootsecurityopentemplate.service.security.IJwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class AuthenticationServiceImpl implements IAuthenticationService {

    private final IUserDetailsService userDetailsService;
    private final IJwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final IJwtTokenService jwtTokenService;
    private final IJwtTokenMapper jwtTokenMapper;
    private final ExceptionShortComponent exceptionShortComponent;

    @Override
    public AuthenticationResponsePojo login(String locale, @NotNull LoginUserVO userVO) {
        // Check for existing token
        var existingToken = this.jwtTokenService.getTokenByUsername(userVO.username());
        if (StringUtils.hasText(existingToken)) {
            try {
                this.jwtService.deleteExpiredToken(existingToken, locale);
                log.info("Token is still valid for user: {}", userVO.username());
                return new AuthenticationResponsePojo(existingToken);
            } catch (TokenExpiredException e) {
                // Token was already deleted in JwtServiceImpl
                log.info("Token expired for user: {}, generating new token", userVO.username());
            }
        }

        // Generate new token
        var authentication = new UsernamePasswordAuthenticationToken(userVO.username(), userVO.password());
        Authentication authResult = this.authenticationManager.authenticate(authentication);
        UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authResult.getPrincipal();

        String jwt = this.jwtService.generateToken(userDetailsDTO, this.generateExtraClaims(userDetailsDTO));

        var expiry = this.jwtService.extractExpiration(jwt);
        this.jwtTokenService.save(this.jwtTokenMapper.toUVO(jwt, expiry, (int) userDetailsDTO.getId()));

        return new AuthenticationResponsePojo(jwt);
    }

    @Override
    public boolean validateToken(String locale, String jwt) {
        try {
            this.jwtService.extractUsername(jwt);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public UserDetailsDTO findLoggedInUser(String locale) {
        var auth = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var username = auth.getPrincipal().toString();
        return this.userDetailsService.findByUsername(locale, username);
    }

    @Override
    public void logout(String locale, HttpServletRequest request) {
        var jwtToken = this.jwtService.extractJwtFromRequest(request);

        if (!StringUtils.hasText(jwtToken)) {
            log.warn("No token found in request");
            return;
        }

        try {
            var existsToken = this.jwtTokenService.existsByJwtTokenToken(locale, jwtToken);

            if (!existsToken) {
                // Check if token has "Bearer " prefix
                if (jwtToken.startsWith("Bearer ")) {
                    var cleanToken = jwtToken.substring(7);
                    existsToken = this.jwtTokenService.existsByJwtTokenToken(locale, cleanToken);
                    if (existsToken) {
                        this.jwtTokenService.deleteByToken(locale, cleanToken);
                        log.info("Token invalidated successfully after removing Bearer prefix");
                    }
                }
            } else {
                this.jwtTokenService.deleteByToken(locale, jwtToken);
                log.info("Token invalidated successfully");
            }
        } catch (TokenNotFoundException e) {
            log.error("Error during logout: {}", e.getMessage());
            throw this.exceptionShortComponent.tokenNotFoundException("token.not.found", locale);
        }
    }

    @Override
    public AuthenticationResponsePojo refreshToken(String locale, HttpServletRequest request) {
        var oldToken = this.jwtService.extractJwtFromRequest(request);
        if (oldToken == null) {
            throw this.exceptionShortComponent.tokenNotFoundException("Token not found in the request", "en");
        }

        // Get username from an old token
        var username = this.jwtService.extractUsername(oldToken);
        var userDetails = this.userDetailsService.findByUsername(locale, username);

        // Check if the token is expired
        if (this.jwtService.isTokenExpired(oldToken)) {
            // Delete old token
            this.jwtTokenService.deleteByToken(locale, oldToken);

            // Generate new token
            var newToken = this.jwtService.refreshToken(oldToken, userDetails);

            // Save a new token
            var expiry = this.jwtService.extractExpiration(newToken);
            this.jwtTokenService.save(this.jwtTokenMapper.toUVO(newToken, expiry, (int) userDetails.getId()));

            return new AuthenticationResponsePojo(newToken);
        }

        return new AuthenticationResponsePojo(oldToken);
    }

    private @NotNull @UnmodifiableView Map<String, Object> generateExtraClaims(@NotNull UserDetailsDTO userDetailsDTO) {
        return Map.of(
                "name", userDetailsDTO.getName(),
                "role", userDetailsDTO.getUserDetailsRoleDTO().getName(),
                "authorities", userDetailsDTO.getAuthorities());
    }
}