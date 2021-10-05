package com.kAIS.KAIMyEntity.renderer;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Arrays;

public class KAIMyEntityRendererPlayerHelper {

    KAIMyEntityRendererPlayerHelper() {
    }

    public static void ResetPhysics(PlayerEntity player) {
        MMDModelManager.Model m = MMDModelManager.GetModel(player);
        if (m != null) {
            MMDModelManager.ModelWithPlayerData mwpd = (MMDModelManager.ModelWithPlayerData) m;
            IMMDModel model = m.model;
            Arrays.fill(mwpd.playerData.stateLayers, MMDModelManager.PlayerData.EntityState.Idle);
            mwpd.playerData.playCustomAnim = false;
            model.ChangeAnim(MMDAnimManager.GetAnimModel(model, "idle"), 0);
            ResetAnimationWithoutLayer0(model);
            model.ResetPhysics();
        }
    }

    public static void CustomAnim(PlayerEntity player, String id) {
        MMDModelManager.Model m = MMDModelManager.GetModel(player);
        if (m != null) {
            MMDModelManager.ModelWithPlayerData mwpd = (MMDModelManager.ModelWithPlayerData) m;
            IMMDModel model = m.model;
            mwpd.playerData.playCustomAnim = true;
            model.ChangeAnim(MMDAnimManager.GetAnimModel(model, "custom_" + id), 0);
            ResetAnimationWithoutLayer0(model);
        }
    }

    static void ResetAnimationWithoutLayer0(IMMDModel model) {
        model.ChangeAnim(0, 1);
        model.ChangeAnim(0, 2);
    }
}