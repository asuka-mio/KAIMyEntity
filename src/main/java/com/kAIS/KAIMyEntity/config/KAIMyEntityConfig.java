package com.kAIS.KAIMyEntity.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class KAIMyEntityConfig {
    public static ForgeConfigSpec config;

    public static ForgeConfigSpec.BooleanValue openGLEnableLighting;

    public static ForgeConfigSpec.IntValue modelPoolMaxCount;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("KAIMyEntity");
        openGLEnableLighting = b.define("openGLEnableLighting", true);
        modelPoolMaxCount = b.defineInRange("modelPoolMaxCount", 20, 0, 100);
        b.pop();
        config = b.build();
    }
}
