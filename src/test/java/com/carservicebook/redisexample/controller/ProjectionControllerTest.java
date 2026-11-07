package com.carservicebook.redisexample.controller;

import com.carservicebook.cache.CacheKeyBuilder;
import com.carservicebook.redisexample.dto.ProjectionRequest;
import com.carservicebook.redisexample.service.ProjectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties = "warmup.enabled=false")
class ProjectionControllerTest {

    @Autowired
    ProjectionService service;
    @Autowired
    StringRedisTemplate redis;

    @BeforeEach
    void clean() {
        redis.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    void secondCallIsServedFromCache() {
        var req = new ProjectionRequest("1.0", "hgihui", List.of("test", "test2"));
        int before = service.getComputeCount();

        var first = service.getProjection(req);
        var second = service.getProjection(req);

        assertEquals("MISS", first.source());
        assertEquals("HIT", second.source());
        assertEquals(before + 1, service.getComputeCount());
    }

    @Test
    void assetOrderDoesNotChangeKey() {
        var a = new ProjectionRequest("1.0", "789-697-ab8", List.of("tees", "test2"));
        var b = new ProjectionRequest("1.0", "789-697-ab8", List.of("test2", "tees"));

        assertEquals(CacheKeyBuilder.build(a.version(), a.list(), a.processId()), CacheKeyBuilder.build(b.version(), b.list(), b.processId()));
    }

    @Test
    void newSnapshotVersionMeansNewKey() {
        var a = new ProjectionRequest("1.0", "89000", List.of("test"));
        var b = new ProjectionRequest("1.0", "798678", List.of("test"));

        assertNotEquals(CacheKeyBuilder.build(a.version(), a.list(), a.processId()), CacheKeyBuilder.build(b.version(), b.list(), b.processId()));
    }
}