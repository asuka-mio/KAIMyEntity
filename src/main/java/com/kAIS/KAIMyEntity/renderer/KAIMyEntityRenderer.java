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
        //Update no matter should render
        MMDModelManager.Update();
        return super.shouldRender(livingEntityIn,camera,camX,camY,camZ);
    }

    @Override
    public void render(T entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn) {
        MMDModelManager.Model model = MMDModelManager.GetModelOrInPool(entityIn, modelName, false);

        if (model != null) {
            if (entityIn.hasPassengers()) {
                AnimStateChangeOnce(model, MMDModelManager.EntityState.Ridden, "ridden");
            } else if (entityIn.isSwimming()) {
                AnimStateChangeOnce(model, MMDModelManager.EntityState.Swim, "swim");
            } else if (entityIn.getX() - entityIn.prevX != 0.0f || entityIn.getZ() - entityIn.prevZ != 0.0f) {
                AnimStateChangeOnce(model, MMDModelManager.EntityState.Walk, "walk");
            } else {
                AnimStateChangeOnce(model, MMDModelManager.EntityState.Idle, "idle");
            }
            model.unusedTime = 0;
            matrixStackIn.push();
            super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
            model.model.Render(entityYaw, matrixStackIn, packedLightIn,context);
            matrixStackIn.pop();
        }

    }

    @Override
    public Identifier getTexture(T entity) {
        return null;
    }

    void AnimStateChangeOnce(MMDModelManager.Model model, MMDModelManager.EntityState targetState, String animName) {
        MMDModelManager.ModelWithEntityState m = (MMDModelManager.ModelWithEntityState) model;
        if (m.state != targetState) {
            m.state = targetState;
            model.model.ChangeAnim(MMDAnimManager.GetAnimModel(model.model, animName), 0);
        }
    }
}
