package io.socol.bundleinventory;

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
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class Hooks {
   public Hooks() {
   }

   @Nullable
   public static class_1269 onItemInteract(class_1657 player, class_1268 hand) {
      class_1799 itemStack = player.method_5998(hand);
      if (player.method_5715()) {
         return null;
      } else if (!(itemStack.method_7909() instanceof class_5537)) {
         return null;
      } else {
         int bundleSlot = hand == class_1268.field_5808 ? player.method_31548().field_7545 : 40;
         return BundleInventoryMod.openBundleInventory(bundleSlot, false) ? class_1269.field_5812 : null;
      }
   }

   public static boolean onScreenSlotClick(class_1703 handler, class_1735 slot, int slotId, int button, class_1713 actionType) {
      class_746 player = class_310.method_1551().field_1724;
      if (player == null || slot == null) {
         return false;
      } else if (handler != player.field_7498) {
         return false;
      } else if (slot.field_7871 != player.method_31548()) {
         return false;
      } else if (slot.method_34266() >= 36 || slotId < 9 || slotId >= 45) {
         return false;
      } else if (actionType != class_1713.field_7794 || button != 1) {
         return false;
      } else if (!handler.method_34255().method_7960()) {
         return false;
      } else if (slot.method_7681() && slot.method_7677().method_7909() instanceof class_5537) {
         int bundleSlot = slot.method_34266();
         return BundleInventoryMod.openBundleInventory(bundleSlot, true);
      } else {
         return false;
      }
   }
}
