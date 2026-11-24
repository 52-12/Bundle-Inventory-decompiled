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
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class Hooks {
   public Hooks() {
   }

   @Nullable
   public static ActionResult onItemInteract(PlayerEntity player, Hand hand) {
      ItemStack itemStack = player.getStackInHand(hand);
      if (player.isSneaking()) {
         return null;
      } else if (!(itemStack.getItem() instanceof BundleItem)) {
         return null;
      } else {
         int bundleSlot = hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
         return BundleInventoryMod.openBundleInventory(bundleSlot, false) ? ActionResult.SUCCESS : null;
      }
   }

   public static boolean onScreenSlotClick(ScreenHandler handler, Slot slot, int slotId, int button, SlotActionType actionType) {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player == null || slot == null) {
         return false;
      } else if (handler != player.playerScreenHandler) {
         return false;
      } else if (slot.inventory != player.getInventory()) {
         return false;
      } else if (slot.getIndex() >= 36 || slotId < 9 || slotId >= 45) {
         return false;
      } else if (actionType != SlotActionType.QUICK_MOVE || button != 1) {
         return false;
      } else if (!handler.getCursorStack().isEmpty()) {
         return false;
      } else if (slot.hasStack() && slot.getStack().getItem() instanceof BundleItem) {
         int bundleSlot = slot.getIndex();
         return BundleInventoryMod.openBundleInventory(bundleSlot, true);
      } else {
         return false;
      }
   }
}
