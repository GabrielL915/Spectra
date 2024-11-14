package org.scanner.domain;

public interface PortScanner {
    boolean isPortOpen(String host, int port);
}
