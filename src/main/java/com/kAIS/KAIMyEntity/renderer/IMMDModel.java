package com.kAIS.KAIMyEntity.renderer;

interface IMMDModel
{
    void Render(double x, double y, double z, float entityYaw);
    void ChangeAnim(long anim, long layer);
    void ResetPhysics();
    long GetModelLong();
    String GetModelDir();
}