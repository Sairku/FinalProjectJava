package com.facebook.controller;

import com.facebook.annotation.CurrentUser;
import com.facebook.dto.CommentRequestDto;
import com.facebook.dto.UserAuthDto;
import com.facebook.openapi.ErrorResponseWrapper;
import com.facebook.openapi.NotFoundResponseWrapper;
import com.facebook.openapi.VoidSuccessResponseWrapper;
import com.facebook.service.CommentService;
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
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comments API", description = "Endpoints for comment operations")
public class CommentController {
    private final CommentService commentService;

    @Operation(
            summary = "Update a comment",
            description = "Updates a comment by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Comment updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = VoidSuccessResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Comment not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = NotFoundResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long commentId,
            @RequestBody @Valid CommentRequestDto request,
            @Parameter(hidden = true) @CurrentUser UserAuthDto currentUser
    ) {
        commentService.updateComment(
                commentId,
                currentUser.getId(),
                request.getText()
        );

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Comment updated successfully",
                null
        );
    }

    @Operation(
            summary = "Delete a comment",
            description = "Deletes a comment by ID for the current user",
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
                            description = "Comment not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = NotFoundResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "User don't have permission to delete this comment",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @CurrentUser UserAuthDto currentUser
    ) {
        commentService.deleteComment(commentId, currentUser.getId());

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Comment deleted successfully",
                null
        );
    }
}
