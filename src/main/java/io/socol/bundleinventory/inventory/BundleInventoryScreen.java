package io.socol.bundleinventory.inventory;

import com.mojang.blaze3d.platform.InputConstants;
import io.socol.bundleinventory.BundleInventoryMod;
import io.socol.bundleinventory.util.UIDrawer;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.BundleContents.Mutable;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BundleInventoryScreen extends AbstractContainerScreen<BundleInventoryHandler> {
   private static final Identifier SLOT_HIGHLIGHT_BACK_TEXTURE = Identifier.withDefaultNamespace("container/slot_highlight_back");
   private static final Identifier SLOT_HIGHLIGHT_FRONT_TEXTURE = Identifier.withDefaultNamespace("container/slot_highlight_front");
   private static final int MAX_TITLE_WIDTH = 120;
   private final FormattedCharSequence trimmedTitle;
   private final boolean openInventoryOnClose;
   private float currentBarOccupancy;
   private float prevBarOccupancy;
   private int scrollBarSelectedRow;
   private int scrollBarRowsCount;
   private Slot focusedBundleSlot = null;

   public BundleInventoryScreen(BundleInventoryHandler handler, Inventory inventory, boolean openInventoryOnClose) {
      super(handler, inventory, handler.getBundleTitle(), 176, 114 + handler.getRowsVisible() * 18);
      this.trimmedTitle = trimTitleToWidth(this.getTitle(), 120);
      this.openInventoryOnClose = openInventoryOnClose;
      this.inventoryLabelY = this.imageHeight - 94;
      this.currentBarOccupancy = handler.getBundleInventory().getBundleContent().weight().getOrThrow().floatValue();
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

   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
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
      super.extractRenderState(context, mouseX, mouseY, delta);
      this.drawMouseoverBundleTooltip(context, mouseX, mouseY);
   }

   private void drawMouseoverBundleTooltip(GuiGraphicsExtractor context, int x, int y) {
      if (this.focusedBundleSlot != null && this.focusedBundleSlot.hasItem()) {
         ItemStack focusedStack = this.focusedBundleSlot.getItem();
         Optional<TooltipComponent> tooltipData = focusedStack.getTooltipImage();
         Boolean stickyTooltip = tooltipData.map(ClientTooltipComponent::create).<Boolean>map(ClientTooltipComponent::showTooltipWithItemInHand).orElse(false);
         if (((BundleInventoryHandler)this.menu).getCarried().isEmpty() || stickyTooltip) {
            context.setTooltipForNextFrame(
               this.font, this.getTooltipFromContainerItem(focusedStack), tooltipData, x, y, (Identifier)focusedStack.get(DataComponents.TOOLTIP_STYLE)
            );
         }
      }
   }

   public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      super.extractBackground(context, mouseX, mouseY, delta);
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
         int barProgress = Mth.ceil(barOccupancy * barWidth);
         BossBarColor barColor = BossBarColor.BLUE;
         if (barOccupancy > 0.9) {
            barColor = BossBarColor.RED;
         } else if (barOccupancy > 0.7) {
            barColor = BossBarColor.YELLOW;
         }

         drawer.move(this.imageWidth - 7 - barWidth, 8).drawBossBar(barWidth, barProgress, barColor, BossBarOverlay.NOTCHED_10);
      }
   }

   protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
      context.text(this.font, this.trimmedTitle, this.titleLabelX, this.titleLabelY, -12566464, false);
      context.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
   }

   protected void extractSlots(GuiGraphicsExtractor context, int mouseX, int mouseY) {
      super.extractSlots(context, mouseX, mouseY);
      this.drawBundleSlotHighlightBack(context);

      for (Slot slot : ((BundleInventoryHandler)this.menu).getBundleSlots()) {
         if (slot.isActive()) {
            this.extractSlot(context, slot, mouseX, mouseY);
         }
      }

      this.drawBundleSlotHighlightFront(context);
   }

   private void drawBundleSlotHighlightBack(GuiGraphicsExtractor context) {
      if (this.focusedBundleSlot != null && this.focusedBundleSlot.isHighlightable()) {
         context.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_TEXTURE, this.focusedBundleSlot.x - 4, this.focusedBundleSlot.y - 4, 24, 24);
      }
   }

   private void drawBundleSlotHighlightFront(GuiGraphicsExtractor context) {
      if (this.focusedBundleSlot != null && this.focusedBundleSlot.isHighlightable()) {
         context.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_TEXTURE, this.focusedBundleSlot.x - 4, this.focusedBundleSlot.y - 4, 24, 24);
      }
   }

   protected void extractSlot(GuiGraphicsExtractor context, Slot slot, int mouseX, int mouseY) {
      if (!BundleInventoryMod.config().indicateOpenBundle()) {
         super.extractSlot(context, slot, mouseX, mouseY);
      } else if (this.minecraft != null && slot instanceof BundleInventoryHandler.BundleItemSlot) {
         ItemStack stack = slot.getItem();
         if (stack.getItem() instanceof BundleItem bundleItem) {
            ItemStack var7 = new ItemStack(bundleItem);
            BundleContents stackContent = stack.get(DataComponents.BUNDLE_CONTENTS);
            if (stackContent != null && stackContent.size() > 0) {
               Mutable openBundleContent = stackContent.asMutable();
               openBundleContent.toggleSelectedItem(0);
               var7.set(DataComponents.BUNDLE_CONTENTS, openBundleContent.toImmutable());
            }

            context.pose().pushMatrix();
            context.item(var7, slot.x, slot.y);
            context.itemDecorations(this.minecraft.font, stack, slot.x, slot.y);
            context.pose().popMatrix();
         } else {
            super.extractSlot(context, slot, mouseX, mouseY);
         }
      } else {
         super.extractSlot(context, slot, mouseX, mouseY);
      }
   }

   protected void containerTick() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && ((BundleInventoryHandler)this.menu).stillValid(player)) {
         ((BundleInventoryHandler)this.menu).tick();
         this.prevBarOccupancy = this.currentBarOccupancy;
         BundleContents content = ((BundleInventoryHandler)this.menu).getBundleInventory().getBundleContent();
         if (content != null) {
            float targetBarValue = content.weight().getOrThrow().floatValue();
            this.currentBarOccupancy = Mth.lerp(0.5F, this.currentBarOccupancy, targetBarValue);
            if (targetBarValue <= 0.0F && this.currentBarOccupancy <= 0.01F) {
               this.currentBarOccupancy = 0.0F;
            }
         }

         super.containerTick();
      } else {
         Minecraft.getInstance().setScreenAndShow(null);
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
         this.minecraft.setScreenAndShow(new InventoryScreen(player));
      } else {
         super.onClose();
      }
   }

   public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
      Slot bundleSlot = this.getBundleSlotAt(click.x(), click.y());
      if (bundleSlot == null) {
         return super.mouseClicked(click, doubled);
      }

      boolean shiftClick = InputConstants.isKeyDown(340)
         || InputConstants.isKeyDown(344);
      ContainerInput actionType = shiftClick ? ContainerInput.QUICK_MOVE : ContainerInput.PICKUP;
      int slotIndex = bundleSlot.getContainerSlot();
      ((BundleInventoryHandler)this.menu).handleBundleSlotAction(slotIndex, click.button(), actionType);
      this.skipNextRelease = true;
      return true;
   }

   public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
      return this.getFocused() != null && this.isDragging() && click.button() == 0
         ? this.getFocused().mouseDragged(click, offsetX, offsetY)
         : super.mouseDragged(click, offsetX, offsetY);
   }

   public boolean mouseReleased(MouseButtonEvent click) {
      if (click.button() == 0 && this.isDragging()) {
         this.setDragging(false);
         GuiEventListener focused = this.getFocused();
         if (focused != null) {
            this.setFocused(null);
            return focused.mouseReleased(click);
         }
      }

      return super.mouseReleased(click);
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

   public boolean keyPressed(KeyEvent input) {
      if (this.focusedBundleSlot != null && this.focusedBundleSlot.hasItem() && this.minecraft != null && this.minecraft.options.keyDrop.matches(input)) {
         int slotIndex = this.focusedBundleSlot.getContainerSlot();
         ((BundleInventoryHandler)this.menu).handleBundleSlotAction(slotIndex, input.hasControlDown() ? 1 : 0, ContainerInput.THROW);
         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
      boolean isOutside = super.hasClickedOutside(mouseX, mouseY, left, top);
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
