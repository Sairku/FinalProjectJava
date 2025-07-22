package com.facebook.controller;

import com.facebook.annotation.CurrentUser;
import com.facebook.dto.*;
import com.facebook.openapi.CommentResponseWrapper;
import com.facebook.openapi.ErrorResponseWrapper;
import com.facebook.openapi.NotFoundResponseWrapper;
import com.facebook.service.CommentService;
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
    private final CommentService commentService;

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
                                                    "text": "Test post",
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
            summary = "Delete user post or repost",
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
    public ResponseEntity<?> deletePost(
            @PathVariable Long id,
            @Parameter(hidden = true) @CurrentUser UserAuthDto currentUser
    ) {
        long currentUserId = currentUser.getId();

        postService.deletePost(id, currentUserId);

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
            summary = "Repost a post",
            description = "Reposts a post by ID for the current user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Post reposted successfully, returning reposts count",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "object", example = """
                                                {
                                                  "error": false,
                                                  "message": "Post reposted successfully",
                                                  "data": 5
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
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "User cannot repost their own post or has already reposted it",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @PostMapping("/{id}/repost")
    public ResponseEntity<?> repost(
            @PathVariable Long id,
            @Parameter(hidden = true) @CurrentUser UserAuthDto currentUser
    ) {
        int repostsCount = postService.repost(id, currentUser.getId());

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Post reposted successfully",
                repostsCount
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
    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getComments(
            @PathVariable Long postId
    ) {
        List<CommentResponseDto> comments = commentService.getPostComments(postId);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Comments retrieved successfully",
                comments
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
    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long postId,
            @RequestBody @Valid CommentRequestDto request,
            @Parameter(hidden = true) @CurrentUser UserAuthDto currentUser
    ) {
        CommentResponseDto commentNew = commentService.addComment(postId, currentUser.getId(), request.getText());

        return ResponseHandler.generateResponse(
                HttpStatus.CREATED,
                false,
                "Comment added successfully",
                commentNew
        );
    }

    @Operation(
            summary = "Get all posts of user and friends (for Homepage)",
            description = "Retrieves all posts of the current user and their friends",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Posts retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            type = "object",
                                            example = """
                                                        {
                                                          "error": false,
                                                          "message": "User posts retrieved successfully",
                                                          "data": [
                                                            {
                                                              "user": {
                                                                    "id": 1,
                                                                    "firstName": "John",
                                                                    "lastName": "Doe"
                                                                },
                                                                "text": "Hello World!",
                                                                "images": ["http://example.com/image.jpg"],
                                                                "createdDate": "2023-10-01T12:00:00Z",
                                                                "likesCount": 10,
                                                                "commentsCount": 5,
                                                                "repostsCount": 2
                                                            }
                                                          ]
                                                        }
                                                    """)
                            )
                    )
            }
    )
    @GetMapping()
    public ResponseEntity<?> getAllPosts(
            @Parameter(hidden = true) @CurrentUser UserAuthDto currentUser
    ) {
        List<PostResponseDto> posts = postService.getUserAndFriendsPosts(currentUser.getId());

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Posts retrieved successfully",
                posts
        );
    }

    @Operation(
            summary = "Get all posts of user and friends (with pagination)",
            description = "Retrieves all posts of the current user and their friends",
            parameters = {
                    @Parameter(name = "page", description = "Page number (default is 0)"),
                    @Parameter(name = "size", description = "Number of users per page (default is 20)")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Posts retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            type = "object",
                                            example = """
                                                        {
                                                          "error": false,
                                                          "message": "User posts retrieved successfully",
                                                          "data": {
                                                            "content": [
                                                            {
                                                              "user": {
                                                                    "id": 1,
                                                                    "firstName": "John",
                                                                    "lastName": "Doe"
                                                                },
                                                                "text": "Hello World!",
                                                                "images": ["http://example.com/image.jpg"],
                                                                "createdDate": "2023-10-01T12:00:00Z",
                                                                "likesCount": 10,
                                                                "commentsCount": 5,
                                                                "repostsCount": 2
                                                            }
                                                          ],
                                                            "totalElements": 1,
                                                            "totalPages": 1,
                                                            "size": 10,
                                                            "number": 0
                                                          }
                                                        }
                                                    """)
                            )
                    )
            }
    )
    @GetMapping("/pagination")
    public ResponseEntity<?> getAllPostsPagination(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @Parameter(hidden = true) @CurrentUser UserAuthDto currentUser
    ) {
        PageResponseDto<PostResponseDto> posts = postService.getUserAndFriendsPosts(currentUser.getId(), page, size);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Posts retrieved successfully",
                posts
        );
    }
}
