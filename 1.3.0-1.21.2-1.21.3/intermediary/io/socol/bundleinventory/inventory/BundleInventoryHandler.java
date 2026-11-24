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
import net.minecraft.class_5536;
import net.minecraft.class_5537;
import net.minecraft.class_634;
import net.minecraft.class_746;
import net.minecraft.class_9276;
import net.minecraft.class_9837;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BundleInventoryHandler extends class_1703 {
   public static final int PLAYER_INVENTORY_FIRST_SLOT = 9;
   public static final int PLAYER_INVENTORY_SLOTS = 36;
   public static final int BUNDLE_INVENTORY_FIRST_SLOT = 45;
   private final class_1661 playerInventory;
   private final BundleInventory bundleInventory;
   private final int bundleSlotId;

   public BundleInventoryHandler(class_1661 playerInventory, BundleInventory bundleInventory, int syncId) {
      super(class_3917.field_17326, syncId);
      this.playerInventory = playerInventory;
      this.bundleInventory = bundleInventory;
      this.bundleSlotId = this.getPlayerInventoryOriginalBundleSlot(playerInventory.field_7546);
      int top = 176;
      int left = 8;

      for (int i = 0; i < 9; i++) {
         this.method_7621(new BundleInventoryHandler.DisabledSlot(playerInventory, i, -20, -20));
      }

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            int slotIndex = j + (i + 1) * 9;
            class_1735 slot = (class_1735)(slotIndex == bundleInventory.getPlayerInventoryBundleSlot()
               ? new BundleInventoryHandler.BundleItemSlot(playerInventory, slotIndex, left + j * 18, top + i * 18)
               : new class_1735(playerInventory, slotIndex, left + j * 18, top + i * 18));
            this.method_7621(slot);
         }
      }

      for (int i = 0; i < 9; i++) {
         class_1735 slot = (class_1735)(i == bundleInventory.getPlayerInventoryBundleSlot()
            ? new BundleInventoryHandler.BundleItemSlot(playerInventory, i, left + i * 18, top + 58)
            : new class_1735(playerInventory, i, left + i * 18, top + 58));
         this.method_7621(slot);
      }

      for (int i = 0; i < 8; i++) {
         for (int j = 0; j < 8; j++) {
            this.method_7621(new BundleInventoryHandler.BundleContentSlot(bundleInventory, j + i * 8, left + j * 18, 18 + i * 18));
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

   @Nullable
   private BundleInventoryHandler.Robot createRobot(class_746 player) {
      class_634 networkHandler = class_310.method_1551().method_1562();
      if (networkHandler == null) {
         return null;
      } else {
         return this.bundleSlotId < 0 ? null : new BundleInventoryHandler.Robot(player, networkHandler);
      }
   }

   public void handleBundleSlotAction(int bundleInnerSlot, int button, class_1713 actionType) {
      class_746 player = class_310.method_1551().field_1724;
      if (player != null) {
         if (button == 0 || button == 1) {
            class_5536 clickType = button == 0 ? class_5536.field_27013 : class_5536.field_27014;
            if (actionType == class_1713.field_7790) {
               this.handleBundleSlotClick(player, clickType, bundleInnerSlot);
            } else if (actionType == class_1713.field_7794) {
               this.quickMoveFromBundle(player, bundleInnerSlot);
            } else if (actionType == class_1713.field_7795) {
               this.dropItemFromBundle(player, bundleInnerSlot);
            }
         }
      }
   }

   public void handleBundleSlotClick(class_746 player, class_5536 clickType, int bundleInnerSlot) {
      BundleInventoryHandler.Robot robot = this.createRobot(player);
      if (robot != null) {
         class_1799 cursorStack = this.method_34255();
         if (cursorStack.method_7960()) {
            if (!this.hasItemInBundleInnerSlot(bundleInnerSlot)) {
               return;
            }

            robot.selectBundleSlot(bundleInnerSlot);
            robot.clickBundle(class_5536.field_27014);
         } else {
            if (cursorStack.method_7909() instanceof class_5537) {
               return;
            }

            robot.clickBundle(class_5536.field_27013);
         }
      }
   }

   public boolean quickMoveIntoBundle(class_746 player, int slotId) {
      if (!this.method_34255().method_7960()) {
         return false;
      } else {
         class_9276 content = this.bundleInventory.getBundleContent();
         if (content != null && content.method_57428().compareTo(Fraction.ONE) < 0) {
            BundleInventoryHandler.Robot robot = this.createRobot(player);
            if (robot == null) {
               return false;
            } else {
               robot.clickSlot(class_5536.field_27013, slotId);
               robot.clickBundle(class_5536.field_27013);
               robot.clickSlot(class_5536.field_27013, slotId);
               return true;
            }
         } else {
            return true;
         }
      }
   }

   public void quickMoveFromBundle(class_746 player, int bundleInnerSlot) {
      if (this.method_34255().method_7960()) {
         BundleInventoryHandler.Robot robot = this.createRobot(player);
         if (robot != null) {
            if (this.hasItemInBundleInnerSlot(bundleInnerSlot)) {
               int targetPlayerSlot = this.findLastFreePlayerInventorySlot();
               if (targetPlayerSlot >= 0) {
                  robot.selectBundleSlot(bundleInnerSlot);
                  robot.clickBundle(class_5536.field_27014);
                  robot.clickSlot(class_5536.field_27013, targetPlayerSlot);
               }
            }
         }
      }
   }

   public void dropItemFromBundle(class_746 player, int bundleInnerSlot) {
      if (this.method_34255().method_7960()) {
         BundleInventoryHandler.Robot robot = this.createRobot(player);
         if (robot != null) {
            if (this.hasItemInBundleInnerSlot(bundleInnerSlot)) {
               robot.selectBundleSlot(bundleInnerSlot);
               robot.clickBundle(class_5536.field_27014);
               robot.dropItemFromCursor();
            }
         }
      }
   }

   private boolean hasItemInBundleInnerSlot(int bundleInnerSlot) {
      class_9276 content = this.bundleInventory.getBundleContent();
      return content != null && bundleInnerSlot < content.method_57426();
   }

   private int findLastFreePlayerInventorySlot() {
      for (int slotId = 44; slotId >= 9; slotId--) {
         if (!this.method_7611(slotId).method_7681()) {
            return slotId;
         }
      }

      return -1;
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
      class_746 player = class_310.method_1551().field_1724;
      return player != null && player.field_7512 != null ? player.field_7512.method_34255() : class_1799.field_8037;
   }

   public void method_34254(class_1799 stack) {
      class_746 player = class_310.method_1551().field_1724;
      if (player != null && player.field_7512 != null) {
         player.field_7512.method_34254(stack);
      }
   }

   @Environment(EnvType.CLIENT)
   static class BundleContentSlot extends class_1735 {
      public BundleContentSlot(class_1263 inventory, int index, int x, int y) {
         super(inventory, index, x, y);
      }

      public boolean method_7680(class_1799 stack) {
         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   static class BundleItemSlot extends class_1735 {
      public BundleItemSlot(class_1263 inventory, int index, int x, int y) {
         super(inventory, index, x, y);
      }
   }

   @Environment(EnvType.CLIENT)
   static class DisabledSlot extends class_1735 {
      public DisabledSlot(class_1263 inventory, int index, int x, int y) {
         super(inventory, index, x, y);
      }

      public boolean method_7682() {
         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   class Robot {
      class_746 player;
      class_634 networkHandler;

      public Robot(class_746 player, class_634 networkHandler) {
         this.player = player;
         this.networkHandler = networkHandler;
      }

      public BundleInventoryHandler.Robot selectBundleSlot(int innerSlotIndex) {
         this.networkHandler.method_52787(new class_9837(BundleInventoryHandler.this.bundleSlotId, innerSlotIndex));
         return this;
      }

      public BundleInventoryHandler.Robot clickSlot(class_5536 clickType, int slotId) {
         int button = clickType == class_5536.field_27013 ? 0 : 1;
         this.networkHandler
            .method_52787(
               new class_2813(
                  BundleInventoryHandler.this.field_7763, -1, slotId, button, class_1713.field_7790, class_1799.field_8037, new Int2ObjectOpenHashMap()
               )
            );
         return this;
      }

      public BundleInventoryHandler.Robot clickBundle(class_5536 clickType) {
         return this.clickSlot(clickType, BundleInventoryHandler.this.bundleSlotId);
      }

      public BundleInventoryHandler.Robot dropItemFromCursor() {
         return this.clickSlot(class_5536.field_27013, -999);
      }
   }
}
