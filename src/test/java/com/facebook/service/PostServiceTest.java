package com.facebook.service;

import com.facebook.dto.*;
import com.facebook.exception.NotFoundException;
import com.facebook.model.Post;
import com.facebook.model.PostImage;
import com.facebook.model.User;
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
        PostCreateRequest request = new PostCreateRequest();
        request.setDescription("Test post");
        request.setImages(List.of("img1.jpg", "img2.jpg"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            savedPost.setCreatedDate(LocalDateTime.now());
            return savedPost;
        });

        PostResponse response = postService.createPost(1L, request);

        assertNotNull(response);
        assertEquals("Test post", response.getDescription());
        assertEquals(2, response.getImages().size());
        assertEquals("John", response.getUser().getFirstName());

        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_shouldThrowNotFound_whenUserMissing() {
        PostCreateRequest request = new PostCreateRequest();
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

        PostUpdateRequest request = new PostUpdateRequest();
        request.setDescription("New description");
        request.setImages(List.of("new1.jpg", "new2.jpg"));

        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post saved = invocation.getArgument(0);
            saved.setCreatedDate(LocalDateTime.now());
            return saved;
        });

        PostResponse response = postService.updatePost(1L, request);

        assertNotNull(response);
        assertEquals("New description", response.getDescription());
        assertEquals(2, response.getImages().size());
        assertEquals("Doe", response.getUser().getLastName());

        verify(postRepository).save(any(Post.class));
    }

    @Test
    void updatePost_shouldThrowNotFound_whenPostMissing() {
        PostUpdateRequest request = new PostUpdateRequest();
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
    void getAllPostsOfUser_shouldReturnPostResponses() {
        Post post1 = new Post();
        post1.setId(1L);
        post1.setDescription("First post");
        post1.setUser(mockUser);

        PostImage image1 = new PostImage();
        image1.setUrl("img1.jpg");
        image1.setPost(post1);

        PostImage image2 = new PostImage();
        image2.setUrl("img2.jpg");
        image2.setPost(post1);

        post1.setImages( new ArrayList<>(List.of(image1, image2)));

        Post post2 = new Post();
        post2.setId(2L);
        post2.setDescription("Second post");
        post2.setUser(mockUser);
        post2.setImages( new ArrayList<>(List.of())); // no images

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(postRepository.findAllByUserId(1L)).thenReturn(Optional.of(List.of(post1, post2)));

        List<PostResponse> responses = postService.getAllPostsOfUser(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());

        PostResponse firstResponse = responses.get(0);
        assertEquals("First post", firstResponse.getDescription());
        assertEquals(2, firstResponse.getImages().size());

        PostResponse secondResponse = responses.get(1);
        assertEquals("Second post", secondResponse.getDescription());
        assertTrue(secondResponse.getImages().isEmpty());

        verify(userRepository).findById(1L);
        verify(postRepository).findAllByUserId(1L);
    }

    @Test
    void getAllPostsOfUser_shouldThrowNotFound_whenUserMissing() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.getAllPostsOfUser(999L));

        verify(userRepository).findById(999L);
        verifyNoInteractions(postRepository);
    }

    @Test
    void getAllPostsOfUser_shouldThrowNotFound_whenNoPostsFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(postRepository.findAllByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.getAllPostsOfUser(1L));

        verify(userRepository).findById(1L);
        verify(postRepository).findAllByUserId(1L);
    }


}
