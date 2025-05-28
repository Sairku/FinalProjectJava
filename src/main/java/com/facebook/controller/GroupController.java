package com.facebook.controller;

import com.facebook.annotation.CurrentUser;
import com.facebook.dto.*;
import com.facebook.enums.GroupJoinStatus;
import com.facebook.openapi.*;
import com.facebook.service.GroupService;
import com.facebook.util.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/groups")
@AllArgsConstructor
@Tag(name = "Groups API", description = "Endpoints for group operations")
public class GroupController {

    private final GroupService groupService;

    @Operation(
            summary = "Create Group",
            description = "Create a new group",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "New group was created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = GroupResponseWrapper.class
                                    )
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
    @PostMapping
    public ResponseEntity<?> createGroup(
            @RequestBody @Valid GroupCreateRequest groupCreateRequest,
            @Parameter(hidden = true)
            @CurrentUser UserAuthDto currentUser
    ) {
        long userId = currentUser.getId();
        GroupResponse groupResponse = groupService.create(userId, groupCreateRequest);

        return ResponseHandler.generateResponse(HttpStatus.CREATED, false, "Group was created", groupResponse);
    }

    @Operation(
            summary = "Group Update",
            description = "Update an existing group",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Group was updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = GroupResponseWrapper.class
                                    )
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
    public ResponseEntity<?> updateGroup(
            @PathVariable Long id,
            @RequestBody @Valid GroupUpdateRequest updateRequest,
            @Parameter(hidden = true)
            @CurrentUser UserAuthDto currentUser
    ) {
        long userId = currentUser.getId();
        GroupResponse response = groupService.update(id, updateRequest, userId);

        return ResponseHandler.generateResponse(HttpStatus.OK, false, "Group was updated", response);
    }

    @Operation(
            summary = "Group Deleting",
            description = "Delete an existing group",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Group was updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = VoidSuccessResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Group not found or you are not the owner of this group",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(
            @PathVariable Long id,
            @Parameter(hidden = true)
            @CurrentUser UserAuthDto currentUser
    ) {
        long userId = currentUser.getId();
        groupService.delete(id, userId);

        return ResponseHandler.generateResponse(HttpStatus.OK, false, "Group was deleted", null);
    }

    @Operation(
            summary = "Join Group",
            description = "Join an existing group",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User was added to the group or request was sent (if the group is private)",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = VoidSuccessResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Group not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = NotFoundResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "You are already a member of this group",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @PostMapping("/{id}/join")
    public ResponseEntity<?> addUserToGroup(
            @PathVariable Long id,
            @Parameter(hidden = true)
            @CurrentUser UserAuthDto currentUser
    ) {
        long userId = currentUser.getId();

        groupService.addUserToGroup(id, userId, userId);

        boolean privateCondition = groupService.findById(id).isPrivate();
        String message = privateCondition ?
                "Request for adding to the group " + id + " was sent from user with Id " + userId :
                "User with Id " + userId + " was added to the group " + id;

        return ResponseHandler.generateResponse(HttpStatus.OK, false, message, null);
    }

    @Operation(
            summary = "Adding a user to a group",
            description = "Adding a user to a group by another user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Request for adding to the group was sent successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = VoidSuccessResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Group not found or user not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = NotFoundResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data or user is already a member of the group",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @PostMapping("/{id}/members")
    public ResponseEntity<?> requestToGroup(
            @PathVariable Long id,
            @RequestBody @Valid GroupMemberRequest addRequest,
            @Parameter(hidden = true)
            @CurrentUser UserAuthDto currentUser
    ) {
        long currentUserId = currentUser.getId();

        groupService.addUserToGroup(id, addRequest.getUserId(), currentUserId);

        String message = "Request for adding to the group " + id + " was sent from user with Id " + addRequest.getUserId();

        return ResponseHandler.generateResponse(HttpStatus.OK, false, message, null);
    }

    @Operation(
            summary = "Update Group Member",
            description = "Update the status of a group member",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User was added to the group or request was rejected",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = VoidSuccessResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Group not found or user not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = NotFoundResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid status for group member request. Status must be APPROVED or REJECTED",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @PutMapping("/{id}/members")
    public ResponseEntity<?> updateGroupMember(
            @PathVariable Long id,
            @RequestBody @Valid GroupMemberRequest request
    ) {
        GroupJoinStatus status;

        try {
            status = GroupJoinStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            return ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "Invalid status for group member request. Status must be APPROVED or REJECTED",
                    null
            );
        }

        if (status.equals(GroupJoinStatus.PENDING)) {
            return ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "Invalid status for group member request. Status must be APPROVED or REJECTED",
                    null
            );
        }

        groupService.respondToAddingRequest(id, request);

        String message = status.equals(GroupJoinStatus.APPROVED) ?
                "User with Id " + request.getUserId() + " was added to the group " + id :
                "User with Id " + request.getUserId() + " was rejected to join to the group " + id;

        return ResponseHandler.generateResponse(HttpStatus.OK, false, message, null);
    }
}
