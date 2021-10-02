package com.kAIS.KAIMyEntity.register;

import com.kAIS.KAIMyEntity.KAIMyEntity;
import com.kAIS.KAIMyEntity.network.KAIMyEntityNetworkPack;
import com.kAIS.KAIMyEntity.renderer.KAIMyEntityRenderFactory;
import com.kAIS.KAIMyEntity.renderer.KAIMyEntityRendererPlayer;
import com.kAIS.KAIMyEntity.renderer.MMDModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.io.File;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class KAIMyEntityRegisterClient {
    static KeyBinding keyResetPhysics = new KeyBinding("key.resetPhysics", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.title");
    static KeyBinding keyReloadModels = new KeyBinding("key.reloadModels", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_KP_1, "key.title");
    static KeyBinding keyCustomAnim1 = new KeyBinding("key.customAnim1", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.title");
    static KeyBinding keyCustomAnim2 = new KeyBinding("key.customAnim2", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.title");
    static KeyBinding keyCustomAnim3 = new KeyBinding("key.customAnim3", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_N, "key.title");
    static KeyBinding keyCustomAnim4 = new KeyBinding("key.customAnim4", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.title");

    public static void Register() {

        for (KeyBinding i : new KeyBinding[]{keyCustomAnim1, keyCustomAnim2, keyCustomAnim3, keyCustomAnim4, keyReloadModels, keyResetPhysics})
            ClientRegistry.registerKeyBinding(i);
        File[] modelDirs = new File(Minecraft.getInstance().gameDir, "KAIMyEntity").listFiles();
        if (modelDirs != null) {
            for (File i : modelDirs) {
                if (!i.getName().equals("EntityPlayer")) {
                    String mcEntityName = i.getName().replace('.', ':');
                    if (EntityType.byKey(mcEntityName).isPresent())
                        RenderingRegistry.registerEntityRenderingHandler(EntityType.byKey(mcEntityName).get(), new KAIMyEntityRenderFactory<>(mcEntityName));
                    else
                        KAIMyEntity.logger.warn(mcEntityName + "not present,ignore rendering it!");
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        //Renderer Create time: When 3rd view. If you use shader then when world entered.
        //Renderer Render time: WHen 3rd view.

        if (event.getEntity() == null)
            return;
        if (KAIMyEntityRendererPlayer.GetInst() == null) {
            KAIMyEntityRendererPlayer.Init(event.getRenderer().getRenderManager());
        }
        event.setCanceled(true);

        float f = event.getEntity().prevRotationYaw + (event.getEntity().rotationYaw - event.getEntity().prevRotationYaw) * event.getPartialRenderTick();
        KAIMyEntityRendererPlayer.GetInst().render((PlayerEntity) event.getEntity(), f, event.getPartialRenderTick(), event.getMatrixStack(), event.getBuffers(), event.getLight());
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onKeyPressed(InputEvent.KeyInputEvent event) {
        if (keyCustomAnim1.isKeyDown()) {
            if (KAIMyEntityRendererPlayer.GetInst() != null) {
                KAIMyEntityRendererPlayer.GetInst().CustomAnim(Minecraft.getInstance().player, "1");
                assert Minecraft.getInstance().player != null;
                KAIMyEntityRegisterCommon.channel.sendToServer(new KAIMyEntityNetworkPack(1, Minecraft.getInstance().player.getUniqueID(), 1));
            }
        }
        if (keyReloadModels.isKeyDown()) {
            MMDModelManager.ReloadModel();
        }
        if (keyCustomAnim2.isKeyDown()) {
            if (KAIMyEntityRendererPlayer.GetInst() != null) {
                KAIMyEntityRendererPlayer.GetInst().CustomAnim(Minecraft.getInstance().player, "2");
                assert Minecraft.getInstance().player != null;
                KAIMyEntityRegisterCommon.channel.sendToServer(new KAIMyEntityNetworkPack(1, Minecraft.getInstance().player.getUniqueID(), 2));
            }
        }
        if (keyCustomAnim3.isKeyDown()) {
            if (KAIMyEntityRendererPlayer.GetInst() != null) {
                KAIMyEntityRendererPlayer.GetInst().CustomAnim(Minecraft.getInstance().player, "3");
                assert Minecraft.getInstance().player != null;
                KAIMyEntityRegisterCommon.channel.sendToServer(new KAIMyEntityNetworkPack(1, Minecraft.getInstance().player.getUniqueID(), 3));
            }
        }
        if (keyCustomAnim4.isKeyDown()) {
            if (KAIMyEntityRendererPlayer.GetInst() != null) {
                KAIMyEntityRendererPlayer.GetInst().CustomAnim(Minecraft.getInstance().player, "4");
                assert Minecraft.getInstance().player != null;
                KAIMyEntityRegisterCommon.channel.sendToServer(new KAIMyEntityNetworkPack(1, Minecraft.getInstance().player.getUniqueID(), 4));
            }
        }
        if (keyResetPhysics.isKeyDown()) {
            if (KAIMyEntityRendererPlayer.GetInst() != null) {
                KAIMyEntityRendererPlayer.GetInst().ResetPhysics(Minecraft.getInstance().player);
                assert Minecraft.getInstance().player != null;
                KAIMyEntityRegisterCommon.channel.sendToServer(new KAIMyEntityNetworkPack(2, Minecraft.getInstance().player.getUniqueID(), 0));
            }
        }
    }
}
