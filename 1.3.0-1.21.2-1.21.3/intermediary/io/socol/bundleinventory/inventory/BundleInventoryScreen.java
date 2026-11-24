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
import net.minecraft.class_465;
import net.minecraft.class_746;
import net.minecraft.class_9276;

@Environment(EnvType.CLIENT)
public class BundleInventoryScreen extends class_465<BundleInventoryHandler> {
   private static final class_2960 TEXTURE = BundleInventoryMod.id("textures/gui/bundle.png");
   private final StepColor barColor;
   private float currentBarOccupancy;
   private float prevBarOccupancy;

   public BundleInventoryScreen(BundleInventoryHandler handler, class_1661 inventory) {
      super(handler, inventory, handler.getBundleTitle());
      this.field_2792 = 176;
      this.field_2779 = 258;
      this.field_25270 = this.field_2779 - 94;
      this.barColor = new StepColor(new float[]{0.75F, 0.9F, 1.0F, 2.0F}, new int[]{-13710223, -2379249, -1671646, -1618884});
      this.currentBarOccupancy = handler.getBundleInventory().getBundleContent().method_57428().floatValue();
      this.prevBarOccupancy = this.currentBarOccupancy;
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      super.method_25394(context, mouseX, mouseY, delta);
      this.method_2380(context, mouseX, mouseY);
   }

   protected void method_2389(class_332 context, float delta, int mouseX, int mouseY) {
      int i = (this.field_22789 - this.field_2792) / 2;
      int j = (this.field_22790 - this.field_2779) / 2;
      context.method_25290(class_1921::method_62277, TEXTURE, i, j, 0.0F, 0.0F, this.field_2792, this.field_2779, 512, 512);
      class_9276 content = ((BundleInventoryHandler)this.field_2797).getBundleInventory().getBundleContent();
      if (content != null) {
         float barOccupancy = class_3532.method_16439(delta, this.prevBarOccupancy, this.currentBarOccupancy);
         int barWidth = class_3532.method_15386(barOccupancy * 38.0F);
         int barColor = this.barColor.get(content.method_57428().floatValue(), delta);
         context.method_25294(i + this.field_2792 - 46, j + 6, i + this.field_2792 - 46 + barWidth, j + 14, barColor);
         context.method_25290(class_1921::method_62277, TEXTURE, i + this.field_2792 - 47, j + 5, 176.0F, 0.0F, 40, 10, 512, 512);
      }
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
      super.method_37432();
      this.prevBarOccupancy = this.currentBarOccupancy;
      class_9276 content = ((BundleInventoryHandler)this.field_2797).getBundleInventory().getBundleContent();
      if (content != null) {
         this.barColor.update(content.method_57428().floatValue());
         float targetBarValue = content.method_57428().floatValue();
         this.currentBarOccupancy = class_3532.method_16439(0.3F, this.currentBarOccupancy, targetBarValue);
         if (targetBarValue <= 0.0F && this.currentBarOccupancy <= 0.01F) {
            this.currentBarOccupancy = 0.0F;
         }
      }

      class_746 player = class_310.method_1551().field_1724;
      if (player == null || !((BundleInventoryHandler)this.field_2797).method_7597(player)) {
         class_310.method_1551().method_1507(null);
      }
   }
}
