package net.boatcrash.mixin;

import net.boatcrash.SharedVariables;
import net.boatcrash.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    @Inject(at = @At("HEAD"), method = "sendChatMessage", cancellable = true)
    public void sendChatMessage(String message, CallbackInfo ci) {
        if (message.startsWith("$boatcrash")) {
            try {
                if (message.split(" ")[1].equals("toggle")) {
                    if (mc.player.getVehicle() != null && mc.player.getVehicle() instanceof BoatEntity) {
                        SharedVariables.crashing = !SharedVariables.crashing;
                        sendMsg(SharedVariables.crashing ? "Starting crash..." : "Stopping crash...");
                        for (int i = 0; i < 100000; i++) {
                            if (SharedVariables.crashing) {
                                if (Util.nettyChannel() != null) {
                                    Vec3d prevPos = mc.player.getVehicle().getPos();
                                    mc.player.getVehicle().setPos(mc.player.getVehicle().getX() - 3, mc.player.getVehicle().getY() - 3, mc.player.getVehicle().getZ() - 3);
                                    Util.nettyChannel().writeAndFlush(new VehicleMoveC2SPacket(mc.player.getVehicle()));
                                    mc.player.getVehicle().setPos(prevPos.getX(), prevPos.getY(), prevPos.getZ());
                                    Util.nettyChannel().writeAndFlush(new VehicleMoveC2SPacket(mc.player.getVehicle()));
                                }
                            } else {
                                break;
                            }
                        }
                        SharedVariables.crashing = false;
                    } else {
                        sendErr("You need to be in a boat to use this.");
                    }
                } else {
                    sendErr("Incorrect usage, use §n$boatcrash toggle");
                }
            } catch (Exception e) {
                sendErr("Incorrect usage, use §n$boatcrash toggle");
                ci.cancel();
            }
            ci.cancel();
        }
    }

    @Inject(at = @At("TAIL"), method = "tick")
    public void tick(CallbackInfo ci) {
        if (mc.player.getVehicle() == null && !(mc.player.getVehicle() instanceof BoatEntity) && SharedVariables.crashing) {
            sendErr("You dismounted the boat, stopping crash...");
            SharedVariables.crashing = false;
        }
    }

    private static void sendMsg(String msg) {
        mc.player.sendMessage(Text.of(msg), false);
    }

    private static void sendErr(String msg) {
        mc.player.sendMessage(Text.of("§c" + msg), false);
    }
}
