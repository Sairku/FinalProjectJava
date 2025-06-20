package com.facebook.service;

import com.facebook.dto.*;
import com.facebook.exception.NotFoundException;
import com.facebook.model.*;
import com.facebook.repository.CommentRepository;
import com.facebook.repository.LikeRepository;
import com.facebook.repository.PostRepository;
import com.facebook.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserAchievementService userAchievementService;

    @InjectMocks
    private PostService postService;

    private User mockUser;

    @BeforeEach
    void init() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
    }

    @Test
    void createPost_shouldReturnPostResponse() {
        PostCreateRequestDto request = new PostCreateRequestDto();
        request.setDescription("Test post");
        request.setImages(List.of("img1.jpg", "img2.jpg"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            savedPost.setCreatedDate(LocalDateTime.now());
            return savedPost;
        });

        PostResponseDto response = postService.createPost(1L, request);

        assertNotNull(response);
        assertEquals("Test post", response.getDescription());
        assertEquals(2, response.getImages().size());
        assertEquals("John", response.getUser().getFirstName());

        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_shouldThrowNotFound_whenUserMissing() {
        PostCreateRequestDto request = new PostCreateRequestDto();
        request.setDescription("Should fail");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.createPost(999L, request));
    }

    @Test
    void updatePost_shouldReturnUpdatedPostResponse() {
        Post existingPost = new Post();
        existingPost.setId(1L);
        existingPost.setDescription("Old description");
        existingPost.setUser(mockUser);

        PostUpdateRequestDto request = new PostUpdateRequestDto();
        request.setDescription("New description");
        request.setImages(List.of("new1.jpg", "new2.jpg"));

        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post saved = invocation.getArgument(0);
            saved.setCreatedDate(LocalDateTime.now());
            return saved;
        });

        PostResponseDto response = postService.updatePost(1L, request);

        assertNotNull(response);
        assertEquals("New description", response.getDescription());
        assertEquals(2, response.getImages().size());
        assertEquals("Doe", response.getUser().getLastName());

        verify(postRepository).save(any(Post.class));
    }

    @Test
    void updatePost_shouldThrowNotFound_whenPostMissing() {
        PostUpdateRequestDto request = new PostUpdateRequestDto();
        request.setDescription("Nothing to update");

        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.updatePost(999L, request));
    }

    @Test
    void deletePost_shouldCallRepositoryDelete_whenPostExists() {
        Post post = new Post();
        post.setId(1L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.deletePost(1L);

        verify(postRepository).delete(post);
    }

    @Test
    void deletePost_shouldThrowNotFound_whenPostMissing() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.deletePost(999L));
    }

    @Test
    void testLikePost_addLike() {
        Post post = new Post();
        post.setId(1L);
        post.setLikes(new ArrayList<>());
        post.setUser(mockUser);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));
        when(likeRepository.findByUserIdAndPostId(2L, 1L)).thenReturn(Optional.empty());
        when(postRepository.save(any(Post.class))).thenReturn(post);

        int likeCount = postService.likePost(1L, 2L);

        assertEquals(1, likeCount);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void testLikePost_removeLike() {
        Post post = new Post();
        post.setId(1L);

        Like like = new Like();
        like.setUser(mockUser);
        like.setPost(post);

        post.setLikes(new ArrayList<>(List.of(like)));

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(likeRepository.findByUserIdAndPostId(1L, 1L)).thenReturn(Optional.of(like));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        int likeCount = postService.likePost(1L, 1L);

        assertEquals(0, likeCount);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void testLikePost_throwNotFoundPost() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.likePost(999L, 1L));
    }

    @Test
    void testLikePost_throwNotFoundUser() {
        Post post = new Post();
        post.setId(1L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.likePost(1L, 999L));
    }

    @Test
    void testAddComment() {
        Post post = new Post();
        post.setId(1L);
        post.setComments(new ArrayList<>());
        post.setUser(mockUser);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(2L)).thenReturn(Optional.of(mockUser));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        CommentResponseDto response = postService.addComment(1L, 2L, "Hello world");

        assertNotNull(response);
        assertEquals("Hello world", response.getText());
        assertEquals("John", response.getUser().getFirstName());
        verify(postRepository).save(post);
    }

    @Test
    void testAddComment_throwNotFoundPost() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.addComment(999L, 1L, "Test comment"));
    }

    @Test
    void testAddComment_throwNotFoundUser() {
        Post post = new Post();
        post.setId(1L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.addComment(1L, 999L, "Test comment"));
    }

    @Test
    void testGetComments() {
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

        List<CommentResponseDto> comments = postService.getComments(1L);

        assertEquals(1, comments.size());
        assertEquals("Hi!", comments.getFirst().getText());
    }

    @Test
    void testGetComments_throwNotFoundPost() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.addComment(999L, 1L, "Test comment"));
    }

    @Test
    void testDeleteComment() {
        Post post = new Post();
        post.setId(1L);
        post.setComments(new ArrayList<>());

        Comment comment = new Comment();
        comment.setId(10L);
        comment.setUser(mockUser);
        post.getComments().add(comment);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        postService.deleteComment(1L, mockUser.getId(), 10L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void testDeleteComment_throwNotFoundPost() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.deleteComment(999L, 1L, 10L));
    }

    @Test
    void testDeleteComment_throwNotFoundComment() {
        Post post = new Post();
        post.setId(1L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.deleteComment(1L, 1L, 999L));
    }

    @Test
    void testDeleteComment_throwInvalidUser() {
        Post post = new Post();
        post.setId(1L);
        post.setComments(new ArrayList<>());

        Comment comment = new Comment();
        comment.setId(10L);
        comment.setUser(mockUser);
        post.getComments().add(comment);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        assertThrows(IllegalArgumentException.class, () -> postService.deleteComment(1L, 999L, 10L));
    }

    @Test
    void createPost_shouldGiveBuzzStartedAchievement_whenFirstPost() {
        PostCreateRequestDto request = new PostCreateRequestDto();
        request.setDescription("First post");
        request.setImages(List.of("img1.jpg"));

        Post savedPost = new Post();
        savedPost.setUser(mockUser);
        savedPost.setImages(new ArrayList<>(List.of(new PostImage())));
        savedPost.setCreatedDate(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(postRepository.save(any())).thenReturn(savedPost);
        when(postRepository.findAllByUserId(1L)).thenReturn(Optional.of(List.of(savedPost)));
        when(userAchievementService.userHaveAchievement(mockUser, "Buzz Started")).thenReturn(false);

        postService.createPost(1L, request);

        verify(userAchievementService).awardAchievement(mockUser, "Buzz Started");
    }

    @Test
    void createPost_shouldGiveAestheticDropAchievement_whenFivePostsWithPhotos() {
        PostCreateRequestDto request = new PostCreateRequestDto();
        request.setDescription("Post 5");
        request.setImages(List.of("img.jpg"));

        List<Post> existingPosts = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Post p = new Post();
            p.setImages(new ArrayList<>(List.of(new PostImage())));
            existingPosts.add(p);
        }

        Post newPost = new Post();
        newPost.setImages(new ArrayList<>(List.of(new PostImage())));
        existingPosts.add(newPost);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(postRepository.save(any())).thenReturn(newPost);
        when(postRepository.findAllByUserId(1L)).thenReturn(Optional.of(existingPosts));
        when(userAchievementService.userHaveAchievement(mockUser, "Aesthetic Drop")).thenReturn(false);

        postService.createPost(1L, request);

        verify(userAchievementService).awardAchievement(mockUser, "Aesthetic Drop");
    }
}
