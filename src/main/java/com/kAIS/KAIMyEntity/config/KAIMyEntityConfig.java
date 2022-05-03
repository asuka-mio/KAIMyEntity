package com.kAIS.KAIMyEntity.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.util.Properties;

public final class KAIMyEntityConfig {
    public static boolean openGLEnableLighting = true;
    public static int modelPoolMaxCount = 100;

    static {
        try (BufferedReader reader = Files.newBufferedReader(FabricLoader.getInstance().getConfigDir().resolve("KAIMyEntity.properties"))) {
            Properties properties = new Properties();
            properties.load(reader);
            KAIMyEntityConfig.openGLEnableLighting = Boolean.parseBoolean(properties.getProperty("openGLEnableLighting"));
            KAIMyEntityConfig.modelPoolMaxCount = Integer.parseInt(properties.getProperty("modelPoolMaxCount"));
        } catch (Exception ignored) {
        }
    }
}
