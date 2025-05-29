package com.facebook.controller;

import com.facebook.dto.MessageCreateRequest;
import com.facebook.dto.MessageResponse;
import com.facebook.dto.MessageUpdateRequest;
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
    public ResponseEntity<?> create(@RequestBody @Valid MessageCreateRequest request) {
        MessageResponse response = messageService.create(request);
        return ResponseHandler.generateResponse(HttpStatus.CREATED, false, "Message created", response);
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
    public ResponseEntity<?> edit(@PathVariable Long id, @RequestBody @Valid MessageUpdateRequest request) {
        if (!id.equals(request.getId())) {
            return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, true, "ID mismatch", null);
        }
        MessageResponse response = messageService.update(request);
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
    public ResponseEntity<?> delete(@PathVariable Long id) {
        messageService.delete(id);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "Message deleted", null);
    }
}
