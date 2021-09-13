package com.kAIS.KAIMyEntity;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.nio.ByteBuffer;

public class NativeFunc
{
    private static String RuntimePath = new File(System.getProperty("java.home")).getParent();
    private static boolean isAndroid = new File("/system/build.prop").exists();
    private static boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
    private static boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
    public static NativeFunc GetInst()
    {
        if (inst == null) {
            inst = new NativeFunc();
            inst.Init();
        }
        return inst;
    }
    private void Init()
    {
        if (isWindows) {
            KAIMyEntity.logger.info("Win32 Env Detected!");
            System.load(new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), "KAIMyEntitySaba.dll").getAbsolutePath());//WIN32
        }
        if (isLinux && !isAndroid) {
            KAIMyEntity.logger.info("Linux Env Detected!");
            System.load(new File(Minecraft.getMinecraft().gameDir.getAbsolutePath() , "KAIMyEntitySaba.so").getAbsolutePath());//Linux
        }
        if(isLinux && isAndroid) {
            KAIMyEntity.logger.info("Android Env Detected!");
            System.load(new File(RuntimePath , "libc++_shared.so").getAbsolutePath());
            System.load(new File(RuntimePath , "KAIMyEntitySaba.so").getAbsolutePath());//Android
        }
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
}
