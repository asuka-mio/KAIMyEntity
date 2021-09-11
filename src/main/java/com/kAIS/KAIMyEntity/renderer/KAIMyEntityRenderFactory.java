package com.kAIS.KAIMyEntity.renderer;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class KAIMyEntityRenderFactory<T extends Entity> implements IRenderFactory<T>
{
    public KAIMyEntityRenderFactory(String entityName)
    {
        this.entityName = entityName;
    }

    @Override
    public Render<T> createRenderFor(RenderManager manager)
    {
        return new KAIMyEntityRenderer<T>(manager, entityName);
    }

    String entityName;
}
