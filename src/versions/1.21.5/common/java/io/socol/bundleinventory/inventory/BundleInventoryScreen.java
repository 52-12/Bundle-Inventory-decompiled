package io.socol.bundleinventory.inventory;

import com.mojang.blaze3d.platform.InputConstants;
import io.socol.bundleinventory.BundleInventoryMod;
import io.socol.bundleinventory.util.UIDrawer;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.BundleContents.Mutable;
import org.jetbrains.annotations.Nullable;

public class BundleInventoryScreen extends AbstractContainerScreen<BundleInventoryHandler> {
   private static final ResourceLocation SLOT_HIGHLIGHT_BACK_TEXTURE = ResourceLocation.withDefaultNamespace("container/slot_highlight_back");
   private static final ResourceLocation SLOT_HIGHLIGHT_FRONT_TEXTURE = ResourceLocation.withDefaultNamespace("container/slot_highlight_front");
   private static final int MAX_TITLE_WIDTH = 120;
   private final FormattedCharSequence trimmedTitle;
   private final boolean openInventoryOnClose;
   private float currentBarOccupancy;
   private float prevBarOccupancy;
   private int scrollBarSelectedRow;
   private int scrollBarRowsCount;
   private Slot focusedBundleSlot = null;

   public BundleInventoryScreen(BundleInventoryHandler handler, Inventory inventory, boolean openInventoryOnClose) {
      super(handler, inventory, handler.getBundleTitle());
      this.trimmedTitle = trimTitleToWidth(this.getTitle(), 120);
      this.openInventoryOnClose = openInventoryOnClose;
      this.imageWidth = 176;
      this.imageHeight = 114 + handler.getRowsVisible() * 18;
      this.inventoryLabelY = this.imageHeight - 94;
      this.currentBarOccupancy = handler.getBundleInventory().getBundleContent().weight().floatValue();
      this.prevBarOccupancy = this.currentBarOccupancy;
   }

   @Nullable
   private Slot getBundleSlotAt(double mouseX, double mouseY) {
      for (Slot slot : ((BundleInventoryHandler)this.menu).getBundleSlots()) {
         if (slot.isActive() && this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
            return slot;
         }
      }

      return null;
   }

   public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
      if (this.scrollBarSelectedRow != ((BundleInventoryHandler)this.menu).getSelectedRow()
         || this.scrollBarRowsCount != ((BundleInventoryHandler)this.menu).getRowsCount()) {
         this.scrollBarSelectedRow = ((BundleInventoryHandler)this.menu).getSelectedRow();
         this.scrollBarRowsCount = ((BundleInventoryHandler)this.menu).getRowsCount();

         for (GuiEventListener child : this.children()) {
            if (child instanceof BundleContentSlider scrollBar) {
               if (((BundleInventoryHandler)this.menu).getRowsCount() < 2) {
                  scrollBar.setValue(-1.0);
               } else {
                  double value = (double)((BundleInventoryHandler)this.menu).getSelectedRow() / (((BundleInventoryHandler)this.menu).getRowsCount() - 1);
                  scrollBar.setValue(value);
               }
            }
         }
      }

