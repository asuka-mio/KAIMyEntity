package com.kAIS.KAIMyEntity.renderer;

import com.kAIS.KAIMyEntity.NativeFunc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class KAIMyEntityRendererPlayer extends Render<EntityPlayer>
{
    public static boolean Init(RenderManager renderManager)
    {
        inst = new KAIMyEntityRendererPlayer(renderManager);
        return true;
    }

    public static KAIMyEntityRendererPlayer GetInst()
    {
        return inst;
    }

    @Override
    public boolean shouldRender(EntityPlayer livingEntity, ICamera camera, double camX, double camY, double camZ)
    {
        //Update no mattor should render
        MMDModelManager.Update();

        return super.shouldRender(livingEntity, camera, camX, camY, camZ);
    }

    @Override
    public void doRender(EntityPlayer entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);

        IMMDModel model = null;
        MMDModelManager.Model m = MMDModelManager.GetModelOrInPool(entity, "EntityPlayer_" + entity.getName(), true);
        if (m == null)
            m = MMDModelManager.GetModelOrInPool(entity, "EntityPlayer", true);
        if (m != null)
            model = m.model;

        MMDModelManager.ModelWithPlayerData mwpd = (MMDModelManager.ModelWithPlayerData)m;

        if (model != null)
        {
            RenderTimer.BeginRecord();
            RenderTimer.LogIfUse("------RenderPlayer Begin------");
            RenderTimer.BeginIfUse("KAIMyEntityRendererPlayer: Check animation state");
            if (!mwpd.playerData.playCustomAnim)
            {
                //Layer 0
                if (entity.getHealth() == 0.0f)
                {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.Die, "die");
                }
                else if (entity.isElytraFlying())
                {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.ElytraFly, "elytraFly");
                }
                else if (entity.isPlayerSleeping())
                {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.Sleep, "sleep");
                }
                else if (entity.isRiding())
                {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.Ride, "ride");
                }
                else if (entity.isInWater())
                {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.Swim, "swim");
                }
                else if (entity.isOnLadder())
                {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.OnLadder, "onLadder");
                }
                else if (entity.posY - entity.prevPosY != 0.0f)
                {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.Air, "air");
                }
                else if (entity.isSprinting())
                {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.Sprint, "sprint");
                }
                else if (entity.posX - entity.prevPosX != 0.0f || entity.posZ - entity.prevPosZ != 0.0f)
                {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.Walk, "walk");
                }
                else
                {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.Idle, "idle");
                }

                //Layer 1
                if (entity.isHandActive())
                {
                    if (entity.getActiveHand() == EnumHand.MAIN_HAND)
                    {
                        CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.Item1Right, "1", entity.getActiveItemStack().getItem().getRegistryName().toString().replace(':', '.'), false);
                        CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.Item2Right, "2", entity.getActiveItemStack().getItem().getRegistryName().toString().replace(':', '.'), false);
                        CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.Item3Right, "3", entity.getActiveItemStack().getItem().getRegistryName().toString().replace(':', '.'), false);
                        CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.Item4Right, "4", entity.getActiveItemStack().getItem().getRegistryName().toString().replace(':', '.'), false);
                    }
                    else
                    {
                        CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.Item1Left, "1", entity.getActiveItemStack().getItem().getRegistryName().toString().replace(':', '.'), true);
                        CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.Item2Left, "2", entity.getActiveItemStack().getItem().getRegistryName().toString().replace(':', '.'), true);
                        CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.Item3Left, "3", entity.getActiveItemStack().getItem().getRegistryName().toString().replace(':', '.'), true);
                        CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.Item4Left, "4", entity.getActiveItemStack().getItem().getRegistryName().toString().replace(':', '.'), true);
                    }
                }
                else if (entity.isSwingInProgress)
                {
                    if (entity.swingingHand == EnumHand.MAIN_HAND)
                    {
                        AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.SwingRight, "swingRight");
                    }
                    else if (entity.swingingHand == EnumHand.OFF_HAND)
                    {
                        AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.SwingLeft, "swingLeft");
                    }
                }
                else
                {
                    if (mwpd.playerData.stateLayer1 != MMDModelManager.PlayerData.EntityStateLayer1.Idle)
                    {
                        mwpd.playerData.stateLayer1 = MMDModelManager.PlayerData.EntityStateLayer1.Idle;
                        model.ChangeAnim(0, 1);
                    }
                }

                //Layer 2
                if (entity.isSneaking())
                {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer2.Sneak, "sneak");
                }
                else
                {
                    if (mwpd.playerData.stateLayer2 != MMDModelManager.PlayerData.EntityStateLayer2.Idle)
                    {
                        mwpd.playerData.stateLayer2 = MMDModelManager.PlayerData.EntityStateLayer2.Idle;
                        model.ChangeAnim(0, 2);
                    }
                }
            }
            RenderTimer.EndIfUse();

            model.Render(x, y, z, entityYaw);

            //Render item
            RenderTimer.BeginIfUse("KAIMyEntityRendererPlayer: Render item");
            NativeFunc nf = NativeFunc.GetInst();
            nf.GetRightHandMat(model.GetModelLong(), mwpd.playerData.rightHandMat);
            for (int i = 0; i < 64; ++i)
                mwpd.playerData.matBuffer.put(nf.ReadByte(mwpd.playerData.rightHandMat, i));
            mwpd.playerData.matBuffer.position(0);
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(-entityYaw, 0.0f, 1.0f, 0.0f);
            GlStateManager.scale(0.1f, 0.1f, 0.1f);
            GlStateManager.multMatrix(mwpd.playerData.matBuffer.asFloatBuffer());
            GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
            GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
            GlStateManager.scale(10.0f, 10.0f, 10.0f);
            Minecraft.getMinecraft().getItemRenderer().renderItemSide(entity, entity.getHeldItem(EnumHand.MAIN_HAND), ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, false);
            GlStateManager.popMatrix();

            nf.GetLeftHandMat(model.GetModelLong(), mwpd.playerData.leftHandMat);
            for (int i = 0; i < 64; ++i)
                mwpd.playerData.matBuffer.put(nf.ReadByte(mwpd.playerData.leftHandMat, i));
            mwpd.playerData.matBuffer.position(0);
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(-entityYaw, 0.0f, 1.0f, 0.0f);
            GlStateManager.scale(0.1f, 0.1f, 0.1f);
            GlStateManager.multMatrix(mwpd.playerData.matBuffer.asFloatBuffer());
            GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
            GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
            GlStateManager.scale(10.0f, 10.0f, 10.0f);
            Minecraft.getMinecraft().getItemRenderer().renderItemSide(entity, entity.getHeldItem(EnumHand.OFF_HAND), ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, true);
            GlStateManager.popMatrix();
            RenderTimer.EndIfUse();
            RenderTimer.LogIfUse("------RenderPlayer End------");
            RenderTimer.EndRecord();
        }
    }

    public void ResetPhysics(EntityPlayer player)
    {
        MMDModelManager.Model m = MMDModelManager.GetModel(player);
        if (m != null)
        {
            MMDModelManager.ModelWithPlayerData mwpd = (MMDModelManager.ModelWithPlayerData)m;
            IMMDModel model = m.model;
            mwpd.playerData.stateLayer0 = MMDModelManager.PlayerData.EntityStateLayer0.Idle;
            mwpd.playerData.stateLayer1 = MMDModelManager.PlayerData.EntityStateLayer1.Idle;
            mwpd.playerData.playCustomAnim = false;
            model.ChangeAnim(MMDAnimManager.GetAnimModel(model, "idle"), 0);
            ResetAnimationWithoutLayer0(model);
            model.ResetPhysics();
        }
    }

    public void CustomAnim(EntityPlayer player, String id)
    {
        MMDModelManager.Model m = MMDModelManager.GetModel(player);
        if (m != null)
        {
            MMDModelManager.ModelWithPlayerData mwpd = (MMDModelManager.ModelWithPlayerData) m;
            IMMDModel model = m.model;
            mwpd.playerData.playCustomAnim = true;
            model.ChangeAnim(MMDAnimManager.GetAnimModel(model, "custom_" + id), 0);
            ResetAnimationWithoutLayer0(model);
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityPlayer entity)
    {
        return null;
    }

    static KAIMyEntityRendererPlayer inst;

    KAIMyEntityRendererPlayer(RenderManager renderManager)
    {
        super(renderManager);
    }

    void AnimStateChangeOnce(MMDModelManager.ModelWithPlayerData model, MMDModelManager.PlayerData.EntityStateLayer0 targetState, String animName)
    {
        if (model.playerData.stateLayer0 != targetState)
        {
            model.playerData.stateLayer0 = targetState;
            model.model.ChangeAnim(MMDAnimManager.GetAnimModel(model.model, animName), 0);
        }
    }

    void AnimStateChangeOnce(MMDModelManager.ModelWithPlayerData model, MMDModelManager.PlayerData.EntityStateLayer1 targetState, String animName)
    {
        if (model.playerData.stateLayer1 != targetState)
        {
            model.playerData.stateLayer1 = targetState;
            model.model.ChangeAnim(MMDAnimManager.GetAnimModel(model.model, animName), 1);
        }
    }

    void AnimStateChangeOnce(MMDModelManager.ModelWithPlayerData model, MMDModelManager.PlayerData.EntityStateLayer2 targetState, String animName)
    {
        if (model.playerData.stateLayer2 != targetState)
        {
            model.playerData.stateLayer2 = targetState;
            model.model.ChangeAnim(MMDAnimManager.GetAnimModel(model.model, animName), 2);
        }
    }

    void CustomItemActiveAnim(MMDModelManager.ModelWithPlayerData model, MMDModelManager.PlayerData.EntityStateLayer1 targetState, String id, String itemName, boolean isLeftHand)
    {
        long anim = MMDAnimManager.GetAnimModel(model.model, String.format("itemActive_%s_%s_%s", id, itemName, isLeftHand ? "left" : "right"));
        if (anim != 0)
        {
            if (model.playerData.stateLayer1 != targetState)
            {
                model.playerData.stateLayer1 = targetState;
                model.model.ChangeAnim(anim, 1);
            }
        }
    }

    void ResetAnimationWithoutLayer0(IMMDModel model)
    {
        model.ChangeAnim(0, 1);
        model.ChangeAnim(0, 2);
    }
}
