package com.rocky.portscanner.output;

import com.rocky.portscanner.model.PortResult;
import com.rocky.portscanner.model.PortState;

import java.util.List;

/** Formats scan results for terminal output. */
public final class ConsoleReporter {
    private ConsoleReporter() {
    }

    public static void print(List<PortResult> results, long elapsedMillis) {
        int open = 0;
        System.out.println(String.format("%-22s %-10s %-12s", "HOST", "PORT", "STATE"));
        System.out.println("------------------------------------------------");
        for (PortResult result : results) {
            if (result.getState() == PortState.OPEN) {
                open++;
                System.out.println(String.format("%-22s %-10d %-12s (%d ms)",
                        result.getHost(), result.getPort(), "OPEN",
                        result.getLatencyMillis()));
            }
        }
        System.out.println("------------------------------------------------");
        System.out.println("Scanned: " + results.size()
                + " | Open: " + open
                + " | Not open: " + (results.size() - open)
                + " | Time: " + elapsedMillis + " ms");
        if (open == 0) {
            System.out.println("No open TCP ports found in the selected range.");
        }
        System.out.println("Note: NOT_OPEN does not distinguish closed from filtered.");
    }
}
