package com.aflyingcar.warring_states.handlers;

import com.aflyingcar.warring_states.WarringStatesConfig;
import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.WarringStatesNetwork;
import com.aflyingcar.warring_states.WarringStatesPotions;
import com.aflyingcar.warring_states.api.IWarGoal;
import com.aflyingcar.warring_states.network.messages.WarDeclaredMessage;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.Timer;
import com.aflyingcar.warring_states.war.Conflict;
import com.aflyingcar.warring_states.war.WarManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;

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

            State playerState = StateManager.getInstance().getStateFromPlayer(mpPlayer);

            // Only apply this potion effect if the war has just started.
            if(playerState != null && WarManager.getInstance().getAllConflictsInvolving(playerState).stream().anyMatch(c -> c.getWarStartTimer().getNumberOfSeconds() < 5))
                mpPlayer.addPotionEffect(new PotionEffect(WarringStatesPotions.POTION_SLOW_FALLING, (int)Timer.toTicksFromSeconds(15)));

            mpPlayer.sendMessage(new TextComponentTranslation("warring_states.messages.player_stop_flying"));

            // TODO: Do we need to do anything else to players during a war?
            // TODO: Totem of Undying???
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        State state = StateManager.getInstance().getStateFromPlayer(event.player);

        if(state != null) {
            state.stopDecayTimer();

            List<Conflict> conflicts = WarManager.getInstance().getAllConflictsInvolving(state);
            callMethodForEveryConflict(event.player, state, conflicts, IWarGoal::onPlayerLogin);
        }

        for(Conflict war : WarManager.getInstance().getAllConflicts()) {
            WarringStatesNetwork.NETWORK.sendTo(new WarDeclaredMessage(war), (EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        State state = StateManager.getInstance().getStateFromPlayer(event.player);

        if(state != null) {
            if(state.getCitizens().stream().noneMatch(WarringStatesMod.proxy::isPlayerOnline)) {
                state.startDecayTimer();
            }

            List<Conflict> conflicts = WarManager.getInstance().getAllConflictsInvolving(state);
            callMethodForEveryConflict(event.player, state, conflicts, IWarGoal::onPlayerLogout);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if(!(entity instanceof EntityPlayer)) {
            return;
        }

        State state = StateManager.getInstance().getStateFromPlayer((EntityPlayer)entity);

        // This does not matter if the player is not a part of a state
        if(state == null) return;

        List<Conflict> conflicts = WarManager.getInstance().getAllConflictsInvolving(state);

        callMethodForEveryConflict((EntityPlayer)entity, state, conflicts, IWarGoal::onPlayerDeath);
    }

    private static void callMethodForEveryConflict(EntityPlayer player, State state, List<Conflict> conflicts, TriConsumer<IWarGoal, EntityPlayer, Conflict.Side> consumer) {
        conflicts.stream().map(c -> Pair.of(c.getWargoals(), c.getSideOf(state))).distinct().forEach(gss -> gss.getLeft().forEach(g -> consumer.accept(g, player, gss.getRight())));
    }
}
