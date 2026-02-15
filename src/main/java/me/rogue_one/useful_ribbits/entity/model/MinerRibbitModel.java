package me.rogue_one.useful_ribbits.entity.model;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

import me.rogue_one.useful_ribbits.entity.MinerRibbitEntity;

public class MinerRibbitModel extends GeoModel<MinerRibbitEntity> {
	@Override
	public ResourceLocation getAnimationResource(MinerRibbitEntity entity) {
		return new ResourceLocation("useful_ribbits", "animations/miner_ribbit.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(MinerRibbitEntity entity) {
		return new ResourceLocation("useful_ribbits", "geo/miner_ribbit.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(MinerRibbitEntity entity) {
		return new ResourceLocation("useful_ribbits", "textures/entities/" + entity.getTexture() + ".png");
	}

}
