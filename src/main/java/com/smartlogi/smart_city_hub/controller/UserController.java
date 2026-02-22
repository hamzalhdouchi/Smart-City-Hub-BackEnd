package com.smartlogi.smart_city_hub.controller;

import com.smartlogi.smart_city_hub.dto.response.ApiResponse;
import com.smartlogi.smart_city_hub.dto.response.UserResponse;
import com.smartlogi.smart_city_hub.entity.enums.Role;
import com.smartlogi.smart_city_hub.service.AuthService;
import com.smartlogi.smart_city_hub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final com.smartlogi.smart_city_hub.service.ProfilePhotoService profilePhotoService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user", description = "Get the profile of the currently authenticated user")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update profile", description = "Update the current user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phone) {
        UserResponse response = userService.updateProfile(firstName, lastName, phone);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", response));
    }

    @PostMapping(value = "/me/photo", consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload profile photo", description = "Upload a profile photo for the current user")
    public ResponseEntity<ApiResponse<String>> uploadProfilePhoto(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        String photoUrl = profilePhotoService.uploadProfilePhoto(file);
        return ResponseEntity.ok(ApiResponse.success("Profile photo uploaded", photoUrl));
    }

    @GetMapping("/{id}/photo")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user photo", description = "Get the profile photo URL for a user")
    public ResponseEntity<ApiResponse<String>> getUserPhoto(@PathVariable String id) {
        String photoUrl = profilePhotoService.getCurrentProfilePhotoUrl(id).orElse(null);
        return ResponseEntity.ok(ApiResponse.success(photoUrl));
    }

    @GetMapping("/me/photo")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user photo", description = "Get the profile photo URL for the current user")
    public ResponseEntity<ApiResponse<String>> getCurrentUserPhoto() {
        com.smartlogi.smart_city_hub.entity.User currentUser = userService.getCurrentUser();
        String photoUrl = profilePhotoService.getCurrentProfilePhotoUrl(currentUser.getId()).orElse(null);
        return ResponseEntity.ok(ApiResponse.success(photoUrl));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users", description = "Get all users (Admin only)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(Pageable pageable) {
        Page<UserResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List pending users", description = "Get all users awaiting approval (Admin only)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getPendingUsers(Pageable pageable) {
        Page<UserResponse> response = userService.getPendingUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve user", description = "Approve a pending user registration (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> approveUser(@PathVariable String id) {
        UserResponse response = authService.approveUser(id);
        return ResponseEntity.ok(ApiResponse.success("User approved and activation email sent", response));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject user", description = "Reject a pending user registration (Admin only)")
    public ResponseEntity<ApiResponse<Void>> rejectUser(@PathVariable String id) {
        authService.rejectUser(id);
        return ResponseEntity.ok(ApiResponse.success("User registration rejected", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Get user details by ID (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/agents")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
    @Operation(summary = "List agents", description = "Get all active agents (Supervisor/Admin)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAgents() {
        List<UserResponse> response = userService.getAgents();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role", description = "Change a user's role (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable String id,
            @RequestParam Role role) {
        UserResponse response = userService.updateUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("Role updated", response));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user", description = "Deactivate a user account (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable String id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated", null));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate user", description = "Activate a user account (Admin only)")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable String id) {
        userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User activated", null));
    }
}
