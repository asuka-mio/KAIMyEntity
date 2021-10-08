package com.kAIS.KAIMyEntity.dummy;

import com.kAIS.KAIMyEntity.mixin.GameRenderMixin;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class DummyGameRender {

    public static double getFov(Camera camera, float tickDelta, boolean changingFov,GameRenderer render) {
        if (render.isRenderingPanorama()) {
            return 90.0D;
        } else {
            double d = 70.0D;
            if (changingFov) {
                d = render.getClient().options.fov;
                d *= (double) MathHelper.lerp(tickDelta, ((GameRenderMixin)render).getlastMovementFovMultiplier(), ((GameRenderMixin)render).getmovementFovMultiplier());
            }

            if (camera.getFocusedEntity() instanceof LivingEntity && ((LivingEntity)camera.getFocusedEntity()).isDead()) {
                float f = Math.min((float)((LivingEntity)camera.getFocusedEntity()).deathTime + tickDelta, 20.0F);
                d /= (1.0F - 500.0F / (f + 500.0F)) * 2.0F + 1.0F;
            }

            CameraSubmersionType cameraSubmersionType = camera.getSubmersionType();
            if (cameraSubmersionType == CameraSubmersionType.LAVA || cameraSubmersionType == CameraSubmersionType.WATER) {
                d *= (double)MathHelper.lerp(render.getClient().options.fovEffectScale, 1.0F, 0.85714287F);
            }

            return d;
        }
    }
}