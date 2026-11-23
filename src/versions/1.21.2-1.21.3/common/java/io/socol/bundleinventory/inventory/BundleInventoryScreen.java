package io.socol.bundleinventory.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import io.socol.bundleinventory.BundleInventoryMod;
import io.socol.bundleinventory.util.UIDrawer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.jetbrains.annotations.Nullable;

public class BundleInventoryScreen extends AbstractContainerScreen<BundleInventoryHandler> {
   private static final int MAX_TITLE_WIDTH = 120;
   private final FormattedCharSequence trimmedTitle = trimTitleToWidth(this.getTitle(), 120);
   private final boolean openInventoryOnClose;
   private float currentBarOccupancy;
   private float prevBarOccupancy;
   private int scrollBarSelectedRow;
   private int scrollBarRowsCount;

   public BundleInventoryScreen(BundleInventoryHandler handler, Inventory inventory, boolean openInventoryOnClose) {
      super(handler, inventory, handler.getBundleTitle());
      this.openInventoryOnClose = openInventoryOnClose;
      this.imageWidth = 176;
      this.imageHeight = 114 + handler.getRowsVisible() * 18;
      this.inventoryLabelY = this.imageHeight - 94;
      this.currentBarOccupancy = handler.getBundleInventory().getBundleContent().weight().floatValue();
      this.prevBarOccupancy = this.currentBarOccupancy;
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

      super.render(context, mouseX, mouseY, delta);
      this.renderTooltip(context, mouseX, mouseY);
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

   protected void renderSlot(GuiGraphics context, Slot slot) {
      if (!BundleInventoryMod.config().indicateOpenBundle()) {
         super.renderSlot(context, slot);
      } else if (this.minecraft != null && slot instanceof BundleInventoryHandler.BundleItemSlot) {
         ItemStack stack = slot.getItem();
         if (stack.getItem() instanceof BundleItem bundleItem) {
            ClientLevel var9 = this.minecraft.level;
            LocalPlayer entity = this.minecraft.player;
            ItemRenderer renderer = this.minecraft.getItemRenderer();
            context.flush();
            PoseStack matrices = context.pose();
            matrices.pushPose();
            matrices.translate(slot.x + 8.0F, slot.y + 8.0F, 150.0F);
            matrices.scale(16.0F, -16.0F, 16.0F);
            Lighting.setupForFlatItems();
            context.drawSpecial(vertexConsumer -> {
               BakedModel openBackTexture = this.getModelOrOverride(renderer.itemModelShaper.getItemModel(bundleItem.openBackModel()), stack, var9, entity);
               renderer.render(stack, ItemDisplayContext.GUI, false, matrices, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, openBackTexture);
               BakedModel openFrontTexture = this.getModelOrOverride(renderer.itemModelShaper.getItemModel(bundleItem.openFrontModel()), stack, var9, entity);
               renderer.render(stack, ItemDisplayContext.GUI, false, matrices, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, openFrontTexture);
            });
            Lighting.setupFor3DItems();
            matrices.popPose();
         } else {
            super.renderSlot(context, slot);
         }
      } else {
         super.renderSlot(context, slot);
      }
   }

   private BakedModel getModelOrOverride(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity) {
      BakedModel override = model.overrides().findOverride(stack, world, entity, 0);
      return override == null ? model : override;
   }

   protected void slotClicked(Slot slot, int slotId, int button, ClickType actionType) {
      if (!(slot instanceof BundleInventoryHandler.BundleItemSlot)) {
         if (actionType != ClickType.SWAP || ((BundleInventoryHandler)this.menu).getPlayerInventoryBundleSlot() != button) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
               if (slotId >= 46) {
                  int slotIndex = slotId - 46;
                  ((BundleInventoryHandler)this.menu).handleBundleSlotAction(slotIndex, button, actionType);
                  return;
               }

               if (slotId >= 9
                  && actionType == ClickType.QUICK_MOVE
                  && slot.hasItem()
                  && ((BundleInventoryHandler)this.menu).quickMoveIntoBundle(player, slotId)) {
                  return;
               }
            }

            super.slotClicked(slot, slotId, button, actionType);
         }
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
