package com.smartlogi.smart_city_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for pending registration confirmation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {

    private String message;
    private String email;

    public static RegistrationResponse pending(String email) {
        return RegistrationResponse.builder()
                .message(
                        "Your profile is currently being processed. You will receive an email notification once your account is approved.")
                .email(email)
                .build();
    }
}
