package com.kAIS.KAIMyEntity;

import com.kAIS.KAIMyEntity.register.KAIMyEntityRegisterClient;
//import com.kAIS.KAIMyEntity.register.KAIMyEntityRegisterCommon;
import com.kAIS.KAIMyEntity.renderer.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.launch.FabricClientTweaker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KAIMyEntity implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger logger = LogManager.getLogger();

	@Override
	public void onInitialize() {
		logger.info("KAIMyEntity Init begin...");

		//KAIMyEntityRegisterCommon.Register();

		if (true) {
			RenderTimer.Init();
			MMDModelManager.Init();
			MMDTextureManager.Init();
			MMDAnimManager.Init();
			KAIMyEntityRegisterClient.Register();
		} else {
			logger.info("KAIMyEntity running on server.");
		}
		logger.info("KAIMyEntity preInit successful.");
	}
}
