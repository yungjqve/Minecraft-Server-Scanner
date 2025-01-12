package dev.jqve.serverscanner.screens;

import dev.jqve.serverscanner.ServerScanner;
import dev.jqve.serverscanner.mixin.MultiplayerScreenInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PortScannerScreen extends Screen {
    private static final Logger log = LogManager.getLogger(PortScannerScreen.class);
    private TextFieldWidget ipTextField;
    List<Integer> openPortList = new ArrayList<>();
    private final List<ServerInfo> foundServers = new ArrayList<>();

    public PortScannerScreen() {
        super(Text.literal("Port Scanner"));
    }

    @Override
    protected void init() {
        // Initialize components

        this.ipTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 20, 200, 20, Text.literal("Enter IP"));
        this.addDrawableChild(ipTextField);
        this.addDrawableChild(ButtonWidget.builder(Text.of("Scan open Ports"), (button) -> {
            String ip = ipTextField.getText();
            try {
                scanPorts(ip);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).width(100).position(this.width / 2 - 50, 50).build());
    }

    private boolean validateIp(String ip) {
        return ip.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
    }

    private void scanPorts(String ip) throws IOException {
        if (!validateIp(ip)) {
            return;
        }

        ConcurrentLinkedQueue<Integer> openPorts = new ConcurrentLinkedQueue<>();
        try (ExecutorService executorService = Executors.newFixedThreadPool(50)) {
            AtomicInteger port = new AtomicInteger(25565);
            while (port.get() < 65535) {
                final int currentPort = port.getAndIncrement();
                executorService.submit(() -> {
                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(ip, currentPort), 100);
                        socket.close();
                        openPorts.add(currentPort);
                        ServerScanner.LOGGER.info("{} ,port open: {}", ip, currentPort);
                    } catch (IOException e) {
                        log.error("e: ", e);
                    }
                });
            }
            executorService.shutdown();
            if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                log.warn("ExecutorService did not terminate in the specified time.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ServerScanner.LOGGER.info("openPortsQueue: {}", openPorts.size());
        while (!openPorts.isEmpty()) {
            openPortList.add(openPorts.poll());
        }
        openPortList.forEach(p -> ServerScanner.LOGGER.info("port {} is open", p));
        openPortList.forEach(p -> foundServers.add(new ServerInfo(ip + ":" + p, ip + ":" + p, ServerInfo.ServerType.LAN)));

        updateServerList();
    }

    private void updateServerList() {
        int y = 80;

        // Clear previous server list on the screen but keep the other components
        this.children().removeIf(child -> child instanceof ButtonWidget && ((ButtonWidget) child).getMessage().getString().startsWith("Server #"));

        for (ServerInfo server : foundServers) {
            this.addDrawableChild(ButtonWidget.builder(Text.of(server.name), (button) -> {
                // Add server to the server list
                addServerToList(server);
            }).width(200).position(this.width / 2 - 100, y).build());
            y += 30;
        }
    }

    private void addServerToList(ServerInfo server) {
        MinecraftClient client = MinecraftClient.getInstance();
        MultiplayerScreen multiplayerScreen = new MultiplayerScreen(this);
        multiplayerScreen.init(client, this.width, this.height); // Initialize the screen

        MultiplayerScreenInvoker invoker = (MultiplayerScreenInvoker) multiplayerScreen;

        // Manually load the server list file
        ServerList serverList = new ServerList(client);
        serverList.loadFile();
        invoker.setServerList(serverList);

        invoker.setSelectedEntry(server);
        invoker.invokeAddEntry(true);

        // Return to the server scanner screen with the updated server list after adding the server
        client.setScreen(this);
        foundServers.remove(server);
        updateServerList();
    }
}