package com.aflyingcar.warring_states.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerUtils {
    /**
     * Helper function to get a player by their UUID
     * May only be used on the Server side
     * @param uuid The {@code UUID} of the player to search for
     * @return The {@code EntityPlayerMP} that corresponds to this {@code UUID}, or null if the {@code UUID} either
     *         does not correspond to a real {@code EntityPlayerMP}, or if said player is not currently online.
     */
    @Nullable
    @SideOnly(Side.SERVER)
    public static EntityPlayerMP getPlayerByUUID(UUID uuid) {
        MinecraftServer mc = FMLCommonHandler.instance().getMinecraftServerInstance();

        return mc.getPlayerList().getPlayerByUUID(uuid);
    }

    /**
     * Gets the GameProfile of the given Player UUID if one exists.
     * @param uuid The UUID of the player to get the GameProfile of
     * @return The GameProfile of the player's UUID, or null if no such profile exists.
     */
    @Nullable
    public static GameProfile getProfileByUUID(UUID uuid) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getProfileByUUID(uuid);
    }

    /**
     * Gets the username of the given player from their UUID, or null if no such player UUID exists.
     * @param uuid The UUID of the player to get the username of.
     * @return The username of the player, or null if either the player is not logged in or if the player does not have a profile.
     */
    @Nullable
    @SideOnly(Side.SERVER)
    public static String getPlayerNameFromUUID(UUID uuid) {
        EntityPlayerMP player = getPlayerByUUID(uuid);
        if(player != null) {
            return player.getName();
        } else {
            GameProfile profile = getProfileByUUID(uuid);
            return profile == null ? null : profile.getName();
        }
    }

    public static boolean isPlayerOnline(UUID playerID) {
        return getPlayerByUUID(playerID) == null;
    }
}
