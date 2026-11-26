package com.rocky.portscanner.core;

import com.rocky.portscanner.config.ScannerConfig;
import com.rocky.portscanner.model.PortResult;
import com.rocky.portscanner.model.PortState;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Bounded-concurrency TCP connect scanner. A successful connect is classified
 * as OPEN; all other connection outcomes are conservatively classified NOT_OPEN.
 */
public final class PortScanner {
    private final int timeoutMillis;
    private final int workerCount;

    public PortScanner(int timeoutMillis, int workerCount) {
        if (timeoutMillis < 1) {
            throw new IllegalArgumentException("Timeout must be positive.");
        }
        if (workerCount < 1 || workerCount > ScannerConfig.MAX_THREADS) {
            throw new IllegalArgumentException("Worker count must be between 1 and "
                    + ScannerConfig.MAX_THREADS + ".");
        }
        this.timeoutMillis = timeoutMillis;
        this.workerCount = workerCount;
    }

    public List<PortResult> scan(final String host, List<Integer> ports)
            throws InterruptedException {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Host cannot be empty.");
        }
        if (ports == null || ports.isEmpty()
                || ports.size() > ScannerConfig.MAX_PORTS_PER_SCAN) {
            throw new IllegalArgumentException("Provide between 1 and 65535 ports.");
        }

        ExecutorService executor = Executors.newFixedThreadPool(workerCount);
        List<Future<PortResult>> futures = new ArrayList<Future<PortResult>>(ports.size());
        try {
            for (final Integer port : ports) {
                if (port == null || port < ScannerConfig.MIN_PORT
                        || port > ScannerConfig.MAX_PORT) {
                    throw new IllegalArgumentException("Port must be between 1 and 65535.");
                }
                futures.add(executor.submit(new Callable<PortResult>() {
                    @Override
                    public PortResult call() {
                        return checkPort(host, port);
                    }
                }));
            }

            List<PortResult> results = new ArrayList<PortResult>(futures.size());
            for (Future<PortResult> future : futures) {
                try {
                    results.add(future.get());
                } catch (ExecutionException ex) {
                    // A task-level failure should not abort the rest of the scan.
                    results.add(new PortResult(host, -1, PortState.NOT_OPEN, 0L));
                }
            }
            return Collections.unmodifiableList(results);
        } catch (InterruptedException ex) {
            for (Future<PortResult> future : futures) {
                future.cancel(true);
            }
            throw ex;
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(2, TimeUnit.SECONDS);
        }
    }

    private PortResult checkPort(String host, int port) {
        long start = System.nanoTime();
        PortState state = PortState.NOT_OPEN;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMillis);
            state = PortState.OPEN;
        } catch (IOException ignored) {
            // Refused, timed out, unreachable, and other failures are not called open.
        }
        long latency = (System.nanoTime() - start) / 1_000_000L;
        return new PortResult(host, port, state, latency);
    }
}
