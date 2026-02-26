package com.hxl.entity;

import java.time.LocalDateTime;

public class VisitLog {
    private Long id;
    private String clientIp;
    private LocalDateTime visitTime;

    // Constructors
    public VisitLog() {
    }

    public VisitLog(String clientIp) {
        this.clientIp = clientIp;
        this.visitTime = LocalDateTime.now();
    }

    // Getters and Setters (可以使用 Lombok @Data 简化)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public LocalDateTime getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(LocalDateTime visitTime) {
        this.visitTime = visitTime;
    }
}
