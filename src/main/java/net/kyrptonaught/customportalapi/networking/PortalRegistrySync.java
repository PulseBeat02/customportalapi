package net.kyrptonaught.customportalapi.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.PerWorldPortals;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PortalRegistrySync {
    public static void registerSyncOnPlayerJoin() {
        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
            for (PortalLink link : CustomPortalApiRegistry.getAllPortalLinks()) {
                packetSender.sendPacket(createPacket(link));
            }
        });
    }

    public static void syncLinkToAllPlayers(PortalLink link, MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            syncLinkToPlayer(link, player);
        }
    }

    public static void syncLinkToPlayer(PortalLink link, ServerPlayerEntity player) {
        player.networkHandler.sendPacket(createPacket(link));
    }

    public static Packet<?> createPacket(PortalLink link) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(link.block);
        buf.writeIdentifier(link.dimID);
        buf.writeInt(link.colorID);
        return ServerPlayNetworking.createS2CPacket(NetworkManager.SYNC_PORTALS, buf);
    }
}
