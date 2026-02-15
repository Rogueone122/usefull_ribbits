
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package me.rogue_one.useful_ribbits.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;

import me.rogue_one.useful_ribbits.block.RibbitChestBlock;
import me.rogue_one.useful_ribbits.block.RibbitBedBlock;
import me.rogue_one.useful_ribbits.UsefulRibbitsMod;

public class UsefulRibbitsModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, UsefulRibbitsMod.MODID);
	public static final RegistryObject<Block> RIBBIT_CHEST = REGISTRY.register("ribbit_chest", () -> new RibbitChestBlock());
	public static final RegistryObject<Block> RIBBIT_BED = REGISTRY.register("ribbit_bed", () -> new RibbitBedBlock());
	// Start of user code block custom blocks
	// End of user code block custom blocks
}
