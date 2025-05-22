package com.facebook.service;

import com.facebook.dto.*;
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
        // post.setImgUrl(request.getImgUrl());
        post.setCreatedDate(LocalDateTime.now());

        Post savedPost = postRepository.save(post);

        UserShortDto userDTO = new UserShortDto(user.getId(), user.getFirstName(), user.getLastName());

        return new PostResponse(userDTO, savedPost.getDescription(), null, savedPost.getCreatedDate());
    }

    public PostResponse updatePost(PostUpdateRequest request) {
        Post post = postRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        if (request.getDescription() != null) {
            post.setDescription(request.getDescription());
        }

//        if (request.getImgUrl() != null) {
//            post.setImgUrl(request.getImgUrl());
//        }

        Post updatedPost = postRepository.save(post);

        User user = updatedPost.getUser();
        UserShortDto userDTO = new UserShortDto(user.getId(), user.getFirstName(), user.getLastName());

        return new PostResponse(
                userDTO,
                updatedPost.getDescription(),
                null, //updatedPost.getImgUrl(),
                updatedPost.getCreatedDate()
        );
    }
}
