package com.facebook.service;

import com.facebook.dto.CommentResponseDto;
import com.facebook.dto.UserShortDto;
import com.facebook.exception.NotFoundException;
import com.facebook.model.Comment;
import com.facebook.model.Post;
import com.facebook.model.User;
import com.facebook.repository.CommentRepository;
import com.facebook.repository.PostRepository;
import com.facebook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CommentService commentService;

    private User mockUser;
    private UserShortDto mockUserShort;

    @BeforeEach
    void init() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");

        mockUserShort = new UserShortDto();
        mockUserShort.setId(mockUser.getId());
        mockUserShort.setFirstName(mockUser.getFirstName());
        mockUserShort.setLastName(mockUser.getLastName());
    }

    @Test
    void testAddComment() {
        Post post = new Post();
        post.setId(1L);
        post.setComments(new ArrayList<>());
        post.setUser(mockUser);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(2L)).thenReturn(Optional.of(mockUser));
        when(modelMapper.map(mockUser, UserShortDto.class)).thenReturn(mockUserShort);
        when(postRepository.save(any(Post.class))).thenReturn(post);

        CommentResponseDto response = commentService.addComment(1L, 2L, "Hello world");

        assertNotNull(response);
        assertEquals("Hello world", response.getText());
        assertEquals("John", response.getUser().getFirstName());
        verify(postRepository).save(post);
    }

    @Test
    void testAddComment_throwNotFoundPost() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.addComment(999L, 1L, "Test comment"));
    }

    @Test
    void testAddComment_throwNotFoundUser() {
        Post post = new Post();
        post.setId(1L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.addComment(1L, 999L, "Test comment"));
    }

    @Test
    void testUpdateCommentSuccess() {
        Comment comment = new Comment();
        comment.setId(10L);
        comment.setUser(mockUser);
        comment.setText("Old text");

        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        commentService.updateComment(10L, mockUser.getId(), "New text");

        assertEquals("New text", comment.getText());
        verify(commentRepository).save(comment);
    }

    @Test
    void testUpdateComment_throwNotFoundComment() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.updateComment(999L, 1L, "New text"));
    }

    @Test
    void testUpdateComment_throwInvalidUser() {
        Comment comment = new Comment();
        comment.setId(10L);
        comment.setUser(mockUser);
        comment.setText("Old text");

        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        assertThrows(IllegalArgumentException.class, () -> commentService.updateComment(10L, 999L, "New text"));
    }

    @Test
    void testGetPostComments() {
        Post post = new Post();
        post.setId(1L);

        Comment comment = new Comment();
        comment.setId(100L);
        comment.setText("Hi!");
        comment.setUser(mockUser);
        comment.setCreatedDate(LocalDateTime.now());

        post.setComments(List.of(comment));
        post.setUser(mockUser);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(modelMapper.map(mockUser, UserShortDto.class)).thenReturn(mockUserShort);

        List<CommentResponseDto> comments = commentService.getPostComments(1L);

        assertEquals(1, comments.size());
        assertEquals("Hi!", comments.getFirst().getText());
    }

    @Test
    void testGetPostComments_throwNotFoundPost() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->commentService.addComment(999L, 1L, "Test comment"));
    }

    @Test
    void testDeleteComment() {
        Comment comment = new Comment();
        comment.setId(10L);
        comment.setUser(mockUser);

        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(10L, mockUser.getId());

        verify(commentRepository).delete(comment);
    }

    @Test
    void testDeleteComment_throwNotFoundComment() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.deleteComment(999L, 1L));
    }

    @Test
    void testDeleteComment_throwInvalidUser() {
        Comment comment = new Comment();
        comment.setId(10L);
        comment.setUser(mockUser);

        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        assertThrows(IllegalArgumentException.class, () -> commentService.deleteComment(10L, 999L));
    }
}
