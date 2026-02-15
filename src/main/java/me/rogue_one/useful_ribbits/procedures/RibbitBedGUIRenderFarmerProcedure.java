package me.rogue_one.useful_ribbits.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;

import me.rogue_one.useful_ribbits.init.UsefulRibbitsModEntities;
import me.rogue_one.useful_ribbits.entity.FarmerRibbitEntity;

public class RibbitBedGUIRenderFarmerProcedure {
	public static Entity execute(LevelAccessor world) {
		return world instanceof Level _level ? new FarmerRibbitEntity(UsefulRibbitsModEntities.FARMER_RIBBIT.get(), _level) : null;
	}
}
