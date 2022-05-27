package com.gmail.sneakdevs.diamondchestshop.mixin;

import com.gmail.sneakdevs.diamondchestshop.config.DiamondChestShopConfig;
import com.gmail.sneakdevs.diamondchestshop.interfaces.BaseContainerBlockEntityInterface;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseContainerBlockEntity.class)
public class BaseContainerBlockEntityMixin implements BaseContainerBlockEntityInterface {
    private String diamondchestshop_owner;
    private String diamondchestshop_item;
    private String diamondchestshop_nbt;
    private boolean diamondchestshop_isShop;

    public void diamondchestshop_setOwner(String newOwner) {
        this.diamondchestshop_owner = newOwner;
    }
    public void diamondchestshop_setItem(String newItem) {
        this.diamondchestshop_item = newItem;
    }
    public void diamondchestshop_setTag(String newTag) {
        this.diamondchestshop_nbt = newTag;
    }
    public void diamondchestshop_setShop(boolean newShop) {
        this.diamondchestshop_isShop = newShop;
    }

    public String diamondchestshop_getOwner() {
        return this.diamondchestshop_owner;
    }
    public String diamondchestshop_getItem() {
        return this.diamondchestshop_item;
    }
    public String diamondchestshop_getNbt() {
        return this.diamondchestshop_nbt;
    }
    public boolean diamondchestshop_getShop() {
        return this.diamondchestshop_isShop;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void diamondchestshop_saveAdditionalMixin(CompoundTag nbt, CallbackInfo ci) {
        if (diamondchestshop_owner == null) diamondchestshop_owner = "";
        if (diamondchestshop_item == null) diamondchestshop_item = "";
        if (diamondchestshop_nbt == null) diamondchestshop_nbt = "";
        if (!nbt.contains("diamondchestshop_ShopOwner"))
            nbt.putString("diamondchestshop_ShopOwner", diamondchestshop_owner);
        if (!nbt.contains("diamondchestshop_ShopItem"))
            nbt.putString("diamondchestshop_ShopItem", diamondchestshop_item);
        if (!nbt.contains("diamondchestshop_NBT")) nbt.putString("diamondchestshop_NBT", diamondchestshop_nbt);
        if (!nbt.contains("diamondchestshop_IsShop"))
            nbt.putBoolean("diamondchestshop_IsShop", diamondchestshop_isShop);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void diamondchestshop_loadMixin(CompoundTag nbt, CallbackInfo ci) {
        this.diamondchestshop_owner = nbt.getString("diamondchestshop_ShopOwner");
        this.diamondchestshop_item = nbt.getString("diamondchestshop_ShopItem");
        this.diamondchestshop_nbt = nbt.getString("diamondchestshop_NBT");
        this.diamondchestshop_isShop = nbt.getBoolean("diamondchestshop_IsShop");
    }

    @Inject(method = "canOpen", at = @At("RETURN"), cancellable = true)
    private void diamondchestshop_canOpenMixin(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (AutoConfig.getConfigHolder(DiamondChestShopConfig.class).getConfig().shopProtection) {
            if (!cir.getReturnValue()) return;
            if (player.isCreative()) return;
            if (diamondchestshop_isShop) {
                if (diamondchestshop_owner.equals(player.getStringUUID())) {
                    cir.setReturnValue(true);
                    return;
                }
                player.displayClientMessage(new TextComponent("Cannot open another player's shop"), true);
                cir.setReturnValue(false);
            }
        }
    }
}