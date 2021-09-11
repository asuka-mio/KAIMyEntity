package com.kAIS.KAIMyEntity.renderer;

import com.kAIS.KAIMyEntity.KAIMyEntity;
import com.kAIS.KAIMyEntity.NativeFunc;
import com.kAIS.KAIMyEntity.config.KAIMyEntityConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;

public class MMDModelOpenGL implements IMMDModel
{
    public static MMDModelOpenGL Create(String modelFilename, String modelDir, boolean isPMD, long layerCount)
    {
        if (nf == null) nf = NativeFunc.GetInst();
        long model;
        if (isPMD)
            model = nf.LoadModelPMD(modelFilename, modelDir, layerCount);
        else
            model = nf.LoadModelPMX(modelFilename, modelDir, layerCount);
        if (model == 0)
        {
            KAIMyEntity.logger.info(String.format("Cannot open model: '%s'.", modelFilename));
            return null;
        }

        //Init vertex buffers
        int vertexCount = (int)nf.GetVertexCount(model);
        ByteBuffer posBuffer = ByteBuffer.allocateDirect(vertexCount * 12); //float * 3
        ByteBuffer norBuffer = ByteBuffer.allocateDirect(vertexCount * 12);
        ByteBuffer uvBuffer = ByteBuffer.allocateDirect(vertexCount * 8); //float * 2

        //Init ibo
        int indexElementSize = (int)nf.GetIndexElementSize(model);
        int indexCount = (int)nf.GetIndexCount(model);
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
        int indexType = 0;
        switch (indexElementSize)
        {
            case 1:
                indexType = GL11.GL_UNSIGNED_BYTE;
                break;
            case 2:
                indexType = GL11.GL_UNSIGNED_SHORT;
                break;
            case 4:
                indexType = GL11.GL_UNSIGNED_INT;
                break;
        }

        //Material
        MMDModelOpenGL.Material[] mats = new MMDModelOpenGL.Material[(int)nf.GetMaterialCount(model)];
        for (int i = 0; i < mats.length; ++i)
        {
            mats[i] = new MMDModelOpenGL.Material();
            String texFilename = nf.GetMaterialTex(model, i);
            if (!texFilename.isEmpty())
            {
                MMDTextureManager.Texture mgrTex = MMDTextureManager.GetTexture(texFilename);
                if (mgrTex != null)
                {
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

    public static void Delete(MMDModelOpenGL model)
    {
        nf.DeleteModel(model.model);
    }

    public void Render(double x, double y, double z, float entityYaw)
    {
        Update();
        RenderModel(x, y, z, entityYaw);
    }

    public void ChangeAnim(long anim, long layer)
    {
        nf.ChangeModelAnim(model, anim, layer);
    }

    public void ResetPhysics()
    {
        nf.ResetModelPhysics(model);
    }

    public long GetModelLong()
    {
        return model;
    }

    public String GetModelDir()
    {
        return modelDir;
    }

    static NativeFunc nf;
    long model;
    String modelDir;

    int vertexCount;
    ByteBuffer posBuffer, norBuffer, uvBuffer;
    int ibo;
    int indexElementSize;
    int indexType;

    static class Material
    {
        int tex;
        boolean hasAlpha;

        Material()
        {
            tex = 0;
            hasAlpha = false;
        }
    }
    Material[] mats;

    MMDModelOpenGL()
    {

    }

    void Update()
    {
        RenderTimer.BeginIfUse("MMDModelOpenGL: Call native function: UpdateModel");
        nf.UpdateModel(model);
        RenderTimer.EndIfUse();
    }

    void RenderModel(double x, double y, double z, float entityYaw)
    {
        //Depth test enabled by default
        //Lighting enabled by default
        if (!KAIMyEntityConfig.openGLEnableLighting)
            GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-entityYaw, 0.0f, 1.0f, 0.0f);
        GlStateManager.scale(0.1, 0.1, 0.1);

        //Read vertex data
        RenderTimer.BeginIfUse("MMDModelOpenGL: Read vertex data into Java byte buffer");
        /*
        int posAndNorSize = vertexCount * 12; //float * 3
        long posData = nf.GetPoss(model);
        for (int i = 0; i < posAndNorSize; ++i)
            posBuffer.put(nf.ReadByte(posData, i));
        posBuffer.position(0);
        long norData = nf.GetNormals(model);
        for (int i = 0; i < posAndNorSize; ++i)
            norBuffer.put(nf.ReadByte(norData, i));
        norBuffer.position(0);
        int uvSize = vertexCount * 8; //float * 2
        long uvData = nf.GetUVs(model);
        for (int i = 0; i < uvSize; ++i)
            uvBuffer.put(nf.ReadByte(uvData, i));
        uvBuffer.position(0);
         */
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
        for (long i = 0; i < subMeshCount; ++i)
        {
            int materialID = nf.GetSubMeshMaterialID(model, i);
            float alpha = nf.GetMaterialAlpha(model, materialID);
            if (alpha == 0.0f)
                continue;

            if (nf.GetMaterialBothFace(model, materialID))
            {
                GlStateManager.disableCull();
            }
            else
            {
                GlStateManager.enableCull();
            }

            if (mats[materialID].tex == 0)
                Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
            else
                GlStateManager.bindTexture(mats[materialID].tex);
            long startPos = nf.GetSubMeshBeginIndex(model, i) * indexElementSize;
            int count = nf.GetSubMeshVertexCount(model, i);
            GL11.glDrawElements(GL11.GL_TRIANGLES, count, indexType, startPos);
        }
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        RenderTimer.EndIfUse();

        //Disable client state
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

        GlStateManager.enableCull();

        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
    }
}
