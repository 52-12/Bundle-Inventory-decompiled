package io.socol.bundleinventory.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.ARGB;

@Environment(EnvType.CLIENT)
public class StepColor {
   private final float[] borders;
   private final int[] colors;
   private Integer targetColor;
   private int currentColor;
   private int prevColor;

   public StepColor(float[] borders, int[] colors) {
      this.borders = borders;
      this.colors = colors;
   }

   public void update(float value) {
      this.init(value);
      this.targetColor = this.getTarget(value);
      this.prevColor = this.currentColor;
      this.currentColor = ARGB.srgbLerp(0.25F, this.currentColor, this.targetColor);
   }

   public int get(float value, float tickDelta) {
      this.init(value);
      return ARGB.srgbLerp(tickDelta, this.prevColor, this.currentColor);
   }

   private void init(float value) {
      if (this.targetColor == null) {
         this.targetColor = this.getTarget(value);
         this.prevColor = this.targetColor;
         this.currentColor = this.targetColor;
      } else if (value <= 0.0) {
         this.reset();
      }
   }

   public void reset() {
      this.targetColor = null;
   }

   private int getTarget(float value) {
      float prev = 0.0F;

      for (int i = 0; i < this.borders.length; i++) {
         if (value >= prev && value < this.borders[i]) {
            return this.colors[i];
         }

         prev = this.borders[i];
      }

      return -1;
   }
}
