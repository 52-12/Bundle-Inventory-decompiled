package io.socol.bundleinventory.inventory;

import io.socol.bundleinventory.BundleInventoryMod;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.inventory.Inventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.text.Text;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.ClickType;
import net.minecraft.item.BundleItem;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.screen.slot.ArmorSlot;
import net.minecraft.network.packet.c2s.play.BundleItemSelectedC2SPacket;
import net.minecraft.component.type.BundleContentsComponent.Builder;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BundleInventoryHandler extends ScreenHandler {
   public static final int PLAYER_INVENTORY_FIRST_SLOT = 9;
   public static final int PLAYER_INVENTORY_LAST_SLOT = 45;
   public static final int BUNDLE_ROW_LENGTH = 8;
   private static final Map<EquipmentSlot, Identifier> EMPTY_ARMOR_SLOT_TEXTURES = Map.of(
      EquipmentSlot.FEET,
      PlayerScreenHandler.EMPTY_BOOTS_SLOT_TEXTURE,
      EquipmentSlot.LEGS,
      PlayerScreenHandler.EMPTY_LEGGINGS_SLOT_TEXTURE,
      EquipmentSlot.CHEST,
      PlayerScreenHandler.EMPTY_CHESTPLATE_SLOT_TEXTURE,
      EquipmentSlot.HEAD,
      PlayerScreenHandler.EMPTY_HELMET_SLOT_TEXTURE
   );
   private static final EquipmentSlot[] EQUIPMENT_SLOT_ORDER = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   private final PlayerInventory playerInventory;
   private final BundleInventory bundleInventory;
   private final int bundleSlotId;
   private final int rowsVisible;
   private final boolean openInventoryOnClose;
   private int selectedRow;
   private int rowsCount;
   private final DefaultedList<Slot> bundleSlots = DefaultedList.of();

   public BundleInventoryHandler(PlayerInventory playerInventory, BundleInventory bundleInventory, int syncId, int rowsVisible, boolean openInventoryOnClose) {
      super(ScreenHandlerType.GENERIC_9X3, syncId);
      this.playerInventory = playerInventory;
      this.bundleInventory = bundleInventory;
      this.bundleSlotId = this.getPlayerInventoryOriginalBundleSlot(playerInventory.player);
      this.rowsVisible = Math.clamp(rowsVisible, 3, 8);
      this.openInventoryOnClose = openInventoryOnClose;
      final PlayerEntity player = playerInventory.player;
      int top = 32 + rowsVisible * 18;
      int left = 8;
      int height = 114 + rowsVisible * 18;

      for (int i = 0; i < 5; i++) {
         this.addSlot(new BundleInventoryHandler.DisabledSlot(playerInventory, i, -20, -20));
      }

      if (BundleInventoryMod.config().showEquipmentSlotsInBundleInventory()) {
         for (int i = 0; i < 4; i++) {
            EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_ORDER[i];
            Identifier background = EMPTY_ARMOR_SLOT_TEXTURES.get(equipmentSlot);
            this.addSlot(new ArmorSlot(playerInventory, player, equipmentSlot, 39 - i, -28, height - 100 + i * 18, background));
         }
      } else {
         for (int i = 0; i < 4; i++) {
            this.addSlot(new BundleInventoryHandler.DisabledSlot(playerInventory, 5 + i, -20, -20));
         }
      }

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            int slotIndex = j + (i + 1) * 9;
            Slot slot = slotIndex == bundleInventory.getPlayerInventoryBundleSlot()
               ? new BundleInventoryHandler.BundleItemSlot(playerInventory, slotIndex, left + j * 18, top + i * 18)
               : new Slot(playerInventory, slotIndex, left + j * 18, top + i * 18);
            this.addSlot(slot);
         }
      }

      for (int i = 0; i < 9; i++) {
         Slot slot = i == bundleInventory.getPlayerInventoryBundleSlot()
            ? new BundleInventoryHandler.BundleItemSlot(playerInventory, i, left + i * 18, top + 58)
            : new Slot(playerInventory, i, left + i * 18, top + 58);
         this.addSlot(slot);
      }

      if (BundleInventoryMod.config().showEquipmentSlotsInBundleInventory()) {
         if (bundleInventory.getPlayerInventoryBundleSlot() == 40) {
            this.addSlot(new BundleInventoryHandler.BundleItemSlot(playerInventory, 40, -28, height - 24));
         } else {
            this.addSlot(new Slot(playerInventory, 40, -28, height - 24) {
               public void setStack(ItemStack stack, ItemStack previousStack) {
                  player.onEquipStack(EquipmentSlot.OFFHAND, previousStack, stack);
                  super.setStack(stack, previousStack);
               }

               public Identifier getBackgroundSprite() {
                  return PlayerScreenHandler.EMPTY_OFF_HAND_SLOT_TEXTURE;
               }
            });
         }
      } else {
         this.addSlot(new BundleInventoryHandler.DisabledSlot(playerInventory, 40, -20, -20));
      }

      for (int i = 0; i < rowsVisible; i++) {
         for (int j = 0; j < 8; j++) {
            BundleInventoryHandler.BundleContentSlot slot = new BundleInventoryHandler.BundleContentSlot(bundleInventory, j + i * 8, left + j * 18, 18 + i * 18);
            this.bundleSlots.add(slot);
         }
      }

      playerOpenSound(playerInventory.player);
      this.updateRowsCount();
   }

   public DefaultedList<Slot> getBundleSlots() {
      return this.bundleSlots;
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

   public boolean handlePlayerSlotClick(int slotId, int button, SlotActionType actionType) {
      if (slotId >= 0 && slotId < this.slots.size()) {
         Slot slot = this.getSlot(slotId);
         if (slot instanceof BundleInventoryHandler.BundleItemSlot) {
            return true;
         } else if (actionType == SlotActionType.SWAP && this.getPlayerInventoryBundleSlot() == button) {
            return true;
         } else {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null && slotId >= 9 && actionType == SlotActionType.QUICK_MOVE) {
               ClickType clickType = button == 0 ? ClickType.LEFT : ClickType.RIGHT;
               return slot.hasStack() && this.quickMoveIntoBundle(player, slotId, clickType);
            } else {
               return false;
            }
         }
      } else {
         return false;
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
            if (BundleInventoryMod.config().clientSideItemInsertionChecks()) {
               int canAddCount = this.checkStackCanBeAddedToBundle(cursorStack);
               if (canAddCount <= 0) {
                  playInsertFailSound(player);
                  return;
               }
            }

            boolean insertingBundle = cursorStack.getItem() instanceof BundleItem;
            if (insertingBundle) {
               robot.clickBundle(ClickType.RIGHT);
               robot.clickBundle(ClickType.LEFT);
               robot.clickBundle(ClickType.LEFT);
            } else {
               robot.clickBundle(ClickType.LEFT);
            }

            playInsertSound(player);
         }
      }
   }

   public boolean quickMoveIntoBundle(ClientPlayerEntity player, int slotId, ClickType clickType) {
      if (!this.getCursorStack().isEmpty()) {
         return false;
      }

      BundleInventoryHandler.Robot robot = this.createRobot(player);
      if (robot == null) {
         return false;
      }

      ItemStack stackToMove = this.getSlot(slotId).getStack();
      boolean movingBundle = stackToMove.getItem() instanceof BundleItem;
      if (movingBundle && clickType == ClickType.RIGHT) {
         int playerInvSlot = this.getSlot(slotId).getIndex();
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

            putBackRemaining = canAddCount < stackToMove.getCount();
         }

         robot.clickSlot(ClickType.LEFT, slotId);
         if (movingBundle) {
            robot.clickBundle(ClickType.RIGHT);
            robot.clickBundle(ClickType.LEFT);
            robot.clickBundle(ClickType.LEFT);
         } else {
            robot.clickBundle(ClickType.LEFT);
            if (putBackRemaining) {
               robot.clickSlot(ClickType.LEFT, slotId);
            }
         }

         playInsertSound(player);
         return true;
      }
   }

   public void quickMoveFromBundle(ClientPlayerEntity player, int bundleInnerSlot) {
      if (this.getCursorStack().isEmpty()) {
         BundleInventoryHandler.Robot robot = this.createRobot(player);
         if (robot != null) {
            ItemStack stackToMove = this.getBundleContentItem(bundleInnerSlot);
            if (!stackToMove.isEmpty()) {
               int targetPlayerSlot = this.findSlotToPutStackInPlayerInventory(stackToMove);
               if (targetPlayerSlot < 0) {
                  playInsertFailSound(player);
               } else {
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

   private int checkStackCanBeAddedToBundle(ItemStack stackToAdd) {
      BundleContentsComponent content = this.bundleInventory.getBundleContent();
      if (content == null) {
         return 0;
      }

      Builder builder = new Builder(content);
      return builder.add(stackToAdd.copy());
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

   void close(ClientPlayerEntity player) {
      playCloseSound(player);
   }

   private static void playerOpenSound(Entity entity) {
      playBundleSound(entity, SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.7F + entity.getEntityWorld().getRandom().nextFloat() * 0.1F);
   }

   private static void playCloseSound(Entity entity) {
      playBundleSound(entity, SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.7F + entity.getEntityWorld().getRandom().nextFloat() * 0.1F);
   }

   private static void playRemoveOneSound(Entity entity) {
      playBundleSound(entity, SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.getEntityWorld().getRandom().nextFloat() * 0.4F);
   }

   private static void playInsertSound(Entity entity) {
      playBundleSound(entity, SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.getEntityWorld().getRandom().nextFloat() * 0.4F);
   }

   private static void playInsertFailSound(Entity entity) {
      playBundleSound(entity, SoundEvents.ITEM_BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
   }

   private static void playDropContentsSound(Entity entity) {
      playBundleSound(entity, SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.getEntityWorld().getRandom().nextFloat() * 0.4F);
   }

   private static void playBundleSound(Entity entity, SoundEvent sound, float volume, float pitch) {
      if (BundleInventoryMod.config().shouldPlayBundleInventorySounds()) {
         entity.playSound(sound, volume, pitch);
      }
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
                  BundleInventoryHandler.this.syncId,
                  -1,
                  (short)slotId,
                  (byte)button,
                  SlotActionType.PICKUP,
                  new Int2ObjectOpenHashMap(),
                  ItemStackHash.EMPTY
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