      this.focusedBundleSlot = this.getBundleSlotAt(mouseX, mouseY);
      super.render(context, mouseX, mouseY, delta);
      this.renderTooltip(context, mouseX, mouseY);
      this.drawMouseoverBundleTooltip(context, mouseX, mouseY);
   }

   private void drawMouseoverBundleTooltip(GuiGraphics context, int x, int y) {
      if (this.focusedBundleSlot != null && this.focusedBundleSlot.hasItem()) {
         ItemStack focusedStack = this.focusedBundleSlot.getItem();
         Optional<TooltipComponent> tooltipData = focusedStack.getTooltipImage();
         Boolean stickyTooltip = tooltipData.map(ClientTooltipComponent::create).<Boolean>map(ClientTooltipComponent::showTooltipWithItemInHand).orElse(false);
         if (((BundleInventoryHandler)this.menu).getCarried().isEmpty() || stickyTooltip) {
            context.renderTooltip(
               this.font, this.getTooltipFromContainerItem(focusedStack), tooltipData, x, y, (ResourceLocation)focusedStack.get(DataComponents.TOOLTIP_STYLE)
            );
         }
      }
   }

   protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
      int x0 = (this.width - this.imageWidth) / 2;
      int y0 = (this.height - this.imageHeight) / 2;
      UIDrawer drawer = new UIDrawer(context).at(x0, y0);
      drawer.drawResizablePlate(this.imageWidth, this.imageHeight);
      drawer.move(7, 17).drawSlots(8, ((BundleInventoryHandler)this.menu).getRowsVisible());
      drawer.move(7, this.imageHeight - 83).drawSlots(9, 3);
      drawer.move(7, this.imageHeight - 25).drawSlots(9, 1);
      drawer.move(155, 17).drawScrollerGroove(((BundleInventoryHandler)this.menu).getRowsVisible() * 18);
      if (BundleInventoryMod.config().showEquipmentSlotsInBundleInventory()) {
         UIDrawer equipmentDrawer = drawer.move(-36, this.imageHeight - 108);
         equipmentDrawer.drawResizablePlate(32, 108);
         equipmentDrawer.move(7, 7).drawSlots(1, 4);
         equipmentDrawer.move(7, 83).drawSlot();
      }

      BundleContents content = ((BundleInventoryHandler)this.menu).getBundleInventory().getBundleContent();
      if (content != null) {
         int barWidth = 55;
         float barOccupancy = Mth.lerp(delta, this.prevBarOccupancy, this.currentBarOccupancy);
         barOccupancy = Mth.clamp(barOccupancy, 0.0F, 1.0F);
         if (BundleInventoryMod.config().useBossBarFullness()) {
            int barProgress = Mth.ceil(barOccupancy * barWidth);
            BossBarColor barColor = BossBarColor.BLUE;
            if (barOccupancy > 0.9) {
               barColor = BossBarColor.RED;
            } else if (barOccupancy > 0.7) {
               barColor = BossBarColor.YELLOW;
            }

            drawer.move(this.imageWidth - 7 - barWidth, 8).drawBossBar(barWidth, barProgress, barColor, BossBarOverlay.NOTCHED_10);
         } else {
            this.drawSmoothBar(context, x0 + this.imageWidth - 7 - barWidth, y0 + 8, barWidth, barOccupancy);
         }
      }
   }

   private void drawSmoothBar(GuiGraphics context, int x, int y, int width, float occupancy) {
      float clamped = Mth.clamp(occupancy, 0.0F, 1.0F);
      int progress = Mth.ceil(clamped * width);
      int color = clamped > 0.9F ? 0xFFFF5555 : (clamped > 0.7F ? 0xFFFFFF55 : 0xFF55FF55);
      context.fill(x - 1, y - 1, x + width + 1, y + 6, 0xFF000000);
      context.fill(x, y, x + width, y + 5, 0xFF3A3A3A);
      context.fill(x, y, x + progress, y + 5, color);
   }

   protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
      context.drawString(this.font, this.trimmedTitle, this.titleLabelX, this.titleLabelY, 4210752, false);
      context.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
   }

   protected void renderSlots(GuiGraphics context) {
      super.renderSlots(context);
      this.drawBundleSlotHighlightBack(context);

      for (Slot slot : ((BundleInventoryHandler)this.menu).getBundleSlots()) {
         if (slot.isActive()) {
            this.renderSlot(context, slot);
         }
      }

      this.drawBundleSlotHighlightFront(context);
   }

   private void drawBundleSlotHighlightBack(GuiGraphics context) {
      if (this.focusedBundleSlot != null && this.focusedBundleSlot.isHighlightable()) {
         context.blitSprite(RenderType::guiTextured, SLOT_HIGHLIGHT_BACK_TEXTURE, this.focusedBundleSlot.x - 4, this.focusedBundleSlot.y - 4, 24, 24);
      }
   }

   private void drawBundleSlotHighlightFront(GuiGraphics context) {
      if (this.focusedBundleSlot != null && this.focusedBundleSlot.isHighlightable()) {
         context.blitSprite(RenderType::guiTexturedOverlay, SLOT_HIGHLIGHT_FRONT_TEXTURE, this.focusedBundleSlot.x - 4, this.focusedBundleSlot.y - 4, 24, 24);
      }
   }

   protected void renderSlot(GuiGraphics context, Slot slot) {
      if (!BundleInventoryMod.config().indicateOpenBundle()) {
         super.renderSlot(context, slot);
      } else if (this.minecraft != null && slot instanceof BundleInventoryHandler.BundleItemSlot) {
         ItemStack stack = slot.getItem();
         if (stack.getItem() instanceof BundleItem bundleItem) {
            ItemStack var7 = new ItemStack(bundleItem);
            Mutable openBundleContent = new Mutable(new BundleContents(List.of(ItemStack.EMPTY)));
            openBundleContent.toggleSelectedItem(0);
            var7.set(DataComponents.BUNDLE_CONTENTS, openBundleContent.toImmutable());
            context.pose().pushPose();
            context.pose().translate(0.0F, 0.0F, 100.0F);
            context.renderItem(var7, slot.x, slot.y);
            context.renderItemDecorations(this.minecraft.font, stack, slot.x, slot.y);
            context.pose().popPose();
         } else {
            super.renderSlot(context, slot);
         }
      } else {
         super.renderSlot(context, slot);
      }
   }

   protected void containerTick() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && ((BundleInventoryHandler)this.menu).stillValid(player)) {
         ((BundleInventoryHandler)this.menu).tick();
         this.prevBarOccupancy = this.currentBarOccupancy;
         BundleContents content = ((BundleInventoryHandler)this.menu).getBundleInventory().getBundleContent();
         if (content != null) {
            float targetBarValue = content.weight().floatValue();
            this.currentBarOccupancy = Mth.lerp(0.5F, this.currentBarOccupancy, targetBarValue);
            if (targetBarValue <= 0.0F && this.currentBarOccupancy <= 0.01F) {
               this.currentBarOccupancy = 0.0F;
            }
         }

         super.containerTick();
      } else {
         Minecraft.getInstance().setScreen(null);
      }
   }

   protected void init() {
      super.init();
      this.scrollBarSelectedRow = -1;
      this.scrollBarRowsCount = -1;
      int x0 = (this.width - this.imageWidth) / 2;
      int y0 = (this.height - this.imageHeight) / 2;
      BundleContentSlider scrollBar = new BundleContentSlider(
         x0 + 155, y0 + 17, 14, ((BundleInventoryHandler)this.menu).getRowsVisible() * 18, this::updateSelectedRow
      );
      this.addRenderableWidget(scrollBar);
   }

   public void onClose() {
      LocalPlayer player = this.minecraft.player;
      if (player != null) {
         ((BundleInventoryHandler)this.menu).close(player);
      }

      if (this.openInventoryOnClose && player != null) {
         this.minecraft.setScreen(new InventoryScreen(player));
      } else {
         super.onClose();
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      Slot bundleSlot = this.getBundleSlotAt(mouseX, mouseY);
      if (bundleSlot == null) {
         return super.mouseClicked(mouseX, mouseY, button);
      }

      boolean shiftClick = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340)
         || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
      ClickType actionType = shiftClick ? ClickType.QUICK_MOVE : ClickType.PICKUP;
      int slotIndex = bundleSlot.getContainerSlot();
      ((BundleInventoryHandler)this.menu).handleBundleSlotAction(slotIndex, button, actionType);
      this.skipNextRelease = true;
      return true;
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      return this.getFocused() != null && this.isDragging() && button == 0
         ? this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
         : super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0 && this.isDragging()) {
         this.setDragging(false);
         GuiEventListener focused = this.getFocused();
         if (focused != null) {
            this.setFocused(null);
            return focused.mouseReleased(mouseX, mouseY, button);
         }
      }

      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      int x0 = (this.width - this.imageWidth) / 2;
      int y0 = (this.height - this.imageHeight) / 2;
      int h = 24 + ((BundleInventoryHandler)this.menu).getRowsVisible() * 18;
      if (mouseX >= x0 && mouseX < x0 + this.imageWidth && mouseY >= y0 && mouseY < y0 + h) {
         ((BundleInventoryHandler)this.menu).setSelectedRow(((BundleInventoryHandler)this.menu).getSelectedRow() - Mth.sign(verticalAmount));
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.focusedBundleSlot != null
         && this.focusedBundleSlot.hasItem()
         && this.minecraft != null
         && this.minecraft.options.keyDrop.matches(keyCode, scanCode)) {
         int slotIndex = this.focusedBundleSlot.getContainerSlot();
         ((BundleInventoryHandler)this.menu).handleBundleSlotAction(slotIndex, hasControlDown() ? 1 : 0, ClickType.THROW);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
      boolean isOutside = super.hasClickedOutside(mouseX, mouseY, left, top, button);
      if (!isOutside) {
         return false;
      } else {
         return !BundleInventoryMod.config().showEquipmentSlotsInBundleInventory()
            ? true
            : mouseX < left - 36.0 || mouseY < (double)top + this.imageWidth - 108.0 || mouseX >= left - 4 || mouseY >= top + this.imageHeight;
      }
   }

   private void updateSelectedRow(double scrollBarValue) {
      int newSelectedRow = (int)(((BundleInventoryHandler)this.menu).getRowsCount() * scrollBarValue);
      this.scrollBarSelectedRow = ((BundleInventoryHandler)this.menu).setSelectedRow(newSelectedRow);
   }

   private static FormattedCharSequence trimTitleToWidth(Component text, int width) {
      Minecraft client = Minecraft.getInstance();
      int i = client.font.width(text);
      if (i > width) {
         FormattedText trimmed = client.font.substrByWidth(text, width - client.font.width("..."));
         FormattedText result = FormattedText.composite(new FormattedText[]{trimmed, FormattedText.of("...")});
         return Language.getInstance().getVisualOrder(result);
      } else {
         return text.getVisualOrderText();
      }
   }
}
