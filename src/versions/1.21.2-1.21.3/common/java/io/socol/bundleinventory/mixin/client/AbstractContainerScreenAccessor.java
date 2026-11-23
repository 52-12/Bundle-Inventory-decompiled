package io.socol.bundleinventory.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 1.21.2 keeps skipNextRelease private; 1.21.4 exposes it to subclasses. */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
   @Accessor("skipNextRelease")
   void setSkipNextRelease(boolean value);
}
