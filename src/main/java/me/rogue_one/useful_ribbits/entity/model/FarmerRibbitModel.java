package me.rogue_one.useful_ribbits.entity.model;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

import me.rogue_one.useful_ribbits.entity.FarmerRibbitEntity;

public class FarmerRibbitModel extends GeoModel<FarmerRibbitEntity> {
	@Override
	public ResourceLocation getAnimationResource(FarmerRibbitEntity entity) {
		return new ResourceLocation("useful_ribbits", "animations/farmer_ribbit.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(FarmerRibbitEntity entity) {
		return new ResourceLocation("useful_ribbits", "geo/farmer_ribbit.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(FarmerRibbitEntity entity) {
		return new ResourceLocation("useful_ribbits", "textures/entities/" + entity.getTexture() + ".png");
	}

}
