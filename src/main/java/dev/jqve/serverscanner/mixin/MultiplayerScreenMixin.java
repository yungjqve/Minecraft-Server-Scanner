package dev.jqve.serverscanner.mixin;

import dev.jqve.serverscanner.screens.DeleteRegexScreen;
import dev.jqve.serverscanner.screens.PortScannerScreen;
import dev.jqve.serverscanner.screens.ServerScannerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin extends Screen {
    @Unique
    private final Screen parentScreen;
    @Unique
    private boolean confirmDelete = false;
    @Unique
    private boolean isFirstScreen = true;

    private MultiplayerScreenMixin(Screen parentScreen) {
        super(null);
        this.parentScreen = parentScreen;
    }

    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {
        // Create your buttons in separate helper methods or inline, as shown:
        ButtonWidget serverScanner = createServerScannerButton();
        ButtonWidget portScanner = createPortScannerButton();
        ButtonWidget deleteAllServersButton = createDeleteAllServersButton();
        ButtonWidget deleteViaRegex = createDeleteViaRegexButton();

        // Toggle button switcher
        ButtonWidget toggleButtonSets = ButtonWidget.builder(Text.of("<->"), button -> {
            isFirstScreen = !isFirstScreen;
            if (!isFirstScreen) {
                // Remove first set, add second
                this.remove(serverScanner);
                this.remove(portScanner);
                this.addDrawableChild(deleteAllServersButton);
                this.addDrawableChild(deleteViaRegex);
            } else {
                // Remove second set, add first
                this.remove(deleteAllServersButton);
                this.remove(deleteViaRegex);
                this.addDrawableChild(serverScanner);
                this.addDrawableChild(portScanner);
            }
        }).width(25).position(115, this.height - 29).build();
        this.addDrawableChild(toggleButtonSets);

        // Display correct screen on initialization
        if (isFirstScreen) {
            this.addDrawableChild(serverScanner);
            this.addDrawableChild(portScanner);
        } else {
            this.addDrawableChild(deleteAllServersButton);
            this.addDrawableChild(deleteViaRegex);
        }
    }

    private ButtonWidget createServerScannerButton() {
        return ButtonWidget.builder(Text.of("Server Scanner"), button ->
                MinecraftClient.getInstance().setScreen(new ServerScannerScreen(this))
        ).width(100).position(10, this.height - 54).build();
    }

    private ButtonWidget createPortScannerButton() {
        return ButtonWidget.builder(Text.of("Port Scanner"), button ->
                MinecraftClient.getInstance().setScreen(new PortScannerScreen(this))
        ).width(100).position(10, this.height - 29).build();
    }

    private ButtonWidget createDeleteViaRegexButton() {
        return ButtonWidget.builder(Text.of("Delete via Regex"), button ->
                MinecraftClient.getInstance().setScreen(new DeleteRegexScreen(this))
        ).width(100).position(10, this.height - 29).build();
    }

    private ButtonWidget createDeleteAllServersButton() {
        return ButtonWidget.builder(Text.of("Delete All Servers"), button -> {
            if (!confirmDelete) {
                // First press: ask for confirmation
                confirmDelete = true;
                button.setMessage(Text.of("Are you sure?"));
            } else {
                // Second press: delete all servers
                confirmDelete = false;
                // Additional logic to delete servers
                MinecraftClient client = MinecraftClient.getInstance();
                ServerList serverList = new ServerList(client);
                serverList.loadFile();
                while (serverList.size() > 0) {
                    serverList.remove(serverList.get(0));
                }
                serverList.saveFile();
                MultiplayerScreenInvoker invoker = (MultiplayerScreenInvoker) this;
                invoker.setServerList(serverList);
                client.setScreen(new MultiplayerScreen(parentScreen));
                button.setMessage(Text.of("Delete All Servers"));
            }
        }).width(100).position(10, this.height - 54).build();
    }
}