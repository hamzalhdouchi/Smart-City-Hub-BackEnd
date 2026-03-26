package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.dto.request.ChangePasswordRequest;
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
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserMapper userMapper;
    @Mock private EmailService emailService;
    @Mock private PasswordGeneratorService passwordGeneratorService;

    @InjectMocks
    private AuthService authService;

    private User activeUser;
    private User pendingUser;
    private User adminUser;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id("user-1")
                .email("user@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .nationalId("ABC12345")
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .mustChangePassword(false)
                .build();

        pendingUser = User.builder()
                .id("pending-1")
                .email("pending@example.com")
                .firstName("Jane")
                .lastName("Doe")
                .nationalId("XYZ67890")
                .role(Role.ROLE_USER)
                .status(UserStatus.PENDING)
                .mustChangePassword(false)
                .build();

        adminUser = User.builder()
                .id("admin-1")
                .email("admin@example.com")
                .password("encodedAdminPwd")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .mustChangePassword(false)
                .build();

        userResponse = new UserResponse();
        userResponse.setId("user-1");
        userResponse.setEmail("user@example.com");
    }

    private void mockSecurityContext(User user) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(user.getEmail());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    

    @Nested
    @DisplayName("register")
    class RegisterTests {

        @Test
        void should_RegisterUser_When_ValidRequest() {
            RegisterRequest request = RegisterRequest.builder()
                    .email("new@example.com")
                    .firstName("New")
                    .lastName("User")
                    .phone("0612345678")
                    .nationalId("NEW12345")
                    .build();

            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(userRepository.existsByNationalId("NEW12345")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(pendingUser);

            RegistrationResponse response = authService.register(request);

            assertNotNull(response);
            verify(userRepository).save(any(User.class));
            verify(emailService).sendAdminNotification(any(User.class));
        }

        @Test
        void should_ThrowBadRequest_When_EmailAlreadyExists() {
            RegisterRequest request = RegisterRequest.builder()
                    .email("user@example.com")
                    .nationalId("NEW12345")
                    .build();

            when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> authService.register(request));
            assertEquals("Email already registered", ex.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        void should_ThrowBadRequest_When_NationalIdAlreadyExists() {
            RegisterRequest request = RegisterRequest.builder()
                    .email("new@example.com")
                    .nationalId("ABC12345")
                    .build();

            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(userRepository.existsByNationalId("ABC12345")).thenReturn(true);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> authService.register(request));
            assertEquals("National ID already registered", ex.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        void should_SaveUserWithPendingStatus_When_Registered() {
            RegisterRequest request = RegisterRequest.builder()
                    .email("new@example.com")
                    .firstName("New")
                    .lastName("User")
                    .phone("0612345678")
                    .nationalId("NEW12345")
                    .build();

            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(userRepository.existsByNationalId("NEW12345")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User saved = inv.getArgument(0);
                assertEquals(UserStatus.PENDING, saved.getStatus());
                assertNull(saved.getPassword());
                assertEquals(Role.ROLE_USER, saved.getRole());
                return pendingUser;
            });

            authService.register(request);
        }
    }

    

    @Nested
    @DisplayName("login")
    class LoginTests {

        @Test
        void should_Login_When_CredentialsValid() {
            LoginRequest request = LoginRequest.builder()
                    .email("user@example.com")
                    .password("password123")
                    .build();

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));
            when(jwtService.generateAccessToken(activeUser)).thenReturn("access-token");
            when(jwtService.getAccessTokenValidity()).thenReturn(86400000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.toResponse(activeUser)).thenReturn(userResponse);

            AuthResponse response = authService.login(request);

            assertNotNull(response);
            assertEquals("access-token", response.getAccessToken());
            assertEquals("Bearer", response.getTokenType());
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        void should_ThrowUnauthorized_When_UserNotFound() {
            LoginRequest request = LoginRequest.builder()
                    .email("notfound@example.com")
                    .password("password")
                    .build();

            when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

            assertThrows(UnauthorizedException.class, () -> authService.login(request));
        }

        @Test
        void should_ThrowUnauthorized_When_UserIsPending() {
            LoginRequest request = LoginRequest.builder()
                    .email("pending@example.com")
                    .password("password")
                    .build();

            when(userRepository.findByEmail("pending@example.com")).thenReturn(Optional.of(pendingUser));

            UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                    () -> authService.login(request));
            assertTrue(ex.getMessage().contains("pending approval"));
        }

        @Test
        void should_ThrowUnauthorized_When_UserIsInactive() {
            User inactiveUser = User.builder()
                    .id("u-2")
                    .email("inactive@example.com")
                    .password("encodedPwd")
                    .role(Role.ROLE_USER)
                    .status(UserStatus.INACTIVE)
                    .mustChangePassword(false)
                    .build();

            LoginRequest request = LoginRequest.builder()
                    .email("inactive@example.com")
                    .password("password")
                    .build();

            when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(inactiveUser));

            UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                    () -> authService.login(request));
            assertTrue(ex.getMessage().contains("deactivated"));
        }

        @Test
        void should_ThrowUnauthorized_When_PasswordIsNull() {
            User noPasswordUser = User.builder()
                    .id("u-3")
                    .email("nopwd@example.com")
                    .password(null)
                    .role(Role.ROLE_USER)
                    .status(UserStatus.ACTIVE)
                    .mustChangePassword(false)
                    .build();

            LoginRequest request = LoginRequest.builder()
                    .email("nopwd@example.com")
                    .password("any")
                    .build();

            when(userRepository.findByEmail("nopwd@example.com")).thenReturn(Optional.of(noPasswordUser));

            UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                    () -> authService.login(request));
            assertTrue(ex.getMessage().contains("not yet activated"));
        }
    }

    

    @Nested
    @DisplayName("approveUser")
    class ApproveUserTests {

        @Test
        void should_ApproveUser_When_UserIsPending() {
            mockSecurityContext(adminUser);
            when(userRepository.findById("pending-1")).thenReturn(Optional.of(pendingUser));
            when(passwordGeneratorService.generateSecurePassword()).thenReturn("TempPwd123!");
            when(passwordEncoder.encode("TempPwd123!")).thenReturn("encodedTemp");
            when(userRepository.save(any(User.class))).thenReturn(pendingUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

            UserResponse response = authService.approveUser("pending-1");

            assertNotNull(response);
            verify(userRepository).save(pendingUser);
            verify(emailService).sendActivationEmail(eq(pendingUser), eq("TempPwd123!"));
            assertEquals(UserStatus.ACTIVE, pendingUser.getStatus());
            assertTrue(pendingUser.getMustChangePassword());
            assertNotNull(pendingUser.getApprovedAt());
            assertEquals(adminUser, pendingUser.getApprovedBy());
        }

        @Test
        void should_ThrowNotFound_When_UserDoesNotExist() {
            mockSecurityContext(adminUser);
            when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> authService.approveUser("nonexistent"));
        }

        @Test
        void should_ThrowBadRequest_When_UserIsNotPending() {
            mockSecurityContext(adminUser);
            when(userRepository.findById("user-1")).thenReturn(Optional.of(activeUser));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> authService.approveUser("user-1"));
            assertEquals("User is not in pending status", ex.getMessage());
        }
    }

    

    @Nested
    @DisplayName("rejectUser")
    class RejectUserTests {

        @Test
        void should_DeleteUser_When_UserIsPending() {
            mockSecurityContext(adminUser);
            when(userRepository.findById("pending-1")).thenReturn(Optional.of(pendingUser));

            assertDoesNotThrow(() -> authService.rejectUser("pending-1"));
            verify(userRepository).delete(pendingUser);
        }

        @Test
        void should_ThrowNotFound_When_UserDoesNotExist() {
            mockSecurityContext(adminUser);
            when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> authService.rejectUser("nonexistent"));
        }

        @Test
        void should_ThrowBadRequest_When_UserIsNotPending() {
            mockSecurityContext(adminUser);
            when(userRepository.findById("user-1")).thenReturn(Optional.of(activeUser));

            assertThrows(BadRequestException.class, () -> authService.rejectUser("user-1"));
            verify(userRepository, never()).delete(any());
        }
    }

    

    @Nested
    @DisplayName("changePassword")
    class ChangePasswordTests {

        @Test
        void should_ChangePassword_When_ValidRequest() {
            mockSecurityContext(activeUser);
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("oldPassword")
                    .newPassword("newPassword123!")
                    .confirmPassword("newPassword123!")
                    .build();

            when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
            when(passwordEncoder.matches("newPassword123!", "encodedPassword")).thenReturn(false);
            when(passwordEncoder.encode("newPassword123!")).thenReturn("newEncoded");
            when(userRepository.save(any(User.class))).thenReturn(activeUser);
            when(jwtService.generateAccessToken(activeUser)).thenReturn("new-access-token");
            when(jwtService.getAccessTokenValidity()).thenReturn(86400000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.toResponse(activeUser)).thenReturn(userResponse);

            AuthResponse response = authService.changePassword(request);

            assertNotNull(response);
            assertFalse(activeUser.getMustChangePassword());
            verify(emailService).sendPasswordChangeConfirmation(activeUser);
        }

        @Test
        void should_ThrowBadRequest_When_CurrentPasswordIncorrect() {
            mockSecurityContext(activeUser);
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("wrongPassword")
                    .newPassword("newPassword123!")
                    .confirmPassword("newPassword123!")
                    .build();

            when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> authService.changePassword(request));
            assertEquals("Current password is incorrect", ex.getMessage());
        }

        @Test
        void should_ThrowBadRequest_When_PasswordConfirmDoesNotMatch() {
            mockSecurityContext(activeUser);
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("oldPassword")
                    .newPassword("newPassword123!")
                    .confirmPassword("differentPassword!")
                    .build();

            when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> authService.changePassword(request));
            assertEquals("New password and confirmation do not match", ex.getMessage());
        }

        @Test
        void should_ThrowBadRequest_When_NewPasswordSameAsCurrent() {
            mockSecurityContext(activeUser);
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("samePassword")
                    .newPassword("samePassword")
                    .confirmPassword("samePassword")
                    .build();

            when(passwordEncoder.matches("samePassword", "encodedPassword")).thenReturn(true);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> authService.changePassword(request));
            assertEquals("New password must be different from current password", ex.getMessage());
        }
    }

    

    @Nested
    @DisplayName("refreshToken")
    class RefreshTokenTests {

        @Test
        void should_ReturnNewTokens_When_ValidRefreshToken() {
            RefreshToken refreshToken = RefreshToken.builder()
                    .id("rt-1")
                    .token("valid-refresh-token")
                    .user(activeUser)
                    .expiryDate(LocalDateTime.now().plusDays(1))
                    .revoked(false)
                    .build();

            RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

            when(refreshTokenRepository.findByTokenAndRevokedFalse("valid-refresh-token"))
                    .thenReturn(Optional.of(refreshToken));
            when(jwtService.generateAccessToken(activeUser)).thenReturn("new-access-token");
            when(jwtService.getAccessTokenValidity()).thenReturn(86400000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.toResponse(activeUser)).thenReturn(userResponse);

            AuthResponse response = authService.refreshToken(request);

            assertNotNull(response);
            assertEquals("new-access-token", response.getAccessToken());
            assertTrue(refreshToken.getRevoked());
        }

        @Test
        void should_ThrowUnauthorized_When_TokenNotFound() {
            RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");
            when(refreshTokenRepository.findByTokenAndRevokedFalse("invalid-token"))
                    .thenReturn(Optional.empty());

            assertThrows(UnauthorizedException.class, () -> authService.refreshToken(request));
        }

        @Test
        void should_ThrowUnauthorized_When_TokenIsExpired() {
            RefreshToken expiredToken = RefreshToken.builder()
                    .id("rt-2")
                    .token("expired-token")
                    .user(activeUser)
                    .expiryDate(LocalDateTime.now().minusDays(1))
                    .revoked(false)
                    .build();

            RefreshTokenRequest request = new RefreshTokenRequest("expired-token");
            when(refreshTokenRepository.findByTokenAndRevokedFalse("expired-token"))
                    .thenReturn(Optional.of(expiredToken));

            assertThrows(UnauthorizedException.class, () -> authService.refreshToken(request));
        }
    }

    

    @Nested
    @DisplayName("logout")
    class LogoutTests {

        @Test
        void should_RevokeToken_When_TokenExists() {
            RefreshToken refreshToken = RefreshToken.builder()
                    .id("rt-1")
                    .token("some-token")
                    .user(activeUser)
                    .expiryDate(LocalDateTime.now().plusDays(1))
                    .revoked(false)
                    .build();

            when(refreshTokenRepository.findByToken("some-token"))
                    .thenReturn(Optional.of(refreshToken));

            authService.logout("some-token");

            assertTrue(refreshToken.getRevoked());
            verify(refreshTokenRepository).save(refreshToken);
        }

        @Test
        void should_DoNothing_When_TokenNotFound() {
            when(refreshTokenRepository.findByToken("nonexistent-token"))
                    .thenReturn(Optional.empty());

            assertDoesNotThrow(() -> authService.logout("nonexistent-token"));
            verify(refreshTokenRepository, never()).save(any());
        }
    }
}
