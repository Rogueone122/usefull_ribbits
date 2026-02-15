package me.rogue_one.useful_ribbits.client.gui;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;

import me.rogue_one.useful_ribbits.world.inventory.RibbitBedGUIMenu;
import me.rogue_one.useful_ribbits.procedures.RibbitBedGUIRenderMinerProcedure;
import me.rogue_one.useful_ribbits.procedures.RibbitBedGUIRenderFarmerProcedure;
import me.rogue_one.useful_ribbits.procedures.RibbitBedGUIRenderChefProcedure;
import me.rogue_one.useful_ribbits.network.RibbitBedGUIButtonMessage;
import me.rogue_one.useful_ribbits.UsefulRibbitsMod;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;

public class RibbitBedGUIScreen extends AbstractContainerScreen<RibbitBedGUIMenu> {
	private final static HashMap<String, Object> guistate = RibbitBedGUIMenu.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	Button button_empty1;
	Button button_empty;
	Button button_empty2;

	public RibbitBedGUIScreen(RibbitBedGUIMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 176;
		this.imageHeight = 200;
	}

	private static final ResourceLocation texture = new ResourceLocation("useful_ribbits:textures/screens/ribbit_bed_gui.png");

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		if (RibbitBedGUIRenderChefProcedure.execute(world) instanceof LivingEntity livingEntity) {
			InventoryScreen.renderEntityInInventoryFollowsAngle(guiGraphics, this.leftPos + 19, this.topPos + 50, 25, 0f + (float) Math.atan((this.leftPos + 19 - mouseX) / 40.0), (float) Math.atan((this.topPos + 1 - mouseY) / 40.0), livingEntity);
		}
		if (RibbitBedGUIRenderMinerProcedure.execute(world) instanceof LivingEntity livingEntity) {
			InventoryScreen.renderEntityInInventoryFollowsAngle(guiGraphics, this.leftPos + 19, this.topPos + 78, 25, 0f + (float) Math.atan((this.leftPos + 19 - mouseX) / 40.0), (float) Math.atan((this.topPos + 29 - mouseY) / 40.0), livingEntity);
		}
		if (RibbitBedGUIRenderFarmerProcedure.execute(world) instanceof LivingEntity livingEntity) {
			InventoryScreen.renderEntityInInventoryFollowsAngle(guiGraphics, this.leftPos + 19, this.topPos + 106, 25, 0f + (float) Math.atan((this.leftPos + 19 - mouseX) / 40.0), (float) Math.atan((this.topPos + 57 - mouseY) / 40.0), livingEntity);
		}
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

		guiGraphics.blit(new ResourceLocation("useful_ribbits:textures/screens/iron_ingot.png"), this.leftPos + 79, this.topPos + 9, 0, 0, 16, 16, 16, 16);

		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
	}

	@Override
	public void init() {
		super.init();
		button_empty1 = Button.builder(Component.translatable("gui.useful_ribbits.ribbit_bed_gui.button_empty1"), e -> {
			if (true) {
				UsefulRibbitsMod.PACKET_HANDLER.sendToServer(new RibbitBedGUIButtonMessage(0, x, y, z));
				RibbitBedGUIButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}).bounds(this.leftPos + 31, this.topPos + 58, 30, 20).build();
		guistate.put("button:button_empty1", button_empty1);
		this.addRenderableWidget(button_empty1);
		button_empty = Button.builder(Component.translatable("gui.useful_ribbits.ribbit_bed_gui.button_empty"), e -> {
			if (true) {
				UsefulRibbitsMod.PACKET_HANDLER.sendToServer(new RibbitBedGUIButtonMessage(1, x, y, z));
				RibbitBedGUIButtonMessage.handleButtonAction(entity, 1, x, y, z);
			}
		}).bounds(this.leftPos + 31, this.topPos + 30, 30, 20).build();
		guistate.put("button:button_empty", button_empty);
		this.addRenderableWidget(button_empty);
		button_empty2 = Button.builder(Component.translatable("gui.useful_ribbits.ribbit_bed_gui.button_empty2"), e -> {
			if (true) {
				UsefulRibbitsMod.PACKET_HANDLER.sendToServer(new RibbitBedGUIButtonMessage(2, x, y, z));
				RibbitBedGUIButtonMessage.handleButtonAction(entity, 2, x, y, z);
			}
		}).bounds(this.leftPos + 32, this.topPos + 86, 30, 20).build();
		guistate.put("button:button_empty2", button_empty2);
		this.addRenderableWidget(button_empty2);
	}
}
