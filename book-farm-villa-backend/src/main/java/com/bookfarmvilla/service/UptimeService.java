package com.bookfarmvilla.service;

import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;

@Service
public class UptimeService {

    public long getUptimeSeconds() {
        return ManagementFactory.getRuntimeMXBean().getUptime() / 1_000;
    }
}
