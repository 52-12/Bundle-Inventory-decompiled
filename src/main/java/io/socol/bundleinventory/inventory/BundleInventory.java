package io.socol.bundleinventory.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

@Environment(EnvType.CLIENT)
public class BundleInventory implements Container {
   private final Player player;
   private final int playerInventoryBundleSlot;

   public BundleInventory(Player player, int playerInventoryBundleSlot) {
      this.player = player;
      this.playerInventoryBundleSlot = playerInventoryBundleSlot;
   }

   public int getPlayerInventoryBundleSlot() {
      return this.playerInventoryBundleSlot;
   }

   public ItemStack getBundleStack() {
      ItemStack stack = this.player.getInventory().getItem(this.playerInventoryBundleSlot);
      if (stack.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         return stack.getItem() instanceof BundleItem ? stack : ItemStack.EMPTY;
      }
   }

   public BundleContents getBundleContent() {
      ItemStack stack = this.getBundleStack();
      return stack.isEmpty() ? null : (BundleContents)stack.get(DataComponents.BUNDLE_CONTENTS);
   }

   public int getContainerSize() {
      BundleContents content = this.getBundleContent();
      return content != null ? content.size() : 0;
   }

   public boolean isEmpty() {
      BundleContents content = this.getBundleContent();
      return content == null || content.isEmpty();
   }

   public ItemStack getItem(int slot) {
      BundleContents content = this.getBundleContent();
      return content != null && slot < content.size() ? content.items().get(slot).create() : ItemStack.EMPTY;
   }

   public ItemStack removeItem(int slot, int amount) {
      return ItemStack.EMPTY;
   }

   public ItemStack removeItemNoUpdate(int slot) {
      return ItemStack.EMPTY;
   }

   public void setItem(int slot, ItemStack stack) {
   }

   public void setChanged() {
   }

   public boolean stillValid(Player player) {
      return !this.getBundleStack().isEmpty();
   }

   public void clearContent() {
   }
}
