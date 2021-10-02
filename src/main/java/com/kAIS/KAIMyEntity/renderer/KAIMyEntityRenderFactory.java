package com.kAIS.KAIMyEntity.renderer;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;

public class KAIMyEntityRenderFactory<T extends Entity> implements EntityRendererFactory<T> {
    String entityName;

    public KAIMyEntityRenderFactory(String entityName) {
        this.entityName = entityName;
    }

    @Override
    public EntityRenderer<T> create(Context manager) {
        return new KAIMyEntityRenderer<>(manager, entityName);
    }
}
