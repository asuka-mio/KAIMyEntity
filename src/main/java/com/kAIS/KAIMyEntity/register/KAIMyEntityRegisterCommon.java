package com.kAIS.KAIMyEntity.register;

import com.kAIS.KAIMyEntity.network.KAIMyEntityNetworkPack;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class KAIMyEntityRegisterCommon
{
    public static void Regist()
    {
        channel.registerMessage(KAIMyEntityNetworkPack.PackHandler.class, KAIMyEntityNetworkPack.class, 0, Side.CLIENT);
        channel.registerMessage(KAIMyEntityNetworkPack.PackHandler.class, KAIMyEntityNetworkPack.class, 1, Side.SERVER);
    }

    public static SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel("kaimyentity");
}
