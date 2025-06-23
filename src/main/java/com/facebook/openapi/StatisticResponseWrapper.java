package com.facebook.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;

public class StatisticResponseWrapper {
    @Schema(example = "false")
    public boolean error = false;

    @Schema(example = "Statistic data retrieved successfully")
    public String message;

    public HashMap<String, Long> data;
}
