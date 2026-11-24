package io.socol.bundleinventory.inventory;

import io.socol.bundleinventory.BundleInventoryMod;
import java.util.function.DoubleConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1144;
import net.minecraft.class_1921;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_6382;

@Environment(EnvType.CLIENT)
public class BundleContentSlider extends class_339 {
   private static final class_2960 TEXTURE = BundleInventoryMod.id("textures/gui/bundle.png");
   private final DoubleConsumer valueConsumer;
   private double value;

   public BundleContentSlider(int x, int y, int width, int height, DoubleConsumer valueConsumer) {
      super(x, y, width, height, class_2561.method_43473());
      this.valueConsumer = valueConsumer;
   }

   protected void method_48579(class_332 context, int mouseX, int mouseY, float delta) {
      int u = 200;
      int dy = 1;
      if (this.value >= 0.0) {
         dy += (int)Math.round(this.value * (this.field_22759 - 17));
         u = !this.method_49606() && !this.method_25370() ? 176 : 188;
      }

      context.method_25290(class_1921::method_62277, TEXTURE, this.method_46426() + 1, this.method_46427() + dy, u, 10.0F, 12, 15, 512, 512);
   }

   protected void method_47399(class_6382 builder) {
   }

   public void setValue(double value) {
      this.value = value;
   }

   private void setValueFromMouse(double mouseY) {
      if (!(this.value < 0.0)) {
         double l = mouseY - (this.method_46427() + 9);
         this.value = l / (this.field_22759 - 17);
         this.value = Math.clamp(this.value, 0.0, 1.0);
         this.valueConsumer.accept(this.value);
      }
   }

   public void method_25348(double mouseX, double mouseY) {
      this.setValueFromMouse(mouseY);
   }

   protected void method_25349(double mouseX, double mouseY, double deltaX, double deltaY) {
      this.setValueFromMouse(mouseY);
      super.method_25349(mouseX, mouseY, deltaX, deltaY);
   }

   public void method_25354(class_1144 soundManager) {
   }

   public void method_25357(double mouseX, double mouseY) {
      if (this.value >= 0.0) {
         super.method_25354(class_310.method_1551().method_1483());
      }
   }
}
