package com.gmail.sneakdevs.diamondchestshop.mixin;

import com.gmail.sneakdevs.diamondchestshop.interfaces.ItemEntityInterface;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class ItemEntityMixin implements ItemEntityInterface {
    private boolean diamondchestshop_isShop;
    private boolean canTick = true;

    public void diamondchestshop_setShop(boolean newVal) {
        this.diamondchestshop_isShop = newVal;
    }


    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void diamondchestshop_tickMixin(CallbackInfo ci) {
        if (((ItemEntity) (Object) this).world.getBlockEntity(((ItemEntity) (Object) this).getBlockPos().down()) instanceof LockableContainerBlockEntity ||
                ((ItemEntity) (Object) this).world.getBlockEntity(((ItemEntity) (Object) this).getBlockPos().down(2)) instanceof LockableContainerBlockEntity) {
            ((ItemEntity) (Object) this).setVelocity(0, 0, 0);
            if (!canTick) {
                ci.cancel();
            }
            canTick = false;
        } else {
            ((ItemEntity) (Object) this).kill();
        }
    }

    @Inject(method = "canMerge()Z", at = @At("HEAD"), cancellable = true)
    private void diamondchestshop_canMergeMixin(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void diamondchestshop_writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("diamondchestshop_IsShop", this.diamondchestshop_isShop);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void diamondchestshop_readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo ci) {
        this.diamondchestshop_isShop = nbt.getBoolean("diamondchestshop_IsShop");
    }
}