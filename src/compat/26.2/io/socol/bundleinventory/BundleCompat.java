package io.socol.bundleinventory;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.component.BundleContents;

/** Version-specific shims. This file is for Minecraft 26.2. */
public final class BundleCompat {
   private BundleCompat() {
   }

   public static BundleContents.Mutable asMutable(BundleContents content) {
      return new BundleContents.Mutable(content);
   }

   public static boolean isKeyDown(int key) {
      return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), key);
   }
}
