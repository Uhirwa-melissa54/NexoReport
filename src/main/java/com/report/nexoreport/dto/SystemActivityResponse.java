package com.report.nexoreport.dto;

import java.time.LocalDate;

public class SystemActivityResponse {
    private LocalDate date;
    private long resolvedCount;

    public SystemActivityResponse(LocalDate date, long resolvedCount) {
        this.date = date;
        this.resolvedCount = resolvedCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getResolvedCount() {
        return resolvedCount;
    }

    public void setResolvedCount(long resolvedCount) {
        this.resolvedCount = resolvedCount;
    }
}

