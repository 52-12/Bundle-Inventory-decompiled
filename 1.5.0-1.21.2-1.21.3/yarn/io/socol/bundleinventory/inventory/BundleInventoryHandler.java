package io.socol.bundleinventory.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.Inventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.ClickType;
import net.minecraft.item.BundleItem;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.network.packet.c2s.play.BundleItemSelectedC2SPacket;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BundleInventoryHandler extends ScreenHandler {
   public static final int PLAYER_INVENTORY_FIRST_SLOT = 9;
   public static final int BUNDLE_INVENTORY_FIRST_SLOT = 45;
   public static final int BUNDLE_ROW_LENGTH = 8;
   public static final int BUNDLE_ROWS = 6;
   private final PlayerInventory playerInventory;
   private final BundleInventory bundleInventory;
   private final int bundleSlotId;
   private int selectedRow;
   private int rowsCount;

   public BundleInventoryHandler(PlayerInventory playerInventory, BundleInventory bundleInventory, int syncId) {
      super(ScreenHandlerType.GENERIC_9X3, syncId);
      this.playerInventory = playerInventory;
      this.bundleInventory = bundleInventory;
      this.bundleSlotId = this.getPlayerInventoryOriginalBundleSlot(playerInventory.player);
      int top = 140;
      int left = 8;

      for (int i = 0; i < 9; i++) {
         this.addSlot(new BundleInventoryHandler.DisabledSlot(playerInventory, i, -20, -20));
      }

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            int slotIndex = j + (i + 1) * 9;
            Slot slot = (Slot)(slotIndex == bundleInventory.getPlayerInventoryBundleSlot()
               ? new BundleInventoryHandler.BundleItemSlot(playerInventory, slotIndex, left + j * 18, top + i * 18)
               : new Slot(playerInventory, slotIndex, left + j * 18, top + i * 18));
            this.addSlot(slot);
         }
      }

      for (int i = 0; i < 9; i++) {
         Slot slot = (Slot)(i == bundleInventory.getPlayerInventoryBundleSlot()
            ? new BundleInventoryHandler.BundleItemSlot(playerInventory, i, left + i * 18, top + 58)
            : new Slot(playerInventory, i, left + i * 18, top + 58));
         this.addSlot(slot);
      }

      for (int i = 0; i < 6; i++) {
         for (int j = 0; j < 8; j++) {
            this.addSlot(new BundleInventoryHandler.BundleContentSlot(bundleInventory, j + i * 8, left + j * 18, 18 + i * 18));
         }
      }

      playerOpenSound(playerInventory.player);
      this.updateRowsCount();
   }

   private void updateRowsCount() {
      BundleContentsComponent content = this.getBundleInventory().getBundleContent();
      if (content == null) {
         this.rowsCount = 1;
      } else {
         int rowsForContent = content.size() / 8;
         if (content.size() % 8 != 0 || !this.isBundleFull()) {
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

   public int getPlayerInventoryOriginalBundleSlot(PlayerEntity player) {
      ScreenHandler invHandler = player.currentScreenHandler;
      if (invHandler instanceof PlayerScreenHandler) {
         for (Slot slot : invHandler.slots) {
            if (slot.inventory == player.getInventory() && slot.getIndex() == this.getPlayerInventoryBundleSlot()) {
               return slot.id;
            }
         }
      }

      return -1;
   }

   public Text getBundleTitle() {
      ItemStack bundle = this.getBundleInventory().getBundleStack();
      return (Text)(bundle.isEmpty() ? Text.empty() : bundle.getName());
   }

   @Nullable
   private BundleInventoryHandler.Robot createRobot(ClientPlayerEntity player) {
      ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
      if (networkHandler == null) {
         return null;
      } else {
         return this.bundleSlotId < 0 ? null : new BundleInventoryHandler.Robot(player, networkHandler);
      }
   }

   public void handleBundleSlotAction(int bundleInnerSlot, int button, SlotActionType actionType) {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player != null) {
         if (button == 0 || button == 1) {
            ClickType clickType = button == 0 ? ClickType.LEFT : ClickType.RIGHT;
            bundleInnerSlot += this.getBundleContentOffset();
            if (actionType == SlotActionType.PICKUP) {
               this.handleBundleSlotClick(player, clickType, bundleInnerSlot);
            } else if (actionType == SlotActionType.QUICK_MOVE) {
               this.quickMoveFromBundle(player, bundleInnerSlot);
            } else if (actionType == SlotActionType.THROW) {
               this.dropItemFromBundle(player, bundleInnerSlot);
            }
         }
      }
   }

   public void handleBundleSlotClick(ClientPlayerEntity player, ClickType clickType, int bundleInnerSlot) {
      BundleInventoryHandler.Robot robot = this.createRobot(player);
      if (robot != null) {
         ItemStack cursorStack = this.getCursorStack();
         if (cursorStack.isEmpty()) {
            if (!this.hasItemInBundleInnerSlot(bundleInnerSlot)) {
               return;
            }

            robot.selectBundleSlot(bundleInnerSlot);
            robot.clickBundle(ClickType.RIGHT);
            playRemoveOneSound(player);
         } else {
            if (cursorStack.getItem() instanceof BundleItem) {
               return;
            }

            if (this.isBundleFull()) {
               playInsertFailSound(player);
               return;
            }

            robot.clickBundle(ClickType.LEFT);
            playInsertSound(player);
         }
      }
   }

   public boolean quickMoveIntoBundle(ClientPlayerEntity player, int slotId) {
      if (!this.getCursorStack().isEmpty()) {
         return false;
      } else if (this.isBundleFull()) {
         playInsertFailSound(player);
         return true;
      } else {
         BundleInventoryHandler.Robot robot = this.createRobot(player);
         if (robot == null) {
            return false;
         } else {
            ItemStack stackToMove = this.getSlot(slotId).getStack();
            if (stackToMove.getItem() instanceof BundleItem) {
               return true;
            } else {
               robot.clickSlot(ClickType.LEFT, slotId);
               robot.clickBundle(ClickType.LEFT);
               robot.clickSlot(ClickType.LEFT, slotId);
               playInsertSound(player);
               return true;
            }
         }
      }
   }

   public void quickMoveFromBundle(ClientPlayerEntity player, int bundleInnerSlot) {
      if (this.getCursorStack().isEmpty()) {
         BundleInventoryHandler.Robot robot = this.createRobot(player);
         if (robot != null) {
            ItemStack stackToMove = this.getBundleContentItem(bundleInnerSlot);
            if (!stackToMove.isEmpty()) {
               int targetPlayerSlot = this.findSlotToPutStackInPlayerInventory(stackToMove);
               if (targetPlayerSlot >= 0) {
                  robot.selectBundleSlot(bundleInnerSlot);
                  robot.clickBundle(ClickType.RIGHT);
                  robot.clickSlot(ClickType.LEFT, targetPlayerSlot);
                  playRemoveOneSound(player);
               }
            }
         }
      }
   }

   public void dropItemFromBundle(ClientPlayerEntity player, int bundleInnerSlot) {
      if (this.getCursorStack().isEmpty()) {
         BundleInventoryHandler.Robot robot = this.createRobot(player);
         if (robot != null) {
            if (this.hasItemInBundleInnerSlot(bundleInnerSlot)) {
               robot.selectBundleSlot(bundleInnerSlot);
               robot.clickBundle(ClickType.RIGHT);
               robot.dropItemFromCursor();
               playDropContentsSound(player);
            }
         }
      }
   }

   private boolean isBundleFull() {
      BundleContentsComponent content = this.bundleInventory.getBundleContent();
      return content == null || content.getOccupancy().compareTo(Fraction.ONE) >= 0;
   }

   private boolean hasItemInBundleInnerSlot(int bundleInnerSlot) {
      BundleContentsComponent content = this.bundleInventory.getBundleContent();
      return content != null && bundleInnerSlot < content.size();
   }

   private ItemStack getBundleContentItem(int bundleInnerSlot) {
      BundleContentsComponent content = this.bundleInventory.getBundleContent();
      return content != null && bundleInnerSlot < content.size() ? content.get(bundleInnerSlot) : ItemStack.EMPTY;
   }

   private int findSlotToPutStackInPlayerInventory(ItemStack addStack) {
      for (int slotId = 44; slotId >= 9; slotId--) {
         ItemStack currentStack = this.getSlot(slotId).getStack();
         if (!currentStack.isEmpty()
            && ItemStack.areItemsAndComponentsEqual(currentStack, addStack)
            && addStack.getCount() + currentStack.getCount() <= currentStack.getMaxCount()) {
            return slotId;
         }
      }

      return this.findLastFreePlayerInventorySlot();
   }

   private int findLastFreePlayerInventorySlot() {
      for (int slotId = 44; slotId >= 9; slotId--) {
         if (!this.getSlot(slotId).hasStack()) {
            return slotId;
         }
      }

      return -1;
   }

   public ItemStack quickMove(PlayerEntity player, int slot) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot2 = (Slot)this.slots.get(slot);
      if (slot2 != null && slot2.hasStack()) {
         ItemStack itemStack2 = slot2.getStack();
         itemStack = itemStack2.copy();
         if (!this.insertItem(itemStack2, 0, 0, false)) {
            return ItemStack.EMPTY;
         }

         if (itemStack2.isEmpty()) {
            slot2.setStack(ItemStack.EMPTY);
         } else {
            slot2.markDirty();
         }
      }

      return itemStack;
   }

   private int getBundleContentOffset() {
      return this.selectedRow * 8;
   }

   public boolean canUse(PlayerEntity player) {
      return this.bundleInventory.canPlayerUse(player);
   }

   public ItemStack getCursorStack() {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      return player != null && player.currentScreenHandler != null ? player.currentScreenHandler.getCursorStack() : ItemStack.EMPTY;
   }

   public void setCursorStack(ItemStack stack) {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player != null && player.currentScreenHandler != null) {
         player.currentScreenHandler.setCursorStack(stack);
      }
   }

   public void tick() {
      this.updateRowsCount();
   }

   void init(ClientPlayerEntity player) {
      playerOpenSound(player);
   }

   void close(ClientPlayerEntity player) {
      playCloseSound(player);
   }

   private static void playerOpenSound(Entity entity) {
      entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.7F + entity.getWorld().getRandom().nextFloat() * 0.1F);
   }

   private static void playCloseSound(Entity entity) {
      entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.7F + entity.getWorld().getRandom().nextFloat() * 0.1F);
   }

   private static void playRemoveOneSound(Entity entity) {
      entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
   }

   private static void playInsertSound(Entity entity) {
      entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
   }

   private static void playInsertFailSound(Entity entity) {
      entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
   }

   private static void playDropContentsSound(Entity entity) {
      entity.playSound(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
   }

   @Environment(EnvType.CLIENT)
   class BundleContentSlot extends Slot {
      public BundleContentSlot(Inventory inventory, int index, int x, int y) {
         super(inventory, index, x, y);
      }

      public boolean canInsert(ItemStack stack) {
         return false;
      }

      public ItemStack getStack() {
         return this.inventory.getStack(BundleInventoryHandler.this.getBundleContentOffset() + this.getIndex());
      }
   }

   @Environment(EnvType.CLIENT)
   static class BundleItemSlot extends Slot {
      public BundleItemSlot(Inventory inventory, int index, int x, int y) {
         super(inventory, index, x, y);
      }
   }

   @Environment(EnvType.CLIENT)
   static class DisabledSlot extends Slot {
      public DisabledSlot(Inventory inventory, int index, int x, int y) {
         super(inventory, index, x, y);
      }

      public boolean isEnabled() {
         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   class Robot {
      ClientPlayerEntity player;
      ClientPlayNetworkHandler networkHandler;

      public Robot(ClientPlayerEntity player, ClientPlayNetworkHandler networkHandler) {
         this.player = player;
         this.networkHandler = networkHandler;
      }

      public BundleInventoryHandler.Robot selectBundleSlot(int innerSlotIndex) {
         this.networkHandler.sendPacket(new BundleItemSelectedC2SPacket(BundleInventoryHandler.this.bundleSlotId, innerSlotIndex));
         return this;
      }

      public BundleInventoryHandler.Robot clickSlot(ClickType clickType, int slotId) {
         int button = clickType == ClickType.LEFT ? 0 : 1;
         this.networkHandler
            .sendPacket(
               new ClickSlotC2SPacket(
                  BundleInventoryHandler.this.syncId, -1, slotId, button, SlotActionType.PICKUP, ItemStack.EMPTY, new Int2ObjectOpenHashMap()
               )
            );
         return this;
      }

      public BundleInventoryHandler.Robot clickBundle(ClickType clickType) {
         return this.clickSlot(clickType, BundleInventoryHandler.this.bundleSlotId);
      }

      public BundleInventoryHandler.Robot dropItemFromCursor() {
         return this.clickSlot(ClickType.LEFT, -999);
      }
   }
}
