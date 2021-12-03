package com.kAIS.KAIMyEntity.renderer;

import com.kAIS.KAIMyEntity.KAIMyEntityClient;
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

    int vertexArrayObject;
    int indexBufferObject;
    int vertexBufferObject;
    int normalBufferObject;
    int texcoordBufferObject;

    int indexElementSize;
    int indexType;

    static int shaderProgram;
    static int positionLocation;
    static int uvLocation;
    static int projMatLocation;
    static int modelViewLocation;
    static int samplerLocation;

    static boolean isShaderInited = false;

    Material[] mats;
    MMDModelOpenGL() {

    }
    public static void InitShader(){
        //Init Shader
        ShaderProvider.Init();
        shaderProgram = ShaderProvider.getProgram();

        //Init ShaderPropLocation
        positionLocation = GL46C.glGetAttribLocation(shaderProgram,"Position");
        uvLocation = GL46C.glGetAttribLocation(shaderProgram,"UV0");
        projMatLocation = GL46C.glGetUniformLocation(shaderProgram,"ProjMat");
        modelViewLocation = GL46C.glGetUniformLocation(shaderProgram,"ModelViewMat");
        samplerLocation = GL46C.glGetUniformLocation(shaderProgram,"Sampler0");
        isShaderInited = true;
    }

    public static MMDModelOpenGL Create(String modelFilename, String modelDir, boolean isPMD, long layerCount) {
        if(!isShaderInited)
            InitShader();
        if (nf == null) nf = NativeFunc.GetInst();
        long model;
        if (isPMD)
            model = nf.LoadModelPMD(modelFilename, modelDir, layerCount);
        else
            model = nf.LoadModelPMX(modelFilename, modelDir, layerCount);
        if (model == 0) {
            KAIMyEntityClient.logger.info(String.format("Cannot open model: '%s'.", modelFilename));
            return null;
        }
        BufferRenderer.unbindAll();
        //Model exists,now we prepare data for OpenGL
        int vertexArrayObject = GL46C.glGenVertexArrays();
        int indexBufferObject = GL46C.glGenBuffers();
        int positionBufferObject = GL46C.glGenBuffers();
        int normalBufferObject = GL46C.glGenBuffers();
        int uvBufferObject = GL46C.glGenBuffers();

        int vertexCount = (int) nf.GetVertexCount(model);
        ByteBuffer posBuffer = ByteBuffer.allocateDirect(vertexCount * 12); //float * 3
        ByteBuffer norBuffer = ByteBuffer.allocateDirect(vertexCount * 12);
        ByteBuffer uvBuffer = ByteBuffer.allocateDirect(vertexCount * 8); //float * 2

        GL46C.glBindVertexArray(vertexArrayObject);
        //Init indexBufferObject
        int indexElementSize = (int) nf.GetIndexElementSize(model);
        int indexCount = (int) nf.GetIndexCount(model);
        int indexSize = indexCount * indexElementSize;
        long indexData = nf.GetIndices(model);
        ByteBuffer indexBuffer = ByteBuffer.allocateDirect(indexSize);
        for (int i = 0; i < indexSize; ++i)
            indexBuffer.put(nf.ReadByte(indexData, i));
        indexBuffer.position(0);
        GL46C.glBindBuffer(GL46C.GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
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
        result.indexBufferObject = indexBufferObject;
        result.vertexBufferObject = positionBufferObject;
        result.texcoordBufferObject = uvBufferObject;
        result.normalBufferObject = normalBufferObject;
        result.vertexArrayObject = vertexArrayObject;
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
        Shader shader = RenderSystem.getShader();

        BufferRenderer.unbindAll();
        GL46C.glBindVertexArray(vertexArrayObject);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        deliverStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-entityYaw));
        deliverStack.scale(0.09f,0.09f,0.09f);
        shader.modelViewMat.set(deliverStack.peek().getPositionMatrix());
        FloatBuffer modelViewMatBuff = shader.modelViewMat.getFloatData();
        FloatBuffer projViewMatBuff = shader.projectionMat.getFloatData();


        GL46C.glEnableVertexAttribArray(positionLocation);
        RenderSystem.activeTexture(GL46C.GL_TEXTURE0);
        GL46C.glEnableVertexAttribArray(uvLocation);

        int posAndNorSize = vertexCount * 12; //float * 3
        long posData = nf.GetPoss(model);
        nf.CopyDataToByteBuffer(posBuffer, posData, posAndNorSize);
        GL46C.glBindBuffer(GL46C.GL_ARRAY_BUFFER, vertexBufferObject);
        GL46C.glBufferData(GL46C.GL_ARRAY_BUFFER,posBuffer,GL46C.GL_STATIC_DRAW);
        GL46C.glVertexAttribPointer(positionLocation,3,GL46C.GL_FLOAT,false,0, 0);


        int uvSize = vertexCount * 8; //float * 2
        long uvData = nf.GetUVs(model);
        nf.CopyDataToByteBuffer(uvBuffer, uvData, uvSize);
        GL46C.glBindBuffer(GL46C.GL_ARRAY_BUFFER, texcoordBufferObject);
        GL46C.glBufferData(GL46C.GL_ARRAY_BUFFER,uvBuffer,GL46C.GL_STATIC_DRAW);
        GL46C.glVertexAttribPointer(uvLocation,2,GL46C.GL_FLOAT,false,0, 0);


        GL46C.glBindBuffer(GL46C.GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
        GL46C.glUseProgram(shaderProgram);
        GL46C.glUniformMatrix4fv(modelViewLocation,false,modelViewMatBuff);
        GL46C.glUniformMatrix4fv(projMatLocation,false,projViewMatBuff);
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

            GL46C.glUniform1i(samplerLocation,0);
            GL46C.glDrawElements(GL46C.GL_TRIANGLES, count, indexType, startPos);
        }
        GL46C.glUseProgram(0);
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
