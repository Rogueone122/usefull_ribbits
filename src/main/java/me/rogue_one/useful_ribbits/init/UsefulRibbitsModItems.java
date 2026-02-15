
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package me.rogue_one.useful_ribbits.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.ForgeSpawnEggItem;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import me.rogue_one.useful_ribbits.UsefulRibbitsMod;

public class UsefulRibbitsModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, UsefulRibbitsMod.MODID);
	public static final RegistryObject<Item> CHEF_RIBBIT_SPAWN_EGG = REGISTRY.register("chef_ribbit_spawn_egg", () -> new ForgeSpawnEggItem(UsefulRibbitsModEntities.CHEF_RIBBIT, -3342541, -3342439, new Item.Properties()));
	public static final RegistryObject<Item> RIBBIT_CHEST = block(UsefulRibbitsModBlocks.RIBBIT_CHEST);
	public static final RegistryObject<Item> RIBBIT_BED = block(UsefulRibbitsModBlocks.RIBBIT_BED);
	public static final RegistryObject<Item> MINER_RIBBIT_SPAWN_EGG = REGISTRY.register("miner_ribbit_spawn_egg", () -> new ForgeSpawnEggItem(UsefulRibbitsModEntities.MINER_RIBBIT, -3342490, -154, new Item.Properties()));
	public static final RegistryObject<Item> FARMER_RIBBIT_SPAWN_EGG = REGISTRY.register("farmer_ribbit_spawn_egg", () -> new ForgeSpawnEggItem(UsefulRibbitsModEntities.FARMER_RIBBIT, -3342490, -13395712, new Item.Properties()));

	// Start of user code block custom items
	// End of user code block custom items
	private static RegistryObject<Item> block(RegistryObject<Block> block) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
	}
}
