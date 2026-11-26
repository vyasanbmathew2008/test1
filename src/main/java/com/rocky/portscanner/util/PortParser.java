package com.rocky.portscanner.util;

import com.rocky.portscanner.config.ScannerConfig;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Parses comma-separated ports and inclusive ranges, preserving input order. */
public final class PortParser {
    private PortParser() {
    }

    public static List<Integer> parse(String specification) {
        if (specification == null || specification.trim().isEmpty()) {
            throw new IllegalArgumentException("Port specification cannot be empty.");
        }

        Set<Integer> ports = new LinkedHashSet<Integer>();
        String[] tokens = specification.split(",");
        for (String rawToken : tokens) {
            String token = rawToken.trim();
            if (token.isEmpty()) {
                throw new IllegalArgumentException("Empty port entry in specification.");
            }

            int separator = token.indexOf('-');
            if (separator >= 0) {
                if (separator != token.lastIndexOf('-')) {
                    throw new IllegalArgumentException("Invalid port range: " + token);
                }
                int first = parsePort(token.substring(0, separator).trim());
                int last = parsePort(token.substring(separator + 1).trim());
                if (first > last) {
                    throw new IllegalArgumentException("Range start exceeds end: " + token);
                }
                for (int port = first; port <= last; port++) {
                    ports.add(port);
                }
            } else {
                ports.add(parsePort(token));
            }

            if (ports.size() > ScannerConfig.MAX_PORTS_PER_SCAN) {
                throw new IllegalArgumentException("Too many ports requested.");
            }
        }
        return new ArrayList<Integer>(ports);
    }

    private static int parsePort(String value) {
        try {
            int port = Integer.parseInt(value);
            if (port < ScannerConfig.MIN_PORT || port > ScannerConfig.MAX_PORT) {
                throw new IllegalArgumentException("Port out of range: " + value);
            }
            return port;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid port: " + value);
        }
    }
}
