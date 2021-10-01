package com.kAIS.KAIMyEntity.renderer;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class KAIMyEntityRenderFactory<T extends Entity> implements IRenderFactory<T> {
    String entityName;

    public KAIMyEntityRenderFactory(String entityName) {
        this.entityName = entityName;
    }

    @Override
    public EntityRenderer<T> createRenderFor(EntityRendererManager manager) {
        return new KAIMyEntityRenderer<>(manager, entityName);
    }
}
