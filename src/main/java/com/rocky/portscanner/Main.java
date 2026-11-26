package com.rocky.portscanner;

import com.rocky.portscanner.config.ScannerConfig;
import com.rocky.portscanner.core.PortScanner;
import com.rocky.portscanner.model.PortResult;
import com.rocky.portscanner.output.ConsoleReporter;
import com.rocky.portscanner.util.PortParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Command-line entry point for the Rocky TCP Port Scanner.
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        printBanner();
        try {
            if (args.length >= 2) {
                runScan(args[0], args[1],
                        args.length > 2 ? parsePositive(args[2], "timeout") : ScannerConfig.DEFAULT_TIMEOUT,
                        args.length > 3 ? parsePositive(args[3], "thread count") : ScannerConfig.DEFAULT_THREADS);
            } else if (args.length == 0) {
                interactiveMode();
            } else {
                printUsage();
            }
        } catch (IllegalArgumentException ex) {
            System.err.println("[!] " + ex.getMessage());
            printUsage();
            System.exit(2);
        } catch (IOException ex) {
            System.err.println("[!] Input error: " + ex.getMessage());
            System.exit(1);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            System.err.println("[!] Scan interrupted.");
            System.exit(130);
        }
    }

    private static void interactiveMode() throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Target host: ");
        String host = reader.readLine();
        System.out.print("Ports (e.g. 1-1024 or 22,80,443): ");
        String ports = reader.readLine();
        System.out.print("Timeout in ms [" + ScannerConfig.DEFAULT_TIMEOUT + "]: ");
        String timeoutText = reader.readLine();
        System.out.print("Worker threads [" + ScannerConfig.DEFAULT_THREADS + "]: ");
        String threadsText = reader.readLine();

        int timeout = timeoutText == null || timeoutText.trim().isEmpty()
                ? ScannerConfig.DEFAULT_TIMEOUT : parsePositive(timeoutText, "timeout");
        int threads = threadsText == null || threadsText.trim().isEmpty()
                ? ScannerConfig.DEFAULT_THREADS : parsePositive(threadsText, "thread count");

        runScan(host, ports, timeout, threads);
    }

    private static void runScan(String host, String portSpec, int timeout, int threads)
            throws InterruptedException {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Target host cannot be empty.");
        }
        List<Integer> ports = PortParser.parse(portSpec);
        System.out.println();
        System.out.println("[*] Target: " + host.trim());
        System.out.println("[*] Ports: " + ports.size());
        System.out.println("[*] Timeout: " + timeout + " ms | Workers: " + threads);
        System.out.println("[*] TCP connect scan started. Scan only authorized systems.");
        System.out.println();

        long start = System.nanoTime();
        PortScanner scanner = new PortScanner(timeout, threads);
        List<PortResult> results = scanner.scan(host.trim(), ports);
        long elapsed = (System.nanoTime() - start) / 1_000_000L;
        ConsoleReporter.print(results, elapsed);
    }

    private static int parsePositive(String value, String label) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 1) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a positive integer.");
        }
    }

    private static void printBanner() {
        System.out.println("========================================");
        System.out.println("          ROCKY PORT SCANNER");
        System.out.println("             Java Edition");
        System.out.println("========================================");
    }

    private static void printUsage() {
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -jar portscanner-1.0.0.jar");
        System.out.println("  java -jar portscanner-1.0.0.jar <host> <ports> [timeout-ms] [threads]");
        System.out.println("Examples:");
        System.out.println("  java -jar portscanner-1.0.0.jar localhost 1-1024");
        System.out.println("  java -jar portscanner-1.0.0.jar 127.0.0.1 22,80,443 750 40");
    }
}
