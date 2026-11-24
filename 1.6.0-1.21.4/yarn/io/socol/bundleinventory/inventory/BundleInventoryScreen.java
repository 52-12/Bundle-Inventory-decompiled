package io.socol.bundleinventory.inventory;

import io.socol.bundleinventory.BundleInventoryMod;
import io.socol.bundleinventory.util.StepColor;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Language;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.OrderedText;
import net.minecraft.item.BundleItem;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent.Builder;

@Environment(EnvType.CLIENT)
public class BundleInventoryScreen extends HandledScreen<BundleInventoryHandler> {
   private static final Identifier TEXTURE = BundleInventoryMod.id("textures/gui/bundle.png");
   private static final int MAX_TITLE_WIDTH = 120;
   private final OrderedText trimmedTitle = trimTitleToWidth(this.getTitle(), 120);
   private final boolean openInventoryOnClose;
   private final StepColor barColor;
   private float currentBarOccupancy;
   private float prevBarOccupancy;
   private int scrollBarSelectedRow;
   private int scrollBarRowsCount;

   public BundleInventoryScreen(BundleInventoryHandler handler, PlayerInventory inventory, boolean openInventoryOnClose) {
      super(handler, inventory, handler.getBundleTitle());
      this.openInventoryOnClose = openInventoryOnClose;
      this.backgroundWidth = 176;
      this.backgroundHeight = 114 + handler.getRowsVisible() * 18;
      this.playerInventoryTitleY = this.backgroundHeight - 94;
      this.barColor = new StepColor(new float[]{0.75F, 0.9F, 1.0F, 2.0F}, new int[]{-13710223, -2379249, -1671646, -1618884});
      this.currentBarOccupancy = handler.getBundleInventory().getBundleContent().getOccupancy().floatValue();
      this.prevBarOccupancy = this.currentBarOccupancy;
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

      super.render(context, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(context, mouseX, mouseY);
   }

   protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
      int x0 = (this.width - this.backgroundWidth) / 2;
      int y0 = (this.height - this.backgroundHeight) / 2;
      context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, x0, y0, 0.0F, 0.0F, this.backgroundWidth, 35, 512, 512);
      int h = 79 + ((BundleInventoryHandler)this.handler).getRowsVisible() * 18;
      context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, x0, y0 + 35, 0.0F, 258 - h, this.backgroundWidth, h, 512, 512);
      if (BundleInventoryMod.config().showEquipmentSlotsInBundleInventory()) {
         context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, x0 - 36, y0 + this.backgroundHeight - 108, 176.0F, 25.0F, 32, 108, 512, 512);
      }

      BundleContentsComponent content = ((BundleInventoryHandler)this.handler).getBundleInventory().getBundleContent();
      if (content != null) {
         float barOccupancy = MathHelper.lerp(delta, this.prevBarOccupancy, this.currentBarOccupancy);
         int barWidth = MathHelper.ceil(barOccupancy * 38.0F);
         int barColor = this.barColor.get(content.getOccupancy().floatValue(), delta);
         context.fill(x0 + this.backgroundWidth - 46, y0 + 6, x0 + this.backgroundWidth - 46 + barWidth, y0 + 14, barColor);
      }

      context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, x0 + this.backgroundWidth - 47, y0 + 5, 176.0F, 0.0F, 40, 10, 512, 512);
   }

   protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
      context.drawText(this.textRenderer, this.trimmedTitle, this.titleX, this.titleY, 4210752, false);
      context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 4210752, false);
   }

   protected void drawSlot(DrawContext context, Slot slot) {
      if (!BundleInventoryMod.config().indicateOpenBundle()) {
         super.drawSlot(context, slot);
      } else if (this.client != null && slot instanceof BundleInventoryHandler.BundleItemSlot) {
         ItemStack stack = slot.getStack();
         if (stack.getItem() instanceof BundleItem bundleItem) {
            ItemStack var7 = new ItemStack(bundleItem);
            Builder openBundleContent = new Builder(new BundleContentsComponent(List.of(ItemStack.EMPTY)));
            openBundleContent.setSelectedStackIndex(0);
            var7.set(DataComponentTypes.BUNDLE_CONTENTS, openBundleContent.build());
            context.getMatrices().push();
            context.getMatrices().translate(0.0F, 0.0F, 100.0F);
            context.drawItem(var7, slot.x, slot.y);
            context.getMatrices().pop();
         } else {
            super.drawSlot(context, slot);
         }
      } else {
         super.drawSlot(context, slot);
      }
   }

   protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
      if (!(slot instanceof BundleInventoryHandler.BundleItemSlot)) {
         if (actionType != SlotActionType.SWAP || ((BundleInventoryHandler)this.handler).getPlayerInventoryBundleSlot() != button) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
               if (slotId >= 46) {
                  int slotIndex = slotId - 46;
                  ((BundleInventoryHandler)this.handler).handleBundleSlotAction(slotIndex, button, actionType);
                  return;
               }

               if (slotId >= 9
                  && actionType == SlotActionType.QUICK_MOVE
                  && slot.hasStack()
                  && ((BundleInventoryHandler)this.handler).quickMoveIntoBundle(player, slotId)) {
                  return;
               }
            }

            super.onMouseClick(slot, slotId, button, actionType);
         }
      }
   }

   protected void handledScreenTick() {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player != null && ((BundleInventoryHandler)this.handler).canUse(player)) {
         ((BundleInventoryHandler)this.handler).tick();
         this.prevBarOccupancy = this.currentBarOccupancy;
         BundleContentsComponent content = ((BundleInventoryHandler)this.handler).getBundleInventory().getBundleContent();
         if (content != null) {
            this.barColor.update(content.getOccupancy().floatValue());
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

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      return this.getFocused() != null && this.isDragging() && button == 0
         ? this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
         : super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0 && this.isDragging()) {
         this.setDragging(false);
         Element focused = this.getFocused();
         if (focused != null) {
            this.setFocused(null);
            return focused.mouseReleased(mouseX, mouseY, button);
         }
      }

      return super.mouseReleased(mouseX, mouseY, button);
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

   protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
      boolean isOutside = super.isClickOutsideBounds(mouseX, mouseY, left, top, button);
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
