package dev.jqve.serverscanner.screens;

import dev.jqve.serverscanner.mixin.MultiplayerScreenInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;

public class DeleteRegexScreen extends Screen {
    private final Screen parentScreen;
    private TextFieldWidget regexTextField;

    public DeleteRegexScreen(Screen parentScreen) {
        super(Text.of("Delete Regex Screen"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        // Initialize components
        this.regexTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 20, 200, 20, Text.of("Enter Regex"));
        this.addDrawableChild(regexTextField);

        this.addDrawableChild(ButtonWidget.builder(Text.of("Delete"), (button) -> {
            String regex = regexTextField.getText();
            deleteServers(regex);
        }).width(100).position(this.width / 2 - 50, 50).build());
    }

    private void deleteServers(String regex) {
        MinecraftClient client = MinecraftClient.getInstance();
        ServerList serverList = new ServerList(client);
        serverList.loadFile();
        for (int i = 0; i < serverList.size(); i++) {
            if (serverList.get(i).name != null && serverList.get(i).name.matches(regex)) {
                serverList.remove(serverList.get(i));
                i--; // Adjust index after removal
            }
        }
        serverList.saveFile();
        MultiplayerScreenInvoker invoker = (MultiplayerScreenInvoker) parentScreen;
        invoker.setServerList(serverList);
        client.setScreen(new MultiplayerScreen(parentScreen));
    }
}