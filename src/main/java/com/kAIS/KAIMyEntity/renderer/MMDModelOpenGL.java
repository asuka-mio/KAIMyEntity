package com.kAIS.KAIMyEntity.renderer;

import com.kAIS.KAIMyEntity.KAIMyEntity;
import com.kAIS.KAIMyEntity.NativeFunc;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderCall;
import com.mojang.blaze3d.systems.RenderCallStorage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.Program;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Quaternion;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.util.Objects;

public class MMDModelOpenGL implements IMMDModel {
    static NativeFunc nf;
    long model;
    String modelDir;
    int vertexCount;
    ByteBuffer posBuffer, norBuffer, uvBuffer;
    int vao;
    int ibo;
    int vbo;
    int nbo;
    int ubo;
    int indexElementSize;
    int indexType;
    Material[] mats;
    MMDModelOpenGL() {

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
        int vao = GL46C.glGenVertexArrays();
        BufferRenderer.unbindAll();
        GL46C.glBindVertexArray(vao);
        int ibo = GL46C.glGenBuffers();
        int vbo = GL46C.glGenBuffers();
        int nbo = GL46C.glGenBuffers();
        int ubo = GL46C.glGenBuffers();
        GL46C.glBindBuffer(GL46C.GL_ELEMENT_ARRAY_BUFFER, ibo);
        ByteBuffer indexBuffer = ByteBuffer.allocateDirect(indexSize);
        for (int i = 0; i < indexSize; ++i)
            indexBuffer.put(nf.ReadByte(indexData, i));
        indexBuffer.position(0);
        GL46C.glBufferData(GL46C.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL46C.GL_STATIC_DRAW);
        int indexType = switch (indexElementSize) {
            case 1 -> GL46C.GL_UNSIGNED_BYTE;
            case 2 -> GL46C.GL_UNSIGNED_SHORT;
            case 4 -> GL46C.GL_UNSIGNED_INT;
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
        result.vbo = vbo;
        result.ubo = ubo;
        result.nbo = nbo;
        result.vao = vao;
        result.indexElementSize = indexElementSize;
        result.indexType = indexType;
        result.mats = mats;
        return result;
    }

    public static void Delete(MMDModelOpenGL model) {
        nf.DeleteModel(model.model);
    }

    public void Render(float entityYaw, MatrixStack mat, int packedLight, EntityRendererFactory.Context context) {
        Update();
        RenderModel(entityYaw, mat,context);
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

    void RenderModel(float entityYaw, MatrixStack deliverStack ,EntityRendererFactory.Context context) {
        int position = 0;
        int normal = 1;
        int texcoord = 2;
        BufferRenderer.unbindAll();
        GL46C.glBindVertexArray(vao);
        Shader shader = RenderSystem.getShader();
        deliverStack.scale(0.1f,0.1f,0.1f);
        shader.modelViewMat.set(deliverStack.peek().getModel());
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        GL46C.glEnableVertexAttribArray(position);
        GL46C.glEnableVertexAttribArray(normal);
        GL46C.glActiveTexture(GL46C.GL_TEXTURE0);
        GL46C.glEnableVertexAttribArray(texcoord);


        int posAndNorSize = vertexCount * 12; //float * 3
        long posData = nf.GetPoss(model);
        nf.CopyDataToByteBuffer(posBuffer, posData, posAndNorSize);
        GL46C.glBindBuffer(GL46C.GL_ARRAY_BUFFER,vbo);
        posBuffer.position(0);
        GL46C.glBufferData(GL46C.GL_ARRAY_BUFFER,posBuffer,GL46C.GL_STATIC_DRAW);
        GL46C.glVertexAttribPointer(position,3,GL46C.GL_FLOAT,false,0, 0);
        long norData = nf.GetNormals(model);
        nf.CopyDataToByteBuffer(norBuffer, norData, posAndNorSize);
        GL46C.glBindBuffer(GL46C.GL_ARRAY_BUFFER,nbo);
        norBuffer.position(0);
        GL46C.glBufferData(GL46C.GL_ARRAY_BUFFER,norBuffer,GL46C.GL_STATIC_DRAW);
        GL46C.glVertexAttribPointer(normal,3,GL46C.GL_FLOAT,true,0, 0);
        int uvSize = vertexCount * 8; //float * 2
        long uvData = nf.GetUVs(model);
        nf.CopyDataToByteBuffer(uvBuffer, uvData, uvSize);
        GL46C.glBindBuffer(GL46C.GL_ARRAY_BUFFER,ubo);
        uvBuffer.position(0);
        GL46C.glBufferData(GL46C.GL_ARRAY_BUFFER,uvBuffer,GL46C.GL_STATIC_DRAW);
        GL46C.glVertexAttribPointer(texcoord,2,GL46C.GL_FLOAT,false,0, 0);
        GL46C.glBindBuffer(GL46C.GL_ELEMENT_ARRAY_BUFFER, ibo);
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
                GL46C.glBindTexture(GL46C.GL_TEXTURE_2D,mats[materialID].tex);
            long startPos = (long) nf.GetSubMeshBeginIndex(model, i) * indexElementSize;
            int count = nf.GetSubMeshVertexCount(model, i);
            shader.bind();
            GL46C.glDrawElements(GL46C.GL_TRIANGLES, count, indexType, startPos);
            shader.unbind();
        }
        RenderSystem.enableCull();
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
