# Rocky Port Scanner

A lightweight, multithreaded TCP connect port scanner built with **Java 8**.

Use it only against systems you own or are explicitly authorized to assess.

## Requirements

- JDK 8 or newer
- Maven 3.6+

## Build

```bash
mvn clean package
```

## Run

Interactive mode:

```bash
java -jar target/portscanner-1.0.0.jar
```

Direct mode:

```bash
java -jar target/portscanner-1.0.0.jar localhost 1-1024
```

Optional arguments:

```text
java -jar target/portscanner-1.0.0.jar <host> <ports> [timeout-ms] [threads]
```

Examples:

- `localhost 1-1024`
- `127.0.0.1 22,80,443,8080`
- `localhost 1-65535 500 100`

The scanner reports ports that accept a TCP connection. A timeout or failed connection is reported as not open; this does **not** prove that a port is filtered.

## Features

- TCP connect checks using the standard Java socket API
- Single ports, comma-separated ports, and inclusive ranges
- Bounded worker pool and configurable socket timeout
- Input validation and graceful interruption
- Console summary with elapsed time
- No privileged raw sockets or packet crafting

## Project layout

```text
src/main/java/com/rocky/portscanner/
  Main.java
  config/ScannerConfig.java
  core/PortScanner.java
  model/PortResult.java
  model/PortState.java
  output/ConsoleReporter.java
  util/PortParser.java
src/test/java/com/rocky/portscanner/util/PortParserTest.java
```

## Safety

Only scan hosts and networks for which you have explicit permission. Keep scan rates reasonable and follow the target system owner's rules.

## License

MIT
