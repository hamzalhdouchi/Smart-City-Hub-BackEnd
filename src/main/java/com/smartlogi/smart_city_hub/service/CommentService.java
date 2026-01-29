package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.dto.request.CreateCommentRequest;
import com.smartlogi.smart_city_hub.dto.response.CommentResponse;
import com.smartlogi.smart_city_hub.entity.Comment;
import com.smartlogi.smart_city_hub.entity.Incident;
import com.smartlogi.smart_city_hub.entity.User;
import com.smartlogi.smart_city_hub.exception.ForbiddenException;
import com.smartlogi.smart_city_hub.exception.ResourceNotFoundException;
import com.smartlogi.smart_city_hub.mapper.CommentMapper;
import com.smartlogi.smart_city_hub.repository.CommentRepository;
import com.smartlogi.smart_city_hub.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final IncidentRepository incidentRepository;
    private final UserService userService;
    private final CommentMapper commentMapper;
    
    public List<CommentResponse> getCommentsByIncident(Long incidentId) {
        // Verify incident exists
        if (!incidentRepository.existsById(incidentId)) {
            throw new ResourceNotFoundException("Incident", incidentId);
        }
        
        return commentRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId).stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CommentResponse addComment(Long incidentId, CreateCommentRequest request) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", incidentId));
        
        User author = userService.getCurrentUser();
        
        Comment comment = Comment.builder()
                .incident(incident)
                .author(author)
                .content(request.getContent())
                .build();
        
        comment = commentRepository.save(comment);
        log.info("Comment added to incident {} by {}", incidentId, author.getEmail());
        
        return commentMapper.toResponse(comment);
    }
    
    @Transactional
    public CommentResponse updateComment(Long incidentId, Long commentId, CreateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));
        
        // Verify comment belongs to this incident
        if (!comment.getIncident().getId().equals(incidentId)) {
            throw new ResourceNotFoundException("Comment", commentId);
        }
        
        // Only author can update their comment
        User currentUser = userService.getCurrentUser();
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only edit your own comments");
        }
        
        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);
        
        log.info("Comment {} updated by {}", commentId, currentUser.getEmail());
        return commentMapper.toResponse(comment);
    }
    
    @Transactional
    public void deleteComment(Long incidentId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));
        
        // Verify comment belongs to this incident
        if (!comment.getIncident().getId().equals(incidentId)) {
            throw new ResourceNotFoundException("Comment", commentId);
        }
        
        User currentUser = userService.getCurrentUser();
        
        // Author or Admin can delete
        boolean isAuthor = comment.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().name().equals("ROLE_ADMIN");
        
        if (!isAuthor && !isAdmin) {
            throw new ForbiddenException("You don't have permission to delete this comment");
        }
        
        commentRepository.delete(comment);
        log.info("Comment {} deleted by {}", commentId, currentUser.getEmail());
    }
}
