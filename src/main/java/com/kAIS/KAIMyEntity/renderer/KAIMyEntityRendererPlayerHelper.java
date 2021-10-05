package com.kAIS.KAIMyEntity.renderer;

import com.kAIS.KAIMyEntity.NativeFunc;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Quaternion;

import java.util.Arrays;

public class KAIMyEntityRendererPlayerHelper {
    static KAIMyEntityRendererPlayerHelper inst;

    KAIMyEntityRendererPlayerHelper() {
    }

    public static KAIMyEntityRendererPlayerHelper GetInst() {
        return inst;
    }

    public void ResetPhysics(PlayerEntity player) {
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

    public void CustomAnim(PlayerEntity player, String id) {
        MMDModelManager.Model m = MMDModelManager.GetModel(player);
        if (m != null) {
            MMDModelManager.ModelWithPlayerData mwpd = (MMDModelManager.ModelWithPlayerData) m;
            IMMDModel model = m.model;
            mwpd.playerData.playCustomAnim = true;
            model.ChangeAnim(MMDAnimManager.GetAnimModel(model, "custom_" + id), 0);
            ResetAnimationWithoutLayer0(model);
        }
    }

    void ResetAnimationWithoutLayer0(IMMDModel model) {
        model.ChangeAnim(0, 1);
        model.ChangeAnim(0, 2);
    }

    float DataToFloat(NativeFunc nf, long data, long pos) {
        int temp = 0;
        temp |= nf.ReadByte(data, pos) & 0xff;
        temp |= (nf.ReadByte(data, pos + 1) & 0xff) << 8;
        temp |= (nf.ReadByte(data, pos + 2) & 0xff) << 16;
        temp |= (nf.ReadByte(data, pos + 3) & 0xff) << 24;
        return Float.intBitsToFloat(temp);
    }
    /*
    Matrix4f DataToMat(NativeFunc nf, long data) {
        float[] fl = new float[16];
        for (int i = 0; i < fl.length; i++)
            fl[i] = DataToFloat(nf, data, i * 4);
        Matrix4f result = new Matrix4f(fl);
        result.transpose();
        return result;
    }
     */
    public static void rotate(MatrixStack stack,Quaternion quaternion) {
        MatrixStack.Entry matrixstack$entry = stack.peek();
        matrixstack$entry.getModel().multiply(quaternion);
        matrixstack$entry.getNormal().multiply(quaternion);
    }
}