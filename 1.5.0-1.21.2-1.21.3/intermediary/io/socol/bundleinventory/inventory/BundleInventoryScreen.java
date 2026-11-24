package io.socol.bundleinventory.inventory;

import io.socol.bundleinventory.BundleInventoryMod;
import io.socol.bundleinventory.util.StepColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_364;
import net.minecraft.class_465;
import net.minecraft.class_490;
import net.minecraft.class_746;
import net.minecraft.class_9276;

@Environment(EnvType.CLIENT)
public class BundleInventoryScreen extends class_465<BundleInventoryHandler> {
   private static final class_2960 TEXTURE = BundleInventoryMod.id("textures/gui/bundle.png");
   private final boolean openInventoryOnClose;
   private final StepColor barColor;
   private float currentBarOccupancy;
   private float prevBarOccupancy;
   private int scrollBarSelectedRow;
   private int scrollBarRowsCount;

   public BundleInventoryScreen(BundleInventoryHandler handler, class_1661 inventory, boolean openInventoryOnClose) {
      super(handler, inventory, handler.getBundleTitle());
      this.openInventoryOnClose = openInventoryOnClose;
      this.field_2792 = 176;
      this.field_2779 = 222;
      this.field_25270 = this.field_2779 - 94;
      this.barColor = new StepColor(new float[]{0.75F, 0.9F, 1.0F, 2.0F}, new int[]{-13710223, -2379249, -1671646, -1618884});
      this.currentBarOccupancy = handler.getBundleInventory().getBundleContent().method_57428().floatValue();
      this.prevBarOccupancy = this.currentBarOccupancy;
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

      super.method_25394(context, mouseX, mouseY, delta);
      this.method_2380(context, mouseX, mouseY);
   }

   protected void method_2389(class_332 context, float delta, int mouseX, int mouseY) {
      int x0 = (this.field_22789 - this.field_2792) / 2;
      int y0 = (this.field_22790 - this.field_2779) / 2;
      context.method_25290(class_1921::method_62277, TEXTURE, x0, y0, 0.0F, 0.0F, this.field_2792, 35, 512, 512);
      int h = 187;
      context.method_25290(class_1921::method_62277, TEXTURE, x0, y0 + 35, 0.0F, 258 - h, this.field_2792, h, 512, 512);
      class_9276 content = ((BundleInventoryHandler)this.field_2797).getBundleInventory().getBundleContent();
      if (content != null) {
         float barOccupancy = class_3532.method_16439(delta, this.prevBarOccupancy, this.currentBarOccupancy);
         int barWidth = class_3532.method_15386(barOccupancy * 38.0F);
         int barColor = this.barColor.get(content.method_57428().floatValue(), delta);
         context.method_25294(x0 + this.field_2792 - 46, y0 + 6, x0 + this.field_2792 - 46 + barWidth, y0 + 14, barColor);
         context.method_25290(class_1921::method_62277, TEXTURE, x0 + this.field_2792 - 47, y0 + 5, 176.0F, 0.0F, 40, 10, 512, 512);
      }
   }

   protected void method_2388(class_332 context, int mouseX, int mouseY) {
      context.method_51439(this.field_22793, ((BundleInventoryHandler)this.field_2797).getBundleTitle(), this.field_25267, this.field_25268, 4210752, false);
      context.method_51439(this.field_22793, this.field_29347, this.field_25269, this.field_25270, 4210752, false);
   }

   protected void method_2383(class_1735 slot, int slotId, int button, class_1713 actionType) {
      if (!(slot instanceof BundleInventoryHandler.BundleItemSlot)) {
         class_746 player = class_310.method_1551().field_1724;
         if (player != null) {
            if (slotId >= 45) {
               int slotIndex = slotId - 45;
               ((BundleInventoryHandler)this.field_2797).handleBundleSlotAction(slotIndex, button, actionType);
               return;
            }

            if (slotId >= 9
               && actionType == class_1713.field_7794
               && slot.method_7681()
               && ((BundleInventoryHandler)this.field_2797).quickMoveIntoBundle(player, slotId)) {
               return;
            }
         }

         super.method_2383(slot, slotId, button, actionType);
      }
   }

   protected void method_37432() {
      class_746 player = class_310.method_1551().field_1724;
      if (player != null && ((BundleInventoryHandler)this.field_2797).method_7597(player)) {
         ((BundleInventoryHandler)this.field_2797).tick();
         this.prevBarOccupancy = this.currentBarOccupancy;
         class_9276 content = ((BundleInventoryHandler)this.field_2797).getBundleInventory().getBundleContent();
         if (content != null) {
            this.barColor.update(content.method_57428().floatValue());
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
      BundleContentSlider scrollBar = new BundleContentSlider(x0 + 155, y0 + 17, 14, 108, this::updateSelectedRow);
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

   public boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      return this.method_25399() != null && this.method_25397() && button == 0
         ? this.method_25399().method_25403(mouseX, mouseY, button, deltaX, deltaY)
         : super.method_25403(mouseX, mouseY, button, deltaX, deltaY);
   }

   public boolean method_25406(double mouseX, double mouseY, int button) {
      if (button == 0 && this.method_25397()) {
         this.method_25398(false);
         class_364 focused = this.method_25399();
         if (focused != null) {
            this.method_25395(null);
            return focused.method_25406(mouseX, mouseY, button);
         }
      }

      return super.method_25406(mouseX, mouseY, button);
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      int x0 = (this.field_22789 - this.field_2792) / 2;
      int y0 = (this.field_22790 - this.field_2779) / 2;
      int h = 132;
      if (mouseX >= x0 && mouseX < x0 + this.field_2792 && mouseY >= y0 && mouseY < y0 + h) {
         ((BundleInventoryHandler)this.field_2797)
            .setSelectedRow(((BundleInventoryHandler)this.field_2797).getSelectedRow() - class_3532.method_17822(verticalAmount));
         return true;
      } else {
         return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   private void updateSelectedRow(double scrollBarValue) {
      int newSelectedRow = (int)(((BundleInventoryHandler)this.field_2797).getRowsCount() * scrollBarValue);
      this.scrollBarSelectedRow = ((BundleInventoryHandler)this.field_2797).setSelectedRow(newSelectedRow);
   }
}
