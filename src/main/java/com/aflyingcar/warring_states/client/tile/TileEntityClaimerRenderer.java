package com.aflyingcar.warring_states.client.tile;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.client.models.ModelClaimer;
import com.aflyingcar.warring_states.tileentities.TileEntityClaimer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;

public class TileEntityClaimerRenderer extends TileEntitySpecialRenderer<TileEntityClaimer> {
    public ModelClaimer model = new ModelClaimer();
    public final static ResourceLocation FLAG_TEXTURE = new ResourceLocation(WarringStatesMod.MOD_ID, "textures/model/claimer.png");

    @Override
    public void render(TileEntityClaimer te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);

        // TODO: We should probably have a way of rendering custom flag textures based on the claimer itself
        bindTexture(FLAG_TEXTURE);

        // Copied from how Galacticraft renders solar panels
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.translate((float)x, (float)y, (float)z);

        // For some reason the flag renders in the wrong location, so we need to perform an offset
        //  TODO: This is dumb
        GlStateManager.translate(0.5f, 1.5f, 0.5f);

        GlStateManager.scale(-0.0625F, -0.0625F, -0.0625F);

        // TODO: This is a hack because the model for some weird reason has the windings backwards
        GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
        model.render(null, 0, 0, 0, 0, 0, 1);
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);

        GlStateManager.popMatrix();
    }
}
