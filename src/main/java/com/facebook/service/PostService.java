package com.facebook.service;

import com.facebook.dto.*;
import com.facebook.exception.NotFoundException;
import com.facebook.model.*;
import com.facebook.repository.CommentRepository;
import com.facebook.repository.LikeRepository;
import com.facebook.repository.PostRepository;
import com.facebook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public PostResponseDto createPost(Long userId, PostCreateRequestDto request) {
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

        return new PostResponseDto(
                userDTO,
                savedPost.getDescription(),
                images,
                savedPost.getCreatedDate(),
                0,
                0
        );
    }

    public PostResponseDto updatePost(long postId, PostUpdateRequestDto request) {
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

        return new PostResponseDto(
                userDTO,
                updatedPost.getDescription(),
                images,
                updatedPost.getCreatedDate(),
                updatedPost.getLikes().size(),
                updatedPost.getComments().size()
        );
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with ID: " + postId));

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

    public CommentResponseDto addComment(Long postId, Long userId, String text) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with ID: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setText(text);

        post.getComments().add(comment);
        postRepository.save(post);

        return new CommentResponseDto(
                comment.getId(),
                new UserShortDto(user.getId(), user.getFirstName(), user.getLastName()),
                comment.getText(),
                comment.getCreatedDate()
        );
    }

    public List<CommentResponseDto> getComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with ID: " + postId));

        if (post.getComments().isEmpty()) {
            return new ArrayList<>();
        }

        return post.getComments().stream()
                .map(comment -> {
                    User user = comment.getUser();

                    return new CommentResponseDto(
                            comment.getId(),
                            new UserShortDto(user.getId(), user.getFirstName(), user.getLastName()),
                            comment.getText(),
                            comment.getCreatedDate()
                    );
                })
                .toList();
    }

    public void deleteComment(Long postId, Long userId, Long commentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with ID: " + postId));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Not found comment with ID: " + commentId));

        if (!post.getComments().contains(comment)) {
            throw new IllegalArgumentException("Comment does not belong to the post");
        }

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User does not have permission to delete this comment");
        }

        post.getComments().remove(comment);
        commentRepository.delete(comment);
    }
}
