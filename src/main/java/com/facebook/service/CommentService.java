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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

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

    public void updateComment(Long commentId, Long userId, String newText) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Not found comment with ID: " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User does not have permission to update this comment");
        }

        comment.setText(newText);
        commentRepository.save(comment);
    }

    public List<CommentResponseDto> getPostComments(Long postId) {
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

    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Not found comment with ID: " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User does not have permission to delete this comment");
        }

        commentRepository.delete(comment);
    }
}
