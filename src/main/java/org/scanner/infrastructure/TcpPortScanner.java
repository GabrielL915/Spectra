package org.scanner.infrastructure;

import org.scanner.domain.PortScanner;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpPortScanner implements PortScanner {

    @Override
    public boolean isPortOpen(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 100);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
