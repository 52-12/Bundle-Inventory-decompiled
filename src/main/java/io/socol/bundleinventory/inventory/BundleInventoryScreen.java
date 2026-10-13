package io.socol.bundleinventory.inventory;

import io.socol.bundleinventory.BundleInventoryMod;
import io.socol.bundleinventory.util.UIDrawer;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.Click;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Language;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.OrderedText;
import net.minecraft.item.BundleItem;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.boss.BossBar.Color;
import net.minecraft.entity.boss.BossBar.Style;
import net.minecraft.component.type.BundleContentsComponent.Builder;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BundleInventoryScreen extends HandledScreen<BundleInventoryHandler> {
   private static final Identifier SLOT_HIGHLIGHT_BACK_TEXTURE = Identifier.ofVanilla("container/slot_highlight_back");
   private static final Identifier SLOT_HIGHLIGHT_FRONT_TEXTURE = Identifier.ofVanilla("container/slot_highlight_front");
   private static final int MAX_TITLE_WIDTH = 120;
   private final OrderedText trimmedTitle;
   private final boolean openInventoryOnClose;
   private float currentBarOccupancy;
   private float prevBarOccupancy;
   private int scrollBarSelectedRow;
   private int scrollBarRowsCount;
   private Slot focusedBundleSlot = null;

   public BundleInventoryScreen(BundleInventoryHandler handler, PlayerInventory inventory, boolean openInventoryOnClose) {
      super(handler, inventory, handler.getBundleTitle());
      this.trimmedTitle = trimTitleToWidth(this.getTitle(), 120);
      this.openInventoryOnClose = openInventoryOnClose;
      this.backgroundWidth = 176;
      this.backgroundHeight = 114 + handler.getRowsVisible() * 18;
      this.playerInventoryTitleY = this.backgroundHeight - 94;
      this.currentBarOccupancy = handler.getBundleInventory().getBundleContent().getOccupancy().floatValue();
      this.prevBarOccupancy = this.currentBarOccupancy;
   }

   @Nullable
   private Slot getBundleSlotAt(double mouseX, double mouseY) {
      for (Slot slot : ((BundleInventoryHandler)this.handler).getBundleSlots()) {
         if (slot.isEnabled() && this.isPointWithinBounds(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
            return slot;
         }
      }

      return null;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      if (this.scrollBarSelectedRow != ((BundleInventoryHandler)this.handler).getSelectedRow()
         || this.scrollBarRowsCount != ((BundleInventoryHandler)this.handler).getRowsCount()) {
         this.scrollBarSelectedRow = ((BundleInventoryHandler)this.handler).getSelectedRow();
         this.scrollBarRowsCount = ((BundleInventoryHandler)this.handler).getRowsCount();

         for (Element child : this.children()) {
            if (child instanceof BundleContentSlider scrollBar) {
               if (((BundleInventoryHandler)this.handler).getRowsCount() < 2) {
                  scrollBar.setValue(-1.0);
               } else {
                  double value = (double)((BundleInventoryHandler)this.handler).getSelectedRow()
                     / (((BundleInventoryHandler)this.handler).getRowsCount() - 1);
                  scrollBar.setValue(value);
               }
            }
         }
      }

      this.focusedBundleSlot = this.getBundleSlotAt(mouseX, mouseY);
      super.render(context, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(context, mouseX, mouseY);
      this.drawMouseoverBundleTooltip(context, mouseX, mouseY);
   }

   private void drawMouseoverBundleTooltip(DrawContext context, int x, int y) {
      if (this.focusedBundleSlot != null && this.focusedBundleSlot.hasStack()) {
         ItemStack focusedStack = this.focusedBundleSlot.getStack();
         Optional<TooltipData> tooltipData = focusedStack.getTooltipData();
         Boolean stickyTooltip = tooltipData.map(TooltipComponent::of).<Boolean>map(TooltipComponent::isSticky).orElse(false);
         if (((BundleInventoryHandler)this.handler).getCursorStack().isEmpty() || stickyTooltip) {
            context.drawTooltip(
               this.textRenderer, this.getTooltipFromItem(focusedStack), tooltipData, x, y, (Identifier)focusedStack.get(DataComponentTypes.TOOLTIP_STYLE)
            );
         }
      }
   }

   protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
      int x0 = (this.width - this.backgroundWidth) / 2;
      int y0 = (this.height - this.backgroundHeight) / 2;
      UIDrawer drawer = new UIDrawer(context).at(x0, y0);
      drawer.drawResizablePlate(this.backgroundWidth, this.backgroundHeight);
      drawer.move(7, 17).drawSlots(8, ((BundleInventoryHandler)this.handler).getRowsVisible());
      drawer.move(7, this.backgroundHeight - 83).drawSlots(9, 3);
      drawer.move(7, this.backgroundHeight - 25).drawSlots(9, 1);
      drawer.move(155, 17).drawScrollerGroove(((BundleInventoryHandler)this.handler).getRowsVisible() * 18);
      if (BundleInventoryMod.config().showEquipmentSlotsInBundleInventory()) {
         UIDrawer equipmentDrawer = drawer.move(-36, this.backgroundHeight - 108);
         equipmentDrawer.drawResizablePlate(32, 108);
         equipmentDrawer.move(7, 7).drawSlots(1, 4);
         equipmentDrawer.move(7, 83).drawSlot();
      }

      BundleContentsComponent content = ((BundleInventoryHandler)this.handler).getBundleInventory().getBundleContent();
      if (content != null) {
         int barWidth = 55;
         float barOccupancy = MathHelper.lerp(delta, this.prevBarOccupancy, this.currentBarOccupancy);
         barOccupancy = MathHelper.clamp(barOccupancy, 0.0F, 1.0F);
         int barProgress = MathHelper.ceil(barOccupancy * barWidth);
         Color barColor = Color.BLUE;
         if (barOccupancy > 0.9) {
            barColor = Color.RED;
         } else if (barOccupancy > 0.7) {
            barColor = Color.YELLOW;
         }

         drawer.move(this.backgroundWidth - 7 - barWidth, 8).drawBossBar(barWidth, barProgress, barColor, Style.NOTCHED_10);
      }
   }

   protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
      context.drawText(this.textRenderer, this.trimmedTitle, this.titleX, this.titleY, -12566464, false);
      context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, -12566464, false);
   }

   protected void drawSlots(DrawContext context, int mouseX, int mouseY) {
      super.drawSlots(context, mouseX, mouseY);
      this.drawBundleSlotHighlightBack(context);

      for (Slot slot : ((BundleInventoryHandler)this.handler).getBundleSlots()) {
         if (slot.isEnabled()) {
            this.drawSlot(context, slot, mouseX, mouseY);
         }
      }

      this.drawBundleSlotHighlightFront(context);
   }

   private void drawBundleSlotHighlightBack(DrawContext context) {
      if (this.focusedBundleSlot != null && this.focusedBundleSlot.canBeHighlighted()) {
         context.drawGuiTexture(
            RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_TEXTURE, this.focusedBundleSlot.x - 4, this.focusedBundleSlot.y - 4, 24, 24
         );
      }
   }

   private void drawBundleSlotHighlightFront(DrawContext context) {
      if (this.focusedBundleSlot != null && this.focusedBundleSlot.canBeHighlighted()) {
         context.drawGuiTexture(
            RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_TEXTURE, this.focusedBundleSlot.x - 4, this.focusedBundleSlot.y - 4, 24, 24
         );
      }
   }

   protected void drawSlot(DrawContext context, Slot slot, int mouseX, int mouseY) {
      if (!BundleInventoryMod.config().indicateOpenBundle()) {
         super.drawSlot(context, slot, mouseX, mouseY);
      } else if (this.client != null && slot instanceof BundleInventoryHandler.BundleItemSlot) {
         ItemStack stack = slot.getStack();
         if (stack.getItem() instanceof BundleItem bundleItem) {
            ItemStack var7 = new ItemStack(bundleItem);
            Builder openBundleContent = new Builder(new BundleContentsComponent(List.of(ItemStack.EMPTY)));
            openBundleContent.setSelectedStackIndex(0);
            var7.set(DataComponentTypes.BUNDLE_CONTENTS, openBundleContent.build());
            context.getMatrices().pushMatrix();
            context.drawItem(var7, slot.x, slot.y);
            context.drawStackOverlay(this.client.textRenderer, stack, slot.x, slot.y);
            context.getMatrices().popMatrix();
         } else {
            super.drawSlot(context, slot, mouseX, mouseY);
         }
      } else {
         super.drawSlot(context, slot, mouseX, mouseY);
      }
   }

   protected void handledScreenTick() {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player != null && ((BundleInventoryHandler)this.handler).canUse(player)) {
         ((BundleInventoryHandler)this.handler).tick();
         this.prevBarOccupancy = this.currentBarOccupancy;
         BundleContentsComponent content = ((BundleInventoryHandler)this.handler).getBundleInventory().getBundleContent();
         if (content != null) {
            float targetBarValue = content.getOccupancy().floatValue();
            this.currentBarOccupancy = MathHelper.lerp(0.5F, this.currentBarOccupancy, targetBarValue);
            if (targetBarValue <= 0.0F && this.currentBarOccupancy <= 0.01F) {
               this.currentBarOccupancy = 0.0F;
            }
         }

         super.handledScreenTick();
      } else {
         MinecraftClient.getInstance().setScreen(null);
      }
   }

   protected void init() {
      super.init();
      this.scrollBarSelectedRow = -1;
      this.scrollBarRowsCount = -1;
      int x0 = (this.width - this.backgroundWidth) / 2;
      int y0 = (this.height - this.backgroundHeight) / 2;
      BundleContentSlider scrollBar = new BundleContentSlider(
         x0 + 155, y0 + 17, 14, ((BundleInventoryHandler)this.handler).getRowsVisible() * 18, this::updateSelectedRow
      );
      this.addDrawableChild(scrollBar);
   }

   public void close() {
      ClientPlayerEntity player = this.client.player;
      if (player != null) {
         ((BundleInventoryHandler)this.handler).close(player);
      }

      if (this.openInventoryOnClose && player != null) {
         this.client.setScreen(new InventoryScreen(player));
      } else {
         super.close();
      }
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      Slot bundleSlot = this.getBundleSlotAt(click.x(), click.y());
      if (bundleSlot == null) {
         return super.mouseClicked(click, doubled);
      }

      boolean shiftClick = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), 340)
         || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), 344);
      SlotActionType actionType = shiftClick ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP;
      int slotIndex = bundleSlot.getIndex();
      ((BundleInventoryHandler)this.handler).handleBundleSlotAction(slotIndex, click.button(), actionType);
      this.cancelNextRelease = true;
      return true;
   }

   public boolean mouseDragged(Click click, double offsetX, double offsetY) {
      return this.getFocused() != null && this.isDragging() && click.button() == 0
         ? this.getFocused().mouseDragged(click, offsetX, offsetY)
         : super.mouseDragged(click, offsetX, offsetY);
   }

   public boolean mouseReleased(Click click) {
      if (click.button() == 0 && this.isDragging()) {
         this.setDragging(false);
         Element focused = this.getFocused();
         if (focused != null) {
            this.setFocused(null);
            return focused.mouseReleased(click);
         }
      }

      return super.mouseReleased(click);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      int x0 = (this.width - this.backgroundWidth) / 2;
      int y0 = (this.height - this.backgroundHeight) / 2;
      int h = 24 + ((BundleInventoryHandler)this.handler).getRowsVisible() * 18;
      if (mouseX >= x0 && mouseX < x0 + this.backgroundWidth && mouseY >= y0 && mouseY < y0 + h) {
         ((BundleInventoryHandler)this.handler)
            .setSelectedRow(((BundleInventoryHandler)this.handler).getSelectedRow() - MathHelper.sign(verticalAmount));
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   public boolean keyPressed(KeyInput input) {
      if (this.focusedBundleSlot != null
         && this.focusedBundleSlot.hasStack()
         && this.client != null
         && this.client.options.dropKey.matchesKey(input)) {
         int slotIndex = this.focusedBundleSlot.getIndex();
         ((BundleInventoryHandler)this.handler).handleBundleSlotAction(slotIndex, input.hasCtrl() ? 1 : 0, SlotActionType.THROW);
         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top) {
      boolean isOutside = super.isClickOutsideBounds(mouseX, mouseY, left, top);
      if (!isOutside) {
         return false;
      } else {
         return !BundleInventoryMod.config().showEquipmentSlotsInBundleInventory()
            ? true
            : mouseX < left - 36.0 || mouseY < (double)top + this.backgroundWidth - 108.0 || mouseX >= left - 4 || mouseY >= top + this.backgroundHeight;
      }
   }

   private void updateSelectedRow(double scrollBarValue) {
      int newSelectedRow = (int)(((BundleInventoryHandler)this.handler).getRowsCount() * scrollBarValue);
      this.scrollBarSelectedRow = ((BundleInventoryHandler)this.handler).setSelectedRow(newSelectedRow);
   }

   private static OrderedText trimTitleToWidth(Text text, int width) {
      MinecraftClient client = MinecraftClient.getInstance();
      int i = client.textRenderer.getWidth(text);
      if (i > width) {
         StringVisitable trimmed = client.textRenderer.trimToWidth(text, width - client.textRenderer.getWidth("..."));
         StringVisitable result = StringVisitable.concat(new StringVisitable[]{trimmed, StringVisitable.plain("...")});
         return Language.getInstance().reorder(result);
      } else {
         return text.asOrderedText();
      }
   }
}
