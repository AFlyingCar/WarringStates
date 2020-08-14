package com.aflyingcar.warring_states.handlers;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.items.ItemWargoalClaimer;
import com.aflyingcar.warring_states.util.GuiUtils;
import com.aflyingcar.warring_states.war.goals.StealChunkWarGoal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = WarringStatesMod.MOD_ID)
public class ItemEventsHandler {
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onGetItemTooltip(ItemTooltipEvent event) {
        EntityPlayer player = event.getEntityPlayer();

        if(event.getItemStack().getItem() instanceof ItemWargoalClaimer) {
            if(!StealChunkWarGoal.canWargoalBeDeclared(player)) {
                event.getToolTip().add(GuiUtils.translate("warring_states.tooltip.not_enough_experience"));
            }
        }

        /*
        TODO: This event is fired on the client-side only, but we need access to the WarManager, which only exists on the
              SERVER. Re-implement this later after we have an easy way to get simple data back down from the server


        // TODO: This will currently fire on _all_ items
        //  We should only do this on items that allow flight
        //  How do we check that??
        if(player != null && WarringStatesConfig.disableFlightDuringWar && WarManager.getInstance().isAtWar(player)) {
            event.getToolTip().add(TextFormatting.RED + I18n.format("warring_states.tooltip.war_flight_disabled"));
        }
         */
    }
}
