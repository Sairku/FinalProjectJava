package com.facebook.controller;

import com.facebook.annotation.CurrentUser;
import com.facebook.dto.*;
import com.facebook.openapi.CommentResponseWrapper;
import com.facebook.openapi.ErrorResponseWrapper;
import com.facebook.openapi.NotFoundResponseWrapper;
import com.facebook.service.PostService;
import com.facebook.util.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts API", description = "Endpoints for post operations")
public class PostController {

    private final PostService postService;

    @Operation(
            summary = "Create a new post",
            description = "Creates a new post for the current user",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Post was created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "object", example = """
                                        {
                                          "error": false,
                                          "message": "Post was created",
                                          "data": {
                                            "description": "Test post",
                                            "images": ["http://image.com/test.jpg"]
                                          }
                                        }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation failed",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )

            }
    )

    @PostMapping("/create")
    public ResponseEntity<?> createPost(
            @RequestBody @Valid PostCreateRequestDto request,
            @Parameter(hidden = true) @CurrentUser UserAuthDto currentUser
    ) {
        Long currentUserId = currentUser.getId();
        PostResponseDto response = postService.createPost(currentUserId, request);

        return ResponseHandler.generateResponse(HttpStatus.CREATED, false, "Post was created", response);
    }

    @Operation(
            summary = "Update a post",
            description = "Updates an existing post by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Post was updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "object", example = """
                                        {
                                          "error": false,
                                          "message": "Post was updated",
                                          "data": {
                                            "description": "Updated description",
                                            "images": ["http://image.com/updated.jpg"]
                                          }
                                        }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation failed",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestBody @Valid PostUpdateRequestDto request
    ) {
        PostResponseDto response = postService.updatePost(id, request);

        return ResponseHandler.generateResponse(HttpStatus.OK, false, "Post was updated", response);
    }

    @Operation(
            summary = "Delete a post",
            description = "Deletes a post by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Post was deleted successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "object", example = """
                                        {
                                          "error": false,
                                          "message": "Post was deleted",
                                          "data": null
                                        }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Post not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "object", example = """
                                        {
                                          "error": true,
                                          "message": "Post not found",
                                          "data": null
                                        }
                                    """)
                            )
                    )
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "Post was deleted", null);
    }

    @Operation(
            summary = "Like a post",
            description = "Likes a post by ID for the current user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Post liked successfully, returning likes count",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "object", example = """
                                        {
                                          "error": false,
                                          "message": "Post liked successfully",
                                          "data": 10
                                        }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Post not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = NotFoundResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(
            @PathVariable Long id,
            @Parameter(hidden = true) @CurrentUser UserAuthDto currentUser
    ) {
        int likesCount = postService.likePost(id, currentUser.getId());

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Post liked successfully",
                likesCount
        );
    }

    @Operation(
            summary = "Add a comment to a post",
            description = "Adds a comment to a post by ID for the current user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "A comment was added successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = CommentResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Post not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = NotFoundResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> commentPost(
            @PathVariable Long id,
            @RequestBody @Valid CommentRequestDto request,
            @Parameter(hidden = true) @CurrentUser UserAuthDto currentUser
    ) {
        CommentResponseDto commentNew = postService.addComment(id, currentUser.getId(), request.getText());

        return ResponseHandler.generateResponse(
                HttpStatus.CREATED,
                false,
                "Comment added successfully",
                commentNew
        );
    }

    @Operation(
            summary = "Get comments for a post",
            description = "Retrieves all comments for a post by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Comments retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "array", implementation = CommentResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Post not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = NotFoundResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getPostComments(
            @PathVariable Long id
    ) {
        List<CommentResponseDto> comments = postService.getComments(id);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Comments retrieved successfully",
                comments
        );
    }

    @Operation(
            summary = "Delete a comment from a post",
            description = "Deletes a comment from a post by ID for the current user",
            parameters = {
                    @Parameter(name = "id", description = "ID of the post"),
                    @Parameter(name = "commentId", description = "ID of the comment to delete")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Comment deleted successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "object", example = """
                                        {
                                          "error": false,
                                          "message": "Comment deleted successfully",
                                          "data": null
                                        }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Post or comment not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = NotFoundResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "User don't have permission to delete this comment or comment does not belong to the post",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            @Parameter(hidden = true) @CurrentUser UserAuthDto currentUser
    ) {
        postService.deleteComment(id, currentUser.getId(), commentId);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Comment deleted successfully",
                null
        );
    }
}
