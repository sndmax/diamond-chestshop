package com.gmail.sneakdevs.diamondchestshop.mixin;

import com.gmail.sneakdevs.diamondchestshop.interfaces.ItemEntityInterface;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.material.PushReaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements ItemEntityInterface {
    private boolean diamondchestshop_isShop;
    private boolean canTick = true;

    public ItemEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public void diamondchestshop_setShop(boolean newVal) {
        this.diamondchestshop_isShop = newVal;
    }
    public boolean diamondchestshop_getShop() {
        return this.diamondchestshop_isShop;
    }
    
    @Override
    public PushReaction getPistonPushReaction() {
        return (diamondchestshop_isShop) ? PushReaction.IGNORE : PushReaction.NORMAL;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void diamondchestshop_tickMixin(CallbackInfo ci) {
        if (diamondchestshop_isShop) {
            if (((ItemEntity)(Object)this).level.getBlockEntity(((ItemEntity)(Object)this).blockPosition().below()) instanceof BaseContainerBlockEntity) {
                ((ItemEntity)(Object)this).setDeltaMovement(0, 0, 0);
                if (!canTick) {
                    ci.cancel();
                }
                canTick = false;
            } else {
                ((ItemEntity)(Object) this).kill();
            }
        }
    }

    @Inject(method = "isMergable", at = @At("HEAD"), cancellable = true)
    private void diamondchestshop_isMergableMixin(CallbackInfoReturnable<Boolean> cir) {
        if (diamondchestshop_isShop) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void diamondchestshop_addAdditionalSaveDataMixin(CompoundTag nbt, CallbackInfo ci) {
        nbt.putBoolean("diamondchestshop_IsShop", this.diamondchestshop_isShop);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void diamondchestshop_readAdditionalSaveDataMixin(CompoundTag nbt, CallbackInfo ci) {
        this.diamondchestshop_isShop = nbt.getBoolean("diamondchestshop_IsShop");
    }
}