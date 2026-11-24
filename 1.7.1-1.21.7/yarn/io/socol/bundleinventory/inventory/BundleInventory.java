package io.socol.bundleinventory.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.BundleItem;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.DataComponentTypes;

@Environment(EnvType.CLIENT)
public class BundleInventory implements Inventory {
   private final PlayerEntity player;
   private final int playerInventoryBundleSlot;

   public BundleInventory(PlayerEntity player, int playerInventoryBundleSlot) {
      this.player = player;
      this.playerInventoryBundleSlot = playerInventoryBundleSlot;
   }

   public int getPlayerInventoryBundleSlot() {
      return this.playerInventoryBundleSlot;
   }

   public ItemStack getBundleStack() {
      ItemStack stack = this.player.getInventory().getStack(this.playerInventoryBundleSlot);
      if (stack.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         return stack.getItem() instanceof BundleItem ? stack : ItemStack.EMPTY;
      }
   }

   public BundleContentsComponent getBundleContent() {
      ItemStack stack = this.getBundleStack();
      return stack.isEmpty() ? null : (BundleContentsComponent)stack.get(DataComponentTypes.BUNDLE_CONTENTS);
   }

   public int size() {
      BundleContentsComponent content = this.getBundleContent();
      return content != null ? content.size() : 0;
   }

   public boolean isEmpty() {
      BundleContentsComponent content = this.getBundleContent();
      return content == null || content.isEmpty();
   }

   public ItemStack getStack(int slot) {
      BundleContentsComponent content = this.getBundleContent();
      return content != null && slot < content.size() ? content.get(slot) : ItemStack.EMPTY;
   }

   public ItemStack removeStack(int slot, int amount) {
      return ItemStack.EMPTY;
   }

   public ItemStack removeStack(int slot) {
      return ItemStack.EMPTY;
   }

   public void setStack(int slot, ItemStack stack) {
   }

   public void markDirty() {
   }

   public boolean canPlayerUse(PlayerEntity player) {
      return !this.getBundleStack().isEmpty();
   }

   public void clear() {
   }
}
