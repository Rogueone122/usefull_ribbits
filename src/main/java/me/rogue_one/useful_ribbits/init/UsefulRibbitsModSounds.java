
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package me.rogue_one.useful_ribbits.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

import me.rogue_one.useful_ribbits.UsefulRibbitsMod;

public class UsefulRibbitsModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, UsefulRibbitsMod.MODID);
	public static final RegistryObject<SoundEvent> RIBBIT_AMBIANT = REGISTRY.register("ribbit_ambiant", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("useful_ribbits", "ribbit_ambiant")));
	public static final RegistryObject<SoundEvent> RIBBIT_DEATH = REGISTRY.register("ribbit_death", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("useful_ribbits", "ribbit_death")));
	public static final RegistryObject<SoundEvent> RIBBIT_HURT = REGISTRY.register("ribbit_hurt", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("useful_ribbits", "ribbit_hurt")));
	public static final RegistryObject<SoundEvent> RIBBIT_STEP = REGISTRY.register("ribbit_step", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("useful_ribbits", "ribbit_step")));
}
