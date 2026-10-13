package io.socol.bundleinventory;

import io.socol.bundleinventory.inventory.BundleInventoryHandler;
import io.socol.bundleinventory.inventory.BundleInventoryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_5537;
import net.minecraft.class_746;
import net.minecraft.class_481.class_483;
import net.minecraft.class_481.class_484;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class Hooks {
   public Hooks() {
   }

   @Nullable
   public static class_1269 onItemInteract(class_1657 player, class_1268 hand) {
      if (player == null || hand == null) {
         return null;
      }

      if (player.method_5715()) {
         return null;
      }

      class_1799 heldStack = player.method_5998(hand);
      if (!(heldStack.method_7909() instanceof class_5537)) {
         return null;
      }

      int bundleSlot = hand == class_1268.field_5808 ? player.method_31548().method_67532() : 40;
      return BundleInventoryMod.openBundleInventory(bundleSlot, false) ? class_1269.field_5812 : null;
   }

   public static boolean onClickSlot(int syncId, int slotId, int button, class_1713 actionType, class_1657 player) {
      if (!(player instanceof class_746)) {
         return false;
      }

      class_310 mc = class_310.method_1551();
      if (!(mc.field_1755 instanceof BundleInventoryScreen)) {
         return false;
      }

      if (player.field_7512.field_7763 != syncId) {
         return false;
      }

      BundleInventoryHandler handler = (BundleInventoryHandler)((BundleInventoryScreen)mc.field_1755).method_17577();
      return handler.handlePlayerSlotClick(slotId, button, actionType);
   }

   public static boolean onScreenSlotClick(class_1703 handler, class_1735 slot, int slotId, int button, class_1713 actionType) {
      class_310 client = class_310.method_1551();
      class_746 player = client.field_1724;
      if (player != null && slot != null && handler != null) {
         boolean isCreativeInventory = handler instanceof class_483;
         if (isCreativeInventory && slot instanceof class_484 creativeSlot) {
            slot = creativeSlot.field_2898;
            slotId = slot.field_7874;
         }

         if (slot.field_7871 != player.method_31548()) {
            return false;
         }

         if (slot.method_34266() >= 36 || slotId < 9 || slotId >= 45) {
            return false;
         }

         if (actionType != class_1713.field_7794 || button != 1) {
            return false;
         }

         if (!handler.method_34255().method_7960()) {
            return false;
         }

         if (slot.method_7681() && slot.method_7677().method_7909() instanceof class_5537) {
            boolean openInventoryOnClose = true;
            if (player.field_7512 != player.field_7498) {
               player.method_7346();
               openInventoryOnClose = isCreativeInventory;
            }

            int bundleSlot = slot.method_34266();
            return BundleInventoryMod.openBundleInventory(bundleSlot, openInventoryOnClose);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
