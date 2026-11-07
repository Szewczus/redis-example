package com.carservicebook.redisexample;

import com.carservicebook.redisexample.dto.ProjectionRequest;
import com.carservicebook.redisexample.service.ProjectionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@ConditionalOnProperty(name = "warmup.enabled", havingValue = "true", matchIfMissing = true)
public class CacheWarmup {
    private final ProjectionService service;

    public CacheWarmup(ProjectionService service) {
        this.service = service;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        CompletableFuture.runAsync(this::warmUp);
    }

    public void warmUp() {
        List<ProjectionRequest> popular = List.of(
                new ProjectionRequest("1.0", "653454", List.of("test1", "test2")),
                new ProjectionRequest("1.0", "653453", List.of("test3", "test5")),
                new ProjectionRequest("1.0", "653456", List.of("test6", "test7")),
                new ProjectionRequest("1.0", "653451", List.of("test8", "test9"))
        );
        popular.forEach(service::getProjection);
    }
}
