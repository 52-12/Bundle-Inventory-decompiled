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
import net.minecraft.item.BundleItem;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.network.packet.c2s.play.BundleItemSelectedC2SPacket;

@Environment(EnvType.CLIENT)
public class BundleInventoryHandler extends ScreenHandler {
   public static final int PLAYER_INVENTORY_SLOTS = 45;
   private final BundleInventory bundleInventory;

   public BundleInventoryHandler(PlayerInventory playerInventory, BundleInventory bundleInventory, int syncId) {
      super(ScreenHandlerType.GENERIC_9X3, syncId);
      this.bundleInventory = bundleInventory;
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
            this.addSlot(new Slot(bundleInventory, j + i * 8, var10 + j * 18, 18 + i * 18));
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

   public void handleBundleSlotClick(Slot slot, int slotIndex, int button, SlotActionType actionType) {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player != null) {
         int originalBundleSlotId = this.getPlayerInventoryOriginalBundleSlot(player);
         if (originalBundleSlotId >= 0) {
            ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
            if (networkHandler != null) {
               if (actionType == SlotActionType.PICKUP) {
                  ItemStack cursorStack = this.getCursorStack();
                  if (cursorStack.isEmpty()) {
                     BundleContentsComponent data = this.bundleInventory.getBundleContent();
                     if (data == null || slotIndex >= data.size()) {
                        return;
                     }

                     networkHandler.sendPacket(new BundleItemSelectedC2SPacket(originalBundleSlotId, slotIndex));
                     networkHandler.sendPacket(
                        new ClickSlotC2SPacket(
                           player.currentScreenHandler.syncId,
                           -1,
                           originalBundleSlotId,
                           1,
                           SlotActionType.PICKUP,
                           data.get(slotIndex),
                           new Int2ObjectOpenHashMap()
                        )
                     );
                  } else {
                     if (cursorStack.getItem() instanceof BundleItem) {
                        return;
                     }

                     networkHandler.sendPacket(
                        new ClickSlotC2SPacket(
                           player.currentScreenHandler.syncId, -1, originalBundleSlotId, 0, SlotActionType.PICKUP, ItemStack.EMPTY, new Int2ObjectOpenHashMap()
                        )
                     );
                  }
               }
            }
         }
      }
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
   static class BundleSlot extends Slot {
      public BundleSlot(Inventory inventory, int index, int x, int y) {
         super(inventory, index, x, y);
      }
   }
}
