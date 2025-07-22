package com.facebook.controller;

import com.facebook.service.CloudinaryService;
import com.facebook.util.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("/api/images")
@Tag(name = "Image API", description = "Endpoints for images")
public class ImageController {
    private CloudinaryService cloudinaryService;

    @Operation(
            summary = "Upload an image",
            description = "Uploads an image to Cloudinary and returns the image URL.",
            parameters = {
                    @Parameter(name = "image", description = "Image file to upload", required = true)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Image uploaded successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            type = "object",
                                            example = """
                                                        {
                                                          "error": false,
                                                          "message": "Image uploaded successfully",
                                                          "data": ""http://example.com/image.jpg"
                                                        }
                                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "File must not be empty"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "File size must not exceed 5 MB"
                    )
            }
    )
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile file) {
        String imageUrl = cloudinaryService.uploadImage(file);

        return ResponseHandler.generateResponse(
                HttpStatus.CREATED,
                false,
                "Image uploaded successfully",
                imageUrl
        );
    }
}
