package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.dto.response.UserResponse;
import com.smartlogi.smart_city_hub.entity.User;
import com.smartlogi.smart_city_hub.entity.enums.Role;
import com.smartlogi.smart_city_hub.entity.enums.UserStatus;
import com.smartlogi.smart_city_hub.exception.ResourceNotFoundException;
import com.smartlogi.smart_city_hub.mapper.UserMapper;
import com.smartlogi.smart_city_hub.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User mockUser;
    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id("user-1")
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .phone("0612345678")
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .mustChangePassword(false)
                .build();

        mockUserResponse = new UserResponse();
        mockUserResponse.setId("user-1");
        mockUserResponse.setEmail("user@example.com");
    }

    private void mockSecurityContext(String email) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ========== getCurrentUser ==========

    @Nested
    @DisplayName("getCurrentUser")
    class GetCurrentUserTests {

        @Test
        void should_ReturnCurrentUser_When_Authenticated() {
            mockSecurityContext("user@example.com");
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));

            User result = userService.getCurrentUser();

            assertNotNull(result);
            assertEquals("user-1", result.getId());
            assertEquals("user@example.com", result.getEmail());
        }

        @Test
        void should_ThrowNotFound_When_UserNotInDatabase() {
            mockSecurityContext("unknown@example.com");
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> userService.getCurrentUser());
        }
    }

    // ========== getCurrentUserProfile ==========

    @Nested
    @DisplayName("getCurrentUserProfile")
    class GetCurrentUserProfileTests {

        @Test
        void should_ReturnUserResponse_When_Authenticated() {
            mockSecurityContext("user@example.com");
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
            when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);

            UserResponse result = userService.getCurrentUserProfile();

            assertNotNull(result);
            assertEquals("user-1", result.getId());
        }
    }

    // ========== getUserById ==========

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {

        @Test
        void should_ReturnUser_When_Found() {
            when(userRepository.findById("user-1")).thenReturn(Optional.of(mockUser));
            when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);

            UserResponse result = userService.getUserById("user-1");

            assertNotNull(result);
            assertEquals("user-1", result.getId());
        }

        @Test
        void should_ThrowNotFound_When_UserDoesNotExist() {
            when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> userService.getUserById("nonexistent"));
        }
    }

    // ========== getAllUsers ==========

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsersTests {

        @Test
        void should_ReturnPagedUserList() {
            Page<User> page = new PageImpl<>(List.of(mockUser));
            when(userRepository.findAll(any(Pageable.class))).thenReturn(page);
            when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);

            Page<UserResponse> result = userService.getAllUsers(PageRequest.of(0, 10));

            assertEquals(1, result.getTotalElements());
            assertEquals("user-1", result.getContent().get(0).getId());
        }

        @Test
        void should_ReturnEmptyPage_When_NoUsers() {
            Page<User> emptyPage = new PageImpl<>(List.of());
            when(userRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            Page<UserResponse> result = userService.getAllUsers(PageRequest.of(0, 10));

            assertEquals(0, result.getTotalElements());
        }
    }

    // ========== getPendingUsers ==========

    @Nested
    @DisplayName("getPendingUsers")
    class GetPendingUsersTests {

        @Test
        void should_ReturnOnlyPendingUsers() {
            User pendingUser = User.builder()
                    .id("pending-1")
                    .email("pending@example.com")
                    .status(UserStatus.PENDING)
                    .mustChangePassword(false)
                    .build();
            UserResponse pendingResponse = new UserResponse();
            pendingResponse.setId("pending-1");

            Page<User> page = new PageImpl<>(List.of(pendingUser));
            when(userRepository.findByStatus(eq(UserStatus.PENDING), any(Pageable.class))).thenReturn(page);
            when(userMapper.toResponse(pendingUser)).thenReturn(pendingResponse);

            Page<UserResponse> result = userService.getPendingUsers(PageRequest.of(0, 10));

            assertEquals(1, result.getTotalElements());
        }
    }

    // ========== getAgents ==========

    @Nested
    @DisplayName("getAgents")
    class GetAgentsTests {

        @Test
        void should_ReturnActiveAgents() {
            User agent = User.builder()
                    .id("agent-1")
                    .email("agent@example.com")
                    .role(Role.ROLE_AGENT)
                    .status(UserStatus.ACTIVE)
                    .mustChangePassword(false)
                    .build();
            UserResponse agentResponse = new UserResponse();
            agentResponse.setId("agent-1");

            when(userRepository.findByRoleAndStatus(Role.ROLE_AGENT, UserStatus.ACTIVE))
                    .thenReturn(List.of(agent));
            when(userMapper.toResponse(agent)).thenReturn(agentResponse);

            List<UserResponse> result = userService.getAgents();

            assertEquals(1, result.size());
            assertEquals("agent-1", result.get(0).getId());
        }

        @Test
        void should_ReturnEmptyList_When_NoAgents() {
            when(userRepository.findByRoleAndStatus(Role.ROLE_AGENT, UserStatus.ACTIVE))
                    .thenReturn(List.of());

            List<UserResponse> result = userService.getAgents();

            assertTrue(result.isEmpty());
        }
    }

    // ========== updateProfile ==========

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfileTests {

        @Test
        void should_UpdateAllFields_When_AllProvided() {
            mockSecurityContext("user@example.com");
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
            when(userRepository.save(mockUser)).thenReturn(mockUser);
            when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);

            userService.updateProfile("NewFirst", "NewLast", "0699999999");

            assertEquals("NewFirst", mockUser.getFirstName());
            assertEquals("NewLast", mockUser.getLastName());
            assertEquals("0699999999", mockUser.getPhone());
            verify(userRepository).save(mockUser);
        }

        @Test
        void should_SkipNullFields_When_PartialUpdate() {
            mockSecurityContext("user@example.com");
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
            when(userRepository.save(mockUser)).thenReturn(mockUser);
            when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);

            userService.updateProfile(null, null, "0611111111");

            assertEquals("John", mockUser.getFirstName());
            assertEquals("Doe", mockUser.getLastName());
            assertEquals("0611111111", mockUser.getPhone());
        }
    }

    // ========== updateUserRole ==========

    @Nested
    @DisplayName("updateUserRole")
    class UpdateUserRoleTests {

        @Test
        void should_UpdateRole_When_UserExists() {
            when(userRepository.findById("user-1")).thenReturn(Optional.of(mockUser));
            when(userRepository.save(mockUser)).thenReturn(mockUser);
            when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);

            userService.updateUserRole("user-1", Role.ROLE_AGENT);

            assertEquals(Role.ROLE_AGENT, mockUser.getRole());
            verify(userRepository).save(mockUser);
        }

        @Test
        void should_ThrowNotFound_When_UserDoesNotExist() {
            when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> userService.updateUserRole("nonexistent", Role.ROLE_AGENT));
        }
    }

    // ========== deactivateUser ==========

    @Nested
    @DisplayName("deactivateUser")
    class DeactivateUserTests {

        @Test
        void should_SetStatusInactive_When_UserExists() {
            when(userRepository.findById("user-1")).thenReturn(Optional.of(mockUser));

            userService.deactivateUser("user-1");

            assertEquals(UserStatus.INACTIVE, mockUser.getStatus());
            verify(userRepository).save(mockUser);
        }

        @Test
        void should_ThrowNotFound_When_UserDoesNotExist() {
            when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> userService.deactivateUser("nonexistent"));
        }
    }

    // ========== activateUser ==========

    @Nested
    @DisplayName("activateUser")
    class ActivateUserTests {

        @Test
        void should_SetStatusActive_When_UserExists() {
            mockUser.setStatus(UserStatus.INACTIVE);
            when(userRepository.findById("user-1")).thenReturn(Optional.of(mockUser));

            userService.activateUser("user-1");

            assertEquals(UserStatus.ACTIVE, mockUser.getStatus());
            verify(userRepository).save(mockUser);
        }

        @Test
        void should_ThrowNotFound_When_UserDoesNotExist() {
            when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> userService.activateUser("nonexistent"));
        }
    }
}
