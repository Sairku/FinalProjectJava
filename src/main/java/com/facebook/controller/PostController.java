package com.facebook.controller;

import com.facebook.dto.PostCreateRequest;
import com.facebook.dto.PostResponse;
import com.facebook.dto.PostUpdateRequest;
import com.facebook.service.PostService;
import com.facebook.util.ResponseHandler;
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

    @PostMapping("/create")
    public ResponseEntity<?> createPost(@RequestBody @Valid PostCreateRequest request) {
        PostResponse response = postService.createPost(request);

        return ResponseHandler.generateResponse(HttpStatus.CREATED, false, "Post was created", response);

    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestBody @Valid PostUpdateRequest request
    ) {
        PostResponse response = postService.updatePost(id, request);

        return ResponseHandler.generateResponse(HttpStatus.OK, false, "Post was updated", response);
    }
}
