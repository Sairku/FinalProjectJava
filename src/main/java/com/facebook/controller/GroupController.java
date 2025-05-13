package com.facebook.controller;

import com.facebook.dto.GroupCreateRequest;
import com.facebook.dto.GroupUpdateRequest;
import com.facebook.dto.GroupResponse;
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
    public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
        groupService.delete(id);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "Group was deleted", null);
    }
}
