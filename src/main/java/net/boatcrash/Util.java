package net.boatcrash;

import io.netty.channel.Channel;
import net.boatcrash.mixin.accessor.ClientConnectionAccessor;
import net.minecraft.client.MinecraftClient;

public class Util {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static Channel nettyChannel() {
        if (mc.player != null && mc.world != null && mc.getNetworkHandler() != null) {
            return ((ClientConnectionAccessor) mc.getNetworkHandler().getConnection()).getChannel();
        } else {
            return null;
        }
    }
}
