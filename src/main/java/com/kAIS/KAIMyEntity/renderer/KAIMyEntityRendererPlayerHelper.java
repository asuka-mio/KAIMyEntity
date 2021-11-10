package com.kAIS.KAIMyEntity.renderer;

import net.minecraft.entity.player.PlayerEntity;
public class KAIMyEntityRendererPlayerHelper {

    KAIMyEntityRendererPlayerHelper() {
    }

    public static void ResetPhysics(PlayerEntity player) {
        MMDModelManager.Model m = MMDModelManager.GetPlayerModel("EntityPlayer_"+player.getName().getString());
        if (m == null)
            m = MMDModelManager.GetPlayerModel("EntityPlayer");
        if (m != null) {
            IMMDModel model = m.model;
            ((MMDModelManager.ModelWithPlayerData)m).playerData.playCustomAnim = false;
            model.ChangeAnim(MMDAnimManager.GetAnimModel(model, "idle"), 0);
            model.ChangeAnim(0, 1);
            model.ChangeAnim(0, 2);
            model.ResetPhysics();
        }
    }

    public static void CustomAnim(PlayerEntity player, String id) {
        MMDModelManager.Model m = MMDModelManager.GetPlayerModel("EntityPlayer_"+player.getName().getString());
        if (m == null)
            m = MMDModelManager.GetPlayerModel("EntityPlayer");
        if (m != null) {
            MMDModelManager.ModelWithPlayerData mwpd = (MMDModelManager.ModelWithPlayerData) m;
            IMMDModel model = m.model;
            mwpd.playerData.playCustomAnim = true;
            model.ChangeAnim(MMDAnimManager.GetAnimModel(model, "custom_" + id), 0);
            model.ChangeAnim(0, 1);
            model.ChangeAnim(0, 2);
        }
    }
}