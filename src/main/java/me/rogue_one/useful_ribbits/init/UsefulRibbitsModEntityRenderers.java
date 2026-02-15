
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package me.rogue_one.useful_ribbits.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;

import me.rogue_one.useful_ribbits.client.renderer.MinerRibbitRenderer;
import me.rogue_one.useful_ribbits.client.renderer.FarmerRibbitRenderer;
import me.rogue_one.useful_ribbits.client.renderer.ChefRibbitRenderer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class UsefulRibbitsModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(UsefulRibbitsModEntities.CHEF_RIBBIT.get(), ChefRibbitRenderer::new);
		event.registerEntityRenderer(UsefulRibbitsModEntities.MINER_RIBBIT.get(), MinerRibbitRenderer::new);
		event.registerEntityRenderer(UsefulRibbitsModEntities.FARMER_RIBBIT.get(), FarmerRibbitRenderer::new);
	}
}
