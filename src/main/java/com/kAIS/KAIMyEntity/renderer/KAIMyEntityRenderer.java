package com.kAIS.KAIMyEntity.renderer;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class KAIMyEntityRenderer<T extends Entity> extends EntityRenderer<T> {
    protected String modelName;
    protected EntityRendererFactory.Context context;

    public KAIMyEntityRenderer(EntityRendererFactory.Context renderManager, String modelName) {
        super(renderManager);
        this.modelName = modelName;
        this.context = renderManager;
    }

    @Override
    public boolean shouldRender(T livingEntityIn, Frustum camera, double camX, double camY, double camZ) {
        return super.shouldRender(livingEntityIn,camera,camX,camY,camZ);
    }

    @Override
    public void render(T entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn) {
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        String animName;
        if (entityIn.hasPassengers()) {
            animName = "ridden";
        } else if (entityIn.isSwimming()) {
            animName = "swim";
        } else if (entityIn.getX() - entityIn.prevX != 0.0f || entityIn.getZ() - entityIn.prevZ != 0.0f) {
            animName = "walk";
        } else {
            animName = "idle";
        }
        MMDModelManager.Model model = MMDModelManager.GetNotPlayerModel(modelName,animName);
        if(model != null) {
            matrixStackIn.push();
            model.model.Render(entityYaw, matrixStackIn, packedLightIn);
            matrixStackIn.pop();
        }
    }

    @Override
    public Identifier getTexture(T entity) {
        return null;
    }
}
