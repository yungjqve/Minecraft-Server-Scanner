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
        ButtonWidget serverScanner = ButtonWidget.builder(Text.of("Server Scanner"), button ->
                MinecraftClient.getInstance().setScreen(new ServerScannerScreen(this))
        ).width(100).position(10, this.height - 54).build();

        ButtonWidget portScanner = ButtonWidget.builder(Text.of("Port Scanner"), button ->
                MinecraftClient.getInstance().setScreen(new PortScannerScreen())
        ).width(100).position(10, this.height - 29).build();

        ButtonWidget deleteViaRegex = ButtonWidget.builder(Text.of("Delete via Regex"), button ->
                MinecraftClient.getInstance().setScreen(new DeleteRegexScreen(this))
        ).width(100).position(10, this.height - 29).build();

        ButtonWidget deleteAllServersButton = ButtonWidget.builder(Text.of("Delete All Servers"), button -> {
            if (confirmDelete) {
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
                confirmDelete = false;
                button.setMessage(Text.of("Delete All"));
                button.active = true;
            } else {
                confirmDelete = true;
                button.setMessage(Text.of("Are you sure?"));
                button.active = false;
                button.setMessage(Text.of("Are you sure?"));
                button.active = true;
            }
        }).width(100).position(10, this.height - 54).build();

        this.addDrawableChild(ButtonWidget.builder(Text.of("<->"), (button) -> {
            isFirstScreen = !isFirstScreen;
            if (!isFirstScreen){
                this.remove(serverScanner);
                this.remove(portScanner);
                this.addDrawableChild(deleteAllServersButton);
                this.addDrawableChild(deleteViaRegex);
            }else{
                this.remove(deleteAllServersButton);
                this.remove(deleteViaRegex);
                this.addDrawableChild(serverScanner);
                this.addDrawableChild(portScanner);
            }
        }).width(25).position(115, this.height - 29).build());

        if (isFirstScreen) {
            this.addDrawableChild(serverScanner);
            this.addDrawableChild(portScanner);
        } else {
            this.addDrawableChild(deleteAllServersButton);
            this.addDrawableChild(deleteViaRegex);
        }

    }
}