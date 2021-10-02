package com.kAIS.KAIMyEntity.renderer;

import com.kAIS.KAIMyEntity.KAIMyEntity;
import com.kAIS.KAIMyEntity.NativeFunc;
import com.kAIS.KAIMyEntity.config.KAIMyEntityConfig;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class MMDModelOpenGL implements IMMDModel {
    static NativeFunc nf;
    long model;
    String modelDir;
    int vertexCount;
    ByteBuffer posBuffer, norBuffer, uvBuffer;
    int ibo;
    int indexElementSize;
    int indexType;
    Material[] mats;
    private static final FloatBuffer MATRIX_BUFFER = GLX.make(MemoryUtil.memAllocFloat(16), (floatBuffer) -> untrack(MemoryUtil.memAddress(floatBuffer)));
    MMDModelOpenGL() {

    }
    @Nullable
    private static final MethodHandle HANDLE = GLX.make(() -> {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> oclass = Class.forName("org.lwjgl.system.MemoryManage$DebugAllocator");
            Method method = oclass.getDeclaredMethod("untrack", Long.TYPE);
            method.setAccessible(true);
            Field field = Class.forName("org.lwjgl.system.MemoryUtil$LazyInit").getDeclaredField("ALLOCATOR");
            field.setAccessible(true);
            Object object = field.get(null);
            return oclass.isInstance(object) ? lookup.unreflect(method) : null;
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | ClassNotFoundException classnotfoundexception) {
            throw new RuntimeException(classnotfoundexception);
        }
    });
    public static void untrack(long memAddr) {
        if (HANDLE != null) {
            try {
                HANDLE.invoke(memAddr);
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }
    }

    public static MMDModelOpenGL Create(String modelFilename, String modelDir, boolean isPMD, long layerCount) {
        if (nf == null) nf = NativeFunc.GetInst();
        long model;
        if (isPMD)
            model = nf.LoadModelPMD(modelFilename, modelDir, layerCount);
        else
            model = nf.LoadModelPMX(modelFilename, modelDir, layerCount);
        if (model == 0) {
            KAIMyEntity.logger.info(String.format("Cannot open model: '%s'.", modelFilename));
            return null;
        }

        //Init vertex buffers
        int vertexCount = (int) nf.GetVertexCount(model);
        ByteBuffer posBuffer = ByteBuffer.allocateDirect(vertexCount * 12); //float * 3
        ByteBuffer norBuffer = ByteBuffer.allocateDirect(vertexCount * 12);
        ByteBuffer uvBuffer = ByteBuffer.allocateDirect(vertexCount * 8); //float * 2

        //Init ibo
        int indexElementSize = (int) nf.GetIndexElementSize(model);
        int indexCount = (int) nf.GetIndexCount(model);
        int indexSize = indexCount * indexElementSize;
        long indexData = nf.GetIndices(model);
        int ibo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        ByteBuffer indexBuffer = ByteBuffer.allocateDirect(indexSize);
        for (int i = 0; i < indexSize; ++i)
            indexBuffer.put(nf.ReadByte(indexData, i));
        indexBuffer.position(0);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        int indexType = switch (indexElementSize) {
            case 1 -> GL11.GL_UNSIGNED_BYTE;
            case 2 -> GL11.GL_UNSIGNED_SHORT;
            case 4 -> GL11.GL_UNSIGNED_INT;
            default -> 0;
        };

        //Material
        MMDModelOpenGL.Material[] mats = new MMDModelOpenGL.Material[(int) nf.GetMaterialCount(model)];
        for (int i = 0; i < mats.length; ++i) {
            mats[i] = new MMDModelOpenGL.Material();
            String texFilename = nf.GetMaterialTex(model, i);
            if (!texFilename.isEmpty()) {
                MMDTextureManager.Texture mgrTex = MMDTextureManager.GetTexture(texFilename);
                if (mgrTex != null) {
                    mats[i].tex = mgrTex.tex;
                    mats[i].hasAlpha = mgrTex.hasAlpha;
                }
            }
        }

        MMDModelOpenGL result = new MMDModelOpenGL();
        result.model = model;
        result.modelDir = modelDir;
        result.vertexCount = vertexCount;
        result.posBuffer = posBuffer;
        result.norBuffer = norBuffer;
        result.uvBuffer = uvBuffer;
        result.ibo = ibo;
        result.indexElementSize = indexElementSize;
        result.indexType = indexType;
        result.mats = mats;
        return result;
    }

    public static void Delete(MMDModelOpenGL model) {
        nf.DeleteModel(model.model);
    }

    public void Render(float entityYaw, Matrix4f mat, int packedLight) {
        Update();
        RenderModel(entityYaw, mat);
    }

    public void ChangeAnim(long anim, long layer) {
        nf.ChangeModelAnim(model, anim, layer);
    }

    public void ResetPhysics() {
        nf.ResetModelPhysics(model);
    }

    public long GetModelLong() {
        return model;
    }

    public String GetModelDir() {
        return modelDir;
    }

    void Update() {
        RenderTimer.BeginIfUse("MMDModelOpenGL: Call native function: UpdateModel");
        nf.UpdateModel(model);
        RenderTimer.EndIfUse();
    }

    void RenderModel(float entityYaw, Matrix4f mat) {
        //Depth test disabled by default (1.16.5)
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        GL11.glPushMatrix();
        //In 1.16.5, base matrix is in matrix's stacks.
        mat.write(MATRIX_BUFFER,false);
        ((Buffer)MATRIX_BUFFER).rewind();
        GL11.glMultMatrixf(MATRIX_BUFFER);
        //In 1.16.5, position is applied in base matrix.
        //RenderSystem.translated(x, y, z);
        GL11.glRotatef(-entityYaw, 0.0f, 1.0f, 0.0f);
        GL11.glScaled(0.1, 0.1, 0.1);

        //Read vertex data
        RenderTimer.BeginIfUse("MMDModelOpenGL: Read vertex data into Java byte buffer");
        int posAndNorSize = vertexCount * 12; //float * 3
        long posData = nf.GetPoss(model);
        nf.CopyDataToByteBuffer(posBuffer, posData, posAndNorSize);
        long norData = nf.GetNormals(model);
        nf.CopyDataToByteBuffer(norBuffer, norData, posAndNorSize);
        int uvSize = vertexCount * 8; //float * 2
        long uvData = nf.GetUVs(model);
        nf.CopyDataToByteBuffer(uvBuffer, uvData, uvSize);
        RenderTimer.EndIfUse();

        //Init vertex pointer
        RenderTimer.BeginIfUse("MMDModelOpenGL: Init vertex pointer");
        GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, posBuffer);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glNormalPointer(GL11.GL_FLOAT, 0, norBuffer);
        GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
        GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, uvBuffer);
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        RenderTimer.EndIfUse();

        //Render
        RenderTimer.BeginIfUse("MMDModelOpenGL: Do OpenGL draw");
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        long subMeshCount = nf.GetSubMeshCount(model);
        for (long i = 0; i < subMeshCount; ++i) {
            int materialID = nf.GetSubMeshMaterialID(model, i);
            float alpha = nf.GetMaterialAlpha(model, materialID);
            if (alpha == 0.0f)
                continue;

            if (nf.GetMaterialBothFace(model, materialID)) {
                RenderSystem.disableCull();
            } else {
                RenderSystem.enableCull();
            }

            if (mats[materialID].tex == 0)
                MinecraftClient.getInstance().getEntityRenderDispatcher().textureManager.bindTexture(TextureManager.MISSING_IDENTIFIER);
            else
                RenderSystem.bindTexture(mats[materialID].tex);
            long startPos = (long) nf.GetSubMeshBeginIndex(model, i) * indexElementSize;
            int count = nf.GetSubMeshVertexCount(model, i);
            GL11.glDrawElements(GL11.GL_TRIANGLES, count, indexType, startPos);
        }
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        RenderTimer.EndIfUse();

        //Disable client state
        if (KAIMyEntityConfig.openGLEnableLighting) {
            GL13.glClientActiveTexture(33986); //Texture id from LightTexture
            GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
            MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().disable();
        }
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

        RenderSystem.enableCull();
        GL11.glPopMatrix();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }

    static class Material {
        int tex;
        boolean hasAlpha;

        Material() {
            tex = 0;
            hasAlpha = false;
        }
    }
}
