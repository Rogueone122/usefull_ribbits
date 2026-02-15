
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package me.rogue_one.useful_ribbits.init;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.gui.screens.MenuScreens;

import me.rogue_one.useful_ribbits.client.gui.RibbitChestGUIScreen;
import me.rogue_one.useful_ribbits.client.gui.RibbitBedGUIScreen;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class UsefulRibbitsModScreens {
	@SubscribeEvent
	public static void clientLoad(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			MenuScreens.register(UsefulRibbitsModMenus.RIBBIT_CHEST_GUI.get(), RibbitChestGUIScreen::new);
			MenuScreens.register(UsefulRibbitsModMenus.RIBBIT_BED_GUI.get(), RibbitBedGUIScreen::new);
		});
	}
}
