package com.facebook.service;

import com.facebook.dto.*;
import com.facebook.exception.NotFoundException;
import com.facebook.model.*;
import com.facebook.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final RepostRepository repostRepository;
    private final FriendService friendService;
    private final UserAchievementService userAchievementService;

    public PostResponseDto createPost(Long userId, PostCreateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Post post = new Post();
        post.setUser(user);
        post.setText(request.getText());
        request.getImages().forEach(imageUrl -> {
            PostImage postImage = new PostImage();
            postImage.setUrl(imageUrl);
            postImage.setPost(post);
            post.getImages().add(postImage);
        });

        Post savedPost = postRepository.save(post);
        Optional<List<Post>> optionalPosts = postRepository.findAllByUserId(userId);
        List<Post> posts = optionalPosts.orElse(List.of());
        int postCount = posts.size();

        String firstPostAchievement = "Buzz Started";
        String fivePostsWithPhotosAchievement = "Aesthetic Drop";

        if (postCount == 1 && !userAchievementService.userHaveAchievement(user, firstPostAchievement)) {
            userAchievementService.awardAchievement(user, firstPostAchievement);
        }

        if (postCount >= 1) {
            long postsWithImages = posts.stream().filter(somePost-> somePost.getImages()!= null && !post.getImages().isEmpty())
                    .count();
            if (postsWithImages == 5  && !userAchievementService.userHaveAchievement(user, fivePostsWithPhotosAchievement)) {
                userAchievementService.awardAchievement(user, fivePostsWithPhotosAchievement);
            }
        }


        UserShortDto userDTO = new UserShortDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatarUrl(),
                user.getBirthdate()
        );
        List<String> images = savedPost.getImages().stream()
                .map(PostImage::getUrl)
                .toList();

        return new PostResponseDto(
                savedPost.getId(),
                userDTO,
                savedPost.getText(),
                images,
                savedPost.getCreatedDate(),
                0,
                0,
                0
        );
    }

    public PostResponseDto updatePost(long postId, PostUpdateRequestDto request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with ID: " + postId));

        if (request.getText() != null) {
            post.setText(request.getText());
        }

        request.getImages().forEach(imageUrl -> {
            PostImage postImage = new PostImage();
            postImage.setUrl(imageUrl);
            postImage.setPost(post);
            post.getImages().add(postImage);
        });

        Post updatedPost = postRepository.save(post);

        User user = updatedPost.getUser();
        UserShortDto userDTO = new UserShortDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatarUrl(),
                user.getBirthdate()
        );
        List<String> images = updatedPost.getImages().stream()
                .map(PostImage::getUrl)
                .toList();

        return new PostResponseDto(
                updatedPost.getId(),
                userDTO,
                updatedPost.getText(),
                images,
                updatedPost.getCreatedDate(),
                updatedPost.getLikes().size(),
                updatedPost.getComments().size(),
                updatedPost.getReposts().size()
        );
    }

    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with ID: " + postId));

        User user = post.getUser();

        if (!user.getId().equals(userId)) {
            repostRepository.findByUserIdAndPostId(userId, postId).ifPresent(repostRepository::delete);

            return;
        }

        postRepository.delete(post);
    }

    public int likePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with ID: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Like like = likeRepository.findByUserIdAndPostId(userId, postId)
                .orElse(null);

        if (like != null) {
            post.getLikes().remove(like);
        } else {
            Like likeNew = new Like();
            likeNew.setPost(post);
            likeNew.setUser(user);

            post.getLikes().add(likeNew);
        }

        postRepository.save(post);

        return post.getLikes().size();
    }

    public int repost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with ID: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User cannot repost their own post");
        }

        Repost repost = repostRepository.findByUserIdAndPostId(userId, postId)
                .orElse(null);

        if (repost != null) {
            throw new IllegalArgumentException("User has already reposted this post");
        } else {
            repost = new Repost();

            repost.setPost(post);
            repost.setUser(user);

            post.getReposts().add(repost);
        }

        postRepository.save(post);

        return post.getReposts().size();
    }

    public List<PostResponseDto> getUserPosts(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        List<Post> posts = new ArrayList<>(postRepository.findAllByUserId(userId).orElse(List.of()));
        List<Repost> reposts = repostRepository.findAllByUserId(userId).orElse(List.of());

        if (!reposts.isEmpty()) {
            posts.addAll(reposts.stream().map(Repost::getPost).toList());
        }

        return posts.stream()
                .sorted((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()))
                .map(post -> {
                    List<String> images = post.getImages().stream()
                            .map(PostImage::getUrl)
                            .toList();

                    return new PostResponseDto(
                            post.getId(),
                            new UserShortDto(
                                    user.getId(),
                                    user.getFirstName(),
                                    user.getLastName(),
                                    user.getAvatarUrl(),
                                    user.getBirthdate()
                            ),
                            post.getText(),
                            images,
                            post.getCreatedDate(),
                            post.getLikes().size(),
                            post.getComments().size(),
                            post.getReposts().size()
                    );
                })
                .toList();
    }

    public PageResponseDto<PostResponseDto> getUserPosts(long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        int offset = page * size;

        List<Post> posts = postRepository.getCombinedPosts(userId, size, offset);
        long totalElements = postRepository.countCombinedPosts(userId);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean last = page >= totalPages - 1;

        List<PostResponseDto> postsResponse = posts.stream()
                .map(post -> {
                    User userPost = post.getUser();
                    List<String> images = post.getImages().stream()
                            .map(PostImage::getUrl)
                            .toList();

                    return new PostResponseDto(
                            post.getId(),
                            new UserShortDto(
                                    userPost.getId(),
                                    userPost.getFirstName(),
                                    userPost.getLastName(),
                                    userPost.getAvatarUrl(),
                                    userPost.getBirthdate()
                            ),
                            post.getText(),
                            images,
                            post.getCreatedDate(),
                            post.getLikes().size(),
                            post.getComments().size(),
                            post.getReposts().size()
                    );
                })
                .toList();

        PageResponseDto<PostResponseDto> response = new PageResponseDto<>();
        response.setContent(postsResponse);
        response.setNumber(page);
        response.setSize(size);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setLast(last);

        return response;
    }

    public List<PostResponseDto> getUserAndFriendsPosts(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        List<Post> posts = new ArrayList<>(postRepository.findAllByUserId(userId).orElse(List.of()));

        List<UserShortDto> friends = friendService.getAllFriendUsers(userId);

        for (UserShortDto friend : friends) {
            List<Post> friendPosts = new ArrayList<>(postRepository.findAllByUserId(friend.getId()).orElse(List.of()));
            posts.addAll(friendPosts);
        }

        return posts.stream()
                .sorted((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()))
                .map(post -> {
                    User userPost  = post.getUser();

                    List<String> images = post.getImages().stream()
                            .map(PostImage::getUrl)
                            .toList();

                    return new PostResponseDto(
                            post.getId(),
                            new UserShortDto(
                                    userPost.getId(),
                                    userPost.getFirstName(),
                                    userPost.getLastName(),
                                    userPost.getAvatarUrl(),
                                    userPost.getBirthdate()
                            ),
                            post.getText(),
                            images,
                            post.getCreatedDate(),
                            post.getLikes().size(),
                            post.getComments().size(),
                            post.getReposts().size()
                    );
                })
                .toList();
    }

    public PageResponseDto<PostResponseDto> getUserAndFriendsPosts(long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        List<UserShortDto> friends = friendService.getAllFriendUsers(userId);
        List<Long> friendsIds = friends.stream().map(UserShortDto::getId).toList();

        if (friendsIds.isEmpty()) {
            friendsIds = List.of(-1L); // ID, якого точно немає в базі
        }

        int offset = page * size;

        List<Post> posts = postRepository.getUserAndFriendsPosts(userId, friendsIds, size, offset);
        long totalElements = postRepository.countUserAndFriendsPosts(userId, friendsIds);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean last = page >= totalPages - 1;

        List<PostResponseDto> postsResponse = posts.stream()
                .map(post -> {
                    User postUser = post.getUser();
                    List<String> images = post.getImages().stream()
                            .map(PostImage::getUrl)
                            .toList();

                    return new PostResponseDto(
                            post.getId(),
                            new UserShortDto(
                                    postUser.getId(),
                                    postUser.getFirstName(),
                                    postUser.getLastName(),
                                    postUser.getAvatarUrl(),
                                    postUser.getBirthdate()
                            ),
                            post.getText(),
                            images,
                            post.getCreatedDate(),
                            post.getLikes().size(),
                            post.getComments().size(),
                            post.getReposts().size()
                    );
                })
                .toList();

        PageResponseDto<PostResponseDto> response = new PageResponseDto<>();
        response.setContent(postsResponse);
        response.setNumber(page);
        response.setSize(size);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setLast(last);

        return response;
    }
}
