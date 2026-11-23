package io.socol.bundleinventory;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.BundleContents.Mutable;

public final class BundleCompat {
   private BundleCompat() {
   }

   public static Mutable asMutable(BundleContents content) {
      return content.asMutable();
   }

   public static boolean isKeyDown(int key) {
      return InputConstants.isKeyDown(key);
   }
}
