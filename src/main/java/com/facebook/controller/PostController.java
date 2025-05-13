package com.facebook.controller;


import com.facebook.dto.PostCreateRequest;
import com.facebook.dto.PostResponse;
import com.facebook.dto.PostUpdateRequest;
import com.facebook.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<PostResponse> createPost(@RequestBody @Valid PostCreateRequest request) {
        PostResponse response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @RequestBody @Valid PostUpdateRequest request
    ) {
        if (!id.equals(request.getId())) {
            return ResponseEntity.badRequest().build();
        }

        PostResponse response = postService.updatePost(request);
        return ResponseEntity.ok(response);
    }
}
