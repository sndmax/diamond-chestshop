package com.gmail.sneakdevs.diamondchestshop.mixin;

import com.gmail.sneakdevs.diamondchestshop.interfaces.SignBlockEntityInterface;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(SignBlockEntity.class)
public class SignBlockEntityMixin implements SignBlockEntityInterface {
    private String diamondchestshop_owner;
    private UUID diamondchestshop_itemEntity;
    private boolean diamondchestshop_isShop;
    private boolean diamondchestshop_isAdminShop;

    public void diamondchestshop_setOwner(String newOwner) {
        this.diamondchestshop_owner = newOwner;
    }

    public void diamondchestshop_setItemEntity(UUID newEntity) {
        this.diamondchestshop_itemEntity = newEntity;
    }

    public void diamondchestshop_setShop(boolean newShop) {
        this.diamondchestshop_isShop = newShop;
    }

    public void diamondchestshop_setAdminShop(boolean newAdminShop) {
        this.diamondchestshop_isAdminShop = newAdminShop;
    }

    public boolean diamondchestshop_getAdminShop() {
        return this.diamondchestshop_isAdminShop;
    }

    public boolean diamondchestshop_getShop() {
        return this.diamondchestshop_isShop;
    }

    public String diamondchestshop_getOwner() {
        return this.diamondchestshop_owner;
    }

    public UUID diamondchestshop_getItemEntity() {
        return this.diamondchestshop_itemEntity;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void diamondchestshop_saveAdditionalMixin(CompoundTag nbt, CallbackInfo ci) {
        if (this.diamondchestshop_owner == null) diamondchestshop_owner = "";
        if (this.diamondchestshop_isShop) nbt.putUUID("diamondchestshop_ItemEntity", diamondchestshop_itemEntity);
        nbt.putString("diamondchestshop_ShopOwner", diamondchestshop_owner);
        if (!nbt.contains("diamondchestshop_IsShop"))
            nbt.putBoolean("diamondchestshop_IsShop", diamondchestshop_isShop);
        if (!nbt.contains("diamondchestshop_IsAdminShop"))
            nbt.putBoolean("diamondchestshop_IsAdminShop", diamondchestshop_isAdminShop);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void diamondchestshop_loadMixin(CompoundTag nbt, CallbackInfo ci) {
        this.diamondchestshop_owner = nbt.getString("diamondchestshop_ShopOwner");
        this.diamondchestshop_isShop = nbt.getBoolean("diamondchestshop_IsShop");
        this.diamondchestshop_isAdminShop = nbt.getBoolean("diamondchestshop_IsAdminShop");
        if (this.diamondchestshop_isShop) this.diamondchestshop_itemEntity = nbt.getUUID("diamondchestshop_ItemEntity");
    }
}