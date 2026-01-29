package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.dto.request.LoginRequest;
import com.smartlogi.smart_city_hub.dto.request.RefreshTokenRequest;
import com.smartlogi.smart_city_hub.dto.request.RegisterRequest;
import com.smartlogi.smart_city_hub.dto.response.AuthResponse;
import com.smartlogi.smart_city_hub.dto.response.UserResponse;
import com.smartlogi.smart_city_hub.entity.RefreshToken;
import com.smartlogi.smart_city_hub.entity.User;
import com.smartlogi.smart_city_hub.entity.enums.Role;
import com.smartlogi.smart_city_hub.exception.BadRequestException;
import com.smartlogi.smart_city_hub.exception.UnauthorizedException;
import com.smartlogi.smart_city_hub.mapper.UserMapper;
import com.smartlogi.smart_city_hub.repository.RefreshTokenRepository;
import com.smartlogi.smart_city_hub.repository.UserRepository;
import com.smartlogi.smart_city_hub.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        
        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(Role.ROLE_USER) // Citizens register with USER role
                .active(true)
                .build();
        
        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());
        
        return generateAuthResponse(user);
    }
    
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        
        if (!user.getActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }
        
        log.info("User logged in: {}", user.getEmail());
        
        return generateAuthResponse(user);
    }
    
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        
        if (!refreshToken.isValid()) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }
        
        User user = refreshToken.getUser();
        
        // Revoke old refresh token
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        
        log.info("Token refreshed for user: {}", user.getEmail());
        
        return generateAuthResponse(user);
    }
    
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                    log.info("User logged out: {}", token.getUser().getEmail());
                });
    }
    
    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);
        UserResponse userResponse = userMapper.toResponse(user);
        
        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtService.getAccessTokenValidity(),
                userResponse
        );
    }
    
    private String createRefreshToken(User user) {
        // Revoke all existing refresh tokens for this user
        refreshTokenRepository.revokeAllByUserId(user.getId());
        
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenValidity() / 1000))
                .revoked(false)
                .build();
        
        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }
}
