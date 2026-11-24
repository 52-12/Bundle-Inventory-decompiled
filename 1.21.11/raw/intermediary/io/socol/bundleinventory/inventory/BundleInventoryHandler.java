package io.socol.bundleinventory.inventory;

import io.socol.bundleinventory.BundleInventoryMod;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10938;
import net.minecraft.class_1263;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1723;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2371;
import net.minecraft.class_2561;
import net.minecraft.class_2813;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3917;
import net.minecraft.class_5536;
import net.minecraft.class_5537;
import net.minecraft.class_634;
import net.minecraft.class_746;
import net.minecraft.class_9276;
import net.minecraft.class_9692;
import net.minecraft.class_9837;
import net.minecraft.class_9276.class_9277;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BundleInventoryHandler extends class_1703 {
   public static final int PLAYER_INVENTORY_FIRST_SLOT = 9;
   public static final int PLAYER_INVENTORY_LAST_SLOT = 45;
   public static final int BUNDLE_ROW_LENGTH = 8;
   private static final Map<class_1304, class_2960> EMPTY_ARMOR_SLOT_TEXTURES = Map.of(
      class_1304.field_6166,
      class_1723.field_21672,
      class_1304.field_6172,
      class_1723.field_21671,
      class_1304.field_6174,
      class_1723.field_21670,
      class_1304.field_6169,
      class_1723.field_21669
   );
   private static final class_1304[] EQUIPMENT_SLOT_ORDER = new class_1304[]{
      class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166
   };
   private final class_1661 playerInventory;
   private final BundleInventory bundleInventory;
   private final int bundleSlotId;
   private final int rowsVisible;
   private final boolean openInventoryOnClose;
   private int selectedRow;
   private int rowsCount;
   private final class_2371<class_1735> bundleSlots = class_2371.method_10211();

   public BundleInventoryHandler(class_1661 playerInventory, BundleInventory bundleInventory, int syncId, int rowsVisible, boolean openInventoryOnClose) {
      super(class_3917.field_17326, syncId);
      this.playerInventory = playerInventory;
      this.bundleInventory = bundleInventory;
      this.bundleSlotId = this.getPlayerInventoryOriginalBundleSlot(playerInventory.field_7546);
      this.rowsVisible = Math.clamp(rowsVisible, 3, 8);
      this.openInventoryOnClose = openInventoryOnClose;
      final class_1657 player = playerInventory.field_7546;
      int top = 32 + rowsVisible * 18;
      int left = 8;
      int height = 114 + rowsVisible * 18;

      for (int i = 0; i < 5; i++) {
         this.method_7621(new BundleInventoryHandler.DisabledSlot(playerInventory, i, -20, -20));
      }

      if (BundleInventoryMod.config().showEquipmentSlotsInBundleInventory()) {
         for (int i = 0; i < 4; i++) {
            class_1304 equipmentSlot = EQUIPMENT_SLOT_ORDER[i];
            class_2960 background = EMPTY_ARMOR_SLOT_TEXTURES.get(equipmentSlot);
            this.method_7621(new class_9692(playerInventory, player, equipmentSlot, 39 - i, -28, height - 100 + i * 18, background));
         }
      } else {
         for (int i = 0; i < 4; i++) {
            this.method_7621(new BundleInventoryHandler.DisabledSlot(playerInventory, 5 + i, -20, -20));
         }
      }

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            int slotIndex = j + (i + 1) * 9;
            class_1735 slot = slotIndex == bundleInventory.getPlayerInventoryBundleSlot()
               ? new BundleInventoryHandler.BundleItemSlot(playerInventory, slotIndex, left + j * 18, top + i * 18)
               : new class_1735(playerInventory, slotIndex, left + j * 18, top + i * 18);
            this.method_7621(slot);
         }
      }

      for (int i = 0; i < 9; i++) {
         class_1735 slot = i == bundleInventory.getPlayerInventoryBundleSlot()
            ? new BundleInventoryHandler.BundleItemSlot(playerInventory, i, left + i * 18, top + 58)
            : new class_1735(playerInventory, i, left + i * 18, top + 58);
         this.method_7621(slot);
      }

      if (BundleInventoryMod.config().showEquipmentSlotsInBundleInventory()) {
         if (bundleInventory.getPlayerInventoryBundleSlot() == 40) {
            this.method_7621(new BundleInventoryHandler.BundleItemSlot(playerInventory, 40, -28, height - 24));
         } else {
            this.method_7621(new class_1735(playerInventory, 40, -28, height - 24) {
               public void method_48931(class_1799 stack, class_1799 previousStack) {
                  player.method_6116(class_1304.field_6171, previousStack, stack);
                  super.method_48931(stack, previousStack);
               }

               public class_2960 method_7679() {
                  return class_1723.field_21673;
               }
            });
         }
      } else {
         this.method_7621(new BundleInventoryHandler.DisabledSlot(playerInventory, 40, -20, -20));
      }

      for (int i = 0; i < rowsVisible; i++) {
         for (int j = 0; j < 8; j++) {
            BundleInventoryHandler.BundleContentSlot slot = new BundleInventoryHandler.BundleContentSlot(bundleInventory, j + i * 8, left + j * 18, 18 + i * 18);
            this.bundleSlots.add(slot);
         }
      }

      playerOpenSound(playerInventory.field_7546);
      this.updateRowsCount();
   }

   public class_2371<class_1735> getBundleSlots() {
      return this.bundleSlots;
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

         this.rowsCount = Math.max(0, rowsForContent - this.rowsVisible) + 1;
      }

      this.setSelectedRow(this.selectedRow);
   }

   public int setSelectedRow(int selectedRow) {
      this.selectedRow = Math.clamp(selectedRow, 0, this.rowsCount - 1);
      return this.selectedRow;
   }

   public int getRowsCount() {
      return this.rowsCount;
   }

   public int getRowsVisible() {
      return this.rowsVisible;
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

   public boolean handlePlayerSlotClick(int slotId, int button, class_1713 actionType) {
      if (slotId >= 0 && slotId < this.field_7761.size()) {
         class_1735 slot = this.method_7611(slotId);
         if (slot instanceof BundleInventoryHandler.BundleItemSlot) {
            return true;
         } else if (actionType == class_1713.field_7791 && this.getPlayerInventoryBundleSlot() == button) {
            return true;
         } else {
            class_746 player = class_310.method_1551().field_1724;
            if (player != null && slotId >= 9 && actionType == class_1713.field_7794) {
               class_5536 clickType = button == 0 ? class_5536.field_27013 : class_5536.field_27014;
               return slot.method_7681() && this.quickMoveIntoBundle(player, slotId, clickType);
            } else {
               return false;
            }
         }
      } else {
         return false;
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
            if (BundleInventoryMod.config().clientSideItemInsertionChecks()) {
               int canAddCount = this.checkStackCanBeAddedToBundle(cursorStack);
               if (canAddCount <= 0) {
                  playInsertFailSound(player);
                  return;
               }
            }

            boolean insertingBundle = cursorStack.method_7909() instanceof class_5537;
            if (insertingBundle) {
               robot.clickBundle(class_5536.field_27014);
               robot.clickBundle(class_5536.field_27013);
               robot.clickBundle(class_5536.field_27013);
            } else {
               robot.clickBundle(class_5536.field_27013);
            }

            playInsertSound(player);
         }
      }
   }

   public boolean quickMoveIntoBundle(class_746 player, int slotId, class_5536 clickType) {
      if (!this.method_34255().method_7960()) {
         return false;
      }

      BundleInventoryHandler.Robot robot = this.createRobot(player);
      if (robot == null) {
         return false;
      }

      class_1799 stackToMove = this.method_7611(slotId).method_7677();
      boolean movingBundle = stackToMove.method_7909() instanceof class_5537;
      if (movingBundle && clickType == class_5536.field_27014) {
         int playerInvSlot = this.method_7611(slotId).method_34266();
         if (!BundleInventoryMod.openBundleInventory(playerInvSlot, this.openInventoryOnClose)) {
            playInsertFailSound(player);
         }

         return true;
      } else {
         boolean putBackRemaining = true;
         if (BundleInventoryMod.config().clientSideItemInsertionChecks()) {
            int canAddCount = this.checkStackCanBeAddedToBundle(stackToMove);
            if (canAddCount <= 0) {
               playInsertFailSound(player);
               return true;
            }

            putBackRemaining = canAddCount < stackToMove.method_7947();
         }

         robot.clickSlot(class_5536.field_27013, slotId);
         if (movingBundle) {
            robot.clickBundle(class_5536.field_27014);
            robot.clickBundle(class_5536.field_27013);
            robot.clickBundle(class_5536.field_27013);
         } else {
            robot.clickBundle(class_5536.field_27013);
            if (putBackRemaining) {
               robot.clickSlot(class_5536.field_27013, slotId);
            }
         }

         playInsertSound(player);
         return true;
      }
   }

   public void quickMoveFromBundle(class_746 player, int bundleInnerSlot) {
      if (this.method_34255().method_7960()) {
         BundleInventoryHandler.Robot robot = this.createRobot(player);
         if (robot != null) {
            class_1799 stackToMove = this.getBundleContentItem(bundleInnerSlot);
            if (!stackToMove.method_7960()) {
               int targetPlayerSlot = this.findSlotToPutStackInPlayerInventory(stackToMove);
               if (targetPlayerSlot < 0) {
                  playInsertFailSound(player);
               } else {
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

   private int checkStackCanBeAddedToBundle(class_1799 stackToAdd) {
      class_9276 content = this.bundleInventory.getBundleContent();
      if (content == null) {
         return 0;
      }

      class_9277 builder = new class_9277(content);
      return builder.method_57432(stackToAdd.method_7972());
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

   void close(class_746 player) {
      playCloseSound(player);
   }

   private static void playerOpenSound(class_1297 entity) {
      playBundleSound(entity, class_3417.field_34377, 0.8F, 0.7F + entity.method_73183().method_8409().method_43057() * 0.1F);
   }

   private static void playCloseSound(class_1297 entity) {
      playBundleSound(entity, class_3417.field_34376, 0.8F, 0.7F + entity.method_73183().method_8409().method_43057() * 0.1F);
   }

   private static void playRemoveOneSound(class_1297 entity) {
      playBundleSound(entity, class_3417.field_34377, 0.8F, 0.8F + entity.method_73183().method_8409().method_43057() * 0.4F);
   }

   private static void playInsertSound(class_1297 entity) {
      playBundleSound(entity, class_3417.field_34376, 0.8F, 0.8F + entity.method_73183().method_8409().method_43057() * 0.4F);
   }

   private static void playInsertFailSound(class_1297 entity) {
      playBundleSound(entity, class_3417.field_52372, 1.0F, 1.0F);
   }

   private static void playDropContentsSound(class_1297 entity) {
      playBundleSound(entity, class_3417.field_34375, 0.8F, 0.8F + entity.method_73183().method_8409().method_43057() * 0.4F);
   }

   private static void playBundleSound(class_1297 entity, class_3414 sound, float volume, float pitch) {
      if (BundleInventoryMod.config().shouldPlayBundleInventorySounds()) {
         entity.method_5783(sound, volume, pitch);
      }
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
                  BundleInventoryHandler.this.field_7763,
                  -1,
                  (short)slotId,
                  (byte)button,
                  class_1713.field_7790,
                  new Int2ObjectOpenHashMap(),
                  class_10938.field_58176
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
