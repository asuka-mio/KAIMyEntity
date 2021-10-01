package com.kAIS.KAIMyEntity.renderer;

import com.kAIS.KAIMyEntity.NativeFunc;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nullable;

public class KAIMyEntityRendererPlayer extends EntityRenderer<PlayerEntity> {
    static KAIMyEntityRendererPlayer inst;

    KAIMyEntityRendererPlayer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    public static void Init(EntityRendererManager renderManager) {
        inst = new KAIMyEntityRendererPlayer(renderManager);
    }

    public static KAIMyEntityRendererPlayer GetInst() {
        return inst;
    }

    @Override
    public boolean shouldRender(PlayerEntity livingEntityIn, ClippingHelper camera, double camX, double camY, double camZ) {
        //Update no matter should render
        MMDModelManager.Update();

        return super.shouldRender(livingEntityIn, camera, camX, camY, camZ);
    }

    @Override
    public void render(PlayerEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);

        IMMDModel model = null;
        MMDModelManager.Model m = MMDModelManager.GetModelOrInPool(entityIn, "EntityPlayer_" + entityIn.getName().getString(), true);
        if (m == null)
            m = MMDModelManager.GetModelOrInPool(entityIn, "EntityPlayer", true);
        if (m != null)
            model = m.model;

        MMDModelManager.ModelWithPlayerData mwpd = (MMDModelManager.ModelWithPlayerData) m;

