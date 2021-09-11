package com.kAIS.KAIMyEntity.network;

import com.kAIS.KAIMyEntity.register.KAIMyEntityRegisterCommon;
import com.kAIS.KAIMyEntity.renderer.KAIMyEntityRendererPlayer;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class KAIMyEntityNetworkPack implements IMessage
{
    public static class PackHandler implements IMessageHandler<KAIMyEntityNetworkPack, IMessage>
    {
        @Override
        public IMessage onMessage(KAIMyEntityNetworkPack message, MessageContext ctx)
        {
            if (ctx.side == Side.CLIENT)
            {
                DoInClient(message);
            }
            else
            {
                KAIMyEntityRegisterCommon.channel.sendToAll(message);
            }
            return null;
        }

        public void DoInClient(KAIMyEntityNetworkPack message)
        {
            Minecraft.getMinecraft().addScheduledTask(() ->
                    {
                            //Ignore message when player is self.
                            //Note that when player die, player will be null.
                            if (Minecraft.getMinecraft().player != null && message.playerUUID.equals(Minecraft.getMinecraft().player.getUniqueID()))
                                return;
                            switch (message.opCode)
                            {
                                case 1:
                                {
                                    EntityPlayer target = Minecraft.getMinecraft().world.getPlayerEntityByUUID(message.playerUUID);
                                    if (KAIMyEntityRendererPlayer.GetInst() != null && target != null)
                                        KAIMyEntityRendererPlayer.GetInst().CustomAnim(target, Integer.toString(message.arg0));
                                    break;
                                }
                                case 2:
                                {
                                    EntityPlayer target = Minecraft.getMinecraft().world.getPlayerEntityByUUID(message.playerUUID);
                                    if (KAIMyEntityRendererPlayer.GetInst() != null && target != null)
                                        KAIMyEntityRendererPlayer.GetInst().ResetPhysics(target);
                                    break;
                                }
                            }
                    });
        }
    }

    //OpCode:
    //1: customAnimation arg0
    //2: resetPhysics
    public int opCode;
    public UUID playerUUID;
    public int arg0;

    public KAIMyEntityNetworkPack()
    {
        this.opCode = 0;
        this.playerUUID = new UUID(0, 0);
        this.arg0 = 0;
    }

    public KAIMyEntityNetworkPack(int opCode, UUID playerUUID, int arg0)
    {
        this.opCode = opCode;
        this.playerUUID = playerUUID;
        this.arg0 = arg0;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        opCode = buf.readInt();
        long ml = buf.readLong();
        long ll = buf.readLong();
        playerUUID = new UUID(ml, ll);
        arg0 = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(opCode);
        buf.writeLong(playerUUID.getMostSignificantBits());
        buf.writeLong(playerUUID.getLeastSignificantBits());
        buf.writeInt(arg0);
    }
}
