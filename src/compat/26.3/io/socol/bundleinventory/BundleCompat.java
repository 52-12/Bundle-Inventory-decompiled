package io.socol.bundleinventory;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.item.component.BundleContents;

/** Version-specific shims. This file is for Minecraft 26.3. */
public final class BundleCompat {
   private BundleCompat() {
   }

   public static BundleContents.Mutable asMutable(BundleContents content) {
      return content.asMutable();
   }

   public static boolean isKeyDown(int key) {
      return InputConstants.isKeyDown(key);
   }
}
