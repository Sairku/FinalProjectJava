package com.facebook.controller;

import com.facebook.annotation.CurrentUser;
import com.facebook.dto.PostCreateRequest;
import com.facebook.dto.PostResponse;
import com.facebook.dto.PostUpdateRequest;
import com.facebook.dto.UserAuthDto;
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
                    )
            }
    )
    @PostMapping("/create")
    public ResponseEntity<?> createPost(
            @RequestBody @Valid PostCreateRequest request,
            @Parameter(hidden = true) @CurrentUser UserAuthDto currentUser
    ) {
        Long currentUserId = currentUser.getId();
        PostResponse response = postService.createPost(currentUserId, request);

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
                    )
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestBody @Valid PostUpdateRequest request
    ) {
        PostResponse response = postService.updatePost(id, request);

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
}
