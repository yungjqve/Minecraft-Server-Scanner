package dev.jqve.serverscanner.screens;

import dev.jqve.serverscanner.mixin.MultiplayerScreenInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
        regexTextField = new TextFieldWidget(this.textRenderer, width/2-100, 60, 200, 20, Text.of(""));
        regexTextField.setPlaceholder(Text.of(".* to delete all")); // Add placeholder text
        addDrawableChild(regexTextField);

        // Add cancel button
        addDrawableChild(ButtonWidget.builder(Text.of("Cancel"), b -> close())
                .dimensions(width/2 - 100, 140, 90, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.of("Delete"), b -> {
            String regex = regexTextField.getText();
            deleteServers(regex);})
                .dimensions(width/2 + 10, 140, 90, 20)
                .build());
    }

    private void deleteServers(String regex) {
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            this.regexTextField.setText("Invalid regex");
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        ServerList serverList = ((MultiplayerScreen) parentScreen).getServerList();
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