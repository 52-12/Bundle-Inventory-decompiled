package io.socol.bundleinventory;

import io.socol.bundleinventory.config.BundleInventoryDefaultConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge entrypoint for Bundle Inventory.
 *
 * The Fabric build reads its config through Cloth Config when that mod is
 * present; the Forge build ships with the mod's built-in defaults.
 * The mod is declared client-side-only in META-INF/mods.toml
 * (clientSideOnly = true), so Forge never constructs this class on a
 * dedicated server.
 */
@Mod("bundleinventory")
public class BundleInventoryForgeMod {
   public BundleInventoryForgeMod(FMLJavaModLoadingContext context) {
      BundleInventoryMod.init(BundleInventoryDefaultConfig.INSTANCE);
   }
}
