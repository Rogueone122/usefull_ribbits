package me.rogue_one.useful_ribbits.entity.model;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

import me.rogue_one.useful_ribbits.entity.ChefRibbitEntity;

public class ChefRibbitModel extends GeoModel<ChefRibbitEntity> {
	@Override
	public ResourceLocation getAnimationResource(ChefRibbitEntity entity) {
		return new ResourceLocation("useful_ribbits", "animations/chef_ribbit.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(ChefRibbitEntity entity) {
		return new ResourceLocation("useful_ribbits", "geo/chef_ribbit.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(ChefRibbitEntity entity) {
		return new ResourceLocation("useful_ribbits", "textures/entities/" + entity.getTexture() + ".png");
	}

}
