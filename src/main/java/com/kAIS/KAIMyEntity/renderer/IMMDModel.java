package com.kAIS.KAIMyEntity.renderer;

import net.minecraft.util.math.vector.Matrix4f;

interface IMMDModel {
    void Render(float entityYaw, Matrix4f mat, int packedLight);

    void ChangeAnim(long anim, long layer);

    void ResetPhysics();

    long GetModelLong();

    String GetModelDir();
}