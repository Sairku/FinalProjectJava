package com.facebook.controller;

import com.facebook.dto.*;
import com.facebook.enums.GroupJoinStatus;
import com.facebook.service.GroupService;
import com.facebook.util.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/groups")
@AllArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<?> createGroup(@Validated @RequestBody GroupCreateRequest groupCreateRequest) {
        GroupResponse groupResponse = groupService.create(groupCreateRequest);
        return ResponseHandler.generateResponse(HttpStatus.CREATED, false, "Group was created", groupResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGroup(@PathVariable Long id,
                                          @Validated @RequestBody GroupUpdateRequest updateRequest) {
        GroupResponse response = groupService.update(id, updateRequest);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "Group was updated", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long id,
                                         @Validated @RequestBody GroupDeleteRequest deleteRequest) {
        groupService.delete(id, deleteRequest);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "Group was deleted", null);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addUserToGroup(@Validated @RequestBody GroupAddRequest addRequest) {
        groupService.addUserToGroup(addRequest);
        boolean privateCondition = groupService.findById(addRequest.getGroupId()).isPrivate();
        String message;
        if (privateCondition)
            message =  "Request for adding to the group " + addRequest.getGroupId()+ " was sent from user with Id "+addRequest.getUserId();
        else
            message = "User with Id "+addRequest.getUserId() +" was added to the group "+addRequest.getGroupId();
        return ResponseHandler.generateResponse(HttpStatus.OK, false, message, null);
    }

    @PutMapping("/respond")
    public ResponseEntity<?> respondToGroup(@Validated @RequestBody GroupRespondRequest respondRequest) {
        groupService.respondToAddingRequest(respondRequest);
        boolean status = respondRequest.getStatus().equals(GroupJoinStatus.APPROVED);
        String message = status ?
                "User with Id "+respondRequest.getUserId() +" was added to the group " + respondRequest.getGroupId() :
                "User with Id "+respondRequest.getUserId() +" was rejected to join to the group " + respondRequest.getGroupId();
        return ResponseHandler.generateResponse(HttpStatus.OK, false, message, null);
    }
}
