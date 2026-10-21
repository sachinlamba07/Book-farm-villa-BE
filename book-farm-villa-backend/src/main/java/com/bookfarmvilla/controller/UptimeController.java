package com.bookfarmvilla.controller;

import com.bookfarmvilla.service.UptimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UptimeController {

    private final UptimeService uptimeService;

    public UptimeController(UptimeService uptimeService) {
        this.uptimeService = uptimeService;
    }

    @GetMapping("/uptime")
    public ResponseEntity<UptimeResponse> uptime() {
        return ResponseEntity.ok(new UptimeResponse("UP", uptimeService.getUptimeSeconds()));
    }

    public record UptimeResponse(String status, long uptimeSeconds) {
    }
}
