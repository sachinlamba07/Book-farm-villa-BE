package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.response.AdminStatsResponse;
import com.bookfarmvilla.service.AdminStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    public AdminStatsController(AdminStatsService adminStatsService) {
        this.adminStatsService = adminStatsService;
    }

    @GetMapping
    public ResponseEntity<AdminStatsResponse> stats() {
        return ResponseEntity.ok(adminStatsService.getStats());
    }
}