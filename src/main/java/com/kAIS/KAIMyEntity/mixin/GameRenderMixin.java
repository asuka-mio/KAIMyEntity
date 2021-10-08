package com.kAIS.KAIMyEntity.mixin;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface GameRenderMixin {
    @Accessor("lastMovementFovMultiplier")
    float getlastMovementFovMultiplier();
    @Accessor("movementFovMultiplier")
    float getmovementFovMultiplier();
}
