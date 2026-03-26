package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.dto.request.CreateCommentRequest;
import com.smartlogi.smart_city_hub.dto.response.CommentResponse;
import com.smartlogi.smart_city_hub.entity.Comment;
import com.smartlogi.smart_city_hub.entity.Incident;
import com.smartlogi.smart_city_hub.entity.User;
import com.smartlogi.smart_city_hub.entity.enums.IncidentStatus;
import com.smartlogi.smart_city_hub.entity.enums.Role;
import com.smartlogi.smart_city_hub.entity.enums.UserStatus;
import com.smartlogi.smart_city_hub.exception.ForbiddenException;
import com.smartlogi.smart_city_hub.exception.ResourceNotFoundException;
import com.smartlogi.smart_city_hub.mapper.CommentMapper;
import com.smartlogi.smart_city_hub.repository.CommentRepository;
import com.smartlogi.smart_city_hub.repository.IncidentRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private IncidentRepository incidentRepository;
    @Mock private UserService userService;
    @Mock private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    private User mockUser;
    private User mockAdmin;
    private User mockOtherUser;
    private Incident mockIncident;
    private Comment mockComment;
    private CommentResponse mockCommentResponse;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id("user-1")
                .email("user@example.com")
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .mustChangePassword(false)
                .build();

        mockAdmin = User.builder()
                .id("admin-1")
                .email("admin@example.com")
                .role(Role.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .mustChangePassword(false)
                .build();

        mockOtherUser = User.builder()
                .id("other-1")
                .email("other@example.com")
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .mustChangePassword(false)
                .build();

        mockIncident = Incident.builder()
                .id("inc-1")
                .title("Test Incident")
                .status(IncidentStatus.NEW)
                .reporter(mockUser)
                .build();

        mockComment = Comment.builder()
                .id("comment-1")
                .incident(mockIncident)
                .author(mockUser)
                .content("This is a test comment")
                .build();

        mockCommentResponse = new CommentResponse();
        mockCommentResponse.setId("comment-1");
        mockCommentResponse.setContent("This is a test comment");
    }

    

    @Nested
    @DisplayName("getCommentsByIncident")
    class GetCommentsByIncidentTests {

        @Test
        void should_ReturnComments_When_IncidentExists() {
            when(incidentRepository.existsById("inc-1")).thenReturn(true);
            when(commentRepository.findByIncidentIdOrderByCreatedAtDesc("inc-1"))
                    .thenReturn(List.of(mockComment));
            when(commentMapper.toResponse(mockComment)).thenReturn(mockCommentResponse);

            List<CommentResponse> result = commentService.getCommentsByIncident("inc-1");

            assertEquals(1, result.size());
            assertEquals("comment-1", result.get(0).getId());
        }

        @Test
        void should_ReturnEmptyList_When_NoComments() {
            when(incidentRepository.existsById("inc-1")).thenReturn(true);
            when(commentRepository.findByIncidentIdOrderByCreatedAtDesc("inc-1"))
                    .thenReturn(List.of());

            List<CommentResponse> result = commentService.getCommentsByIncident("inc-1");

            assertTrue(result.isEmpty());
        }

        @Test
        void should_ThrowNotFound_When_IncidentDoesNotExist() {
            when(incidentRepository.existsById("nonexistent")).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> commentService.getCommentsByIncident("nonexistent"));
        }
    }

    

    @Nested
    @DisplayName("addComment")
    class AddCommentTests {

        @Test
        void should_AddComment_When_ValidRequest() {
            CreateCommentRequest request = new CreateCommentRequest();
            request.setContent("New comment");

            when(incidentRepository.findById("inc-1")).thenReturn(Optional.of(mockIncident));
            when(userService.getCurrentUser()).thenReturn(mockUser);
            when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);
            when(commentMapper.toResponse(mockComment)).thenReturn(mockCommentResponse);

            CommentResponse result = commentService.addComment("inc-1", request);

            assertNotNull(result);
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        void should_ThrowNotFound_When_IncidentDoesNotExist() {
            CreateCommentRequest request = new CreateCommentRequest();
            request.setContent("New comment");

            when(incidentRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> commentService.addComment("nonexistent", request));
            verify(commentRepository, never()).save(any());
        }
    }

    

    @Nested
    @DisplayName("updateComment")
    class UpdateCommentTests {

        @Test
        void should_UpdateComment_When_AuthorUpdates() {
            CreateCommentRequest request = new CreateCommentRequest();
            request.setContent("Updated content");

            when(commentRepository.findById("comment-1")).thenReturn(Optional.of(mockComment));
            when(userService.getCurrentUser()).thenReturn(mockUser);
            when(commentRepository.save(mockComment)).thenReturn(mockComment);
            when(commentMapper.toResponse(mockComment)).thenReturn(mockCommentResponse);

            CommentResponse result = commentService.updateComment("inc-1", "comment-1", request);

            assertNotNull(result);
            assertEquals("Updated content", mockComment.getContent());
            verify(commentRepository).save(mockComment);
        }

        @Test
        void should_ThrowNotFound_When_CommentDoesNotExist() {
            CreateCommentRequest request = new CreateCommentRequest();
            when(commentRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> commentService.updateComment("inc-1", "nonexistent", request));
        }

        @Test
        void should_ThrowNotFound_When_CommentBelongsToDifferentIncident() {
            Incident anotherIncident = Incident.builder().id("inc-2").build();
            Comment commentOnOtherIncident = Comment.builder()
                    .id("comment-1")
                    .incident(anotherIncident)
                    .author(mockUser)
                    .content("Some content")
                    .build();

            CreateCommentRequest request = new CreateCommentRequest();
            when(commentRepository.findById("comment-1")).thenReturn(Optional.of(commentOnOtherIncident));

            assertThrows(ResourceNotFoundException.class,
                    () -> commentService.updateComment("inc-1", "comment-1", request));
        }

        @Test
        void should_ThrowForbidden_When_NonAuthorTriesToUpdate() {
            CreateCommentRequest request = new CreateCommentRequest();
            request.setContent("Hijacked content");

            when(commentRepository.findById("comment-1")).thenReturn(Optional.of(mockComment));
            when(userService.getCurrentUser()).thenReturn(mockOtherUser);

            ForbiddenException ex = assertThrows(ForbiddenException.class,
                    () -> commentService.updateComment("inc-1", "comment-1", request));
            assertEquals("You can only edit your own comments", ex.getMessage());
        }
    }

    

    @Nested
    @DisplayName("deleteComment")
    class DeleteCommentTests {

        @Test
        void should_DeleteComment_When_AuthorDeletes() {
            when(commentRepository.findById("comment-1")).thenReturn(Optional.of(mockComment));
            when(userService.getCurrentUser()).thenReturn(mockUser);

            assertDoesNotThrow(() -> commentService.deleteComment("inc-1", "comment-1"));
            verify(commentRepository).delete(mockComment);
        }

        @Test
        void should_DeleteComment_When_AdminDeletes() {
            when(commentRepository.findById("comment-1")).thenReturn(Optional.of(mockComment));
            when(userService.getCurrentUser()).thenReturn(mockAdmin);

            assertDoesNotThrow(() -> commentService.deleteComment("inc-1", "comment-1"));
            verify(commentRepository).delete(mockComment);
        }

        @Test
        void should_ThrowForbidden_When_NeitherAuthorNorAdmin() {
            when(commentRepository.findById("comment-1")).thenReturn(Optional.of(mockComment));
            when(userService.getCurrentUser()).thenReturn(mockOtherUser);

            ForbiddenException ex = assertThrows(ForbiddenException.class,
                    () -> commentService.deleteComment("inc-1", "comment-1"));
            assertEquals("You don't have permission to delete this comment", ex.getMessage());
            verify(commentRepository, never()).delete(any());
        }

        @Test
        void should_ThrowNotFound_When_CommentDoesNotExist() {
            when(commentRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> commentService.deleteComment("inc-1", "nonexistent"));
        }

        @Test
        void should_ThrowNotFound_When_CommentBelongsToDifferentIncident() {
            Incident anotherIncident = Incident.builder().id("inc-99").build();
            Comment commentOnOtherIncident = Comment.builder()
                    .id("comment-1")
                    .incident(anotherIncident)
                    .author(mockUser)
                    .content("Some content")
                    .build();

            when(commentRepository.findById("comment-1")).thenReturn(Optional.of(commentOnOtherIncident));

            assertThrows(ResourceNotFoundException.class,
                    () -> commentService.deleteComment("inc-1", "comment-1"));
        }
    }
}
