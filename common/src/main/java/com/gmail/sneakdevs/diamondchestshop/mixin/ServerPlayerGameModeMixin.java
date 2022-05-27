package com.gmail.sneakdevs.diamondchestshop.mixin;

import com.gmail.sneakdevs.diamondchestshop.config.DiamondChestShopConfig;
import com.gmail.sneakdevs.diamondchestshop.interfaces.BaseContainerBlockEntityInterface;
import com.gmail.sneakdevs.diamondchestshop.interfaces.SignBlockEntityInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Shadow
    protected ServerLevel level;

    @Final
    @Shadow
    protected ServerPlayer player;

    @Inject(method = "destroyAndAck", at = @At("HEAD"), cancellable = true)
    private void diamondchestshop_destroyBlockMixin(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, String string, CallbackInfo ci) {
        if (DiamondChestShopConfig.getInstance().shopProtection) {
            if (player.isCreative()) return;
            BlockEntity be = level.getBlockEntity(blockPos);
            if (be != null) {
                if (be instanceof BaseContainerBlockEntity) {
                    if (!((BaseContainerBlockEntityInterface) be).diamondchestshop_getShop()) return;
                    if (!((BaseContainerBlockEntityInterface) be).diamondchestshop_getOwner().equals(player.getStringUUID())) {
                        this.player.connection.send(new ClientboundBlockUpdatePacket(level, blockPos));
                        BlockEntity blockEntity = level.getBlockEntity(blockPos);
                        Packet<ClientGamePacketListener> updatePacket = blockEntity.getUpdatePacket();
                        if (updatePacket != null) {
                            this.player.connection.send(updatePacket);
                        }
                        player.displayClientMessage(new TextComponent("Cannot break another player's shop"), true);
                        ci.cancel();
                    }
                }
                if (be instanceof SignBlockEntity) {
                    if (!((SignBlockEntityInterface) be).diamondchestshop_getShop()) return;
                    if (!((SignBlockEntityInterface) be).diamondchestshop_getOwner().equals(player.getStringUUID())) {
                        this.player.connection.send(new ClientboundBlockUpdatePacket(level, blockPos));
                        BlockEntity blockEntity = level.getBlockEntity(blockPos);
                        Packet<ClientGamePacketListener> updatePacket = blockEntity.getUpdatePacket();
                        if (updatePacket != null) {
                            this.player.connection.send(updatePacket);
                        }
                        player.displayClientMessage(new TextComponent("Cannot break another player's shop"), true);
                        ci.cancel();
                    }
                }
            }
        }
    }
}