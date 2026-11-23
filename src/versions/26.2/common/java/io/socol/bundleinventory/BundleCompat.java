package io.socol.bundleinventory;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.BundleContents.Mutable;

public final class BundleCompat {
   private BundleCompat() {
   }

   public static Mutable asMutable(BundleContents content) {
      return new Mutable(content);
   }

   public static boolean isKeyDown(int key) {
      return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), key);
   }
}
