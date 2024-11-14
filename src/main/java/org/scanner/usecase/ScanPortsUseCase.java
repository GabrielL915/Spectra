package org.scanner.usecase;

import org.scanner.domain.PortScanner;
import org.scanner.domain.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class ScanPortsUseCase {

    Logger logger = LoggerFactory.getLogger(ScanPortsUseCase.class);

    private final PortScanner portScanner;

    public ScanPortsUseCase(PortScanner portScanner) {
        this.portScanner = portScanner;
    }

    public List<ScanResult> executePortScan(String host, int startPort, int endPort, int threadCount) {

        List<ScanResult> scanResults = new CopyOnWriteArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<ScanResult> resolvedHostNameResults = validateAndResolveHostName(host, scanResults);
        if (resolvedHostNameResults != null) return resolvedHostNameResults;

        try {
            List<CompletableFuture<Void>> scanTasks = scanPortRangeAsync(host, startPort, endPort, scanResults, executor);
            CompletableFuture.allOf(scanTasks.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            logger.error("Erro ao escanear portas: {}", e.getMessage());
        } finally {
            executor.shutdown();
        }

        return new ArrayList<>(scanResults);
    }

    private List<CompletableFuture<Void>> scanPortRangeAsync(String host, int startPort, int endPort,
                                                             List<ScanResult> scanResults, ExecutorService executor) {

        return IntStream.rangeClosed(startPort, endPort)
                .mapToObj(port -> CompletableFuture.runAsync(() -> {
                    boolean isOpen = portScanner.isPortOpen(host, port);
                    scanResults.add(new ScanResult(port, isOpen));
                }, executor))
                .toList();
    }

    private List<ScanResult> validateAndResolveHostName(String host, List<ScanResult> scanResults) {

        String resolvedIP;
        try {
            InetAddress inetAddress = InetAddress.getByName(host);
            resolvedIP = inetAddress.getHostAddress();
            logger.info("Hostname {} resolvido para IP: {}", host, resolvedIP);
        } catch (UnknownHostException e) {
            logger.error("Hostname inválido: {}", host);
            return scanResults;
        }
        return null;
    }
}
