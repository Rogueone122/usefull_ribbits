
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package me.rogue_one.useful_ribbits.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import me.rogue_one.useful_ribbits.UsefulRibbitsMod;

public class UsefulRibbitsModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UsefulRibbitsMod.MODID);
	public static final RegistryObject<CreativeModeTab> USEFUL_RIBBITS = REGISTRY.register("useful_ribbits",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.useful_ribbits.useful_ribbits")).icon(() -> new ItemStack(UsefulRibbitsModBlocks.RIBBIT_CHEST.get())).displayItems((parameters, tabData) -> {
				tabData.accept(UsefulRibbitsModBlocks.RIBBIT_CHEST.get().asItem());
				tabData.accept(UsefulRibbitsModBlocks.RIBBIT_BED.get().asItem());
				tabData.accept(UsefulRibbitsModItems.CHEF_RIBBIT_SPAWN_EGG.get());
				tabData.accept(UsefulRibbitsModItems.MINER_RIBBIT_SPAWN_EGG.get());
				tabData.accept(UsefulRibbitsModItems.FARMER_RIBBIT_SPAWN_EGG.get());
			}).withSearchBar().build());
}
