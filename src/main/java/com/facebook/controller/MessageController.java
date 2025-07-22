package com.facebook.controller;

import com.facebook.annotation.CurrentUser;
import com.facebook.dto.*;
import com.facebook.service.MessageService;
import com.facebook.util.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages API", description = "Endpoints for message operations")
public class MessageController {
    private final MessageService messageService;

    @Operation(
            summary = "Create a message",
            description = "Create a new message",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Message created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MessageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request"
                    )
            }
    )
    @PostMapping("/create")
    public ResponseEntity<?> create(
            @Parameter(hidden = true) @CurrentUser UserAuthDto currentUser,
            @RequestBody @Valid MessageCreateRequest request
    ) {
        MessageResponse response = messageService.create(currentUser.getId(), request);
        return ResponseHandler.generateResponse(HttpStatus.CREATED, false, "Message created", response);
    }

    @Operation(
            summary = "Get messages with a friend",
            description = "Retrieve messages exchanged with a specific friend",
            parameters = {
                    @Parameter(name = "friendId", description = "ID of the friend to retrieve messages with", required = true),
                    @Parameter(name = "page", description = "Page number for pagination", required = false, example = "0"),
                    @Parameter(name = "size", description = "Number of messages per page", required = false, example = "20")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Messages retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "array", implementation = MessageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No user found with the given friend ID"
                    )
            }
    )
    @GetMapping("/{friendId}")
    public ResponseEntity<?> getMessagesWithFriend(
            @PathVariable Long friendId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @Parameter(hidden = true) @CurrentUser UserAuthDto currentUser
    ) {
        Page<MessageResponse> messages = messageService.getMessagesWithFriend(currentUser.getId(), friendId, page, size);
        PageResponseDto<MessageResponse> response = new PageResponseDto<>(messages);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Messages retrieved",
                response
        );
    }

    @Operation(
            summary = "Edit a message",
            description = "Edit an existing message",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Message updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MessageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "ID mismatch or invalid request"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Message not found"
                    )
            }
    )
    @PutMapping("/edit/{id}")
    public ResponseEntity<?> edit(@PathVariable Long id,
                                  @RequestBody @Valid MessageUpdateRequest request,
                                  @Parameter(hidden = true) @CurrentUser UserAuthDto currentUser) {

        // Передаємо userId та id з URL до сервісу
        MessageResponse response = messageService.update(currentUser.getId(), id, request);

        return ResponseHandler.generateResponse(HttpStatus.OK, false, "Message updated", response);
    }


    @Operation(
            summary = "Mark message as read",
            description = "Mark message as read by id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Message marked as read",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MessageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Message not found"
                    )
            }
    )
    @PutMapping("/read/{id}")
    public ResponseEntity<?> markRead(@PathVariable Long id) {
        MessageResponse response = messageService.read(id);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "Message marked as read", response);
    }

    @Operation(
            summary = "Delete a message",
            description = "Delete a message by id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Message deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Message not found"
                    )
            }
    )
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @Parameter(hidden = true) @CurrentUser UserAuthDto currentUser) {
        messageService.delete(id, currentUser.getId());

        return ResponseHandler.generateResponse(HttpStatus.OK, false, "Message deleted", null);
    }
}
