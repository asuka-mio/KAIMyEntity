package com.kAIS.KAIMyEntity;

import net.minecraft.client.Minecraft;
import scala.tools.nsc.transform.patmat.Logic;

import java.nio.ByteBuffer;

public class NativeFunc
{
    private static String AndroidRuntimePath = "/data/user/0/com.aof.mcinabox/files/runtime/boat/";
    public static NativeFunc GetInst()
    {
        if (inst == null) {
            if (System.getProperty("os.name").toLowerCase().contains("windows"))
                System.load(Minecraft.getMinecraft().gameDir.getAbsolutePath() + "\\KAIMyEntitySaba.dll");//WIN32
            else {
                try {
                    if (System.getProperty("os.name").toLowerCase().contains("linux"))
                        System.load(Minecraft.getMinecraft().gameDir.getAbsolutePath() + "/KAIMyEntitySaba.so");//Linux
                }
                catch (Throwable e) {
                    KAIMyEntity.logger.info("Cannot load native library at " + Minecraft.getMinecraft().gameDir);
                    KAIMyEntity.logger.info("Are we running on mcinabox?");
                    try {
                        System.load(AndroidRuntimePath + "libc++_shared.so");
                        System.load(AndroidRuntimePath + "KAIMyEntitySaba.so");//Android
                    } catch (Throwable f) {
                        KAIMyEntity.logger.info("Cannot load native library at " + AndroidRuntimePath + ",terminated.");
                        throw f;
                    }
                }
            }
        }
        inst = new NativeFunc();
        return inst;
    }

    public native String GetVersion();
    public native byte ReadByte(long data, long pos);
    public native void CopyDataToByteBuffer(ByteBuffer buffer, long data, long pos);

    public native long LoadModelPMX(String filename, String dir, long layerCount);
    public native long LoadModelPMD(String filename, String dir, long layerCount);
    public native void DeleteModel(long model);
    public native void UpdateModel(long model);

    public native long GetVertexCount(long model);
    public native long GetPoss(long model);
    public native long GetNormals(long model);
    public native long GetUVs(long model);
    public native long GetIndexElementSize(long model);
    public native long GetIndexCount(long model);
    public native long GetIndices(long model);
    public native long GetMaterialCount(long model);
    public native String GetMaterialTex(long model, long pos);
    public native String GetMaterialSpTex(long model, long pos);
    public native String GetMaterialToonTex(long model, long pos);
    public native long GetMaterialAmbient(long model, long pos);
    public native long GetMaterialDiffuse(long model, long pos);
    public native long GetMaterialSpecular(long model, long pos);
    public native float GetMaterialSpecularPower(long model, long pos);
    public native float GetMaterialAlpha(long model, long pos);
    public native long GetMaterialTextureMulFactor(long model, long pos);
    public native long GetMaterialTextureAddFactor(long model, long pos);
    public native int GetMaterialSpTextureMode(long model, long pos);
    public native long GetMaterialSpTextureMulFactor(long model, long pos);
    public native long GetMaterialSpTextureAddFactor(long model, long pos);
    public native long GetMaterialToonTextureMulFactor(long model, long pos);
    public native long GetMaterialToonTextureAddFactor(long model, long pos);
    public native boolean GetMaterialBothFace(long model, long pos);
    public native long GetSubMeshCount(long model);
    public native int GetSubMeshMaterialID(long model, long pos);
    public native int GetSubMeshBeginIndex(long model, long pos);
    public native int GetSubMeshVertexCount(long model, long pos);
    public native void ChangeModelAnim(long model, long anim, long layer);
    public native void ResetModelPhysics(long model);

    public native long CreateMat();
    public native void DeleteMat(long mat);
    public native void GetRightHandMat(long model, long mat);
    public native void GetLeftHandMat(long model, long mat);

    public native long LoadTexture(String filename);
    public native void DeleteTexture(long tex);
    public native int GetTextureX(long tex);
    public native int GetTextureY(long tex);
    public native long GetTextureData(long tex);
    public native boolean TextureHasAlpha(long tex);

    public native long LoadAnimation(long model, String filename);
    public native void DeleteAnimation(long anim);

    static NativeFunc inst;

    NativeFunc()
    {
    }
}
