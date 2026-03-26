package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.dto.request.ChangePasswordRequest;
import com.smartlogi.smart_city_hub.dto.request.ForgotPasswordRequest;
import com.smartlogi.smart_city_hub.dto.request.LoginRequest;
import com.smartlogi.smart_city_hub.dto.request.RefreshTokenRequest;
import com.smartlogi.smart_city_hub.dto.request.RegisterRequest;
import com.smartlogi.smart_city_hub.dto.response.AuthResponse;
import com.smartlogi.smart_city_hub.dto.response.RegistrationResponse;
import com.smartlogi.smart_city_hub.dto.response.UserResponse;
import com.smartlogi.smart_city_hub.entity.RefreshToken;
import com.smartlogi.smart_city_hub.entity.User;
import com.smartlogi.smart_city_hub.entity.enums.Role;
import com.smartlogi.smart_city_hub.entity.enums.UserStatus;
import com.smartlogi.smart_city_hub.exception.BadRequestException;
import com.smartlogi.smart_city_hub.exception.ResourceNotFoundException;
import com.smartlogi.smart_city_hub.exception.UnauthorizedException;
import com.smartlogi.smart_city_hub.mapper.UserMapper;
import com.smartlogi.smart_city_hub.repository.RefreshTokenRepository;
import com.smartlogi.smart_city_hub.repository.UserRepository;
import com.smartlogi.smart_city_hub.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final EmailService emailService;
    private final PasswordGeneratorService passwordGeneratorService;

    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        if (userRepository.existsByNationalId(request.getNationalId())) {
            throw new BadRequestException("National ID already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(null)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .nationalId(request.getNationalId())
                .role(Role.ROLE_USER)
                .status(UserStatus.PENDING)
                .mustChangePassword(false)
                .build();

        user = userRepository.save(user);
        log.info("New user registered (pending approval): {}", user.getEmail());

        emailService.sendAdminNotification(user);

        return RegistrationResponse.pending(user.getEmail());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (user.getStatus() == UserStatus.PENDING) {
            throw new UnauthorizedException("Your account is pending approval. Please wait for admin confirmation.");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new UnauthorizedException("Your account has been deactivated. Please contact support.");
        }

        if (user.getPassword() == null) {
            throw new UnauthorizedException("Account not yet activated. Please wait for admin approval.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        log.info("User logged in: {} (mustChangePassword: {})", user.getEmail(), user.getMustChangePassword());

        return generateAuthResponse(user);
    }

    @Transactional
    public UserResponse approveUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new BadRequestException("User is not in pending status");
        }

        User admin = getCurrentUser();

        String temporaryPassword = passwordGeneratorService.generateSecurePassword();

        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setStatus(UserStatus.ACTIVE);
        user.setMustChangePassword(true);
        user.setApprovedAt(LocalDateTime.now());
        user.setApprovedBy(admin);

        user = userRepository.save(user);
        log.info("User approved by admin {}: {}", admin.getEmail(), user.getEmail());

        emailService.sendActivationEmail(user, temporaryPassword);

        return userMapper.toResponse(user);
    }

    @Transactional
    public void rejectUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new BadRequestException("User is not in pending status");
        }

        User admin = getCurrentUser();

        userRepository.delete(user);
        log.info("User registration rejected by admin {}: {}", admin.getEmail(), user.getEmail());
    }

    @Transactional
    public AuthResponse changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);

        user = userRepository.save(user);
        log.info("User changed password: {}", user.getEmail());

        emailService.sendPasswordChangeConfirmation(user);

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

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        log.info("Token refreshed for user: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            return;
        }

        String newPassword = passwordGeneratorService.generateSecurePassword();
        String encodedPassword = passwordEncoder.encode(newPassword);

        emailService.sendForgotPasswordEmail(user, newPassword);

        user.setPassword(encodedPassword);
        user.setMustChangePassword(true);
        userRepository.save(user);
        log.info("Password reset for user: {}", user.getEmail());
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
                userResponse);
    }

    private String createRefreshToken(User user) {
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

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
    }
}
