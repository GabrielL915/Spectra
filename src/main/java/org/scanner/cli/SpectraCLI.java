package org.scanner.cli;

import org.scanner.domain.PortScanner;
import org.scanner.domain.ScanResult;
import org.scanner.infrastructure.TcpPortScanner;
import org.scanner.infrastructure.UdpPortScanner;
import org.scanner.usecase.ScanPortsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

@Command(name = "spt", description = "Scanner de portas TCP e UDP", mixinStandardHelpOptions = true)
public class SpectraCLI implements Runnable {

    Logger logger = LoggerFactory.getLogger(SpectraCLI.class);

    @Option(names = {"-h", "--host"}, description = "IP ou hostname do alvo", required = true)
    private String host;

    @Option(names = {"-sp", "--start-port"}, description = "Porta inicial para o escaneamento", defaultValue = "1")
    private int startPort;

    @Option(names = {"-ep", "--end-port"}, description = "Porta final para o escaneamento", defaultValue = "1024")
    private int endPort;

    @Option(names = {"-t", "--threads"}, description = "Número de threads para o escaneamento", defaultValue = "10")
    private int threads;

    @Option(names = {"-p", "--protocol"}, description = "Protocolo (TCP ou UDP)", defaultValue = "TCP")
    private String protocol;

    @Override
    public void run() {

        PortScanner portScanner;

        if (protocol.equalsIgnoreCase("TCP")) {
            portScanner = new TcpPortScanner();
        } else if (protocol.equalsIgnoreCase("UDP")) {
            portScanner = new UdpPortScanner();
        } else {
            logger.error("Protocolo invalido: {}", protocol);
            return;
        }

        ScanPortsUseCase useCase = new ScanPortsUseCase(portScanner);
        List<ScanResult> results = useCase.executePortScan(host, startPort, endPort, threads);

        results.stream()
                .filter(ScanResult::isOpen)
                .forEach(result ->
                        logger.info("Porta {} esta aberta", result.port()));

        logger.info("Escaneamento concluido.");
    }

    public static void main(String[] args) {

        int exitCode = new CommandLine(new SpectraCLI()).execute(args);
        System.exit(exitCode);
    }
}