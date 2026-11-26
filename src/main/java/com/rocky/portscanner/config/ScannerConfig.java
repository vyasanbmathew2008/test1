package com.rocky.portscanner.config;

/** Central defaults and safety limits for the scanner. */
public final class ScannerConfig {
    public static final int MIN_PORT = 1;
    public static final int MAX_PORT = 65535;
    public static final int DEFAULT_TIMEOUT = 1000;
    public static final int DEFAULT_THREADS = 50;
    public static final int MAX_THREADS = 200;
    public static final int MAX_PORTS_PER_SCAN = 65535;

    private ScannerConfig() {
        throw new AssertionError("No instances");
    }
}
