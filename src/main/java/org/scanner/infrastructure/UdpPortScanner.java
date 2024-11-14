package org.scanner.infrastructure;

import org.scanner.domain.PortScanner;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class UdpPortScanner implements PortScanner {

    @Override
    public boolean isPortOpen(String host, int port) {
        try (DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.connect(new InetSocketAddress(host, port));
            return true;
        } catch (SocketException e) {
            return false;
        }
    }
}
