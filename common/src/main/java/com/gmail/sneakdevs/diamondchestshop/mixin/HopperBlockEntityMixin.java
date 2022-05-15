package com.gmail.sneakdevs.diamondchestshop.mixin;

import com.gmail.sneakdevs.diamondchestshop.config.DiamondChestShopConfig;
import com.gmail.sneakdevs.diamondchestshop.interfaces.BaseContainerBlockEntityInterface;
import com.gmail.sneakdevs.diamondchestshop.interfaces.ItemEntityInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(value = HopperBlockEntity.class, priority = 100)
public class HopperBlockEntityMixin {
    @Inject(method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/item/ItemEntity;)Z", at = @At("HEAD"), cancellable = true)
    private static void diamondchestshop_addItemMixin(Container container, ItemEntity itemEntity, CallbackInfoReturnable<Boolean> cir) {
        if (((ItemEntityInterface)itemEntity).diamondchestshop_getShop()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tryMoveItems", at = @At("HEAD"), cancellable = true)
    private static void diamondchestshop_tryMoveItemsMixin(Level level, BlockPos blockPos, BlockState blockState, HopperBlockEntity hopperBlockEntity, BooleanSupplier booleanSupplier, CallbackInfoReturnable<Boolean> cir) {
        if (DiamondChestShopConfig.getInstance().shopProtection && level.getBlockEntity(blockPos.above()) instanceof BaseContainerBlockEntity && ((BaseContainerBlockEntityInterface)level.getBlockEntity(blockPos.above())).diamondchestshop_getShop() && !((BaseContainerBlockEntityInterface)level.getBlockEntity(blockPos.above())).diamondchestshop_getOwner().equals(((BaseContainerBlockEntityInterface)hopperBlockEntity).diamondchestshop_getOwner())) {
            cir.setReturnValue(false);
        }
    }
}