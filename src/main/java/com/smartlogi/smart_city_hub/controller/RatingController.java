package com.smartlogi.smart_city_hub.controller;

import com.smartlogi.smart_city_hub.dto.request.CreateRatingRequest;
import com.smartlogi.smart_city_hub.dto.response.ApiResponse;
import com.smartlogi.smart_city_hub.dto.response.RatingResponse;
import com.smartlogi.smart_city_hub.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incidents/{incidentId}")
@RequiredArgsConstructor
@Tag(name = "Ratings", description = "Incident rating management")
@SecurityRequirement(name = "Bearer Authentication")
public class RatingController {
    
    private final RatingService ratingService;
    
    @GetMapping("/rating")
    @Operation(summary = "Get rating", description = "Get the rating for an incident")
    public ResponseEntity<ApiResponse<RatingResponse>> getRating(@PathVariable Long incidentId) {
        RatingResponse response = ratingService.getRatingByIncident(incidentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/rate")
    @Operation(summary = "Rate incident", description = "Rate a resolved incident (reporter only)")
    public ResponseEntity<ApiResponse<RatingResponse>> rateIncident(
            @PathVariable Long incidentId,
            @Valid @RequestBody CreateRatingRequest request
    ) {
        RatingResponse response = ratingService.rateIncident(incidentId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Incident rated", response));
    }
}
