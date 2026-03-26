package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.entity.User;
import com.smartlogi.smart_city_hub.entity.enums.Role;
import com.smartlogi.smart_city_hub.entity.enums.UserStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "adminEmail", "admin@smartcityhub.com");
        ReflectionTestUtils.setField(emailService, "adminDashboardUrl", "http://localhost:3000/admin/users/pending");
        ReflectionTestUtils.setField(emailService, "loginUrl", "http://localhost:3000/login");
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@smartcityhub.com");

        mockUser = User.builder()
                .id("user-1")
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .phone("0612345678")
                .nationalId("ABC12345")
                .role(Role.ROLE_USER)
                .status(UserStatus.PENDING)
                .mustChangePassword(false)
                .build();
    }

    

    @Nested
    @DisplayName("sendAdminNotification")
    class SendAdminNotificationTests {

        @Test
        void should_SendEmail_When_NewUserRegisters() {
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            assertDoesNotThrow(() -> emailService.sendAdminNotification(mockUser));

            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        void should_NotThrow_When_MailSenderFails() {
            doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

            assertDoesNotThrow(() -> emailService.sendAdminNotification(mockUser));
        }

        @Test
        void should_SendToAdminEmail() {
            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            doNothing().when(mailSender).send(captor.capture());

            emailService.sendAdminNotification(mockUser);

            SimpleMailMessage sent = captor.getValue();
            assertArrayEquals(new String[]{"admin@smartcityhub.com"}, sent.getTo());
            assertEquals("noreply@smartcityhub.com", sent.getFrom());
            assertNotNull(sent.getSubject());
            assertTrue(sent.getText().contains("John"));
            assertTrue(sent.getText().contains("Doe"));
            assertTrue(sent.getText().contains("user@example.com"));
        }
    }

    

    @Nested
    @DisplayName("sendActivationEmail")
    class SendActivationEmailTests {

        @Test
        void should_SendEmail_When_UserApproved() {
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            assertDoesNotThrow(() -> emailService.sendActivationEmail(mockUser, "TempPwd123!"));

            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        void should_NotThrow_When_MailSenderFails() {
            doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

            assertDoesNotThrow(() -> emailService.sendActivationEmail(mockUser, "TempPwd123!"));
        }

        @Test
        void should_SendToUserEmail_WithTempPassword() {
            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            doNothing().when(mailSender).send(captor.capture());

            emailService.sendActivationEmail(mockUser, "TempPwd123!");

            SimpleMailMessage sent = captor.getValue();
            assertArrayEquals(new String[]{"user@example.com"}, sent.getTo());
            assertEquals("noreply@smartcityhub.com", sent.getFrom());
            assertTrue(sent.getText().contains("TempPwd123!"));
            assertTrue(sent.getText().contains("John"));
            assertTrue(sent.getText().contains("user@example.com"));
        }
    }

    

    @Nested
    @DisplayName("sendPasswordChangeConfirmation")
    class SendPasswordChangeConfirmationTests {

        @Test
        void should_SendConfirmationEmail_When_PasswordChanged() {
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            assertDoesNotThrow(() -> emailService.sendPasswordChangeConfirmation(mockUser));

            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        void should_NotThrow_When_MailSenderFails() {
            doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

            assertDoesNotThrow(() -> emailService.sendPasswordChangeConfirmation(mockUser));
        }

        @Test
        void should_SendToUserEmail_WithUserName() {
            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            doNothing().when(mailSender).send(captor.capture());

            emailService.sendPasswordChangeConfirmation(mockUser);

            SimpleMailMessage sent = captor.getValue();
            assertArrayEquals(new String[]{"user@example.com"}, sent.getTo());
            assertEquals("noreply@smartcityhub.com", sent.getFrom());
            assertTrue(sent.getText().contains("John"));
            assertNotNull(sent.getSubject());
        }
    }
}
