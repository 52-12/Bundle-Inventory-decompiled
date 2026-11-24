package io.socol.bundleinventory;

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
      } else if (player.isSneaking()) {
         return null;
      } else {
         ItemStack heldStack = player.getStackInHand(hand);
         if (!(heldStack.getItem() instanceof BundleItem)) {
            return null;
         } else {
            int bundleSlot = hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
            return BundleInventoryMod.openBundleInventory(bundleSlot, false) ? ActionResult.SUCCESS : null;
         }
      }
   }

   public static boolean onScreenSlotClick(ScreenHandler handler, Slot slot, int slotId, int button, SlotActionType actionType) {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity player = client.player;
      if (player != null && slot != null && handler != null) {
         if (handler instanceof CreativeScreenHandler) {
            handler = player.playerScreenHandler;
         } else if (handler != player.playerScreenHandler) {
            return false;
         }

         if (slot instanceof CreativeSlot creativeSlot) {
            slot = creativeSlot.slot;
            slotId = slot.id;
         }

         if (slot.inventory != player.getInventory()) {
            return false;
         } else if (slot.getIndex() >= 36 || slotId < 9 || slotId >= 45) {
            return false;
         } else if (actionType != SlotActionType.QUICK_MOVE || button != 1) {
            return false;
         } else if (!handler.getCursorStack().isEmpty()) {
            return false;
         } else if (slot.hasStack() && slot.getStack().getItem() instanceof BundleItem) {
            player.currentScreenHandler = player.playerScreenHandler;
            int bundleSlot = slot.getIndex();
            return BundleInventoryMod.openBundleInventory(bundleSlot, true);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
