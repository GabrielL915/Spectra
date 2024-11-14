package org.scanner.usecase;

import org.scanner.domain.PortScanner;
import org.scanner.domain.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public List<ScanResult> scanPorts(String host, int startPort, int endPort, int threads) {
        List<ScanResult> results = new CopyOnWriteArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        try {
            List<CompletableFuture<Void>> futures = IntStream.rangeClosed(startPort, endPort)
                    .mapToObj(port -> CompletableFuture.runAsync(() -> {
                        boolean isOpen = portScanner.isPortOpen(host, port);
                        results.add(new ScanResult(port, isOpen));
                    }, executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } catch (Exception e) {
            logger.error("Erro ao escanear portas: {}", e.getMessage());

        } finally {
            executor.shutdown();
        }

        return new ArrayList<>(results);
    }
}
