package com.example.controller;

import com.example.service.DemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DemoController {

    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from VictoriaMetrics Demo!");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/data")
    public Map<String, Object> getData() {
        Map<String, Object> response = new HashMap<>();
        response.put("data", demoService.fetchData());
        response.put("status", "success");
        return response;
    }

    @PostMapping("/process")
    public Map<String, Object> processData(@RequestBody Map<String, String> request) {
        String value = request.getOrDefault("value", "");
        Map<String, Object> response = new HashMap<>();
        response.put("input", value);
        response.put("result", demoService.processData(value));
        response.put("status", "processed");
        return response;
    }

    @GetMapping("/slow-operation")
    public Map<String, Object> slowOperation() {
        demoService.slowOperation(2000);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Operation completed");
        response.put("duration_ms", 2000);
        return response;
    }

    @GetMapping("/error")
    public Map<String, Object> errorEndpoint() {
        throw new RuntimeException("Simulated error for testing");
    }

    @GetMapping("/health/custom")
    public Map<String, String> customHealth() {
        return Map.of("status", "UP", "service", "victoria-metrics-demo");
    }
}
