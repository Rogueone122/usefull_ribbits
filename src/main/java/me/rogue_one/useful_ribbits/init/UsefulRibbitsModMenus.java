
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package me.rogue_one.useful_ribbits.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.extensions.IForgeMenuType;

import net.minecraft.world.inventory.MenuType;

import me.rogue_one.useful_ribbits.world.inventory.RibbitChestGUIMenu;
import me.rogue_one.useful_ribbits.world.inventory.RibbitBedGUIMenu;
import me.rogue_one.useful_ribbits.UsefulRibbitsMod;

public class UsefulRibbitsModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, UsefulRibbitsMod.MODID);
	public static final RegistryObject<MenuType<RibbitChestGUIMenu>> RIBBIT_CHEST_GUI = REGISTRY.register("ribbit_chest_gui", () -> IForgeMenuType.create(RibbitChestGUIMenu::new));
	public static final RegistryObject<MenuType<RibbitBedGUIMenu>> RIBBIT_BED_GUI = REGISTRY.register("ribbit_bed_gui", () -> IForgeMenuType.create(RibbitBedGUIMenu::new));
}
