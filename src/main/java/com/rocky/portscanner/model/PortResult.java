package com.rocky.portscanner.model;

/** Immutable result for one target TCP port. */
public final class PortResult {
    private final String host;
    private final int port;
    private final PortState state;
    private final long latencyMillis;

    public PortResult(String host, int port, PortState state, long latencyMillis) {
        this.host = host;
        this.port = port;
        this.state = state;
        this.latencyMillis = latencyMillis;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public PortState getState() {
        return state;
    }

    public long getLatencyMillis() {
        return latencyMillis;
    }
}
