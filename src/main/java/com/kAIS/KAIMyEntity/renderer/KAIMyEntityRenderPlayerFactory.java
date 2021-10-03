package com.kAIS.KAIMyEntity.renderer;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.player.PlayerEntity;

public class KAIMyEntityRenderPlayerFactory implements EntityRendererFactory<PlayerEntity> {

    @Override
    public EntityRenderer<PlayerEntity> create(Context manager) {
        return new KAIMyEntityRendererPlayer(manager);
    }
}
