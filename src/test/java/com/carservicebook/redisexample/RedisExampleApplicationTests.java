package com.carservicebook.redisexample;

import com.carservicebook.redisexample.dto.ProjectionRequest;
import com.carservicebook.redisexample.dto.ProjectionResponse;
import com.carservicebook.redisexample.service.ProjectionService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "warmup.enabled=false")
class RedisExampleApplicationTests {

    @Autowired
    private ProjectionService service;

    @Test
    void contextLoads() throws ExecutionException, InterruptedException {
        var req = new ProjectionRequest("1.0", "8293489", List.of("test"));
        int before = service.getComputeCount();

        ExecutorService pool = Executors.newFixedThreadPool(10);
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Future<ProjectionResponse>> futures = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            futures.add(pool.submit(() -> {
                startSignal.await();
                return service.getProjection(req);
            }));
        }

        startSignal.countDown();
        for (Future<ProjectionResponse> f : futures) {
            f.get();
        }
        pool.shutdown();

        assertEquals(before + 1, service.getComputeCount());
    }

}
