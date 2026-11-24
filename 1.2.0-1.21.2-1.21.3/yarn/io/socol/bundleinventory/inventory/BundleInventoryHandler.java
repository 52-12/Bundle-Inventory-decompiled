package io.socol.bundleinventory.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.Inventory;
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
   public static final int PLAYER_INVENTORY_SLOTS = 36;
   public static final int BUNDLE_INVENTORY_FIRST_SLOT = 45;
   private final PlayerInventory playerInventory;
   private final BundleInventory bundleInventory;
   private final int bundleSlotId;

   public BundleInventoryHandler(PlayerInventory playerInventory, BundleInventory bundleInventory, int syncId) {
      super(ScreenHandlerType.GENERIC_9X3, syncId);
      this.playerInventory = playerInventory;
      this.bundleInventory = bundleInventory;
      this.bundleSlotId = this.getPlayerInventoryOriginalBundleSlot(playerInventory.player);
      int top = 176;
      int left = 8;

      for (int i = 0; i < 9; i++) {
         Slot slot = new Slot(playerInventory, i, -10000, -10000);
         this.addSlot(slot);
      }

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            int slotIndex = j + (i + 1) * 9;
            Slot slot = (Slot)(slotIndex == bundleInventory.getPlayerInventoryBundleSlot()
               ? new BundleInventoryHandler.BundleSlot(playerInventory, slotIndex, left + j * 18, top + i * 18)
               : new Slot(playerInventory, slotIndex, left + j * 18, top + i * 18));
            this.addSlot(slot);
         }
      }

      for (int i = 0; i < 9; i++) {
         Slot slot = (Slot)(i == bundleInventory.getPlayerInventoryBundleSlot()
            ? new BundleInventoryHandler.BundleSlot(playerInventory, i, left + i * 18, top + 58)
            : new Slot(playerInventory, i, left + i * 18, top + 58));
         this.addSlot(slot);
      }

      int var10 = 17;

      for (int i = 0; i < 8; i++) {
         for (int j = 0; j < 8; j++) {
            this.addSlot(new BundleInventoryHandler.BundleContentSlot(bundleInventory, j + i * 8, var10 + j * 18, 18 + i * 18));
         }
      }
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
            if (actionType == SlotActionType.PICKUP) {
               this.handleBundleSlotClick(player, clickType, bundleInnerSlot);
            } else if (actionType == SlotActionType.QUICK_MOVE) {
               this.quickMoveFromBundle(player, bundleInnerSlot);
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
         } else {
            if (cursorStack.getItem() instanceof BundleItem) {
               return;
            }

            robot.clickBundle(ClickType.LEFT);
         }
      }
   }

   public boolean quickMoveIntoBundle(ClientPlayerEntity player, int slotId) {
      if (!this.getCursorStack().isEmpty()) {
         return false;
      } else {
         BundleContentsComponent content = this.bundleInventory.getBundleContent();
         if (content != null && content.getOccupancy().compareTo(Fraction.ONE) < 0) {
            BundleInventoryHandler.Robot robot = this.createRobot(player);
            if (robot == null) {
               return false;
            } else {
               robot.clickSlot(ClickType.LEFT, slotId);
               robot.clickBundle(ClickType.LEFT);
               robot.clickSlot(ClickType.LEFT, slotId);
               return true;
            }
         } else {
            return true;
         }
      }
   }

   public void quickMoveFromBundle(ClientPlayerEntity player, int bundleInnerSlot) {
      if (this.getCursorStack().isEmpty()) {
         BundleInventoryHandler.Robot robot = this.createRobot(player);
         if (robot != null) {
            if (this.hasItemInBundleInnerSlot(bundleInnerSlot)) {
               int targetPlayerSlot = this.findLastFreePlayerInventorySlot();
               if (targetPlayerSlot >= 0) {
                  robot.selectBundleSlot(bundleInnerSlot);
                  robot.clickBundle(ClickType.RIGHT);
                  robot.clickSlot(ClickType.LEFT, targetPlayerSlot);
               }
            }
         }
      }
   }

   private boolean hasItemInBundleInnerSlot(int bundleInnerSlot) {
      BundleContentsComponent content = this.bundleInventory.getBundleContent();
      return content != null && bundleInnerSlot < content.size();
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

   public boolean canUse(PlayerEntity player) {
      return this.bundleInventory.canPlayerUse(player);
   }

   public ItemStack getCursorStack() {
      return MinecraftClient.getInstance().player.currentScreenHandler.getCursorStack();
   }

   public void setCursorStack(ItemStack stack) {
      MinecraftClient.getInstance().player.currentScreenHandler.setCursorStack(stack);
   }

   @Environment(EnvType.CLIENT)
   static class BundleContentSlot extends Slot {
      public BundleContentSlot(Inventory inventory, int index, int x, int y) {
         super(inventory, index, x, y);
      }

      public boolean canInsert(ItemStack stack) {
         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   static class BundleSlot extends Slot {
      public BundleSlot(Inventory inventory, int index, int x, int y) {
         super(inventory, index, x, y);
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
   }
}
