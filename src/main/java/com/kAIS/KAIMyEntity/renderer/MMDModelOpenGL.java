package com.kAIS.KAIMyEntity.renderer;

import com.kAIS.KAIMyEntity.KAIMyEntity;
import com.kAIS.KAIMyEntity.NativeFunc;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

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
        nf.UpdateModel(model);
    }

    void RenderModel(float entityYaw, MatrixStack deliverStack ) {
        ShaderProvider.Init();
        int mmdProgram = ShaderProvider.getProgram();
        Shader shader = RenderSystem.getShader();

        int position = GL46C.glGetAttribLocation(mmdProgram,"Position");
        int texcoord = GL46C.glGetAttribLocation(mmdProgram,"UV0");
        BufferRenderer.unbindAll();

        GL46C.glBindVertexArray(vao);

        shader.projectionMat.set(RenderSystem.getProjectionMatrix());
        shader.colorModulator.set(RenderSystem.getShaderColor());
        deliverStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-entityYaw));
        deliverStack.scale(0.09f,0.09f,0.09f);
        shader.modelViewMat.set(deliverStack.peek().getModel());
        FloatBuffer modelViewMatBuff = shader.modelViewMat.getFloatData();
        FloatBuffer projViewMatBuff = shader.projectionMat.getFloatData();

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        GL46C.glEnableVertexAttribArray(position);
        RenderSystem.activeTexture(GL46C.GL_TEXTURE1);
        GL46C.glEnableVertexAttribArray(texcoord);

        int posAndNorSize = vertexCount * 12; //float * 3
        long posData = nf.GetPoss(model);
        nf.CopyDataToByteBuffer(posBuffer, posData, posAndNorSize);
        GL46C.glBindBuffer(GL46C.GL_ARRAY_BUFFER,vbo);
        posBuffer.position(0);
        GL46C.glBufferData(GL46C.GL_ARRAY_BUFFER,posBuffer,GL46C.GL_STATIC_DRAW);
        GL46C.glVertexAttribPointer(position,3,GL46C.GL_FLOAT,false,0, 0);


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
                GL46C.glBindTexture(GL46C.GL_TEXTURE_2D,mats[materialID].tex);
            long startPos = (long) nf.GetSubMeshBeginIndex(model, i) * indexElementSize;
            int count = nf.GetSubMeshVertexCount(model, i);

            GL46C.glUseProgram(mmdProgram);
            GL46C.glUniformMatrix4fv(GL46C.glGetUniformLocation(mmdProgram,"ModelViewMat"),false,modelViewMatBuff);
            GL46C.glUniformMatrix4fv(GL46C.glGetUniformLocation(mmdProgram,"ProjMat"),false,projViewMatBuff);
            GL46C.glUniform1i(GL46C.glGetUniformLocation(mmdProgram,"Sampler0"),1);
            GL46C.glDrawElements(GL46C.GL_TRIANGLES, count, indexType, startPos);
            GL46C.glUseProgram(0);
        }
        BufferRenderer.unbindAll();
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
