package com.carservicebook.redisexample.dto;

public record ProjectionResponse(
        String source,
        String cacheKey,
        long tookMs,
        String result
) {
}
