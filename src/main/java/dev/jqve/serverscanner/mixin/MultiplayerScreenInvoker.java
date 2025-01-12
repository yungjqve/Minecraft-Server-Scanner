package dev.jqve.serverscanner.mixin;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiplayerScreen.class)
public interface MultiplayerScreenInvoker {

    @Accessor("serverList")
    void setServerList(ServerList serverList);

    @Accessor("selectedEntry")
    void setSelectedEntry(ServerInfo selectedEntry);

    @Invoker("addEntry")
    void invokeAddEntry(boolean confirmedAction);
}
