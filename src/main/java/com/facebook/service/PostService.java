package com.facebook.service;

import com.facebook.dto.PostCreateRequest;
import com.facebook.dto.PostResponse;
import com.facebook.dto.PostUpdateRequest;
import com.facebook.dto.UserPostDTO;
import com.facebook.model.Post;
import com.facebook.model.User;
import com.facebook.repository.PostRepository;
import com.facebook.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostResponse createPost(PostCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Post post = new Post();
        post.setUser(user);
        post.setDescription(request.getDescription());
        post.setImgUrl(request.getImgUrl());
        post.setCreatedDate(LocalDateTime.now());

        Post savedPost = postRepository.save(post);

        UserPostDTO userDTO = new UserPostDTO(user.getId(), user.getFirstName(), user.getLastName());

        return new PostResponse(userDTO, savedPost.getDescription(), savedPost.getImgUrl(), savedPost.getCreatedDate());
    }

    public PostResponse updatePost(PostUpdateRequest request) {
        Post post = postRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        if (request.getDescription() != null) {
            post.setDescription(request.getDescription());
        }

        if (request.getImgUrl() != null) {
            post.setImgUrl(request.getImgUrl());
        }

        Post updatedPost = postRepository.save(post);

        User user = updatedPost.getUser();
        UserPostDTO userDTO = new UserPostDTO(user.getId(), user.getFirstName(), user.getLastName());

        return new PostResponse(
                userDTO,
                updatedPost.getDescription(),
                updatedPost.getImgUrl(),
                updatedPost.getCreatedDate()
        );
    }
}
