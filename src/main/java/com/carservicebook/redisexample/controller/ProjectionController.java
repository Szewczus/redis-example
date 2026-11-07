package com.carservicebook.redisexample.controller;

import com.carservicebook.redisexample.dto.ProjectionRequest;
import com.carservicebook.redisexample.dto.ProjectionResponse;
import com.carservicebook.redisexample.service.ProjectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class ProjectionController {
    private final ProjectionService service;

    public ProjectionController(ProjectionService service) {
        this.service = service;
    }

    @PostMapping("/projections")
    public ProjectionResponse projection(@RequestBody ProjectionRequest request) {
        return service.getProjection(request);
    }

    @GetMapping("/stats")
    public Map<String, Integer> stats() {
        return Map.of("computeCount", service.getComputeCount());
    }
}