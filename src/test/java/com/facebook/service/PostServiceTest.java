package com.facebook.service;

import com.facebook.dto.*;
import com.facebook.exception.NotFoundException;
import com.facebook.model.*;
import com.facebook.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    private RepostRepository repostRepository;

    @Mock
    private UserAchievementService userAchievementService;

    @Mock
    private FriendService friendService;

    @InjectMocks
    private PostService postService;

    private User mockUser;

    private long mockUserId = 1L;

    @BeforeEach
    void init() {
        mockUser = new User();
        mockUser.setId(mockUserId);
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
    }

    @Test
    void createPost_shouldReturnPostResponse() {
        PostCreateRequestDto request = new PostCreateRequestDto();
        request.setText("Test post");
        request.setImages(List.of("img1.jpg", "img2.jpg"));

        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            savedPost.setCreatedDate(LocalDateTime.now());
            return savedPost;
        });

        PostResponseDto response = postService.createPost(mockUserId, request);

        assertNotNull(response);
        assertEquals("Test post", response.getText());
        assertEquals(2, response.getImages().size());
        assertEquals("John", response.getUser().getFirstName());

        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_shouldThrowNotFound_whenUserMissing() {
        PostCreateRequestDto request = new PostCreateRequestDto();
        request.setText("Should fail");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.createPost(999L, request));
    }

    @Test
    void updatePost_shouldReturnUpdatedPostResponse() {
        Post existingPost = new Post();
        existingPost.setId(1L);
        existingPost.setText("Old description");
        existingPost.setUser(mockUser);

        PostUpdateRequestDto request = new PostUpdateRequestDto();
        request.setText("New description");
        request.setImages(List.of("new1.jpg", "new2.jpg"));

        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post saved = invocation.getArgument(0);
            saved.setCreatedDate(LocalDateTime.now());
            return saved;
        });

        PostResponseDto response = postService.updatePost(1L, request);

        assertNotNull(response);
        assertEquals("New description", response.getText());
        assertEquals(2, response.getImages().size());
        assertEquals("Doe", response.getUser().getLastName());

        verify(postRepository).save(any(Post.class));
    }

    @Test
    void updatePost_shouldThrowNotFound_whenPostMissing() {
        PostUpdateRequestDto request = new PostUpdateRequestDto();
        request.setText("Nothing to update");

        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.updatePost(999L, request));
    }

    @Test
    void deletePost_shouldCallRepositoryDelete_whenPostExists() {
        Post post = new Post();
        post.setId(1L);
        post.setUser(mockUser);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.deletePost(1L, mockUser.getId());

        verify(postRepository).delete(post);
    }

    @Test
    void deletePost_shouldThrowNotFound_whenPostMissing() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.deletePost(999L, 999L));
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
        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));
        when(likeRepository.findByUserIdAndPostId(mockUserId, 1L)).thenReturn(Optional.of(like));
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
    void testRepostSuccess() {
        Post originalPost = new Post();
        originalPost.setId(1L);
        originalPost.setUser(mockUser);

        User repostingUser = new User();
        repostingUser.setId(2L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(originalPost));
        when(userRepository.findById(2L)).thenReturn(Optional.of(repostingUser));
        when(repostRepository.findByUserIdAndPostId(2L, 1L)).thenReturn(Optional.empty());

        int repostCount = postService.repost(1L, 2L);

        assertEquals(1, repostCount);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void testRepost_AlreadyExists() {
        Post originalPost = new Post();
        originalPost.setId(1L);
        originalPost.setUser(mockUser);

        User repostingUser = new User();
        repostingUser.setId(2L);

        Repost repost = new Repost();
        repost.setPost(originalPost);
        repost.setUser(repostingUser);

        when(postRepository.findById(1L)).thenReturn(Optional.of(originalPost));
        when(userRepository.findById(2L)).thenReturn(Optional.of(repostingUser));
        when(repostRepository.findByUserIdAndPostId(2L, 1L)).thenReturn(Optional.of(repost));

        assertThrows(IllegalArgumentException.class, () -> postService.repost(1L, 2L));
    }

    @Test
    void testRepost_SameUser() {
        Post originalPost = new Post();
        originalPost.setId(1L);
        originalPost.setUser(mockUser);

        when(postRepository.findById(1L)).thenReturn(Optional.of(originalPost));
        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));

        assertThrows(IllegalArgumentException.class, () -> postService.repost(1L, mockUserId));
    }

    @Test
    void testRepost_NotFoundPost() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.repost(999L, 1L));
    }

    @Test
    void testRepost_NotFoundUser() {
        Post originalPost = new Post();
        originalPost.setId(1L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(originalPost));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.repost(1L, 999L));
    }

    @Test
    void createPost_shouldGiveBuzzStartedAchievement_whenFirstPost() {
        PostCreateRequestDto request = new PostCreateRequestDto();
        request.setText("First post");
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
        request.setText("Post 5");
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

    @Test
    void testGetUserPosts() {
        long userId = 1L;
        List<Post> posts = new ArrayList<>();

        Post post1 = new Post();
        post1.setId(1L);
        post1.setText("Post 1");
        post1.setUser(mockUser);
        post1.setCreatedDate(LocalDateTime.now().minusHours(2));
        posts.add(post1);

        Post post2 = new Post();
        post2.setId(2L);
        post2.setText("Post 2");
        post2.setUser(mockUser);
        post2.setCreatedDate(LocalDateTime.now());
        posts.add(post2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(postRepository.findAllByUserId(userId)).thenReturn(Optional.of(posts));

        List<PostResponseDto> response = postService.getUserPosts(userId);

        assertEquals(2, response.size());
        assertEquals("Post 2", response.get(0).getText());
        assertEquals("Post 1", response.get(1).getText());
    }

    @Test
    void testGetUserPosts_NotFoundUser() {
        long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.getUserPosts(userId));
    }

    @Test
    void testGetUserPosts_pagination() {
        long userId = 1L;
        List<Post> posts = new ArrayList<>();
        int page = 0;
        int size = 2;

        Post post1 = new Post();
        post1.setId(1L);
        post1.setText("Post 1");
        post1.setUser(mockUser);
        post1.setCreatedDate(LocalDateTime.now().minusHours(2));
        posts.add(post1);

        Post post2 = new Post();
        post2.setId(2L);
        post2.setText("Post 2");
        post2.setUser(mockUser);
        post2.setCreatedDate(LocalDateTime.now());
        posts.addFirst(post2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(postRepository.getCombinedPosts(userId, size, size * page)).thenReturn(posts);
        when(postRepository.countCombinedPosts(userId)).thenReturn((long) posts.size());

        PageResponseDto<PostResponseDto> response = postService.getUserPosts(userId, page, size);

        assertEquals(2, response.getSize());
        assertEquals("Post 2", response.getContent().get(0).getText());
        assertEquals("Post 1", response.getContent().get(1).getText());
    }

    @Test
    void testGetUserAndFriendsPosts() {
        Post post1 = new Post();
        post1.setId(1L);
        post1.setText("User Post");
        post1.setUser(mockUser);
        post1.setCreatedDate(LocalDateTime.now().minusHours(3));

        User friend = new User();
        friend.setId(2L);

        UserShortDto friendShort = new UserShortDto();
        friendShort.setId(friend.getId());

        Post post2 = new Post();
        post2.setId(2L);
        post2.setText("Friend Post");
        post2.setUser(friend);
        post2.setCreatedDate(LocalDateTime.now());

        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));
        when(postRepository.findAllByUserId(mockUserId)).thenReturn(Optional.of(List.of(post1)));
        when(friendService.getAllFriendUsers(mockUserId)).thenReturn(List.of(friendShort));
        when(postRepository.findAllByUserId(friend.getId())).thenReturn(Optional.of(List.of(post2)));

        List<PostResponseDto> response = postService.getUserAndFriendsPosts(mockUserId);

        assertEquals(2, response.size());
        assertEquals("Friend Post", response.get(0).getText());
        assertEquals("User Post", response.get(1).getText());
    }

    @Test
    void testGetUserAndFriendsPosts_NotFoundUser() {
        long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.getUserAndFriendsPosts(userId));
    }


    @Test
    void testGetUserAndFriendsPosts_pagination() {
        int page = 0;
        int size = 2;

        Post post1 = new Post();
        post1.setId(1L);
        post1.setText("User Post");
        post1.setUser(mockUser);
        post1.setCreatedDate(LocalDateTime.now().minusHours(3));

        User friend = new User();
        friend.setId(2L);

        UserShortDto friendShort = new UserShortDto();
        friendShort.setId(friend.getId());

        Post post2 = new Post();
        post2.setId(2L);
        post2.setText("Friend Post");
        post2.setUser(friend);
        post2.setCreatedDate(LocalDateTime.now());

        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));
        when(friendService.getAllFriendUsers(mockUserId)).thenReturn(List.of(friendShort));
        when(postRepository.getUserAndFriendsPosts(mockUserId, List.of(friend.getId()), size, size * page))
                .thenReturn(List.of(post2, post1));
        when(postRepository.countUserAndFriendsPosts(mockUserId, List.of(friend.getId())))
                .thenReturn(2L);

        PageResponseDto<PostResponseDto> response = postService.getUserAndFriendsPosts(mockUserId, page, size);

        assertEquals(2, response.getSize());
        assertEquals("Friend Post", response.getContent().get(0).getText());
        assertEquals("User Post", response.getContent().get(1).getText());
    }
}
