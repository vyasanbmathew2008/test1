package com.rocky.portscanner;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

final class PortScanner {
    interface Listener {
        void onProgress(int done, int total);
        void onPort(int port, boolean open, String service);
    }

    private static final String[] NAMES = {
        "FTP", "SSH", "Telnet", "SMTP", "DNS", "HTTP", "POP3", "NTP",
        "IMAP", "HTTPS", "SMB", "RDP", "MySQL", "PostgreSQL", "VNC",
        "HTTP-alt", "HTTPS-alt"
    };
    private static final int[] PORTS = {
        21,22,23,25,53,80,110,123,143,443,445,3389,3306,5432,5900,8080,8443
    };

    static String serviceName(int port) {
        for (int i = 0; i < PORTS.length; i++) if (PORTS[i] == port) return NAMES[i];
        return "Unknown";
    }

    static List<Integer> parsePorts(String input) {
        if (input == null || input.trim().isEmpty()) throw new IllegalArgumentException("Enter at least one port.");
        List<Integer> out = new ArrayList<>();
        String[] parts = input.split(",");
        for (String raw : parts) {
            String token = raw.trim();
            if (token.isEmpty()) throw new IllegalArgumentException("Invalid empty port entry.");
            int dash = token.indexOf('-');
            if (dash >= 0) {
                if (dash != token.lastIndexOf('-')) throw new IllegalArgumentException("Invalid range: " + token);
                int start = parseOne(token.substring(0, dash));
                int end = parseOne(token.substring(dash + 1));
                if (end < start) throw new IllegalArgumentException("Range end must be >= start.");
                if (end - start > 4095) throw new IllegalArgumentException("A single range is limited to 4096 ports.");
                for (int p = start; p <= end; p++) out.add(p);
            } else out.add(parseOne(token));
            if (out.size() > 4096) throw new IllegalArgumentException("Maximum 4096 ports per scan.");
        }
        return new ArrayList<>(new java.util.LinkedHashSet<>(out));
    }

    private static int parseOne(String s) {
        try {
            int p = Integer.parseInt(s.trim());
            if (p < 1 || p > 65535) throw new NumberFormatException();
            return p;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ports must be numbers from 1 to 65535.");
        }
    }

    static void scan(String host, List<Integer> ports, int timeoutMs, Listener listener) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(32);
        try {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (final int port : ports) {
                futures.add(pool.submit(new Callable<Boolean>() {
                    @Override public Boolean call() {
                        boolean open = false;
                        try (Socket socket = new Socket()) {
                            socket.connect(new InetSocketAddress(host, port), timeoutMs);
                            open = true;
                        } catch (Exception ignored) { }
                        return open;
                    }
                }));
            }
            int done = 0;
            for (int i = 0; i < ports.size(); i++) {
                if (Thread.currentThread().isInterrupted()) {
                    for (Future<Boolean> future : futures) future.cancel(true);
                    throw new InterruptedException("Scan cancelled");
                }
                boolean open;
                try { open = futures.get(i).get(); }
                catch (Exception e) { open = false; }
                int port = ports.get(i);
                listener.onPort(port, open, serviceName(port));
                listener.onProgress(++done, ports.size());
            }
        } finally {
            pool.shutdownNow();
            pool.awaitTermination(1, TimeUnit.SECONDS);
        }
    }

    static List<Integer> commonPorts() {
        List<Integer> p = new ArrayList<>();
        for (int value : PORTS) p.add(value);
        return Collections.unmodifiableList(p);
    }
}
