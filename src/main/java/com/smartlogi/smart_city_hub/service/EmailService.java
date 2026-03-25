package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.admin-email:admin@smartcityhub.com}")
    private String adminEmail;

    @Value("${app.admin-dashboard-url:http://localhost:3000/admin/users/pending}")
    private String adminDashboardUrl;

    @Value("${app.login-url:http://localhost:3000/login}")
    private String loginUrl;

    @Value("${spring.mail.username:noreply@smartcityhub.com}")
    private String fromEmail;

    @Async
    public void sendAdminNotification(User newUser) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("New User Registration - Smart City Hub");
            message.setText(buildAdminNotificationBody(newUser));

            mailSender.send(message);
            log.info("Admin notification sent for new user: {}", newUser.getEmail());
        } catch (Exception e) {
            log.error("Failed to send admin notification for user {}: {}", newUser.getEmail(), e.getMessage());
        }
    }

    @Async
    public void sendActivationEmail(User user, String temporaryPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Your Smart City Hub Account is Approved!");
            message.setText(buildActivationEmailBody(user, temporaryPassword));

            mailSender.send(message);
            log.info("Activation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send activation email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Async
    public void sendForgotPasswordEmail(User user, String newPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Your New Password - Smart City Hub");
            message.setText(buildForgotPasswordEmailBody(user, newPassword));

            mailSender.send(message);
            log.info("New password email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send new password email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Async
    public void sendPasswordChangeConfirmation(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Password Changed - Smart City Hub");
            message.setText(buildPasswordChangeBody(user));

            mailSender.send(message);
            log.info("Password change confirmation sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password change confirmation to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private String buildAdminNotificationBody(User user) {
        return String.format("""
                New User Registration Alert
                ============================

                A new user has registered and is awaiting your approval.

                User Details:
                - Name: %s %s
                - Email: %s
                - Phone: %s
                - National ID: %s
                - Registration Date: %s

                Please review and approve/reject this registration:
                %s

                --
                Smart City Hub System
                """,
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getNationalId(),
                user.getCreatedAt(),
                adminDashboardUrl);
    }

    private String buildActivationEmailBody(User user, String temporaryPassword) {
        return String.format("""
                Welcome to Smart City Hub!
                ==========================

                Dear %s,

                Great news! Your account has been approved and is now active.

                Your Login Credentials:
                - Email: %s
                - Temporary Password: %s

                Login URL: %s

                IMPORTANT: For security reasons, you will be required to change your password upon first login.

                Thank you for joining Smart City Hub!

                --
                Smart City Hub Team
                """,
                user.getFirstName(),
                user.getEmail(),
                temporaryPassword,
                loginUrl);
    }

    private String buildPasswordChangeBody(User user) {
        return String.format("""
                Password Changed Successfully
                =============================

                Dear %s,

                Your password has been successfully changed.

                If you did not make this change, please contact our support team immediately.

                --
                Smart City Hub Team
                """,
                user.getFirstName());
    }

    private String buildForgotPasswordEmailBody(User user, String newPassword) {
        return String.format("""
                Password Reset - Smart City Hub
                ================================

                Dear %s,

                You requested a password reset. A new password has been generated for your account.

                Your New Credentials:
                - Email: %s
                - New Password: %s

                Login URL: %s

                IMPORTANT: For security reasons, you will be required to change this password upon your next login.

                If you did not request this reset, please contact our support team immediately.

                --
                Smart City Hub Team
                """,
                user.getFirstName(),
                user.getEmail(),
                newPassword,
                loginUrl);
    }
}
