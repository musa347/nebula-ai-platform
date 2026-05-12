package com.aiagent.orchestrator.retry;

public class RetryPolicy {

    private final int maxRetries;
    private final long retryDelayMs;
    private final long maxExecutionDurationMs;

    private RetryPolicy(Builder builder) {
        this.maxRetries = builder.maxRetries;
        this.retryDelayMs = builder.retryDelayMs;
        this.maxExecutionDurationMs = builder.maxExecutionDurationMs;
    }

    public static RetryPolicy defaultPolicy() {
        return new Builder().build();
    }

    public int getMaxRetries() { return maxRetries; }
    public long getRetryDelayMs() { return retryDelayMs; }
    public long getMaxExecutionDurationMs() { return maxExecutionDurationMs; }

    public long backoffDelayMs(int attempt) {
        return retryDelayMs * (1L << attempt); // 2s → 4s → 8s
    }

    public static class Builder {
        private int maxRetries = 3;
        private long retryDelayMs = 2000;
        private long maxExecutionDurationMs = 600_000; // 10 minutes

        public Builder maxRetries(int maxRetries) { this.maxRetries = maxRetries; return this; }
        public Builder retryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; return this; }
        public Builder maxExecutionDurationMs(long ms) { this.maxExecutionDurationMs = ms; return this; }
        public RetryPolicy build() { return new RetryPolicy(this); }
    }
}
