package com.gmail.sneakdevs.diamondchestshop.mixin;

import com.gmail.sneakdevs.diamondchestshop.interfaces.LockableContainerBlockEntityInterface;
import com.gmail.sneakdevs.diamondchestshop.interfaces.SignBlockEntityInterface;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "writeNbtToBlockEntity", at = @At("HEAD"))
    private static void diamondchestshop_writeNbtToBlockEntityMixin(World world, PlayerEntity player, BlockPos pos, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (world.getServer() != null) {
            if (world.getBlockEntity(pos) instanceof LockableContainerBlockEntity) {
                ((LockableContainerBlockEntityInterface) world.getBlockEntity(pos)).diamondchestshop_setOwner(player.getUuidAsString());
            } else if (world.getBlockEntity(pos) instanceof SignBlockEntity) {
                ((SignBlockEntityInterface) world.getBlockEntity(pos)).diamondchestshop_setOwner(player.getUuidAsString());
            }
        }
    }
}