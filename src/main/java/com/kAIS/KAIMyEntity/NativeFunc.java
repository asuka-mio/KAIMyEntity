package com.kAIS.KAIMyEntity;

import net.minecraft.client.MinecraftClient;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class NativeFunc {
    private static final String RuntimePath = new File(System.getProperty("java.home")).getParent();
    private static final boolean isAndroid = new File("/system/build.prop").exists();
    private static final boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
    private static final HashMap<runtimeUrlRes, String> urlMap = new HashMap<runtimeUrlRes, String>() {
        {
            put(runtimeUrlRes.android_arch64, "https://github.com.cnpmjs.org/asuka-mio/KAIMyEntitySaba/releases/download/crossplatform/KAIMyEntitySaba.so");
            put(runtimeUrlRes.android_arch64_libc, "https://github.com.cnpmjs.org/asuka-mio/KAIMyEntitySaba/releases/download/crossplatform/libc++_shared.so");
        }
    };
    static NativeFunc inst;

    public static NativeFunc GetInst() {
        if (inst == null) {
            inst = new NativeFunc();
            inst.Init();
        }
        return inst;
    }

    private void DownloadSingleFile(URL url, File file) throws IOException {
        if (file.exists()) {
            try {
                System.load(file.getAbsolutePath());
                return; //File exist and loadable
            } catch (Error e) {
                KAIMyEntityClient.logger.info(file.getAbsolutePath() + "broken!Trying recover it!");
            }
        }
        try {
            file.delete();
            file.createNewFile();
            FileUtils.copyURLToFile(url, file, 30000, 30000);
            System.load(file.getAbsolutePath());
        } catch (IOException e) {
            file.delete();
            KAIMyEntityClient.logger.info("Download" + url.getPath() + "failed!");
            KAIMyEntityClient.logger.info("Cannot download runtime!");
            KAIMyEntityClient.logger.info("Check you internet connection and restart game!");
            e.printStackTrace();
            throw e;
        }
    }

    private void DownloadRuntime() throws Exception {
        if (isWindows) {
            KAIMyEntityClient.logger.info("Not support!");
            throw new Error();
        }
        if (isLinux && !isAndroid) {
            KAIMyEntityClient.logger.info("Not support!");
            throw new Error();
        }
        if (isLinux && isAndroid) {
            DownloadSingleFile(new URL(urlMap.get(runtimeUrlRes.android_arch64_libc)), new File(RuntimePath, "libc++_shared.so"));
            DownloadSingleFile(new URL(urlMap.get(runtimeUrlRes.android_arch64)), new File(RuntimePath, "KAIMyEntitySaba.so"));
        }
    }

    private void LoadLibrary(File file) {
        try {
            System.load(file.getAbsolutePath());
        } catch (Error e) {
            KAIMyEntityClient.logger.info("Runtime" + file.getAbsolutePath() + "not found,try download from github!");
            throw e;
        }
    }

    private void Init() {
        try {
            if (isWindows) {
                KAIMyEntityClient.logger.info("Win32 Env Detected!");
                LoadLibrary(new File(MinecraftClient.getInstance().runDirectory.getAbsolutePath(), "KAIMyEntitySaba.dll"));//WIN32
            }
            if (isLinux && !isAndroid) {
                KAIMyEntityClient.logger.info("Linux Env Detected!");
                LoadLibrary(new File(MinecraftClient.getInstance().runDirectory.getAbsolutePath(), "KAIMyEntitySaba.so"));//Linux
            }
            if (isLinux && isAndroid) {
                KAIMyEntityClient.logger.info("Android Env Detected!");
                LoadLibrary(new File(RuntimePath, "libc++_shared.so"));
                LoadLibrary(new File(RuntimePath, "KAIMyEntitySaba.so"));//Android
            }
        } catch (Error e) {
            try {
                DownloadRuntime();
            } catch (Exception ex) {
                throw e;
            }
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

    enum runtimeUrlRes {
        android_arch64, android_arch64_libc
    }
}
