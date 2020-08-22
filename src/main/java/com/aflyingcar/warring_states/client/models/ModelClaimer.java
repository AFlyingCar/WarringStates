package com.aflyingcar.warring_states.client.models;
// Made with Blockbench 3.5.2
// Exported for Minecraft version 1.12
// Paste this class into your mod and generate all required imports


import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelClaimer extends ModelBase {
	private final ModelRenderer claimer;
	private final ModelRenderer pole;
	private final ModelRenderer flag;
	private final ModelRenderer base;
	private final ModelRenderer cap;

	public ModelClaimer() {
		textureWidth = 64;
		textureHeight = 64;

		claimer = new ModelRenderer(this);
		claimer.setRotationPoint(1.0F, -8.0F, -1.0F);


		pole = new ModelRenderer(this);
		pole.setRotationPoint(0.0F, 30.0F, 0.0F);
		claimer.addChild(pole);
		pole.cubeList.add(new ModelBox(pole, 0, 11, -2.0F, -29.0F, 0.0F, 2, 29, 2, 0.0F, true));

		flag = new ModelRenderer(this);
		flag.setRotationPoint(0.0F, 30.0F, 0.0F);
		claimer.addChild(flag);
		flag.cubeList.add(new ModelBox(flag, 0, 0, -15.0F, -28.0F, 0.5F, 13, 10, 1, 0.0F, true));

		base = new ModelRenderer(this);
		base.setRotationPoint(2.0F, 32.0F, -2.0F);
		claimer.addChild(base);
		base.cubeList.add(new ModelBox(base, 8, 16, -6.0F, -2.0F, 0.0F, 6, 2, 6, 0.0F, true));

		cap = new ModelRenderer(this);
		cap.setRotationPoint(0.0F, 0.0F, 0.0F);
		claimer.addChild(cap);
		cap.cubeList.add(new ModelBox(cap, 8, 11, -3.0F, 0.0F, -1.0F, 4, 1, 4, 0.0F, true));
		cap.cubeList.add(new ModelBox(cap, 24, 13, -2.0F, -1.0F, 0.0F, 2, 1, 2, 0.0F, true));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		claimer.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}