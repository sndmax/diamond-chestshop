package com.gmail.sneakdevs.diamondchestshop.mixin;

import com.gmail.sneakdevs.diamondchestshop.config.DiamondChestShopConfig;
import com.gmail.sneakdevs.diamondchestshop.interfaces.BaseContainerBlockEntityInterface;
import com.gmail.sneakdevs.diamondchestshop.interfaces.SignBlockEntityInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow
    protected ServerLevel level;

    @Final
    @Shadow
    protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void diamondchestshop_destroyBlockMixin(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        if (DiamondChestShopConfig.getInstance().shopProtection) {
            if (player.isCreative()) return;
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof BaseContainerBlockEntity || be instanceof SignBlockEntity)) return;
            if (be instanceof BaseContainerBlockEntity) {
                if (!((BaseContainerBlockEntityInterface) be).diamondchestshop_getShop()) return;
                if (((BaseContainerBlockEntityInterface) be).diamondchestshop_getOwner().equals(player.getStringUUID()))
                    return;
            } else {
                if (!((SignBlockEntityInterface) be).diamondchestshop_getShop()) return;
                if (((SignBlockEntityInterface) be).diamondchestshop_getOwner().equals(player.getStringUUID()))
                    return;
            }
            player.displayClientMessage(new TextComponent("Cannot break another player's shop"), true);
            info.setReturnValue(false);
        }
    }
}