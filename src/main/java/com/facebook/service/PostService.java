package com.facebook.service;

import com.facebook.dto.*;
import com.facebook.exception.NotFoundException;
import com.facebook.model.Post;
import com.facebook.model.PostImage;
import com.facebook.model.User;
import com.facebook.repository.PostRepository;
import com.facebook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostResponse createPost(Long userId, PostCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Post post = new Post();
        post.setUser(user);
        post.setDescription(request.getDescription());
        request.getImages().forEach(imageUrl -> {
            PostImage postImage = new PostImage();
            postImage.setUrl(imageUrl);
            postImage.setPost(post);
            post.getImages().add(postImage);
        });

        Post savedPost = postRepository.save(post);

        UserShortDto userDTO = new UserShortDto(user.getId(), user.getFirstName(), user.getLastName());
        List<String> images = savedPost.getImages().stream()
                .map(PostImage::getUrl)
                .toList();

        return new PostResponse(userDTO, savedPost.getDescription(), images, savedPost.getCreatedDate());
    }

    public PostResponse updatePost(long postId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with ID: " + postId));

        if (request.getDescription() != null) {
            post.setDescription(request.getDescription());
        }

        request.getImages().forEach(imageUrl -> {
            PostImage postImage = new PostImage();
            postImage.setUrl(imageUrl);
            postImage.setPost(post);
            post.getImages().add(postImage);
        });

        Post updatedPost = postRepository.save(post);

        User user = updatedPost.getUser();
        UserShortDto userDTO = new UserShortDto(user.getId(), user.getFirstName(), user.getLastName());
        List<String> images = updatedPost.getImages().stream()
                .map(PostImage::getUrl)
                .toList();

        return new PostResponse(
                userDTO,
                updatedPost.getDescription(),
                images,
                updatedPost.getCreatedDate()
        );
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with ID: " + postId));

        postRepository.delete(post);
    }

    public List<PostResponse> getAllPostsOfUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        List<Post> posts = postRepository.findAllByUserId(userId)
                .orElseThrow(() -> new NotFoundException("No posts found for user with ID: " + userId));

        UserShortDto userDTO = new UserShortDto(user.getId(), user.getFirstName(), user.getLastName());

        return posts.stream()
                .map(post -> {
                    List<String> images = post.getImages().stream()
                            .map(PostImage::getUrl)
                            .toList();
                    return new PostResponse(
                            userDTO,
                            post.getDescription(),
                            images,
                            post.getCreatedDate()
                    );
                })
                .toList();
    }

}
