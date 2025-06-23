package com.facebook.controller;

import com.facebook.annotation.CurrentUser;
import com.facebook.dto.UserAuthDto;
import com.facebook.openapi.ErrorResponseWrapper;
import com.facebook.openapi.GroupResponseWrapper;
import com.facebook.openapi.StatisticResponseWrapper;
import com.facebook.service.StatisticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistic API", description = "Endpoints for statistic operations")
public class StatisticController {
    private final StatisticService statisticService;

    @Operation(
            summary = "Get Statistic for all Time",
            description = "Fetches all-time statistics for a user",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Statistic fetched successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = StatisticResponseWrapper.class
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
    @GetMapping("/get-statistic-for-all-time")
    public ResponseEntity<?> getAllTimeStatistic(@Parameter(hidden = true)
                                                 @CurrentUser UserAuthDto currentUser) {
        Long userId = currentUser.getId();
        log.info("Fetching statistic for user with ID: {}", userId);
        return statisticService.getAllTimeStatistic(userId);
    }

    @Operation(
            summary = "Get Statistic for Last Days",
            description = "Fetches statistics for the last specified number of days for a user",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Statistic fetched successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = StatisticResponseWrapper.class
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
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Bad request",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @GetMapping("/get-statistic-for-last-days/{days}")
    public ResponseEntity<?> getStatisticForLastDays(@PathVariable Long days,
                                                     @Parameter(hidden = true)
                                                     @CurrentUser UserAuthDto currentUser) {
        Long userId = currentUser.getId();
        log.info("Fetching statistic for user with ID: {} for last {} days", userId, days);
        return statisticService.getStatisticForLastDays(userId, days);
    }
}
