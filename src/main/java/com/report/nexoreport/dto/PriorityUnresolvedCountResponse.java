package com.report.nexoreport.dto;

public class PriorityUnresolvedCountResponse {
    private long priorityUnresolvedTotal;

    public PriorityUnresolvedCountResponse(long priorityUnresolvedTotal) {
        this.priorityUnresolvedTotal = priorityUnresolvedTotal;
    }

    public long getPriorityUnresolvedTotal() {
        return priorityUnresolvedTotal;
    }

    public void setPriorityUnresolvedTotal(long priorityUnresolvedTotal) {
        this.priorityUnresolvedTotal = priorityUnresolvedTotal;
    }
}

