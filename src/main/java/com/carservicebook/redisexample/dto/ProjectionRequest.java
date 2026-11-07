package com.carservicebook.redisexample.dto;

import java.util.List;

public record ProjectionRequest(
        String version,
        String processId,
        List<String> list
) {
}
