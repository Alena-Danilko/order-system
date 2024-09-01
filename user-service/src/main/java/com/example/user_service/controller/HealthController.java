package com.example.user_service.controller;

import com.example.user_service.model.Health;
import com.example.user_service.model.HealthStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class HealthController {
    private final Logger log = LoggerFactory.getLogger(HealthController.class);

    @GetMapping(value = "/health", produces = "application/json")
    public ResponseEntity<Health> getHealth() {
        log.debug("REST request to get the Health Status");
        final Health health = new Health();
        health.setStatus(HealthStatus.UP);

        return ResponseEntity.ok().body(health);
    }
}
