package com.kAIS.KAIMyEntity.renderer;

import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class KAIMyEntityRenderer<T extends Entity> extends Render<T>
{
    @Override
    public boolean shouldRender(T livingEntity, ICamera camera, double camX, double camY, double camZ)
    {
        //Update no mattor should render
        MMDModelManager.Update();

        return super.shouldRender(livingEntity, camera, camX, camY, camZ);
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);

        MMDModelManager.Model model = MMDModelManager.GetModelOrInPool(entity, modelName, false);

        if (model != null)
        {
            if (entity.isBeingRidden())
            {
                AnimStateChangeOnce(model, MMDModelManager.EntityState.Ridden, "ridden");
            }
            else if (entity.isInWater())
            {
                AnimStateChangeOnce(model, MMDModelManager.EntityState.Swim, "swim");
            }
            else if (entity.posX - entity.prevPosX != 0.0f || entity.posZ - entity.prevPosZ != 0.0f)
            {
                AnimStateChangeOnce(model, MMDModelManager.EntityState.Walk, "walk");
            }
            else
            {
                AnimStateChangeOnce(model, MMDModelManager.EntityState.Idle, "idle");
            }
            model.unuseTime = 0;
            model.model.Render(x, y, z, entityYaw);
        }
    }

    public KAIMyEntityRenderer(RenderManager renderManager, String modelName)
    {
        super(renderManager);
        this.modelName = modelName;
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return null;
    }

    protected String modelName;

    void AnimStateChangeOnce(MMDModelManager.Model model, MMDModelManager.EntityState targetState, String animName)
    {
        MMDModelManager.ModelWithEntityState m = (MMDModelManager.ModelWithEntityState)model;
        if (m.state != targetState)
        {
            m.state = targetState;
            model.model.ChangeAnim(MMDAnimManager.GetAnimModel(model.model, animName), 0);
        }
    }
}
