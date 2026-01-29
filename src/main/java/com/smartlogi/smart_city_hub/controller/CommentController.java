package com.smartlogi.smart_city_hub.controller;

import com.smartlogi.smart_city_hub.dto.request.CreateCommentRequest;
import com.smartlogi.smart_city_hub.dto.response.ApiResponse;
import com.smartlogi.smart_city_hub.dto.response.CommentResponse;
import com.smartlogi.smart_city_hub.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents/{incidentId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Incident comments management")
@SecurityRequirement(name = "Bearer Authentication")
public class CommentController {
    
    private final CommentService commentService;
    
    @GetMapping
    @Operation(summary = "List comments", description = "Get all comments for an incident")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable Long incidentId) {
        List<CommentResponse> response = commentService.getCommentsByIncident(incidentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping
    @Operation(summary = "Add comment", description = "Add a comment to an incident")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long incidentId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        CommentResponse response = commentService.addComment(incidentId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added", response));
    }
    
    @PutMapping("/{commentId}")
    @Operation(summary = "Update comment", description = "Update your own comment")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long incidentId,
            @PathVariable Long commentId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        CommentResponse response = commentService.updateComment(incidentId, commentId, request);
        return ResponseEntity.ok(ApiResponse.success("Comment updated", response));
    }
    
    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete comment", description = "Delete your own comment (or any as Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long incidentId,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(incidentId, commentId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted", null));
    }
}
