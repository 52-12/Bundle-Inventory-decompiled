package io.socol.bundleinventory.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1263;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1723;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2813;
import net.minecraft.class_310;
import net.minecraft.class_3917;
import net.minecraft.class_5537;
import net.minecraft.class_634;
import net.minecraft.class_746;
import net.minecraft.class_9276;
import net.minecraft.class_9837;

@Environment(EnvType.CLIENT)
public class BundleInventoryHandler extends class_1703 {
   public static final int PLAYER_INVENTORY_SLOTS = 45;
   private final BundleInventory bundleInventory;

   public BundleInventoryHandler(class_1661 playerInventory, BundleInventory bundleInventory, int syncId) {
      super(class_3917.field_17326, syncId);
      this.bundleInventory = bundleInventory;
      int top = 176;
      int left = 8;

      for (int i = 0; i < 9; i++) {
         class_1735 slot = new class_1735(playerInventory, i, -10000, -10000);
         this.method_7621(slot);
      }

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            int slotIndex = j + (i + 1) * 9;
            class_1735 slot = (class_1735)(slotIndex == bundleInventory.getPlayerInventoryBundleSlot()
               ? new BundleInventoryHandler.BundleSlot(playerInventory, slotIndex, left + j * 18, top + i * 18)
               : new class_1735(playerInventory, slotIndex, left + j * 18, top + i * 18));
            this.method_7621(slot);
         }
      }

      for (int i = 0; i < 9; i++) {
         class_1735 slot = (class_1735)(i == bundleInventory.getPlayerInventoryBundleSlot()
            ? new BundleInventoryHandler.BundleSlot(playerInventory, i, left + i * 18, top + 58)
            : new class_1735(playerInventory, i, left + i * 18, top + 58));
         this.method_7621(slot);
      }

      int var10 = 17;

      for (int i = 0; i < 8; i++) {
         for (int j = 0; j < 8; j++) {
            this.method_7621(new class_1735(bundleInventory, j + i * 8, var10 + j * 18, 18 + i * 18));
         }
      }
   }

   public BundleInventory getBundleInventory() {
      return this.bundleInventory;
   }

   public int getPlayerInventoryBundleSlot() {
      return this.bundleInventory.getPlayerInventoryBundleSlot();
   }

   public int getPlayerInventoryOriginalBundleSlot(class_1657 player) {
      class_1703 invHandler = player.field_7512;
      if (invHandler instanceof class_1723) {
         for (class_1735 slot : invHandler.field_7761) {
            if (slot.field_7871 == player.method_31548() && slot.method_34266() == this.getPlayerInventoryBundleSlot()) {
               return slot.field_7874;
            }
         }
      }

      return -1;
   }

   public class_2561 getBundleTitle() {
      class_1799 bundle = this.getBundleInventory().getBundleStack();
      return (class_2561)(bundle.method_7960() ? class_2561.method_43473() : bundle.method_7964());
   }

   public void handleBundleSlotClick(class_1735 slot, int slotIndex, int button, class_1713 actionType) {
      class_746 player = class_310.method_1551().field_1724;
      if (player != null) {
         int originalBundleSlotId = this.getPlayerInventoryOriginalBundleSlot(player);
         if (originalBundleSlotId >= 0) {
            class_634 networkHandler = class_310.method_1551().method_1562();
            if (networkHandler != null) {
               if (actionType == class_1713.field_7790) {
                  class_1799 cursorStack = this.method_34255();
                  if (cursorStack.method_7960()) {
                     class_9276 data = this.bundleInventory.getBundleContent();
                     if (data == null || slotIndex >= data.method_57426()) {
                        return;
                     }

                     networkHandler.method_52787(new class_9837(originalBundleSlotId, slotIndex));
                     networkHandler.method_52787(
                        new class_2813(
                           player.field_7512.field_7763,
                           -1,
                           originalBundleSlotId,
                           1,
                           class_1713.field_7790,
                           data.method_57422(slotIndex),
                           new Int2ObjectOpenHashMap()
                        )
                     );
                  } else {
                     if (cursorStack.method_7909() instanceof class_5537) {
                        return;
                     }

                     networkHandler.method_52787(
                        new class_2813(
                           player.field_7512.field_7763, -1, originalBundleSlotId, 0, class_1713.field_7790, class_1799.field_8037, new Int2ObjectOpenHashMap()
                        )
                     );
                  }
               }
            }
         }
      }
   }

   public class_1799 method_7601(class_1657 player, int slot) {
      class_1799 itemStack = class_1799.field_8037;
      class_1735 slot2 = (class_1735)this.field_7761.get(slot);
      if (slot2 != null && slot2.method_7681()) {
         class_1799 itemStack2 = slot2.method_7677();
         itemStack = itemStack2.method_7972();
         if (!this.method_7616(itemStack2, 0, 0, false)) {
            return class_1799.field_8037;
         }

         if (itemStack2.method_7960()) {
            slot2.method_53512(class_1799.field_8037);
         } else {
            slot2.method_7668();
         }
      }

      return itemStack;
   }

   public boolean method_7597(class_1657 player) {
      return this.bundleInventory.method_5443(player);
   }

   public class_1799 method_34255() {
      return class_310.method_1551().field_1724.field_7512.method_34255();
   }

   public void method_34254(class_1799 stack) {
      class_310.method_1551().field_1724.field_7512.method_34254(stack);
   }

   @Environment(EnvType.CLIENT)
   static class BundleSlot extends class_1735 {
      public BundleSlot(class_1263 inventory, int index, int x, int y) {
         super(inventory, index, x, y);
      }
   }
}
