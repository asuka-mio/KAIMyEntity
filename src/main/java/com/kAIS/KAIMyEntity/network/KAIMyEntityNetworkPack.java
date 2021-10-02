package com.kAIS.KAIMyEntity.network;

import com.kAIS.KAIMyEntity.register.KAIMyEntityRegisterCommon;
import com.kAIS.KAIMyEntity.renderer.KAIMyEntityRendererPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class KAIMyEntityNetworkPack {
    public int opCode;
    public UUID playerUUID;
    public int arg0;

    public KAIMyEntityNetworkPack(int opCode, UUID playerUUID, int arg0) {
        this.opCode = opCode;
        this.playerUUID = playerUUID;
        this.arg0 = arg0;
    }

    public KAIMyEntityNetworkPack(PacketBuffer buffer) {
        opCode = buffer.readInt();
        playerUUID = new UUID(buffer.readLong(), buffer.readLong());
        arg0 = buffer.readInt();
    }

    public void Pack(PacketBuffer buffer) {
        buffer.writeInt(opCode);
        buffer.writeLong(playerUUID.getMostSignificantBits());
        buffer.writeLong(playerUUID.getLeastSignificantBits());
        buffer.writeInt(arg0);
    }

    public void Do(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                {
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        DoInClient();
                    } else {
                        KAIMyEntityRegisterCommon.channel.send(PacketDistributor.ALL.noArg(), this);
                    }
                }
        );
        ctx.get().setPacketHandled(true);
    }

    public void DoInClient() {
        //Ignore message when player is self.
        assert Minecraft.getInstance().player != null;
        if (playerUUID.equals(Minecraft.getInstance().player.getUniqueID()))
            return;
        switch (opCode) {
            case 1: {
                assert Minecraft.getInstance().world != null;
                PlayerEntity target = Minecraft.getInstance().world.getPlayerByUuid(playerUUID);
                if (KAIMyEntityRendererPlayer.GetInst() != null && target != null)
                    KAIMyEntityRendererPlayer.GetInst().CustomAnim(target, Integer.toString(arg0));
                break;
            }
            case 2: {
                assert Minecraft.getInstance().world != null;
                PlayerEntity target = Minecraft.getInstance().world.getPlayerByUuid(playerUUID);
                if (KAIMyEntityRendererPlayer.GetInst() != null && target != null)
                    KAIMyEntityRendererPlayer.GetInst().ResetPhysics(target);
                break;
            }
        }
    }
}
