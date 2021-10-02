package com.kAIS.KAIMyEntity.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

public class KAIMyEntityRenderer<T extends Entity> extends EntityRenderer<T> {
    protected String modelName;

    public KAIMyEntityRenderer(EntityRendererManager renderManager, String modelName) {
        super(renderManager);
        this.modelName = modelName;
    }

    @Override
    public boolean shouldRender(T livingEntityIn, ClippingHelper camera, double camX, double camY, double camZ) {
        //Update no matter should render
        MMDModelManager.Update();

        if (!livingEntityIn.isInRangeToRender3d(camX, camY, camZ)) {
            return false;
        } else if (livingEntityIn.ignoreFrustumCheck) {
            return true;
        } else {
            AxisAlignedBB axisalignedbb = livingEntityIn.getRenderBoundingBox().grow(0.5D);
            if (axisalignedbb.hasNaN() || axisalignedbb.getAverageEdgeLength() == 0.0D) {
                axisalignedbb = new AxisAlignedBB(livingEntityIn.getPosX() - 2.0D, livingEntityIn.getPosY() - 2.0D, livingEntityIn.getPosZ() - 2.0D, livingEntityIn.getPosX() + 2.0D, livingEntityIn.getPosY() + 2.0D, livingEntityIn.getPosZ() + 2.0D);
            }

            return camera.isBoundingBoxInFrustum(axisalignedbb);
        }
    }

    @Override
    public void render(T entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);

        MMDModelManager.Model model = MMDModelManager.GetModelOrInPool(entityIn, modelName, false);

        if (model != null) {
            if (entityIn.isBeingRidden()) {
                AnimStateChangeOnce(model, MMDModelManager.EntityState.Ridden, "ridden");
            } else if (entityIn.isInWater()) {
                AnimStateChangeOnce(model, MMDModelManager.EntityState.Swim, "swim");
            } else if (entityIn.getPosX() - entityIn.prevPosX != 0.0f || entityIn.getPosZ() - entityIn.prevPosZ != 0.0f) {
                AnimStateChangeOnce(model, MMDModelManager.EntityState.Walk, "walk");
            } else {
                AnimStateChangeOnce(model, MMDModelManager.EntityState.Idle, "idle");
            }
            model.unusedTime = 0;
            matrixStackIn.push();
            model.model.Render(entityYaw, matrixStackIn.getLast().getMatrix(), packedLightIn);
            matrixStackIn.pop();
        }
    }

    @Override
    public ResourceLocation getEntityTexture(T entity) {
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
