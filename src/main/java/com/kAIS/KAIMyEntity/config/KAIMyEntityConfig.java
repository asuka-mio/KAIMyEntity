package com.kAIS.KAIMyEntity.config;

import com.kAIS.KAIMyEntity.KAIMyEntity;
import net.minecraftforge.common.config.Config;

@Config(modid = KAIMyEntity.MODID)
public final class KAIMyEntityConfig
{
    public static boolean openGLEnableLighting = true;

    @Config.RangeInt(min = 0)
    @Config.RequiresMcRestart
    public static int modelPoolMaxCount = 20;
}
