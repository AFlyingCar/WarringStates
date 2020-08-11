package com.aflyingcar.warring_states.handlers;

import com.aflyingcar.warring_states.WarringStatesConfig;
import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.war.WarManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = WarringStatesMod.MOD_ID)
public class PlayerEventsHandler {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Do nothing on the client
        if(event.side == Side.CLIENT) return;

        EntityPlayer player = event.player;

        // Do nothing on the client-side, or if the player is creative or spectator, or if flight can never be disabled
        if(player.world.isRemote || player.isCreative() || player.isSpectator() || !WarringStatesConfig.disableFlightDuringWar)
            return;

        // We know by now that flight _must_ be disabled during war, so no need to ever check it again after this point

        // We also know that the player _must_ be *MP, since isRemote was false
        EntityPlayerMP mpPlayer = (EntityPlayerMP)player;

        if(WarringStatesMod.proxy.isFlying(mpPlayer) && WarManager.getInstance().isAtWar(mpPlayer)) {
            WarringStatesMod.proxy.stopFlying(mpPlayer);

            mpPlayer.addPotionEffect(new PotionEffect(Objects.requireNonNull(Potion.getPotionFromResourceLocation("feather_falling"))));

            mpPlayer.sendMessage(new TextComponentString(I18n.format("warring_states.messages.player_stop_flying")));

            // TODO: Do we need to do anything else to players during a war?
            // TODO: Totem of Undying???
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        State state = StateManager.getInstance().getStateFromPlayer(event.player);

        if(state != null) {
            state.stopDecayTimer();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        State state = StateManager.getInstance().getStateFromPlayer(event.player);

        if(state != null) {
            if(state.getCitizens().stream().noneMatch(WarringStatesMod.proxy::isPlayerOnline)) {
                state.startDecayTimer();
            }
        }
    }
}
