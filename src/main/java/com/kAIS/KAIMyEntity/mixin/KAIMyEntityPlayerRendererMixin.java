package com.kAIS.KAIMyEntity.mixin;

import com.kAIS.KAIMyEntity.NativeFunc;
import com.kAIS.KAIMyEntity.renderer.IMMDModel;
import com.kAIS.KAIMyEntity.renderer.MMDAnimManager;
import com.kAIS.KAIMyEntity.renderer.MMDModelManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(PlayerEntityRenderer.class)
public abstract class KAIMyEntityPlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

	public KAIMyEntityPlayerRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
		super(ctx, model, shadowRadius);
	}

	@Inject(method = {"render"}, at = @At("HEAD"))
	public void render(AbstractClientPlayerEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, VertexConsumerProvider vertexConsumers, int packedLightIn, CallbackInfo ci) {
		IMMDModel model = null;
		MMDModelManager.Model m = MMDModelManager.GetPlayerModel("EntityPlayer_" + entityIn.getName().getString());
		if (m == null)
			m = MMDModelManager.GetPlayerModel("EntityPlayer");
		if (m != null)
			model = m.model;

		MMDModelManager.ModelWithPlayerData mwpd = (MMDModelManager.ModelWithPlayerData) m;

		if (model != null) {
			if (!mwpd.playerData.playCustomAnim) {
				//Layer 0
				if (entityIn.getHealth() == 0.0f) {
					AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityState.Die, 0);
				} else if (entityIn.isFallFlying()) {
					AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityState.ElytraFly, 0);
				} else if (entityIn.isSleeping()) {
					AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityState.Sleep, 0);
				} else if (entityIn.hasVehicle()) {
					AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityState.Ride, 0);
				} else if (entityIn.isSwimming()) {
					AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityState.Swim, 0);
				} else if (entityIn.isClimbing()) {
					AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityState.OnLadder, 0);
				} else if (entityIn.isSprinting()) {
					AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityState.Sprint, 0);
				} else if (entityIn.getX() - entityIn.prevX != 0.0f || entityIn.getZ() - entityIn.prevZ != 0.0f) {
					AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityState.Walk, 0);
				} else {
					AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityState.Idle, 0);
				}

				//Layer 1
				if (entityIn.getActiveItem() != ItemStack.EMPTY) {
					if (entityIn.getActiveHand() == Hand.MAIN_HAND) {
						CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityState.ItemRight, Objects.requireNonNull(entityIn.getActiveItem().getItem().getName()).toString().replace(':', '.'), false);
					} else {
						CustomItemActiveAnim(mwpd, MMDModelManager.PlayerData.EntityState.ItemLeft, Objects.requireNonNull(entityIn.getActiveItem().getItem().getName()).toString().replace(':', '.'), true);
					}
				} else if (entityIn.handSwinging) {
					if (entityIn.preferredHand == Hand.MAIN_HAND) {
						AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityState.SwingRight, 1);
					} else if (entityIn.preferredHand == Hand.OFF_HAND) {
						AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityState.SwingLeft, 1);
					}
				} else {
					AnimStateChangeOnce(mwpd,MMDModelManager.PlayerData.EntityState.Idle,1);
				}

				//Layer 2
				if (entityIn.isSneaking()) {
					AnimStateChangeOnce(mwpd, MMDModelManager.PlayerData.EntityState.Sneak, 2);
				} else {
					AnimStateChangeOnce(mwpd,MMDModelManager.PlayerData.EntityState.Idle,2);
				}
			}

			model.Render(entityYaw, matrixStackIn, packedLightIn);

			NativeFunc nf = NativeFunc.GetInst();
			nf.GetRightHandMat(model.GetModelLong(), mwpd.playerData.rightHandMat);
			matrixStackIn.push();
			MinecraftClient.getInstance().getItemRenderer().renderItem(entityIn, entityIn.getMainHandStack(), ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND, false, matrixStackIn,vertexConsumers,entityIn.world,packedLightIn, OverlayTexture.DEFAULT_UV, 0);
			matrixStackIn.pop();

			nf.GetLeftHandMat(model.GetModelLong(), mwpd.playerData.leftHandMat);
			matrixStackIn.push();
			MinecraftClient.getInstance().getItemRenderer().renderItem(entityIn,entityIn.getOffHandStack(),ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND,true,matrixStackIn,vertexConsumers,entityIn.world,packedLightIn,OverlayTexture.DEFAULT_UV,0);
			matrixStackIn.pop();
		}
		ci.cancel();//Added By FMyuchuan. | 隐藏模型脚下的史蒂夫
	}

	void AnimStateChangeOnce(MMDModelManager.ModelWithPlayerData model, MMDModelManager.PlayerData.EntityState targetState, Integer layer) {
		String Property = MMDModelManager.PlayerData.stateProperty.get(targetState);
		if (model.playerData.stateLayers[layer] != targetState) {
			model.playerData.stateLayers[layer] = targetState;
			model.model.ChangeAnim(MMDAnimManager.GetAnimModel(model.model, Property), layer);
		}
	}

	void CustomItemActiveAnim(MMDModelManager.ModelWithPlayerData model, MMDModelManager.PlayerData.EntityState targetState, String itemName, boolean isLeftHand) {
		long anim = MMDAnimManager.GetAnimModel(model.model, String.format("itemActive_%s_%s", itemName, isLeftHand ? "left" : "right"));
		if (anim != 0) {
			if (model.playerData.stateLayers[1] != targetState) {
				model.playerData.stateLayers[1] = targetState;
				model.model.ChangeAnim(anim, 1);
			}
		}
	}
}
