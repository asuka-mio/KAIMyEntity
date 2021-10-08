package com.kAIS.KAIMyEntity.renderer;

import com.kAIS.KAIMyEntity.KAIMyEntityClient;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL46C;
import java.io.FileInputStream;

public class ShaderProvider {
    private static boolean isInited = false;
    private static int program = 0;
    private static String vertexPath = MinecraftClient.getInstance().runDirectory.getAbsolutePath()+"/KAIMyEntity/Shader/MMDShader.vsh";
    private static String fragPath = MinecraftClient.getInstance().runDirectory.getAbsolutePath()+"/KAIMyEntity/Shader/MMDShader.fsh";
    public static void Init(){
        if(!isInited){
            try {
                int vertexShader = GL46C.glCreateShader(GL46C.GL_VERTEX_SHADER);
                FileInputStream vertexSource = new FileInputStream(vertexPath);
                GL46C.glShaderSource(vertexShader,new String(vertexSource.readAllBytes()));

                int fragShader = GL46C.glCreateShader(GL46C.GL_FRAGMENT_SHADER);
                FileInputStream fragSource = new FileInputStream(fragPath);
                GL46C.glShaderSource(fragShader,new String(fragSource.readAllBytes()));

                GL46C.glCompileShader(vertexShader);
                if (GL46C.glGetShaderi(vertexShader, GL46C.GL_COMPILE_STATUS) == GL46C.GL_FALSE) {
                    String log = GL46C.glGetShaderInfoLog(vertexShader, 8192).trim();
                    KAIMyEntityClient.logger.error("Failed to compile shader {}", log);
                    GL46C.glDeleteShader(vertexShader);
                }

                GL46C.glCompileShader(fragShader);
                if (GL46C.glGetShaderi(fragShader, GL46C.GL_COMPILE_STATUS) == GL46C.GL_FALSE) {
                    String log = GL46C.glGetShaderInfoLog(fragShader, 8192).trim();
                    KAIMyEntityClient.logger.error("Failed to compile shader {}", log);
                    GL46C.glDeleteShader(fragShader);
                }
                program = GL46C.glCreateProgram();
                GL46C.glAttachShader(program,vertexShader);
                GL46C.glAttachShader(program,fragShader);
                GL46C.glLinkProgram(program);
                if (GL46C.glGetProgrami(program, GL46C.GL_LINK_STATUS) == GL46C.GL_FALSE) {
                    String log = GL46C.glGetProgramInfoLog(program, 8192);
                    KAIMyEntityClient.logger.error("Failed to link shader program\n{}", log);
                    GL46C.glDeleteProgram(program);
                    program = 0;
                }
                KAIMyEntityClient.logger.info("MMD Shader Initialize finished");
            } catch (Exception e) {
                e.printStackTrace();
            }
            isInited = true;
        }
    }
    public static int getProgram(){
        if(program <= 0)
            throw new Error("Call Shader before init");
        return program;
    }
}
