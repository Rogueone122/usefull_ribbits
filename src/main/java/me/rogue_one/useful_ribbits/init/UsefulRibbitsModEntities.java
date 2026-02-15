
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package me.rogue_one.useful_ribbits.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;

import me.rogue_one.useful_ribbits.entity.MinerRibbitEntity;
import me.rogue_one.useful_ribbits.entity.FarmerRibbitEntity;
import me.rogue_one.useful_ribbits.entity.ChefRibbitEntity;
import me.rogue_one.useful_ribbits.UsefulRibbitsMod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class UsefulRibbitsModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, UsefulRibbitsMod.MODID);
	public static final RegistryObject<EntityType<ChefRibbitEntity>> CHEF_RIBBIT = register("chef_ribbit",
			EntityType.Builder.<ChefRibbitEntity>of(ChefRibbitEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(ChefRibbitEntity::new)

					.sized(0.5f, 0.8f));
	public static final RegistryObject<EntityType<MinerRibbitEntity>> MINER_RIBBIT = register("miner_ribbit",
			EntityType.Builder.<MinerRibbitEntity>of(MinerRibbitEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(MinerRibbitEntity::new)

					.sized(0.5f, 0.7f));
	public static final RegistryObject<EntityType<FarmerRibbitEntity>> FARMER_RIBBIT = register("farmer_ribbit",
			EntityType.Builder.<FarmerRibbitEntity>of(FarmerRibbitEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(FarmerRibbitEntity::new)

					.sized(0.5f, 0.7f));

	// Start of user code block custom entities
	// End of user code block custom entities
	private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			ChefRibbitEntity.init();
			MinerRibbitEntity.init();
			FarmerRibbitEntity.init();
		});
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(CHEF_RIBBIT.get(), ChefRibbitEntity.createAttributes().build());
		event.put(MINER_RIBBIT.get(), MinerRibbitEntity.createAttributes().build());
		event.put(FARMER_RIBBIT.get(), FarmerRibbitEntity.createAttributes().build());
	}
}
