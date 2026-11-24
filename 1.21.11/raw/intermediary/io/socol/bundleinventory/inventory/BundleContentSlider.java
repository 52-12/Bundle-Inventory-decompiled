package io.socol.bundleinventory.inventory;

import io.socol.bundleinventory.BundleInventoryMod;
import io.socol.bundleinventory.util.UIDrawer;
import java.util.function.DoubleConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1144;
import net.minecraft.class_11909;
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
      int dy = 1;
      if (this.value >= 0.0) {
         dy += (int)Math.round(this.value * (this.field_22759 - 17));
      }

      new UIDrawer(context).at(this.method_46426() + 1, this.method_46427() + dy).drawScroller(this.value >= 0.0);
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

   public void method_25348(class_11909 click, boolean doubled) {
      this.setValueFromMouse(click.comp_4799());
   }

   protected void method_25349(class_11909 click, double offsetX, double offsetY) {
      this.setValueFromMouse(click.comp_4799());
      super.method_25349(click, offsetX, offsetY);
   }

   public void method_25354(class_1144 soundManager) {
   }

   public void method_25357(class_11909 click) {
      if (this.value >= 0.0) {
         super.method_25354(class_310.method_1551().method_1483());
      }
   }
}
