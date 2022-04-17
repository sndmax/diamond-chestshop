package com.gmail.sneakdevs.diamondchestshop.mixin;

import com.gmail.sneakdevs.diamondchestshop.DiamondChestShop;
import com.gmail.sneakdevs.diamondeconomy.DatabaseManager;
import com.gmail.sneakdevs.diamondeconomy.config.DEConfig;
import com.gmail.sneakdevs.diamondchestshop.interfaces.ItemEntityInterface;
import com.gmail.sneakdevs.diamondchestshop.interfaces.LockableContainerBlockEntityInterface;
import com.gmail.sneakdevs.diamondchestshop.interfaces.SignBlockEntityInterface;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(AbstractSignBlock.class)
public abstract class AbstractSignBlockMixin extends BlockWithEntity {

    protected AbstractSignBlockMixin(Settings settings) {
        super(settings);
    }

    //remove shop from chest
    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && ((SignBlockEntityInterface) Objects.requireNonNull(world.getBlockEntity(pos))).diamondchestshop_getShop()) {
            BlockPos hangingPos = pos.add(state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetX(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetY(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetZ());
            List<Entity> entities = world.getOtherEntities(player, new Box(new BlockPos(hangingPos.getX() - 2, hangingPos.getY() - 2, hangingPos.getZ() - 2), new BlockPos(hangingPos.getX() + 2, hangingPos.getY() + 2, hangingPos.getZ() + 2)));
            while (entities.size() > 0) {
                if (entities.get(0).getUuid().equals(((SignBlockEntityInterface) world.getBlockEntity(pos)).diamondchestshop_getItemEntity())) {
                    entities.get(0).kill();
                }
                entities.remove(0);
            }
            if (world.getBlockEntity(hangingPos) instanceof LockableContainerBlockEntity shop) {
                ((LockableContainerBlockEntityInterface) shop).diamondchestshop_setShop(false);
                shop.markDirty();
            }
        }
    }

    @Inject(method = "onUse", at = @At("HEAD"))
    private void diamondchestshop_onUseMixin(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!world.isClient) {
            ItemStack itemStack = player.getStackInHand(hand);
            Item item = itemStack.getItem();
            BlockEntity be = world.getBlockEntity(pos);
            if (be == null) return;
            NbtCompound nbt = be.toInitialChunkDataNbt();

            if (be.toInitialChunkDataNbt().getBoolean("diamondchestshop_IsShop")) {
                //admin shops
                if (item.equals(Items.COMMAND_BLOCK)) {
                    ((SignBlockEntityInterface) be).diamondchestshop_setAdminShop(!((SignBlockEntityInterface) be).diamondchestshop_getAdminShop());
                    be.markDirty();
                    player.sendMessage(new LiteralText((((SignBlockEntityInterface) be).diamondchestshop_getAdminShop()) ? "Created admin shop" : "Removed admin shop"), true);
                    return;
                }
                if (!nbt.getString("diamondchestshop_ShopOwner").equals(player.getUuidAsString()) || ((SignBlockEntityInterface) be).diamondchestshop_getAdminShop()) {
                    //sell shops
                    if (DiamondChestShop.signTextToReadable(nbt.getString("Text1")).contains("sell")) {
                        try {
                            int quantity = Integer.parseInt(DiamondChestShop.signTextToReadable(nbt.getString("Text2")));
                            int money = Integer.parseInt(DiamondChestShop.signTextToReadable(nbt.getString("Text3")));
                            DatabaseManager dm = new DatabaseManager();
                            BlockPos hangingPos = pos.add(state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetX(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetY(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetZ());
                            LootableContainerBlockEntity shop = (LootableContainerBlockEntity) world.getBlockEntity(hangingPos);
                            assert shop != null;
                            String owner = ((LockableContainerBlockEntityInterface) shop).diamondchestshop_getOwner();
                            Item sellItem = Registry.ITEM.get(Identifier.tryParse(((LockableContainerBlockEntityInterface) shop).diamondchestshop_getItem()));

                            if (dm.getBalanceFromUUID(player.getUuidAsString()) < money) {
                                player.sendMessage(new LiteralText("You don't have enough money"), true);
                                return;
                            }
                            if (dm.getBalanceFromUUID(owner) + money >= Integer.MAX_VALUE && !((SignBlockEntityInterface) be).diamondchestshop_getAdminShop()) {
                                player.sendMessage(new LiteralText("The owner is too rich"), true);
                                return;
                            }

                            //check shop has item in proper quantity
                            if (!((SignBlockEntityInterface) be).diamondchestshop_getAdminShop()) {
                                Block shopBlock = world.getBlockState(hangingPos).getBlock();
                                Inventory inventory;
                                if (shop instanceof ChestBlockEntity && shopBlock instanceof ChestBlock) {
                                    inventory = ChestBlock.getInventory((ChestBlock)shopBlock, world.getBlockState(hangingPos), world, hangingPos, true);
                                } else {
                                    inventory = shop;
                                }

                                int itemCount = 0;
                                for (int i = 0; i < inventory.size(); i++) {
                                    if (inventory.getStack(i).getItem().equals(sellItem) && (!inventory.getStack(i).hasNbt() || inventory.getStack(i).getNbt().asString().equals(((LockableContainerBlockEntityInterface) shop).diamondchestshop_getNbt()))) {
                                        itemCount += inventory.getStack(i).getCount();
                                    }
                                }
                                if (itemCount < quantity) {
                                    player.sendMessage(new LiteralText("The shop is sold out"), true);
                                    return;
                                }

                                //take items from chest
                                itemCount = quantity;
                                for (int i = 0; i < inventory.size(); i++) {
                                    if (inventory.getStack(i).getItem().equals(sellItem) && (!inventory.getStack(i).hasNbt() || inventory.getStack(i).getNbt().asString().equals(((LockableContainerBlockEntityInterface) shop).diamondchestshop_getNbt()))) {
                                        itemCount -= inventory.getStack(i).getCount();
                                        inventory.setStack(i, new ItemStack(Items.AIR));
                                        if (itemCount < 0) {
                                            ItemStack stack = new ItemStack(sellItem, Math.abs(itemCount));
                                            stack.setNbt(NbtHelper.fromNbtProviderString(((LockableContainerBlockEntityInterface) shop).diamondchestshop_getNbt()));
                                            inventory.setStack(i, stack);
                                            break;
                                        }
                                    }
                                }
                            }

                            //give the player the items
                            while (quantity > sellItem.getMaxCount()) {
                                ItemStack stack = new ItemStack(sellItem, sellItem.getMaxCount());
                                stack.setNbt(NbtHelper.fromNbtProviderString(((LockableContainerBlockEntityInterface) shop).diamondchestshop_getNbt()));
                                ItemEntity itemEntity = player.dropItem(stack, true);
                                assert itemEntity != null;
                                itemEntity.resetPickupDelay();
                                itemEntity.setOwner(player.getUuid());
                                quantity -= sellItem.getMaxCount();
                            }

                            ItemStack stack2 = new ItemStack(sellItem, quantity);
                            stack2.setNbt(NbtHelper.fromNbtProviderString(((LockableContainerBlockEntityInterface) shop).diamondchestshop_getNbt()));
                            ItemEntity itemEntity2 = player.dropItem(stack2, true);
                            assert itemEntity2 != null;
                            itemEntity2.resetPickupDelay();
                            itemEntity2.setOwner(player.getUuid());

                            //make the transaction
                            if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().transactionHistory) {
                                dm.createTransaction("send", player.getUuidAsString(), owner, money, -1);
                            }
                            dm.setBalance(player.getUuidAsString(), dm.getBalanceFromUUID(player.getUuidAsString()) - money);
                            if (!((SignBlockEntityInterface) be).diamondchestshop_getAdminShop()) {
                                dm.setBalance(owner, dm.getBalanceFromUUID(owner) + money);
                            }

                            player.sendMessage(new LiteralText("Bought " + quantity + " " + sellItem.getName().getString() + " for " + money + " " + DEConfig.getCurrencyName()), true);
                            return;
                        } catch (NumberFormatException | CommandSyntaxException ignored) {
                            return;
                        }
                    }

                    //buy shops
                    if (DiamondChestShop.signTextToReadable(nbt.getString("Text1")).contains("buy")) {
                        try {
                            int quantity = Integer.parseInt(DiamondChestShop.signTextToReadable(nbt.getString("Text2")));
                            int money = Integer.parseInt(DiamondChestShop.signTextToReadable(nbt.getString("Text3")));
                            DatabaseManager dm = new DatabaseManager();
                            BlockPos hangingPos = pos.add(state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetX(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetY(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetZ());
                            LootableContainerBlockEntity shop = (LootableContainerBlockEntity) world.getBlockEntity(hangingPos);
                            assert shop != null;
                            String owner = ((LockableContainerBlockEntityInterface) shop).diamondchestshop_getOwner();
                            Item buyItem = Registry.ITEM.get(Identifier.tryParse(((LockableContainerBlockEntityInterface) shop).diamondchestshop_getItem()));

                            if (dm.getBalanceFromUUID(owner) < money && !((SignBlockEntityInterface) be).diamondchestshop_getAdminShop()) {
                                player.sendMessage(new LiteralText("The owner hasn't got enough money"), true);
                                return;
                            }

                            if (dm.getBalanceFromUUID(player.getUuidAsString()) + money >= Integer.MAX_VALUE) {
                                player.sendMessage(new LiteralText("You are too rich"), true);
                                return;
                            }

                            //check player has item in proper quantity
                            int itemCount = 0;
                            for (int i = 0; i < player.getInventory().size(); i++) {
                                if (player.getInventory().getStack(i).getItem().equals(buyItem) && (!player.getInventory().getStack(i).hasNbt() || player.getInventory().getStack(i).getNbt().asString().equals(((LockableContainerBlockEntityInterface) shop).diamondchestshop_getNbt()))) {
                                    itemCount += player.getInventory().getStack(i).getCount();
                                }
                            }
                            if (itemCount < quantity) {
                                player.sendMessage(new LiteralText("You don't have enough of that item"), true);
                                return;
                            }
                            int emptySpaces = 0;
                            Block shopBlock = world.getBlockState(hangingPos).getBlock();
                            Inventory inventory;
                            if (shop instanceof ChestBlockEntity && shopBlock instanceof ChestBlock) {
                                inventory = ChestBlock.getInventory((ChestBlock)shopBlock, world.getBlockState(hangingPos), world, hangingPos, true);
                            } else {
                                inventory = shop;
                            }
                            for (int i = 0; i < inventory.size(); i++) {
                                if (inventory.getStack(i).getItem().equals(Items.AIR)) {
                                    emptySpaces += buyItem.getMaxCount();
                                    continue;
                                }
                                if (inventory.getStack(i).getItem().equals(buyItem)) {
                                    emptySpaces += buyItem.getMaxCount() - inventory.getStack(i).getCount();
                                }
                            }
                            if (emptySpaces < quantity) {
                                player.sendMessage(new LiteralText("The chest is full"), true);
                                return;
                            }

                            //take items from player
                            itemCount = quantity;
                            for (int i = 0; i < player.getInventory().size(); i++) {
                                if (player.getInventory().getStack(i).getItem().equals(buyItem) && (!player.getInventory().getStack(i).hasNbt() || player.getInventory().getStack(i).getNbt().asString().equals(((LockableContainerBlockEntityInterface) shop).diamondchestshop_getNbt()))) {
                                    itemCount -= player.getInventory().getStack(i).getCount();
                                    player.getInventory().setStack(i, new ItemStack(Items.AIR));
                                    if (itemCount < 0) {
                                        ItemStack stack = new ItemStack(buyItem, Math.abs(itemCount));
                                        stack.setNbt(NbtHelper.fromNbtProviderString(((LockableContainerBlockEntityInterface) shop).diamondchestshop_getNbt()));
                                        player.getInventory().setStack(i, stack);
                                        break;
                                    }
                                }
                            }

                            //give the chest the items
                            if (!((SignBlockEntityInterface) be).diamondchestshop_getAdminShop()) {
                                int itemsToAdd = quantity;
                                for (int i = 0; i < inventory.size(); i++) {
                                    if (inventory.getStack(i).getItem().equals(buyItem) && (!inventory.getStack(i).hasNbt() || inventory.getStack(i).getNbt().asString().equals(((LockableContainerBlockEntityInterface) shop).diamondchestshop_getNbt()))) {
                                        itemsToAdd += inventory.getStack(i).getCount();
                                        itemsToAdd -= buyItem.getMaxCount();
                                        ItemStack stack = new ItemStack(buyItem, buyItem.getMaxCount());
                                        stack.setNbt(NbtHelper.fromNbtProviderString(((LockableContainerBlockEntityInterface) shop).diamondchestshop_getNbt()));
                                        inventory.setStack(i, stack);
                                    }
                                    if (inventory.getStack(i).getItem().equals(Items.AIR)) {
                                        itemsToAdd -= buyItem.getMaxCount();
                                        ItemStack stack = new ItemStack(buyItem, buyItem.getMaxCount());
                                        stack.setNbt(NbtHelper.fromNbtProviderString(((LockableContainerBlockEntityInterface) shop).diamondchestshop_getNbt()));
                                        inventory.setStack(i, stack);
                                    }
                                    if (itemsToAdd < 0) {
                                        ItemStack stack = new ItemStack(buyItem, buyItem.getMaxCount() + itemsToAdd);
                                        stack.setNbt(NbtHelper.fromNbtProviderString(((LockableContainerBlockEntityInterface) shop).diamondchestshop_getNbt()));
                                        inventory.setStack(i, stack);
                                        break;
                                    }
                                }
                            }

                            //make the transaction
                            if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().transactionHistory) {
                                dm.createTransaction("send", owner, player.getUuidAsString(), money, -1);
                            }
                            if (!((SignBlockEntityInterface) be).diamondchestshop_getAdminShop()) {
                                dm.setBalance(owner, dm.getBalanceFromUUID(owner) - money);
                            }
                            dm.setBalance(player.getUuidAsString(), dm.getBalanceFromUUID(player.getUuidAsString()) + money);

                            player.sendMessage(new LiteralText("Sold " + quantity + " " + buyItem.getName().getString() + " for " + money + " " + DEConfig.getCurrencyName()), true);
                            return;
                        } catch (NumberFormatException | CommandSyntaxException ignored) {
                            return;
                        }
                    }
                }
            }

            //create the chest shop
            if (item.equals(DEConfig.getCurrency())) {
                if (nbt.getBoolean("diamondchestshop_IsShop")) {
                    player.sendMessage(new LiteralText("This is already a shop"), true);
                    return;
                }

                BlockPos hangingPos = pos.add(state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetX(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetY(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetZ());
                if (!(world.getBlockEntity(hangingPos) instanceof LockableContainerBlockEntity shop && world.getBlockEntity(hangingPos) instanceof LootableContainerBlockEntity)) {
                    player.sendMessage(new LiteralText("Sign must be on a valid container"), true);
                    return;
                }

                if (!nbt.getString("diamondchestshop_ShopOwner").equals(player.getUuidAsString()) || !((LockableContainerBlockEntityInterface) shop).diamondchestshop_getOwner().equals(player.getUuidAsString())) {
                    player.sendMessage(new LiteralText("You must have placed down the sign and chest"), true);
                    return;
                }

                if (player.getOffHandStack().getItem().equals(Items.AIR)) {
                    player.sendMessage(new LiteralText("The sell item must be in your offhand"), true);
                    return;
                }

                if (!(DiamondChestShop.signTextToReadable(nbt.getString("Text1")).equals("sell") || DiamondChestShop.signTextToReadable(nbt.getString("Text1")).equals("buy"))) {
                    player.sendMessage(new LiteralText("The first line must be either \"Buy\" or \"Sell\""), true);
                    return;
                }

                if (((LockableContainerBlockEntityInterface) shop).diamondchestshop_getShop()) {
                    player.sendMessage(new LiteralText("That chest already is a shop"), true);
                    return;
                }

                try {
                    int quantity = Integer.parseInt(DiamondChestShop.signTextToReadable(nbt.getString("Text2")));
                    int money = Integer.parseInt(DiamondChestShop.signTextToReadable(nbt.getString("Text3")));
                    if (quantity >= 1) {
                        if (money >= 0) {
                            ((SignBlockEntityInterface) be).diamondchestshop_setShop(true);
                            ((LockableContainerBlockEntityInterface) shop).diamondchestshop_setShop(true);
                            ((LockableContainerBlockEntityInterface) shop).diamondchestshop_setItem(Registry.ITEM.getId(player.getOffHandStack().getItem()).toString());
                            try {
                                ((LockableContainerBlockEntityInterface) shop).diamondchestshop_setNbt(player.getOffHandStack().getNbt().asString());
                            } catch (NullPointerException ignored) {
                                ((LockableContainerBlockEntityInterface) shop).diamondchestshop_setNbt("{}");
                            }
                            be.markDirty();
                            shop.markDirty();

                            ItemEntity itemEntity = EntityType.ITEM.create(world);
                            itemEntity.setStack(new ItemStack(player.getOffHandStack().getItem(), Math.min(quantity, player.getOffHandStack().getItem().getMaxCount())));
                            itemEntity.setNeverDespawn();
                            itemEntity.setPickupDelayInfinite();
                            itemEntity.setInvulnerable(true);
                            itemEntity.setNoGravity(true);
                            itemEntity.setPosition(new Vec3d(hangingPos.getX() + 0.5, hangingPos.getY() + 1.05, hangingPos.getZ() + 0.5));
                            ((ItemEntityInterface) itemEntity).diamondchestshop_setShop(true);
                            world.spawnEntity(itemEntity);
                            ((SignBlockEntityInterface) be).diamondchestshop_setItemEntity(itemEntity.getUuid());

                            player.sendMessage(new LiteralText("Created shop with " + quantity + " " + player.getOffHandStack().getItem().getName().getString() + (((nbt.getString("Text1")).contains("sell")) ? " sold for " : " bought for ") + money + " " + DEConfig.getCurrencyName()), true);
                        } else {
                            player.sendMessage(new LiteralText("Positive quantity required"), true);
                        }
                    } else {
                        player.sendMessage(new LiteralText("Negative prices are not allowed"), true);

                    }
                } catch (NumberFormatException ignored) {
                    player.sendMessage(new LiteralText("The second and third lines must be numbers (quantity then money)"), true);
                }
            }
        }
    }
}