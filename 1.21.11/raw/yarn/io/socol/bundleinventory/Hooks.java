package io.socol.bundleinventory;

import io.socol.bundleinventory.inventory.BundleInventoryHandler;
import io.socol.bundleinventory.inventory.BundleInventoryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BundleItem;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeSlot;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class Hooks {
   public Hooks() {
   }

   @Nullable
   public static ActionResult onItemInteract(PlayerEntity player, Hand hand) {
      if (player == null || hand == null) {
         return null;
      }

      if (player.isSneaking()) {
         return null;
      }

      ItemStack heldStack = player.getStackInHand(hand);
      if (!(heldStack.getItem() instanceof BundleItem)) {
         return null;
      }

      int bundleSlot = hand == Hand.MAIN_HAND ? player.getInventory().getSelectedSlot() : 40;
      return BundleInventoryMod.openBundleInventory(bundleSlot, false) ? ActionResult.SUCCESS : null;
   }

   public static boolean onClickSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player) {
      if (!(player instanceof ClientPlayerEntity)) {
         return false;
      }

      MinecraftClient mc = MinecraftClient.getInstance();
      if (!(mc.currentScreen instanceof BundleInventoryScreen)) {
         return false;
      }

      if (player.currentScreenHandler.syncId != syncId) {
         return false;
      }

      BundleInventoryHandler handler = (BundleInventoryHandler)((BundleInventoryScreen)mc.currentScreen).getScreenHandler();
      return handler.handlePlayerSlotClick(slotId, button, actionType);
   }

   public static boolean onScreenSlotClick(ScreenHandler handler, Slot slot, int slotId, int button, SlotActionType actionType) {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity player = client.player;
      if (player != null && slot != null && handler != null) {
         boolean isCreativeInventory = handler instanceof CreativeScreenHandler;
         if (isCreativeInventory && slot instanceof CreativeSlot creativeSlot) {
            slot = creativeSlot.slot;
            slotId = slot.id;
         }

         if (slot.inventory != player.getInventory()) {
            return false;
         }

         if (slot.getIndex() >= 36 || slotId < 9 || slotId >= 45) {
            return false;
         }

         if (actionType != SlotActionType.QUICK_MOVE || button != 1) {
            return false;
         }

         if (!handler.getCursorStack().isEmpty()) {
            return false;
         }

         if (slot.hasStack() && slot.getStack().getItem() instanceof BundleItem) {
            boolean openInventoryOnClose = true;
            if (player.currentScreenHandler != player.playerScreenHandler) {
               player.closeHandledScreen();
               openInventoryOnClose = isCreativeInventory;
            }

            int bundleSlot = slot.getIndex();
            return BundleInventoryMod.openBundleInventory(bundleSlot, openInventoryOnClose);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
