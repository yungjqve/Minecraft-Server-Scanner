package dev.jqve.serverscanner.screens;

import dev.jqve.serverscanner.mixin.MultiplayerScreenInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerScannerScreen extends Screen {
    private TextFieldWidget ipTextField;
    private List<ServerInfo> foundServers = new ArrayList<>();
    private String savedIpText = "";

    public ServerScannerScreen(Screen parent) {
        super(Text.of("Server Scanner"));
    }

    @Override
    protected void init() {
        // Save the current state
        if (this.ipTextField != null) {
            savedIpText = this.ipTextField.getText();
        }
        List<ServerInfo> savedFoundServers = new ArrayList<>(foundServers);

        // Initialize components
        this.ipTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 20, 200, 20, Text.of("Enter IP"));
        this.addDrawableChild(ipTextField);

        this.addDrawableChild(ButtonWidget.builder(Text.of("Scan"), (button) -> {
            String ip = ipTextField.getText();
            scanNetwork(ip);
        }).width(100).position(this.width / 2 - 50, 50).build());

        // Restore the saved state
        this.ipTextField.setText(savedIpText);
        this.foundServers = new ArrayList<>(savedFoundServers);
    }

    /**
     * Scans the network for servers.
     * @param ip The base IP address to scan.
     */
    private void scanNetwork(String ip) {
        // Clear previous results
        foundServers.clear();
        int counter = 0;

        String baseIp = ip.substring(0, ip.lastIndexOf('.') + 1);

        // Scan the network for servers
        for (int i = 1; i <= 254; i++) {
            String testIp = baseIp + i;
            if (isPortOpen(testIp)) {
                counter++;
                foundServers.add(new ServerInfo("Server #" + counter, testIp, ServerInfo.ServerType.LAN));
            }
        }

        // Update the screen with found servers
        updateServerList();
    }

    /**
     * Checks if a port is open on a given IP address.
     *
     * @param ip The IP address to check.
     * @return True if the port is open, false otherwise.
     */
    private boolean isPortOpen(String ip) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, 25565), 200);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Updates the server list on the screen with the found servers.
     */
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

    /**
     * Adds a server to the server list.
     * @param server The server to add.
     */
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