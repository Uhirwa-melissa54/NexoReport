package com.report.nexoreport.dto;

public class PrioritySummaryResponse {
    private long priorityUnresolvedTotal;
    private long overduePriorityTotal;

    public PrioritySummaryResponse(long priorityUnresolvedTotal, long overduePriorityTotal) {
        this.priorityUnresolvedTotal = priorityUnresolvedTotal;
        this.overduePriorityTotal = overduePriorityTotal;
    }

    public long getPriorityUnresolvedTotal() {
        return priorityUnresolvedTotal;
    }

    public void setPriorityUnresolvedTotal(long priorityUnresolvedTotal) {
        this.priorityUnresolvedTotal = priorityUnresolvedTotal;
    }

    public long getOverduePriorityTotal() {
        return overduePriorityTotal;
    }

    public void setOverduePriorityTotal(long overduePriorityTotal) {
        this.overduePriorityTotal = overduePriorityTotal;
    }
}

