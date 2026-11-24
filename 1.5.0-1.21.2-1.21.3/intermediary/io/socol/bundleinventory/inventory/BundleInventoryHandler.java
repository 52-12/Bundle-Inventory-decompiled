package io.socol.bundleinventory.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1263;
import net.minecraft.class_1297;
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
import net.minecraft.class_3417;
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
   public static final int BUNDLE_INVENTORY_FIRST_SLOT = 45;
   public static final int BUNDLE_ROW_LENGTH = 8;
   public static final int BUNDLE_ROWS = 6;
   private final class_1661 playerInventory;
   private final BundleInventory bundleInventory;
   private final int bundleSlotId;
   private int selectedRow;
   private int rowsCount;

   public BundleInventoryHandler(class_1661 playerInventory, BundleInventory bundleInventory, int syncId) {
      super(class_3917.field_17326, syncId);
      this.playerInventory = playerInventory;
      this.bundleInventory = bundleInventory;
      this.bundleSlotId = this.getPlayerInventoryOriginalBundleSlot(playerInventory.field_7546);
      int top = 140;
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

      for (int i = 0; i < 6; i++) {
         for (int j = 0; j < 8; j++) {
            this.method_7621(new BundleInventoryHandler.BundleContentSlot(bundleInventory, j + i * 8, left + j * 18, 18 + i * 18));
         }
      }

      playerOpenSound(playerInventory.field_7546);
      this.updateRowsCount();
   }

   private void updateRowsCount() {
      class_9276 content = this.getBundleInventory().getBundleContent();
      if (content == null) {
         this.rowsCount = 1;
      } else {
         int rowsForContent = content.method_57426() / 8;
         if (content.method_57426() % 8 != 0 || !this.isBundleFull()) {
            rowsForContent++;
         }

         this.rowsCount = Math.max(0, rowsForContent - 6) + 1;
      }

      this.setSelectedRow(this.selectedRow);
   }

   public int setSelectedRow(int selectedRow) {
      this.selectedRow = Math.clamp((long)selectedRow, 0, this.rowsCount - 1);
      return this.selectedRow;
   }

   public int getRowsCount() {
      return this.rowsCount;
   }

   public int getSelectedRow() {
      return this.selectedRow;
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
            bundleInnerSlot += this.getBundleContentOffset();
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
            playRemoveOneSound(player);
         } else {
            if (cursorStack.method_7909() instanceof class_5537) {
               return;
            }

            if (this.isBundleFull()) {
               playInsertFailSound(player);
               return;
            }

            robot.clickBundle(class_5536.field_27013);
            playInsertSound(player);
         }
      }
   }

   public boolean quickMoveIntoBundle(class_746 player, int slotId) {
      if (!this.method_34255().method_7960()) {
         return false;
      } else if (this.isBundleFull()) {
         playInsertFailSound(player);
         return true;
      } else {
         BundleInventoryHandler.Robot robot = this.createRobot(player);
         if (robot == null) {
            return false;
         } else {
            class_1799 stackToMove = this.method_7611(slotId).method_7677();
            if (stackToMove.method_7909() instanceof class_5537) {
               return true;
            } else {
               robot.clickSlot(class_5536.field_27013, slotId);
               robot.clickBundle(class_5536.field_27013);
               robot.clickSlot(class_5536.field_27013, slotId);
               playInsertSound(player);
               return true;
            }
         }
      }
   }

   public void quickMoveFromBundle(class_746 player, int bundleInnerSlot) {
      if (this.method_34255().method_7960()) {
         BundleInventoryHandler.Robot robot = this.createRobot(player);
         if (robot != null) {
            class_1799 stackToMove = this.getBundleContentItem(bundleInnerSlot);
            if (!stackToMove.method_7960()) {
               int targetPlayerSlot = this.findSlotToPutStackInPlayerInventory(stackToMove);
               if (targetPlayerSlot >= 0) {
                  robot.selectBundleSlot(bundleInnerSlot);
                  robot.clickBundle(class_5536.field_27014);
                  robot.clickSlot(class_5536.field_27013, targetPlayerSlot);
                  playRemoveOneSound(player);
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
               playDropContentsSound(player);
            }
         }
      }
   }

   private boolean isBundleFull() {
      class_9276 content = this.bundleInventory.getBundleContent();
      return content == null || content.method_57428().compareTo(Fraction.ONE) >= 0;
   }

   private boolean hasItemInBundleInnerSlot(int bundleInnerSlot) {
      class_9276 content = this.bundleInventory.getBundleContent();
      return content != null && bundleInnerSlot < content.method_57426();
   }

   private class_1799 getBundleContentItem(int bundleInnerSlot) {
      class_9276 content = this.bundleInventory.getBundleContent();
      return content != null && bundleInnerSlot < content.method_57426() ? content.method_57422(bundleInnerSlot) : class_1799.field_8037;
   }

   private int findSlotToPutStackInPlayerInventory(class_1799 addStack) {
      for (int slotId = 44; slotId >= 9; slotId--) {
         class_1799 currentStack = this.method_7611(slotId).method_7677();
         if (!currentStack.method_7960()
            && class_1799.method_31577(currentStack, addStack)
            && addStack.method_7947() + currentStack.method_7947() <= currentStack.method_7914()) {
            return slotId;
         }
      }

      return this.findLastFreePlayerInventorySlot();
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

   private int getBundleContentOffset() {
      return this.selectedRow * 8;
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

   public void tick() {
      this.updateRowsCount();
   }

   void init(class_746 player) {
      playerOpenSound(player);
   }

   void close(class_746 player) {
      playCloseSound(player);
   }

   private static void playerOpenSound(class_1297 entity) {
      entity.method_5783(class_3417.field_34377, 0.8F, 0.7F + entity.method_37908().method_8409().method_43057() * 0.1F);
   }

   private static void playCloseSound(class_1297 entity) {
      entity.method_5783(class_3417.field_34376, 0.8F, 0.7F + entity.method_37908().method_8409().method_43057() * 0.1F);
   }

   private static void playRemoveOneSound(class_1297 entity) {
      entity.method_5783(class_3417.field_34377, 0.8F, 0.8F + entity.method_37908().method_8409().method_43057() * 0.4F);
   }

   private static void playInsertSound(class_1297 entity) {
      entity.method_5783(class_3417.field_34376, 0.8F, 0.8F + entity.method_37908().method_8409().method_43057() * 0.4F);
   }

   private static void playInsertFailSound(class_1297 entity) {
      entity.method_5783(class_3417.field_52372, 1.0F, 1.0F);
   }

   private static void playDropContentsSound(class_1297 entity) {
      entity.method_5783(class_3417.field_34375, 0.8F, 0.8F + entity.method_37908().method_8409().method_43057() * 0.4F);
   }

   @Environment(EnvType.CLIENT)
   class BundleContentSlot extends class_1735 {
      public BundleContentSlot(class_1263 inventory, int index, int x, int y) {
         super(inventory, index, x, y);
      }

      public boolean method_7680(class_1799 stack) {
         return false;
      }

      public class_1799 method_7677() {
         return this.field_7871.method_5438(BundleInventoryHandler.this.getBundleContentOffset() + this.method_34266());
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
