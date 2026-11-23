package io.socol.bundleinventory;

import io.socol.bundleinventory.inventory.BundleInventoryHandler;
import io.socol.bundleinventory.inventory.BundleInventoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.SlotWrapper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class Hooks {
   @Nullable
   public static InteractionResult onItemInteract(Player player, InteractionHand hand) {
      if (player == null || hand == null) {
         return null;
      }

      if (player.isShiftKeyDown()) {
         return null;
      }

      ItemStack heldStack = player.getItemInHand(hand);
      if (!(heldStack.getItem() instanceof BundleItem)) {
         return null;
      }

      int bundleSlot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : 40;
      return BundleInventoryMod.openBundleInventory(bundleSlot, false) ? InteractionResult.SUCCESS : null;
   }

   public static boolean onClickSlot(int syncId, int slotId, int button, ClickType actionType, Player player) {
      if (!(player instanceof LocalPlayer)) {
         return false;
      }

      Minecraft mc = Minecraft.getInstance();
      if (!(mc.screen instanceof BundleInventoryScreen)) {
         return false;
      }

      if (player.containerMenu.containerId != syncId) {
         return false;
      }

      BundleInventoryHandler handler = (BundleInventoryHandler)((BundleInventoryScreen)mc.screen).getMenu();
      return handler.handlePlayerSlotClick(slotId, button, actionType);
   }

   public static boolean onScreenSlotClick(AbstractContainerMenu handler, Slot slot, int slotId, int button, ClickType actionType) {
      Minecraft client = Minecraft.getInstance();
      LocalPlayer player = client.player;
      if (player != null && slot != null && handler != null) {
         boolean isCreativeInventory = handler instanceof ItemPickerMenu;
         if (isCreativeInventory && slot instanceof SlotWrapper creativeSlot) {
            slot = creativeSlot.target;
            slotId = slot.index;
         }

         if (slot.container != player.getInventory()) {
            return false;
         }

         if (slot.getContainerSlot() >= 36) {
            return false;
         }

         if (actionType != ClickType.QUICK_MOVE || button != 1) {
            return false;
         }

         if (!handler.getCarried().isEmpty()) {
            return false;
         }

         if (slot.hasItem() && slot.getItem().getItem() instanceof BundleItem) {
            boolean openInventoryOnClose = true;
            if (player.containerMenu != player.inventoryMenu) {
               player.closeContainer();
               openInventoryOnClose = isCreativeInventory;
            }

            int bundleSlot = slot.getContainerSlot();
            return BundleInventoryMod.openBundleInventory(bundleSlot, openInventoryOnClose);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
