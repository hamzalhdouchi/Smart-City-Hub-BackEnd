package com.smartlogi.smart_city_hub.dto.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDepartmentRequest {

    private String name;

    private String description;

    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;
}
