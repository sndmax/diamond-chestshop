package com.gmail.sneakdevs.diamondchestshop.mixin;

import com.gmail.sneakdevs.diamondchestshop.DiamondChestShop;
import com.gmail.sneakdevs.diamondchestshop.interfaces.BaseContainerBlockEntityInterface;
import com.gmail.sneakdevs.diamondchestshop.interfaces.ItemEntityInterface;
import com.gmail.sneakdevs.diamondchestshop.interfaces.SignBlockEntityInterface;
import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.gmail.sneakdevs.diamondeconomy.sql.DatabaseManager;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(value = SignBlock.class, priority =  999)
public abstract class SignBlockMixin extends BaseEntityBlock {
    
    protected SignBlockMixin(Properties properties) {
        super(properties);
    }

    //remove shop from chest
    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!world.isClientSide() && ((SignBlockEntityInterface) Objects.requireNonNull(world.getBlockEntity(pos))).diamondchestshop_getShop()) {
            BlockPos hangingPos = pos.offset(state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepX(), state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepY(), state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepZ());
            List<ItemEntity> entities = world.getEntitiesOfClass(ItemEntity.class, new AABB(new BlockPos(hangingPos.getX() - 2, hangingPos.getY() - 2, hangingPos.getZ() - 2), new BlockPos(hangingPos.getX() + 2, hangingPos.getY() + 2, hangingPos.getZ() + 2)));
            while (entities.size() > 0) {
                if (entities.get(0).getUUID().equals(((SignBlockEntityInterface) world.getBlockEntity(pos)).diamondchestshop_getItemEntity())) {
                    entities.get(0).kill();
                }
                entities.remove(0);
            }
            if (world.getBlockEntity(hangingPos) instanceof BaseContainerBlockEntity shop) {
                ((BaseContainerBlockEntityInterface) shop).diamondchestshop_setShop(false);
                shop.setChanged();
            }
        }
    }

    @Inject(method = "use", at = @At("HEAD"))
    private void diamondchestshop_useMixin(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (!world.isClientSide()) {
            ItemStack itemStack = player.getItemInHand(hand);
            Item item = itemStack.getItem();
            BlockEntity be = world.getBlockEntity(pos);
            if (be == null) return;
            CompoundTag nbt = be.getUpdateTag();

            if (be.getUpdateTag().getBoolean("diamondchestshop_IsShop")) {
                //admin shops
                if (item.equals(Items.COMMAND_BLOCK)) {
                    ((SignBlockEntityInterface) be).diamondchestshop_setAdminShop(!((SignBlockEntityInterface) be).diamondchestshop_getAdminShop());
                    be.setChanged();
                    player.displayClientMessage(new TextComponent((((SignBlockEntityInterface) be).diamondchestshop_getAdminShop()) ? "Created admin shop" : "Removed admin shop"), true);
                    cir.setReturnValue(InteractionResult.PASS);
                    return;
                }
                if (!nbt.getString("diamondchestshop_ShopOwner").equals(player.getStringUUID()) || ((SignBlockEntityInterface) be).diamondchestshop_getAdminShop()) {
                    //sell shops
                    if (DiamondChestShop.signTextToReadable(nbt.getString("Text1")).contains("sell")) {
                        try {
                            int quantity = Integer.parseInt(DiamondChestShop.signTextToReadable(nbt.getString("Text2")));
                            int money = Integer.parseInt(DiamondChestShop.signTextToReadable(nbt.getString("Text3")));
                            DatabaseManager dm = DiamondEconomy.getDatabaseManager();
                            BlockPos hangingPos = pos.offset(state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepX(), state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepY(), state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepZ());
                            RandomizableContainerBlockEntity shop = (RandomizableContainerBlockEntity) world.getBlockEntity(hangingPos);
                            assert shop != null;
                            String owner = ((BaseContainerBlockEntityInterface) shop).diamondchestshop_getOwner();
                            Item sellItem = Registry.ITEM.get(ResourceLocation.tryParse(((BaseContainerBlockEntityInterface) shop).diamondchestshop_getItem()));

                            if (dm.getBalanceFromUUID(player.getStringUUID()) < money) {
                                player.displayClientMessage(new TextComponent("You don't have enough money"), true);
                                cir.setReturnValue(InteractionResult.PASS);
                                return;
                            }
                            if (dm.getBalanceFromUUID(owner) + money >= Integer.MAX_VALUE && !((SignBlockEntityInterface) be).diamondchestshop_getAdminShop()) {
                                player.displayClientMessage(new TextComponent("The owner is too rich"), true);
                                cir.setReturnValue(InteractionResult.PASS);
                                return;
                            }

                            //check shop has item in proper quantity
                            if (!((SignBlockEntityInterface) be).diamondchestshop_getAdminShop()) {
                                Block shopBlock = world.getBlockState(hangingPos).getBlock();
                                Container inventory;
                                if (shop instanceof ChestBlockEntity && shopBlock instanceof ChestBlock) {
                                    inventory = ChestBlock.getContainer((ChestBlock)shopBlock, world.getBlockState(hangingPos), world, hangingPos, true);
                                } else {
                                    inventory = shop;
                                }

                                int itemCount = 0;
                                for (int i = 0; i < inventory.getContainerSize(); i++) {
                                    if (inventory.getItem(i).getItem().equals(sellItem) && (!inventory.getItem(i).hasTag() || inventory.getItem(i).getTag().getAsString().equals(((BaseContainerBlockEntityInterface) shop).diamondchestshop_getNbt()))) {
                                        itemCount += inventory.getItem(i).getCount();
                                    }
                                }
                                if (itemCount < quantity) {
                                    player.displayClientMessage(new TextComponent("The shop is sold out"), true);
                                    cir.setReturnValue(InteractionResult.PASS);
                                    return;
                                }

                                //take items from chest
                                itemCount = quantity;
                                for (int i = 0; i < inventory.getContainerSize(); i++) {
                                    if (inventory.getItem(i).getItem().equals(sellItem) && (!inventory.getItem(i).hasTag() || inventory.getItem(i).getTag().getAsString().equals(((BaseContainerBlockEntityInterface) shop).diamondchestshop_getNbt()))) {
                                        itemCount -= inventory.getItem(i).getCount();
                                        inventory.setItem(i, new ItemStack(Items.AIR));
                                        if (itemCount < 0) {
                                            ItemStack stack = new ItemStack(sellItem, Math.abs(itemCount));
                                            stack.setTag(DiamondChestShop.getNbtData(((BaseContainerBlockEntityInterface) shop).diamondchestshop_getNbt()));
                                            inventory.setItem(i, stack);
                                            break;
                                        }
                                    }
                                }
                            }

                            //give the player the items
                            while (quantity > sellItem.getMaxStackSize()) {
                                ItemStack stack = new ItemStack(sellItem, sellItem.getMaxStackSize());
                                stack.setTag(DiamondChestShop.getNbtData(((BaseContainerBlockEntityInterface) shop).diamondchestshop_getNbt()));
                                ItemEntity itemEntity = player.drop(stack, true);
                                assert itemEntity != null;
                                itemEntity.setNoPickUpDelay();
                                itemEntity.setOwner(player.getUUID());
                                quantity -= sellItem.getMaxStackSize();
                            }

                            ItemStack stack2 = new ItemStack(sellItem, quantity);
                            stack2.setTag(DiamondChestShop.getNbtData(((BaseContainerBlockEntityInterface) shop).diamondchestshop_getNbt()));
                            ItemEntity itemEntity2 = player.drop(stack2, true);
                            assert itemEntity2 != null;
                            itemEntity2.setNoPickUpDelay();
                            itemEntity2.setOwner(player.getUUID());

                            dm.setBalance(player.getStringUUID(), dm.getBalanceFromUUID(player.getStringUUID()) - money);
                            if (!((SignBlockEntityInterface) be).diamondchestshop_getAdminShop()) {
                                dm.setBalance(owner, dm.getBalanceFromUUID(owner) + money);
                            }

                            player.displayClientMessage(new TextComponent("Bought " + quantity + " " + sellItem.getDescription().getString() + " for $" + money), true);
                            cir.setReturnValue(InteractionResult.PASS);
                            return;
                        } catch (NumberFormatException | CommandSyntaxException ignored) {
                            cir.setReturnValue(InteractionResult.PASS);
                            return;
                        }
                    }

                    //buy shops
                    if (DiamondChestShop.signTextToReadable(nbt.getString("Text1")).contains("buy")) {
                        try {
                            int quantity = Integer.parseInt(DiamondChestShop.signTextToReadable(nbt.getString("Text2")));
                            int money = Integer.parseInt(DiamondChestShop.signTextToReadable(nbt.getString("Text3")));
                            DatabaseManager dm = DiamondEconomy.getDatabaseManager();
                            BlockPos hangingPos = pos.offset(state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepX(), state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepY(), state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepZ());
                            RandomizableContainerBlockEntity shop = (RandomizableContainerBlockEntity) world.getBlockEntity(hangingPos);
                            assert shop != null;
                            String owner = ((BaseContainerBlockEntityInterface) shop).diamondchestshop_getOwner();
                            Item buyItem = Registry.ITEM.get(ResourceLocation.tryParse(((BaseContainerBlockEntityInterface) shop).diamondchestshop_getItem()));

                            if (dm.getBalanceFromUUID(owner) < money && !((SignBlockEntityInterface) be).diamondchestshop_getAdminShop()) {
                                player.displayClientMessage(new TextComponent("The owner hasn't got enough money"), true);
                                cir.setReturnValue(InteractionResult.PASS);
                                return;
                            }

                            if (dm.getBalanceFromUUID(player.getStringUUID()) + money >= Integer.MAX_VALUE) {
                                player.displayClientMessage(new TextComponent("You are too rich"), true);
                                cir.setReturnValue(InteractionResult.PASS);
                                return;
                            }

                            //check player has item in proper quantity
                            int itemCount = 0;
                            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                                if (player.getInventory().getItem(i).getItem().equals(buyItem) && (!player.getInventory().getItem(i).hasTag() || player.getInventory().getItem(i).getTag().getAsString().equals(((BaseContainerBlockEntityInterface) shop).diamondchestshop_getNbt()))) {
                                    itemCount += player.getInventory().getItem(i).getCount();
                                }
                            }
                            if (itemCount < quantity) {
                                player.displayClientMessage(new TextComponent("You don't have enough of that item"), true);
                                cir.setReturnValue(InteractionResult.PASS);
                                return;
                            }
                            int emptySpaces = 0;
                            Block shopBlock = world.getBlockState(hangingPos).getBlock();
                            Container inventory;
                            if (shop instanceof ChestBlockEntity && shopBlock instanceof ChestBlock) {
                                inventory = ChestBlock.getContainer((ChestBlock)shopBlock, world.getBlockState(hangingPos), world, hangingPos, true);
                            } else {
                                inventory = shop;
                            }
                            for (int i = 0; i < inventory.getContainerSize(); i++) {
                                if (inventory.getItem(i).getItem().equals(Items.AIR)) {
                                    emptySpaces += buyItem.getMaxStackSize();
                                    continue;
                                }
                                if (inventory.getItem(i).getItem().equals(buyItem)) {
                                    emptySpaces += buyItem.getMaxStackSize() - inventory.getItem(i).getCount();
                                }
                            }
                            if (emptySpaces < quantity) {
                                player.displayClientMessage(new TextComponent("The chest is full"), true);
                                cir.setReturnValue(InteractionResult.PASS);
                                return;
                            }

                            //take items from player
                            itemCount = quantity;
                            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                                if (player.getInventory().getItem(i).getItem().equals(buyItem) && (!player.getInventory().getItem(i).hasTag() || player.getInventory().getItem(i).getTag().getAsString().equals(((BaseContainerBlockEntityInterface) shop).diamondchestshop_getNbt()))) {
                                    itemCount -= player.getInventory().getItem(i).getCount();
                                    player.getInventory().setItem(i, new ItemStack(Items.AIR));
                                    if (itemCount < 0) {
                                        ItemStack stack = new ItemStack(buyItem, Math.abs(itemCount));
                                        stack.setTag(DiamondChestShop.getNbtData(((BaseContainerBlockEntityInterface) shop).diamondchestshop_getNbt()));
                                        player.getInventory().setItem(i, stack);
                                        break;
                                    }
                                }
                            }

                            //give the chest the items
                            if (!((SignBlockEntityInterface) be).diamondchestshop_getAdminShop()) {
                                int itemsToAdd = quantity;
                                for (int i = 0; i < inventory.getContainerSize(); i++) {
                                    if (inventory.getItem(i).getItem().equals(buyItem) && (!inventory.getItem(i).hasTag() || inventory.getItem(i).getTag().getAsString().equals(((BaseContainerBlockEntityInterface) shop).diamondchestshop_getNbt()))) {
                                        itemsToAdd += inventory.getItem(i).getCount();
                                        itemsToAdd -= buyItem.getMaxStackSize();
                                        ItemStack stack = new ItemStack(buyItem, buyItem.getMaxStackSize());
                                        stack.setTag(DiamondChestShop.getNbtData(((BaseContainerBlockEntityInterface) shop).diamondchestshop_getNbt()));
                                        inventory.setItem(i, stack);
                                    }
                                    if (inventory.getItem(i).getItem().equals(Items.AIR)) {
                                        itemsToAdd -= buyItem.getMaxStackSize();
                                        ItemStack stack = new ItemStack(buyItem, buyItem.getMaxStackSize());
                                        stack.setTag(DiamondChestShop.getNbtData(((BaseContainerBlockEntityInterface) shop).diamondchestshop_getNbt()));
                                        inventory.setItem(i, stack);
                                    }
                                    if (itemsToAdd < 0) {
                                        ItemStack stack = new ItemStack(buyItem, buyItem.getMaxStackSize() + itemsToAdd);
                                        stack.setTag(DiamondChestShop.getNbtData(((BaseContainerBlockEntityInterface) shop).diamondchestshop_getNbt()));
                                        inventory.setItem(i, stack);
                                        break;
                                    }
                                }
                            }

                            if (!((SignBlockEntityInterface) be).diamondchestshop_getAdminShop()) {
                                dm.setBalance(owner, dm.getBalanceFromUUID(owner) - money);
                            }
                            dm.setBalance(player.getStringUUID(), dm.getBalanceFromUUID(player.getStringUUID()) + money);

                            player.displayClientMessage(new TextComponent("Sold " + quantity + " " + buyItem.getDescription().getString() + " for $" + money), true);
                            cir.setReturnValue(InteractionResult.PASS);
                            return;
                        } catch (NumberFormatException | CommandSyntaxException ignored) {
                            cir.setReturnValue(InteractionResult.PASS);
                            return;
                        }
                    }
                }
            }

            //create the chest shop
            if (item.equals(Registry.ITEM.get(ResourceLocation.tryParse(DiamondEconomyConfig.getInstance().currencies[0])))) {
                if (nbt.getBoolean("diamondchestshop_IsShop")) {
                    player.displayClientMessage(new TextComponent("This is already a shop"), true);
                    cir.setReturnValue(InteractionResult.PASS);
                    return;
                }

                BlockPos hangingPos = pos.offset(state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepX(), state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepY(), state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepZ());
                if (!(world.getBlockEntity(hangingPos) instanceof BaseContainerBlockEntity shop && world.getBlockEntity(hangingPos) instanceof RandomizableContainerBlockEntity)) {
                    player.displayClientMessage(new TextComponent("Sign must be on a valid container"), true);
                    cir.setReturnValue(InteractionResult.PASS);
                    return;
                }

                if (!nbt.getString("diamondchestshop_ShopOwner").equals(player.getStringUUID()) || !((BaseContainerBlockEntityInterface) shop).diamondchestshop_getOwner().equals(player.getStringUUID())) {
                    player.displayClientMessage(new TextComponent("You must have placed down the sign and chest"), true);
                    cir.setReturnValue(InteractionResult.PASS);
                    return;
                }

                if (player.getOffhandItem().getItem().equals(Items.AIR)) {
                    player.displayClientMessage(new TextComponent("The sell item must be in your offhand"), true);
                    cir.setReturnValue(InteractionResult.PASS);
                    return;
                }

                if (!(DiamondChestShop.signTextToReadable(nbt.getString("Text1")).equals("sell") || DiamondChestShop.signTextToReadable(nbt.getString("Text1")).equals("buy"))) {
                    player.displayClientMessage(new TextComponent("The first line must be either \"Buy\" or \"Sell\""), true);
                    cir.setReturnValue(InteractionResult.PASS);
                    return;
                }

                if (((BaseContainerBlockEntityInterface) shop).diamondchestshop_getShop()) {
                    player.displayClientMessage(new TextComponent("That chest already is a shop"), true);
                    cir.setReturnValue(InteractionResult.PASS);
                    return;
                }

                try {
                    int quantity = Integer.parseInt(DiamondChestShop.signTextToReadable(nbt.getString("Text2")));
                    int money = Integer.parseInt(DiamondChestShop.signTextToReadable(nbt.getString("Text3")));
                    if (quantity >= 1) {
                        if (money >= 0) {
                            ((SignBlockEntityInterface) be).diamondchestshop_setShop(true);
                            ((BaseContainerBlockEntityInterface) shop).diamondchestshop_setShop(true);
                            ((BaseContainerBlockEntityInterface) shop).diamondchestshop_setItem(Registry.ITEM.getKey(player.getOffhandItem().getItem()).toString());
                            try {
                                ((BaseContainerBlockEntityInterface) shop).diamondchestshop_setTag(player.getOffhandItem().getTag().getAsString());
                            } catch (NullPointerException ignored) {
                                ((BaseContainerBlockEntityInterface) shop).diamondchestshop_setTag("{}");
                            }
                            be.setChanged();
                            shop.setChanged();

                            ItemEntity itemEntity = EntityType.ITEM.create(world);
                            itemEntity.setItem(new ItemStack(player.getOffhandItem().getItem(), Math.min(quantity, player.getOffhandItem().getItem().getMaxStackSize())));
                            itemEntity.setUnlimitedLifetime();
                            itemEntity.setNeverPickUp();
                            itemEntity.setInvulnerable(true);
                            itemEntity.setNoGravity(true);
                            itemEntity.setPos(new Vec3(hangingPos.getX() + 0.5, hangingPos.getY() + 1.05, hangingPos.getZ() + 0.5));
                            ((ItemEntityInterface) itemEntity).diamondchestshop_setShop(true);
                            world.addFreshEntity(itemEntity);
                            ((SignBlockEntityInterface) be).diamondchestshop_setItemEntity(itemEntity.getUUID());
                            player.displayClientMessage(new TextComponent("Created shop with " + quantity + " " + player.getOffhandItem().getItem().getDescription().getString() + (((nbt.getString("Text1")).contains("sell")) ? " sold for $" : " bought for $") + money), true);
                            cir.setReturnValue(InteractionResult.PASS);
                        } else {
                            player.displayClientMessage(new TextComponent("Positive quantity required"), true);
                            cir.setReturnValue(InteractionResult.PASS);
                        }
                    } else {
                        player.displayClientMessage(new TextComponent("Negative prices are not allowed"), true);
                        cir.setReturnValue(InteractionResult.PASS);
                    }
                } catch (NumberFormatException ignored) {
                    player.displayClientMessage(new TextComponent("The second and third lines must be numbers (quantity then money)"), true);
                    cir.setReturnValue(InteractionResult.PASS);
                }
            }
        }
    }
}