        if (model != null) {
            RenderTimer.BeginRecord();
            RenderTimer.LogIfUse("------RenderPlayer Begin------");
            RenderTimer.BeginIfUse("KAIMyEntityRendererPlayer: Check animation state");
            if (!mwpd.playerData.playCustomAnim) {
                //Layer 0
                if (entityIn.getHealth() == 0.0f) {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.Die, "die");
                } else if (entityIn.isElytraFlying()) {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.ElytraFly, "elytraFly");
                } else if (entityIn.isSleeping()) {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.Sleep, "sleep");
                } else if (entityIn.isPassenger()) {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.Ride, "ride");
                } else if (entityIn.isInWater()) {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.Swim, "swim");
                } else if (entityIn.isOnLadder()) {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.OnLadder, "onLadder");
                } else if (entityIn.getPosY() - entityIn.prevPosY != 0.0f) {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.Air, "air");
                } else if (entityIn.isSprinting()) {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.Sprint, "sprint");
                } else if (entityIn.getPosX() - entityIn.prevPosX != 0.0f || entityIn.getPosZ() - entityIn.prevPosZ != 0.0f) {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.Walk, "walk");
                } else {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer0.Idle, "idle");
                }

                //Layer 1
                if (entityIn.isHandActive()) {
                    if (entityIn.getActiveHand() == Hand.MAIN_HAND) {
                        CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.Item1Right, "1", entityIn.getActiveItemStack().getItem().getRegistryName().toString().replace(':', '.'), false);
                        CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.Item2Right, "2", entityIn.getActiveItemStack().getItem().getRegistryName().toString().replace(':', '.'), false);
                        CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.Item3Right, "3", entityIn.getActiveItemStack().getItem().getRegistryName().toString().replace(':', '.'), false);
                        CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.Item4Right, "4", entityIn.getActiveItemStack().getItem().getRegistryName().toString().replace(':', '.'), false);
                    } else {
                        CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.Item1Left, "1", entityIn.getActiveItemStack().getItem().getRegistryName().toString().replace(':', '.'), true);
                        CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.Item2Left, "2", entityIn.getActiveItemStack().getItem().getRegistryName().toString().replace(':', '.'), true);
                        CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.Item3Left, "3", entityIn.getActiveItemStack().getItem().getRegistryName().toString().replace(':', '.'), true);
                        CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.Item4Left, "4", entityIn.getActiveItemStack().getItem().getRegistryName().toString().replace(':', '.'), true);
                    }
                } else if (entityIn.isSwingInProgress) {
                    if (entityIn.swingingHand == Hand.MAIN_HAND) {
                        AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.SwingRight, "swingRight");
                    } else if (entityIn.swingingHand == Hand.OFF_HAND) {
                        AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer1.SwingLeft, "swingLeft");
                    }
                } else {
                    if (mwpd.playerData.stateLayer1 != MMDModelManager.PlayerData.EntityStateLayer1.Idle) {
                        mwpd.playerData.stateLayer1 = MMDModelManager.PlayerData.EntityStateLayer1.Idle;
                        model.ChangeAnim(0, 1);
                    }
                }

                //Layer 2
                if (entityIn.isSneaking()) {
                    AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityStateLayer2.Sneak, "sneak");
                } else {
                    if (mwpd.playerData.stateLayer2 != MMDModelManager.PlayerData.EntityStateLayer2.Idle) {
                        mwpd.playerData.stateLayer2 = MMDModelManager.PlayerData.EntityStateLayer2.Idle;
                        model.ChangeAnim(0, 2);
                    }
                }
            }
            RenderTimer.EndIfUse();

            model.Render(entityYaw, matrixStackIn.getLast().getMatrix(), packedLightIn);

            //Render item
            RenderTimer.BeginIfUse("KAIMyEntityRendererPlayer: Render item");
            NativeFunc nf = NativeFunc.GetInst();
            nf.GetRightHandMat(model.GetModelLong(), mwpd.playerData.rightHandMat);
            matrixStackIn.push();
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-entityYaw));
            matrixStackIn.scale(0.1f, 0.1f, 0.1f);
            Matrix4f mat1 = matrixStackIn.getLast().getMatrix();
            mat1.mul(DataToMat(nf, mwpd.playerData.rightHandMat));
            matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(180.0f));
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90.0f));
            matrixStackIn.scale(10.0f, 10.0f, 10.0f);
            Minecraft.getInstance().getItemRenderer().renderItem(entityIn, entityIn.getHeldItemMainhand(),
                    ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, false, matrixStackIn,
                    bufferIn, entityIn.world, packedLightIn, OverlayTexture.NO_OVERLAY);
            matrixStackIn.pop();

            nf.GetLeftHandMat(model.GetModelLong(), mwpd.playerData.leftHandMat);
            matrixStackIn.push();
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-entityYaw));
            matrixStackIn.scale(0.1f, 0.1f, 0.1f);
            Matrix4f mat2 = matrixStackIn.getLast().getMatrix();
            mat2.mul(DataToMat(nf, mwpd.playerData.leftHandMat));
            matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(180.0f));
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90.0f));
            matrixStackIn.scale(10.0f, 10.0f, 10.0f);
            Minecraft.getInstance().getItemRenderer().renderItem(entityIn, entityIn.getHeldItemOffhand(),
                    ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, true, matrixStackIn,
                    bufferIn, entityIn.world, packedLightIn, OverlayTexture.NO_OVERLAY);
            matrixStackIn.pop();
            RenderTimer.EndIfUse();
            RenderTimer.LogIfUse("------RenderPlayer End------");
            RenderTimer.EndRecord();
        }
    }

    @Nullable
    @Override
    public ResourceLocation getEntityTexture(PlayerEntity entity) {
        return null;
    }

    public void ResetPhysics(PlayerEntity player) {
        MMDModelManager.Model m = MMDModelManager.GetModel(player);
        if (m != null) {
            MMDModelManager.ModelWithPlayerData mwpd = (MMDModelManager.ModelWithPlayerData) m;
            IMMDModel model = m.model;
            mwpd.playerData.stateLayer0 = MMDModelManager.PlayerData.EntityStateLayer0.Idle;
            mwpd.playerData.stateLayer1 = MMDModelManager.PlayerData.EntityStateLayer1.Idle;
            mwpd.playerData.playCustomAnim = false;
            model.ChangeAnim(MMDAnimManager.GetAnimModel(model, "idle"), 0);
            ResetAnimationWithoutLayer0(model);
            model.ResetPhysics();
        }
    }

    public void CustomAnim(PlayerEntity player, String id) {
        MMDModelManager.Model m = MMDModelManager.GetModel(player);
        if (m != null) {
            MMDModelManager.ModelWithPlayerData mwpd = (MMDModelManager.ModelWithPlayerData) m;
            IMMDModel model = m.model;
            mwpd.playerData.playCustomAnim = true;
            model.ChangeAnim(MMDAnimManager.GetAnimModel(model, "custom_" + id), 0);
            ResetAnimationWithoutLayer0(model);
        }
    }

    void AnimStateChangeOnce(MMDModelManager.ModelWithPlayerData model, MMDModelManager.PlayerData.EntityStateLayer0 targetState, String animName) {
        if (model.playerData.stateLayer0 != targetState) {
            model.playerData.stateLayer0 = targetState;
            model.model.ChangeAnim(MMDAnimManager.GetAnimModel(model.model, animName), 0);
        }
    }

    void AnimStateChangeOnce(MMDModelManager.ModelWithPlayerData model, MMDModelManager.PlayerData.EntityStateLayer1 targetState, String animName) {
        if (model.playerData.stateLayer1 != targetState) {
            model.playerData.stateLayer1 = targetState;
            model.model.ChangeAnim(MMDAnimManager.GetAnimModel(model.model, animName), 1);
        }
    }

    void AnimStateChangeOnce(MMDModelManager.ModelWithPlayerData model, MMDModelManager.PlayerData.EntityStateLayer2 targetState, String animName) {
        if (model.playerData.stateLayer2 != targetState) {
            model.playerData.stateLayer2 = targetState;
            model.model.ChangeAnim(MMDAnimManager.GetAnimModel(model.model, animName), 2);
        }
    }

    void CustomItemActiveAnim(MMDModelManager.ModelWithPlayerData model, MMDModelManager.PlayerData.EntityStateLayer1 targetState, String id, String itemName, boolean isLeftHand) {
        long anim = MMDAnimManager.GetAnimModel(model.model, String.format("itemActive_%s_%s_%s", id, itemName, isLeftHand ? "left" : "right"));
        if (anim != 0) {
            if (model.playerData.stateLayer1 != targetState) {
                model.playerData.stateLayer1 = targetState;
                model.model.ChangeAnim(anim, 1);
            }
        }
    }

    void ResetAnimationWithoutLayer0(IMMDModel model) {
        model.ChangeAnim(0, 1);
        model.ChangeAnim(0, 2);
    }

    float DataToFloat(NativeFunc nf, long data, long pos) {
        int temp = 0;
        temp |= nf.ReadByte(data, pos) & 0xff;
        temp |= (nf.ReadByte(data, pos + 1) & 0xff) << 8;
        temp |= (nf.ReadByte(data, pos + 2) & 0xff) << 16;
        temp |= (nf.ReadByte(data, pos + 3) & 0xff) << 24;
        return Float.intBitsToFloat(temp);
    }

    Matrix4f DataToMat(NativeFunc nf, long data) {
        Matrix4f result = new Matrix4f(new float[]
                {
                        DataToFloat(nf, data, 0),
                        DataToFloat(nf, data, 4),
                        DataToFloat(nf, data, 8),
                        DataToFloat(nf, data, 12),
                        DataToFloat(nf, data, 16),
                        DataToFloat(nf, data, 20),
                        DataToFloat(nf, data, 24),
                        DataToFloat(nf, data, 28),
                        DataToFloat(nf, data, 32),
                        DataToFloat(nf, data, 36),
                        DataToFloat(nf, data, 40),
                        DataToFloat(nf, data, 44),
                        DataToFloat(nf, data, 48),
                        DataToFloat(nf, data, 52),
                        DataToFloat(nf, data, 56),
                        DataToFloat(nf, data, 60),
                });
        result.transpose();
        return result;
    }
}
