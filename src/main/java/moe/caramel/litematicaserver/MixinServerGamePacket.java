package moe.caramel.litematicaserver;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public final class MixinServerGamePacket {

    private static final ResourceLocation NAMESPACE = new ResourceLocation("litematica", "command");

    @Shadow @Final private MinecraftServer server;
    @Shadow public ServerPlayer player;

    // =============

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void handleCustomPayloadAsync(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (!packet.getIdentifier().equals(NAMESPACE)) return;
        final String command = packet.getData().readUtf();
        final ParseResults<CommandSourceStack> result = this.parseCommand(command);
        if (SignableCommand.of(result).arguments().isEmpty()) {
            this.server.submit(() -> this.server.getCommands().performCommand(result, command));
        }
        ci.cancel();
    }

    ParseResults<CommandSourceStack> parseCommand(final String string) {
        final CommandDispatcher<CommandSourceStack> dispatcher = this.server.getCommands().getDispatcher();
        return dispatcher.parse(string, this.player.createCommandSourceStack());
    }
}
