package me.rogue_one.useful_ribbits.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;

import me.rogue_one.useful_ribbits.init.UsefulRibbitsModEntities;
import me.rogue_one.useful_ribbits.entity.ChefRibbitEntity;

public class RibbitBedGUIRenderChefProcedure {
	public static Entity execute(LevelAccessor world) {
		return world instanceof Level _level ? new ChefRibbitEntity(UsefulRibbitsModEntities.CHEF_RIBBIT.get(), _level) : null;
	}
}
