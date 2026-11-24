package io.socol.bundleinventory.inventory;

import io.socol.bundleinventory.BundleInventoryMod;
import io.socol.bundleinventory.util.UIDrawer;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2477;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_364;
import net.minecraft.class_3675;
import net.minecraft.class_465;
import net.minecraft.class_490;
import net.minecraft.class_5348;
import net.minecraft.class_5481;
import net.minecraft.class_5537;
import net.minecraft.class_5632;
import net.minecraft.class_5684;
import net.minecraft.class_746;
import net.minecraft.class_9276;
import net.minecraft.class_9334;
import net.minecraft.class_1259.class_1260;
import net.minecraft.class_1259.class_1261;
import net.minecraft.class_9276.class_9277;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BundleInventoryScreen extends class_465<BundleInventoryHandler> {
   private static final class_2960 SLOT_HIGHLIGHT_BACK_TEXTURE = class_2960.method_60656("container/slot_highlight_back");
   private static final class_2960 SLOT_HIGHLIGHT_FRONT_TEXTURE = class_2960.method_60656("container/slot_highlight_front");
   private static final int MAX_TITLE_WIDTH = 120;
   private final class_5481 trimmedTitle;
   private final boolean openInventoryOnClose;
   private float currentBarOccupancy;
   private float prevBarOccupancy;
   private int scrollBarSelectedRow;
   private int scrollBarRowsCount;
   private class_1735 focusedBundleSlot = null;

   public BundleInventoryScreen(BundleInventoryHandler handler, class_1661 inventory, boolean openInventoryOnClose) {
      super(handler, inventory, handler.getBundleTitle());
      this.trimmedTitle = trimTitleToWidth(this.method_25440(), 120);
      this.openInventoryOnClose = openInventoryOnClose;
      this.field_2792 = 176;
      this.field_2779 = 114 + handler.getRowsVisible() * 18;
      this.field_25270 = this.field_2779 - 94;
      this.currentBarOccupancy = handler.getBundleInventory().getBundleContent().method_57428().floatValue();
      this.prevBarOccupancy = this.currentBarOccupancy;
   }

   @Nullable
   private class_1735 getBundleSlotAt(double mouseX, double mouseY) {
      for (class_1735 slot : ((BundleInventoryHandler)this.field_2797).getBundleSlots()) {
         if (slot.method_7682() && this.method_2378(slot.field_7873, slot.field_7872, 16, 16, mouseX, mouseY)) {
            return slot;
         }
      }

      return null;
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      if (this.scrollBarSelectedRow != ((BundleInventoryHandler)this.field_2797).getSelectedRow()
         || this.scrollBarRowsCount != ((BundleInventoryHandler)this.field_2797).getRowsCount()) {
         this.scrollBarSelectedRow = ((BundleInventoryHandler)this.field_2797).getSelectedRow();
         this.scrollBarRowsCount = ((BundleInventoryHandler)this.field_2797).getRowsCount();

         for (class_364 child : this.method_25396()) {
            if (child instanceof BundleContentSlider scrollBar) {
               if (((BundleInventoryHandler)this.field_2797).getRowsCount() < 2) {
                  scrollBar.setValue(-1.0);
               } else {
                  double value = (double)((BundleInventoryHandler)this.field_2797).getSelectedRow()
                     / (((BundleInventoryHandler)this.field_2797).getRowsCount() - 1);
                  scrollBar.setValue(value);
               }
            }
         }
      }

      this.focusedBundleSlot = this.getBundleSlotAt(mouseX, mouseY);
      super.method_25394(context, mouseX, mouseY, delta);
      this.method_2380(context, mouseX, mouseY);
      this.drawMouseoverBundleTooltip(context, mouseX, mouseY);
   }

   private void drawMouseoverBundleTooltip(class_332 context, int x, int y) {
      if (this.focusedBundleSlot != null && this.focusedBundleSlot.method_7681()) {
         class_1799 focusedStack = this.focusedBundleSlot.method_7677();
         Optional<class_5632> tooltipData = focusedStack.method_32347();
         Boolean stickyTooltip = tooltipData.map(class_5684::method_32663).<Boolean>map(class_5684::method_62003).orElse(false);
         if (((BundleInventoryHandler)this.field_2797).method_34255().method_7960() || stickyTooltip) {
            context.method_51437(
               this.field_22793, this.method_51454(focusedStack), tooltipData, x, y, (class_2960)focusedStack.method_58694(class_9334.field_54198)
            );
         }
      }
   }

   protected void method_2389(class_332 context, float delta, int mouseX, int mouseY) {
      int x0 = (this.field_22789 - this.field_2792) / 2;
      int y0 = (this.field_22790 - this.field_2779) / 2;
      UIDrawer drawer = new UIDrawer(context).at(x0, y0);
      drawer.drawResizablePlate(this.field_2792, this.field_2779);
      drawer.move(7, 17).drawSlots(8, ((BundleInventoryHandler)this.field_2797).getRowsVisible());
      drawer.move(7, this.field_2779 - 83).drawSlots(9, 3);
      drawer.move(7, this.field_2779 - 25).drawSlots(9, 1);
      drawer.move(155, 17).drawScrollerGroove(((BundleInventoryHandler)this.field_2797).getRowsVisible() * 18);
      if (BundleInventoryMod.config().showEquipmentSlotsInBundleInventory()) {
         UIDrawer equipmentDrawer = drawer.move(-36, this.field_2779 - 108);
         equipmentDrawer.drawResizablePlate(32, 108);
         equipmentDrawer.move(7, 7).drawSlots(1, 4);
         equipmentDrawer.move(7, 83).drawSlot();
      }

      class_9276 content = ((BundleInventoryHandler)this.field_2797).getBundleInventory().getBundleContent();
      if (content != null) {
         int barWidth = 55;
         float barOccupancy = class_3532.method_16439(delta, this.prevBarOccupancy, this.currentBarOccupancy);
         barOccupancy = class_3532.method_15363(barOccupancy, 0.0F, 1.0F);
         int barProgress = class_3532.method_15386(barOccupancy * barWidth);
         class_1260 barColor = class_1260.field_5780;
         if (barOccupancy > 0.9) {
            barColor = class_1260.field_5784;
         } else if (barOccupancy > 0.7) {
            barColor = class_1260.field_5782;
         }

         drawer.move(this.field_2792 - 7 - barWidth, 8).drawBossBar(barWidth, barProgress, barColor, class_1261.field_5791);
      }
   }

   protected void method_2388(class_332 context, int mouseX, int mouseY) {
      context.method_51430(this.field_22793, this.trimmedTitle, this.field_25267, this.field_25268, -12566464, false);
      context.method_51439(this.field_22793, this.field_29347, this.field_25269, this.field_25270, -12566464, false);
   }

   protected void method_64508(class_332 context) {
      super.method_64508(context);
      this.drawBundleSlotHighlightBack(context);

      for (class_1735 slot : ((BundleInventoryHandler)this.field_2797).getBundleSlots()) {
         if (slot.method_7682()) {
            this.method_2385(context, slot);
         }
      }

      this.drawBundleSlotHighlightFront(context);
   }

   private void drawBundleSlotHighlightBack(class_332 context) {
      if (this.focusedBundleSlot != null && this.focusedBundleSlot.method_51306()) {
         context.method_52706(
            class_10799.field_56883, SLOT_HIGHLIGHT_BACK_TEXTURE, this.focusedBundleSlot.field_7873 - 4, this.focusedBundleSlot.field_7872 - 4, 24, 24
         );
      }
   }

   private void drawBundleSlotHighlightFront(class_332 context) {
      if (this.focusedBundleSlot != null && this.focusedBundleSlot.method_51306()) {
         context.method_52706(
            class_10799.field_56883, SLOT_HIGHLIGHT_FRONT_TEXTURE, this.focusedBundleSlot.field_7873 - 4, this.focusedBundleSlot.field_7872 - 4, 24, 24
         );
      }
   }

   protected void method_2385(class_332 context, class_1735 slot) {
      if (!BundleInventoryMod.config().indicateOpenBundle()) {
         super.method_2385(context, slot);
      } else if (this.field_22787 != null && slot instanceof BundleInventoryHandler.BundleItemSlot) {
         class_1799 stack = slot.method_7677();
         if (stack.method_7909() instanceof class_5537 bundleItem) {
            class_1799 var7 = new class_1799(bundleItem);
            class_9277 openBundleContent = new class_9277(new class_9276(List.of(class_1799.field_8037)));
            openBundleContent.method_64662(0);
            var7.method_57379(class_9334.field_49650, openBundleContent.method_57435());
            context.method_51448().pushMatrix();
            context.method_51427(var7, slot.field_7873, slot.field_7872);
            context.method_51431(this.field_22787.field_1772, stack, slot.field_7873, slot.field_7872);
            context.method_51448().popMatrix();
         } else {
            super.method_2385(context, slot);
         }
      } else {
         super.method_2385(context, slot);
      }
   }

   protected void method_37432() {
      class_746 player = class_310.method_1551().field_1724;
      if (player != null && ((BundleInventoryHandler)this.field_2797).method_7597(player)) {
         ((BundleInventoryHandler)this.field_2797).tick();
         this.prevBarOccupancy = this.currentBarOccupancy;
         class_9276 content = ((BundleInventoryHandler)this.field_2797).getBundleInventory().getBundleContent();
         if (content != null) {
            float targetBarValue = content.method_57428().floatValue();
            this.currentBarOccupancy = class_3532.method_16439(0.5F, this.currentBarOccupancy, targetBarValue);
            if (targetBarValue <= 0.0F && this.currentBarOccupancy <= 0.01F) {
               this.currentBarOccupancy = 0.0F;
            }
         }

         super.method_37432();
      } else {
         class_310.method_1551().method_1507(null);
      }
   }

   protected void method_25426() {
      super.method_25426();
      this.scrollBarSelectedRow = -1;
      this.scrollBarRowsCount = -1;
      int x0 = (this.field_22789 - this.field_2792) / 2;
      int y0 = (this.field_22790 - this.field_2779) / 2;
      BundleContentSlider scrollBar = new BundleContentSlider(
         x0 + 155, y0 + 17, 14, ((BundleInventoryHandler)this.field_2797).getRowsVisible() * 18, this::updateSelectedRow
      );
      this.method_37063(scrollBar);
   }

   public void method_25419() {
      class_746 player = this.field_22787.field_1724;
      if (player != null) {
         ((BundleInventoryHandler)this.field_2797).close(player);
      }

      if (this.openInventoryOnClose && player != null) {
         this.field_22787.method_1507(new class_490(player));
      } else {
         super.method_25419();
      }
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      class_1735 bundleSlot = this.getBundleSlotAt(click.comp_4798(), click.comp_4799());
      if (bundleSlot == null) {
         return super.method_25402(click, doubled);
      }

      boolean shiftClick = class_3675.method_15987(class_310.method_1551().method_22683(), 340)
         || class_3675.method_15987(class_310.method_1551().method_22683(), 344);
      class_1713 actionType = shiftClick ? class_1713.field_7794 : class_1713.field_7790;
      int slotIndex = bundleSlot.method_34266();
      ((BundleInventoryHandler)this.field_2797).handleBundleSlotAction(slotIndex, click.method_74245(), actionType);
      this.field_2798 = true;
      return true;
   }

   public boolean method_25403(class_11909 click, double offsetX, double offsetY) {
      return this.method_25399() != null && this.method_25397() && click.method_74245() == 0
         ? this.method_25399().method_25403(click, offsetX, offsetY)
         : super.method_25403(click, offsetX, offsetY);
   }

   public boolean method_25406(class_11909 click) {
      if (click.method_74245() == 0 && this.method_25397()) {
         this.method_25398(false);
         class_364 focused = this.method_25399();
         if (focused != null) {
            this.method_25395(null);
            return focused.method_25406(click);
         }
      }

      return super.method_25406(click);
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      int x0 = (this.field_22789 - this.field_2792) / 2;
      int y0 = (this.field_22790 - this.field_2779) / 2;
      int h = 24 + ((BundleInventoryHandler)this.field_2797).getRowsVisible() * 18;
      if (mouseX >= x0 && mouseX < x0 + this.field_2792 && mouseY >= y0 && mouseY < y0 + h) {
         ((BundleInventoryHandler)this.field_2797)
            .setSelectedRow(((BundleInventoryHandler)this.field_2797).getSelectedRow() - class_3532.method_17822(verticalAmount));
         return true;
      } else {
         return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   public boolean method_25404(class_11908 input) {
      if (this.focusedBundleSlot != null
         && this.focusedBundleSlot.method_7681()
         && this.field_22787 != null
         && this.field_22787.field_1690.field_1869.method_1417(input)) {
         int slotIndex = this.focusedBundleSlot.method_34266();
         ((BundleInventoryHandler)this.field_2797).handleBundleSlotAction(slotIndex, input.method_74240() ? 1 : 0, class_1713.field_7795);
         return true;
      } else {
         return super.method_25404(input);
      }
   }

   protected boolean method_2381(double mouseX, double mouseY, int left, int top) {
      boolean isOutside = super.method_2381(mouseX, mouseY, left, top);
      if (!isOutside) {
         return false;
      } else {
         return !BundleInventoryMod.config().showEquipmentSlotsInBundleInventory()
            ? true
            : mouseX < left - 36.0 || mouseY < (double)top + this.field_2792 - 108.0 || mouseX >= left - 4 || mouseY >= top + this.field_2779;
      }
   }

   private void updateSelectedRow(double scrollBarValue) {
      int newSelectedRow = (int)(((BundleInventoryHandler)this.field_2797).getRowsCount() * scrollBarValue);
      this.scrollBarSelectedRow = ((BundleInventoryHandler)this.field_2797).setSelectedRow(newSelectedRow);
   }

   private static class_5481 trimTitleToWidth(class_2561 text, int width) {
      class_310 client = class_310.method_1551();
      int i = client.field_1772.method_27525(text);
      if (i > width) {
         class_5348 trimmed = client.field_1772.method_1714(text, width - client.field_1772.method_1727("..."));
         class_5348 result = class_5348.method_29433(new class_5348[]{trimmed, class_5348.method_29430("...")});
         return class_2477.method_10517().method_30934(result);
      } else {
         return text.method_30937();
      }
   }
}
