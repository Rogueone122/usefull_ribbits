
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package me.rogue_one.useful_ribbits.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;

import me.rogue_one.useful_ribbits.block.entity.RibbitChestBlockEntity;
import me.rogue_one.useful_ribbits.UsefulRibbitsMod;

public class UsefulRibbitsModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, UsefulRibbitsMod.MODID);
	public static final RegistryObject<BlockEntityType<?>> RIBBIT_CHEST = register("ribbit_chest", UsefulRibbitsModBlocks.RIBBIT_CHEST, RibbitChestBlockEntity::new);

	// Start of user code block custom block entities
	// End of user code block custom block entities
	private static RegistryObject<BlockEntityType<?>> register(String registryname, RegistryObject<Block> block, BlockEntityType.BlockEntitySupplier<?> supplier) {
		return REGISTRY.register(registryname, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
	}
}
