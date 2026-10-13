package io.socol.bundleinventory.inventory;

import io.socol.bundleinventory.BundleCompat;
import io.socol.bundleinventory.BundleInventoryMod;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.HashedStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.BundleContents.Mutable;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BundleInventoryHandler extends AbstractContainerMenu {
   public static final int PLAYER_INVENTORY_FIRST_SLOT = 9;
   public static final int PLAYER_INVENTORY_LAST_SLOT = 45;
   public static final int BUNDLE_ROW_LENGTH = 8;
   private static final Map<EquipmentSlot, Identifier> EMPTY_ARMOR_SLOT_TEXTURES = Map.of(
      EquipmentSlot.FEET,
      InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS,
      EquipmentSlot.LEGS,
      InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
      EquipmentSlot.CHEST,
      InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
      EquipmentSlot.HEAD,
      InventoryMenu.EMPTY_ARMOR_SLOT_HELMET
   );
   private static final EquipmentSlot[] EQUIPMENT_SLOT_ORDER = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   private final Inventory playerInventory;
   private final BundleInventory bundleInventory;
   private final int bundleSlotId;
   private final int rowsVisible;
   private final boolean openInventoryOnClose;
   private int selectedRow;
   private int rowsCount;
   private final NonNullList<Slot> bundleSlots = NonNullList.create();

   public BundleInventoryHandler(Inventory playerInventory, BundleInventory bundleInventory, int syncId, int rowsVisible, boolean openInventoryOnClose) {
      super(MenuType.GENERIC_9x3, syncId);
      this.playerInventory = playerInventory;
      this.bundleInventory = bundleInventory;
      this.bundleSlotId = this.getPlayerInventoryOriginalBundleSlot(playerInventory.player);
      this.rowsVisible = Math.clamp(rowsVisible, 3, 8);
      this.openInventoryOnClose = openInventoryOnClose;
      final Player player = playerInventory.player;
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
               public void setByPlayer(ItemStack stack, ItemStack previousStack) {
                  player.onEquipItem(EquipmentSlot.OFFHAND, previousStack, stack);
                  super.setByPlayer(stack, previousStack);
               }

               public Identifier getNoItemIcon() {
                  return InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;
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

   public NonNullList<Slot> getBundleSlots() {
      return this.bundleSlots;
   }

   private void updateRowsCount() {
      BundleContents content = this.getBundleInventory().getBundleContent();
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

   public int getPlayerInventoryOriginalBundleSlot(Player player) {
      AbstractContainerMenu invHandler = player.containerMenu;
      if (invHandler instanceof InventoryMenu) {
         for (Slot slot : invHandler.slots) {
            if (slot.container == player.getInventory() && slot.getContainerSlot() == this.getPlayerInventoryBundleSlot()) {
               return slot.index;
            }
         }
      }

      return -1;
   }

   public Component getBundleTitle() {
      ItemStack bundle = this.getBundleInventory().getBundleStack();
      return (Component)(bundle.isEmpty() ? Component.empty() : bundle.getHoverName());
   }

   @Nullable
   private BundleInventoryHandler.Robot createRobot(LocalPlayer player) {
      ClientPacketListener networkHandler = Minecraft.getInstance().getConnection();
      if (networkHandler == null) {
         return null;
      } else {
         return this.bundleSlotId < 0 ? null : new BundleInventoryHandler.Robot(player, networkHandler);
      }
   }

   public boolean handlePlayerSlotClick(int slotId, int button, ContainerInput actionType) {
      if (slotId >= 0 && slotId < this.slots.size()) {
         Slot slot = this.getSlot(slotId);
         if (slot instanceof BundleInventoryHandler.BundleItemSlot) {
            return true;
         } else if (actionType == ContainerInput.SWAP && this.getPlayerInventoryBundleSlot() == button) {
            return true;
         } else {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && slotId >= 9 && actionType == ContainerInput.QUICK_MOVE) {
               ClickAction clickType = button == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
               return slot.hasItem() && this.quickMoveIntoBundle(player, slotId, clickType);
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public void handleBundleSlotAction(int bundleInnerSlot, int button, ContainerInput actionType) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && (button == 0 || button == 1)) {
         ClickAction clickType = button == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
         bundleInnerSlot += this.getBundleContentOffset();
         if (actionType == ContainerInput.PICKUP) {
            this.handleBundleSlotClick(player, clickType, bundleInnerSlot);
         } else if (actionType == ContainerInput.QUICK_MOVE) {
            this.quickMoveFromBundle(player, bundleInnerSlot);
         } else if (actionType == ContainerInput.THROW) {
            this.dropItemFromBundle(player, bundleInnerSlot);
         }
      }
   }

   public void handleBundleSlotClick(LocalPlayer player, ClickAction clickType, int bundleInnerSlot) {
      BundleInventoryHandler.Robot robot = this.createRobot(player);
      if (robot != null) {
         ItemStack cursorStack = this.getCarried();
         if (cursorStack.isEmpty()) {
            if (!this.hasItemInBundleInnerSlot(bundleInnerSlot)) {
               return;
            }

            robot.selectBundleSlot(bundleInnerSlot);
            robot.clickBundle(ClickAction.SECONDARY);
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
               robot.clickBundle(ClickAction.SECONDARY);
               robot.clickBundle(ClickAction.PRIMARY);
               robot.clickBundle(ClickAction.PRIMARY);
            } else {
               robot.clickBundle(ClickAction.PRIMARY);
            }

            playInsertSound(player);
         }
      }
   }

   public boolean quickMoveIntoBundle(LocalPlayer player, int slotId, ClickAction clickType) {
      if (!this.getCarried().isEmpty()) {
         return false;
      }

      BundleInventoryHandler.Robot robot = this.createRobot(player);
      if (robot == null) {
         return false;
      }

      ItemStack stackToMove = this.getSlot(slotId).getItem();
      boolean movingBundle = stackToMove.getItem() instanceof BundleItem;
      if (movingBundle && clickType == ClickAction.SECONDARY) {
         int playerInvSlot = this.getSlot(slotId).getContainerSlot();
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

         robot.clickSlot(ClickAction.PRIMARY, slotId);
         if (movingBundle) {
            robot.clickBundle(ClickAction.SECONDARY);
            robot.clickBundle(ClickAction.PRIMARY);
            robot.clickBundle(ClickAction.PRIMARY);
         } else {
            robot.clickBundle(ClickAction.PRIMARY);
            if (putBackRemaining) {
               robot.clickSlot(ClickAction.PRIMARY, slotId);
            }
         }

         playInsertSound(player);
         return true;
      }
   }

   public void quickMoveFromBundle(LocalPlayer player, int bundleInnerSlot) {
      if (this.getCarried().isEmpty()) {
         BundleInventoryHandler.Robot robot = this.createRobot(player);
         if (robot != null) {
            ItemStack stackToMove = this.getBundleContentItem(bundleInnerSlot);
            if (!stackToMove.isEmpty()) {
               int targetPlayerSlot = this.findSlotToPutStackInPlayerInventory(stackToMove);
               if (targetPlayerSlot < 0) {
                  playInsertFailSound(player);
               } else {
                  robot.selectBundleSlot(bundleInnerSlot);
                  robot.clickBundle(ClickAction.SECONDARY);
                  robot.clickSlot(ClickAction.PRIMARY, targetPlayerSlot);
                  playRemoveOneSound(player);
               }
            }
         }
      }
   }

   public void dropItemFromBundle(LocalPlayer player, int bundleInnerSlot) {
      if (this.getCarried().isEmpty()) {
         BundleInventoryHandler.Robot robot = this.createRobot(player);
         if (robot != null && this.hasItemInBundleInnerSlot(bundleInnerSlot)) {
            robot.selectBundleSlot(bundleInnerSlot);
            robot.clickBundle(ClickAction.SECONDARY);
            robot.dropItemFromCursor();
            playDropContentsSound(player);
         }
      }
   }

   private boolean isBundleFull() {
      BundleContents content = this.bundleInventory.getBundleContent();
      return content == null || content.weight().getOrThrow().compareTo(Fraction.ONE) >= 0;
   }

   private int checkStackCanBeAddedToBundle(ItemStack stackToAdd) {
      BundleContents content = this.bundleInventory.getBundleContent();
      if (content == null) {
         return 0;
      }

      Mutable builder = BundleCompat.asMutable(content);
      return builder.tryInsert(stackToAdd.copy());
   }

   private boolean hasItemInBundleInnerSlot(int bundleInnerSlot) {
      BundleContents content = this.bundleInventory.getBundleContent();
      return content != null && bundleInnerSlot < content.size();
   }

   private ItemStack getBundleContentItem(int bundleInnerSlot) {
      BundleContents content = this.bundleInventory.getBundleContent();
      return content != null && bundleInnerSlot < content.size() ? content.items().get(bundleInnerSlot).create() : ItemStack.EMPTY;
   }

   private int findSlotToPutStackInPlayerInventory(ItemStack addStack) {
      for (int slotId = 44; slotId >= 9; slotId--) {
         ItemStack currentStack = this.getSlot(slotId).getItem();
         if (!currentStack.isEmpty()
            && ItemStack.isSameItemSameComponents(currentStack, addStack)
            && addStack.getCount() + currentStack.getCount() <= currentStack.getMaxStackSize()) {
            return slotId;
         }
      }

      return this.findLastFreePlayerInventorySlot();
   }

   private int findLastFreePlayerInventorySlot() {
      for (int slotId = 44; slotId >= 9; slotId--) {
         if (!this.getSlot(slotId).hasItem()) {
            return slotId;
         }
      }

      return -1;
   }

   public ItemStack quickMoveStack(Player player, int slot) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot2 = (Slot)this.slots.get(slot);
      if (slot2 != null && slot2.hasItem()) {
         ItemStack itemStack2 = slot2.getItem();
         itemStack = itemStack2.copy();
         if (!this.moveItemStackTo(itemStack2, 0, 0, false)) {
            return ItemStack.EMPTY;
         }

         if (itemStack2.isEmpty()) {
            slot2.setByPlayer(ItemStack.EMPTY);
         } else {
            slot2.setChanged();
         }
      }

      return itemStack;
   }

   private int getBundleContentOffset() {
      return this.selectedRow * 8;
   }

   public boolean stillValid(Player player) {
      return this.bundleInventory.stillValid(player);
   }

   public ItemStack getCarried() {
      LocalPlayer player = Minecraft.getInstance().player;
      return player != null && player.containerMenu != null ? player.containerMenu.getCarried() : ItemStack.EMPTY;
   }

   public void setCarried(ItemStack stack) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && player.containerMenu != null) {
         player.containerMenu.setCarried(stack);
      }
   }

   public void tick() {
      this.updateRowsCount();
   }

   void close(LocalPlayer player) {
      playCloseSound(player);
   }

   private static void playerOpenSound(Entity entity) {
      playBundleSound(entity, SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.7F + entity.level().getRandom().nextFloat() * 0.1F);
   }

   private static void playCloseSound(Entity entity) {
      playBundleSound(entity, SoundEvents.BUNDLE_INSERT, 0.8F, 0.7F + entity.level().getRandom().nextFloat() * 0.1F);
   }

   private static void playRemoveOneSound(Entity entity) {
      playBundleSound(entity, SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
   }

   private static void playInsertSound(Entity entity) {
      playBundleSound(entity, SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
   }

   private static void playInsertFailSound(Entity entity) {
      playBundleSound(entity, SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
   }

   private static void playDropContentsSound(Entity entity) {
      playBundleSound(entity, SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
   }

   private static void playBundleSound(Entity entity, SoundEvent sound, float volume, float pitch) {
      if (BundleInventoryMod.config().shouldPlayBundleInventorySounds()) {
         entity.playSound(sound, volume, pitch);
      }
   }

   @Environment(EnvType.CLIENT)
   class BundleContentSlot extends Slot {
      public BundleContentSlot(Container inventory, int index, int x, int y) {
         super(inventory, index, x, y);
      }

      public boolean mayPlace(ItemStack stack) {
         return false;
      }

      public ItemStack getItem() {
         return this.container.getItem(BundleInventoryHandler.this.getBundleContentOffset() + this.getContainerSlot());
      }
   }

   @Environment(EnvType.CLIENT)
   static class BundleItemSlot extends Slot {
      public BundleItemSlot(Container inventory, int index, int x, int y) {
         super(inventory, index, x, y);
      }
   }

   @Environment(EnvType.CLIENT)
   static class DisabledSlot extends Slot {
      public DisabledSlot(Container inventory, int index, int x, int y) {
         super(inventory, index, x, y);
      }

      public boolean isActive() {
         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   class Robot {
      LocalPlayer player;
      ClientPacketListener networkHandler;

      public Robot(LocalPlayer player, ClientPacketListener networkHandler) {
         this.player = player;
         this.networkHandler = networkHandler;
      }

      public BundleInventoryHandler.Robot selectBundleSlot(int innerSlotIndex) {
         this.networkHandler.send(new ServerboundSelectBundleItemPacket(BundleInventoryHandler.this.bundleSlotId, innerSlotIndex));
         return this;
      }

      public BundleInventoryHandler.Robot clickSlot(ClickAction clickType, int slotId) {
         int button = clickType == ClickAction.PRIMARY ? 0 : 1;
         this.networkHandler
            .send(
               new ServerboundContainerClickPacket(
                  BundleInventoryHandler.this.containerId, -1, (short)slotId, (byte)button, ContainerInput.PICKUP, new Int2ObjectOpenHashMap<>(), HashedStack.EMPTY
               )
            );
         return this;
      }

      public BundleInventoryHandler.Robot clickBundle(ClickAction clickType) {
         return this.clickSlot(clickType, BundleInventoryHandler.this.bundleSlotId);
      }

      public BundleInventoryHandler.Robot dropItemFromCursor() {
         return this.clickSlot(ClickAction.PRIMARY, -999);
      }
   }
}
