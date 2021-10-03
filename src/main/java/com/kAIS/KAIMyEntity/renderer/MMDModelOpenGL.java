package com.kAIS.KAIMyEntity.renderer;

import com.kAIS.KAIMyEntity.KAIMyEntity;
import com.kAIS.KAIMyEntity.NativeFunc;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

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
        int ibo = GL46C.glGenBuffers();
        GL46C.glBindBuffer(GL46C.GL_ELEMENT_ARRAY_BUFFER, ibo);
        ByteBuffer indexBuffer = ByteBuffer.allocateDirect(indexSize);
        for (int i = 0; i < indexSize; ++i)
            indexBuffer.put(nf.ReadByte(indexData, i));
        indexBuffer.position(0);
        GL46C.glBufferData(GL46C.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL46C.GL_STATIC_DRAW);
        GL46C.glBindBuffer(GL46C.GL_ELEMENT_ARRAY_BUFFER, 0);
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
        result.indexElementSize = indexElementSize;
        result.indexType = indexType;
        result.mats = mats;
        return result;
    }

    public static void Delete(MMDModelOpenGL model) {
        nf.DeleteModel(model.model);
    }

    public void Render(float entityYaw, MatrixStack mat, int packedLight) {
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

    void RenderModel(float entityYaw, MatrixStack deliverStack) {
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        Matrix4f mat4f = deliverStack.peek().getModel();

        deliverStack.push();
        deliverStack.method_34425(mat4f);
        KAIMyEntityRendererPlayer.rotate(deliverStack,new Quaternion( 0.0f, 1.0f, 0.0f,-entityYaw));
        deliverStack.scale(0.1f, 0.1f, 0.1f);

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
        GL46C.glEnableVertexAttribArray(GL46C.GL_VERTEX_ARRAY);
        GL46C.glVertexAttribPointer(GL46C.GL_VERTEX_ARRAY,posAndNorSize,GL46C.GL_FLOAT,false,0,posBuffer);
        GL46C.glEnableVertexAttribArray(GL11.GL_NORMAL_ARRAY);
        GL46C.glVertexAttribPointer(GL11.GL_NORMAL_ARRAY,posAndNorSize,GL46C.GL_FLOAT,false,0,norBuffer);
        GL46C.glEnableVertexAttribArray(GL11.GL_TEXTURE_COORD_ARRAY);
        GL46C.glVertexAttribPointer(GL11.GL_TEXTURE_COORD_ARRAY,uvSize,GL46C.GL_FLOAT,false,0,uvBuffer);

        RenderTimer.EndIfUse();

        //Render
        RenderTimer.BeginIfUse("MMDModelOpenGL: Do OpenGL draw");
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
            long startPos = (long) nf.GetSubMeshBeginIndex(model, i) * indexElementSize;
            int count = nf.GetSubMeshVertexCount(model, i);
            GL46C.glDrawElements(GL46C.GL_TRIANGLES, count, indexType, startPos);
        }
        GL46C.glBindBuffer(GL46C.GL_ELEMENT_ARRAY_BUFFER, 0);
        RenderTimer.EndIfUse();


        if (false) {
            //GL46C.glClientActiveTexture(33986); //Texture id from LightTexture
            //GL46C.glDisableClientState(GL46C.GL_TEXTURE_COORD_ARRAY);
            //GL46C.glClientActiveTexture(GL46C.GL_TEXTURE0);
            MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().disable();
        }
        GL46C.glDisableVertexAttribArray(GL11.GL_TEXTURE_COORD_ARRAY);
        GL46C.glDisableVertexAttribArray(GL11.GL_NORMAL_ARRAY);
        GL46C.glDisableVertexAttribArray(GL46C.GL_VERTEX_ARRAY);


        RenderSystem.enableCull();
        deliverStack.pop();
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
