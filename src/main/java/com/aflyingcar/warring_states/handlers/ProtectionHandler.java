package com.aflyingcar.warring_states.handlers;

import com.aflyingcar.warring_states.WarringStatesConfig;
import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.api.WarringStatesAPI;
import com.aflyingcar.warring_states.blocks.BlockClaimer;
import com.aflyingcar.warring_states.client.gui.GuiID;
import com.aflyingcar.warring_states.events.TerritoryClaimedEvent;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.tileentities.TileEntityClaimer;
import com.aflyingcar.warring_states.util.*;
import com.aflyingcar.warring_states.war.Conflict;
import com.aflyingcar.warring_states.war.WarManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = WarringStatesMod.MOD_ID)
public class ProtectionHandler {
    private static void saveChangeForRollback(@Nonnull Conflict conflict, @Nonnull ExtendedBlockPos position, @Nonnull IBlockState blockState, @Nullable TileEntity tileEntity) {
        conflict.saveChangeForRollback(new RestorableBlock(position, blockState, tileEntity));
    }

    private static void saveChangeForRollback(@Nonnull Conflict conflict, @Nonnull ExtendedBlockPos position, @Nonnull IBlockState originalState, @Nullable TileEntity originalTileEntity, @Nonnull IBlockState placedBlockState) {
        conflict.saveChangeForRollback(new RestorableBlock(position, originalState, originalTileEntity, placedBlockState));
    }

    @SubscribeEvent
    public static void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        if(event.getWorld().isRemote) return;

        State owningState = StateManager.getInstance().getStateAtPosition(event.getWorld(), event.getPos());

        // Don't worry about doing anything outside of claimed territory
        if(owningState == null) return;

        List<Conflict> conflicts = WarManager.getInstance().getAllConflictsInvolving(owningState);

        // If there are no conflicts currently going on with this state, do nothing
        if(conflicts.isEmpty()) {
            return;
        }

        for(Conflict conflict : conflicts) {
            List<RestorableBlock> readOnlyRestorables = conflict.getRestorableBlocks();

            // There should only be one restoration operation at this position, so lets find it
            for(RestorableBlock block : readOnlyRestorables) {
                if(!block.isCurrentlyRestoring() && block.getPos().equals(event.getPos())) {
                    // Is this a person doing the harvesting?
                    if(event.getHarvester() != null) {
                        // Is this the result of somebody placing a block earlier?
                        //   Also, did this change to being a breakage happen recently?
                        if(!block.wasPreviouslyReplacement()) {
                            // If this was a block that was previously here, then do not allow blocks to drop
                            event.getDrops().clear();
                        }
                    } else {
                        event.getDrops().clear();
                    }
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if(event.getWorld().isRemote) return;

        EntityPlayer player = event.getPlayer();

        if(player.isCreative() && WarringStatesConfig.allowCreativeToIgnoreProtections) {
            return;
        }

        // TODO: Check if block placed is a BlockClaimer. If it is, make sure that they have permission to claim territory

        if(!WarringStatesAPI.doesPlayerHavePermissionForAction(event.getWorld(), player.getPersistentID(), event.getPos(), CitizenPrivileges.MODIFY_BLOCKS))
        {
            player.sendMessage(new TextComponentTranslation("warring_states.messages.break_block_permission_denied"));
            if(WarringStatesConfig.shouldBlockBreakageBeBlocked)
                event.setCanceled(true);
        } else {
            // If we are at war with this state, then we will allow it if no predicate says otherwise
            State owningState = StateManager.getInstance().getStateAtPosition(event.getWorld(), event.getPos());

            // But first, if somebody owns this position, and the player is not part of the that state, then they are only
            //  allowed to modify blocks if they are a part of a conflict that this state is in
            if(owningState != null && !owningState.hasCitizen(player.getPersistentID())) {
                int dimension = WorldUtils.getDimensionIDForWorld((WorldServer) event.getWorld());
                Pair<Integer, Conflict> currentConflict = WarManager.getInstance().getConflictBetween(player, owningState);
                if(currentConflict != null) {
                    ExtendedBlockPos extendedPosition = new ExtendedBlockPos(event.getPos(), dimension);
                    if(WarManager.getInstance().shouldBlockBreakBeIgnored(extendedPosition)) {
                        // Do not break this block even though we are at war, because a predicate says so
                        event.setCanceled(true);
                    } else {
                        if(WarringStatesConfig.shouldChangesBeRolledBack) {
                            // Save the original block so that we can roll it back once the war ends
                            IBlockState blockState = event.getWorld().getBlockState(event.getPos());
                            TileEntity entity = event.getWorld().getTileEntity(event.getPos());
                            saveChangeForRollback(currentConflict.getRight(), extendedPosition, blockState, entity);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if(event.getWorld().isRemote) return;

        Entity entity = event.getEntity();

        // Check if block placed is a BlockClaimer, and then make sure that they have permission to claim territory here
        Block placedBlock = event.getPlacedBlock().getBlock();
        if(placedBlock instanceof BlockClaimer) {
            WarringStatesMod.getLogger().info("ProtectionHandler#onBlockPlace[BlockClaimer]");

            // Do not allow non-players to place Claimers
            if(!(entity instanceof EntityPlayer)) {
                WarringStatesMod.getLogger().info("Non-players may not place BlockClaimers. Blocking EntityPlaceEvent.");
                event.setCanceled(true);
                return;
            }

            BlockPos position = event.getPos();
            State owner = StateManager.getInstance().getStateAtPosition(event.getWorld(), position);

            // You are not allowed to claim territory that somebody else has already claimed.
            if(owner != null) {
                WarringStatesMod.getLogger().info("Chunk already claimed: blocking EntityPlaceEvent for [BlockClaimer]");
                entity.sendMessage(new TextComponentTranslation("warring_states.messages.place_block.chunk_claimed", owner.getName()));
                if(WarringStatesConfig.shouldBlockPlacementBeBlocked)
                    event.setCanceled(true);
            } else {
                State state = StateManager.getInstance().getStateFromPlayer((EntityPlayer)entity);

                if(state != null) {
                    // Player is a part of a state, so they must have permission to claim territory for that state
                    if(WarringStatesAPI.doesPlayerHavePermissionForAction(event.getWorld(), entity.getPersistentID(), entity.getPosition(), CitizenPrivileges.CLAIM_TERRITORY)) {
                        TileEntity claimer = event.getWorld().getTileEntity(position);

                        if(claimer instanceof TileEntityClaimer) {
                            ((TileEntityClaimer)claimer).changeOwner(state);
                        } else {
                            WarringStatesMod.getLogger().error("Missing TileEntityClaimer at " + position + ". Cannot claim chunk without it!");
                            event.setCanceled(true);
                        }

                        MinecraftForge.EVENT_BUS.post(new TerritoryClaimedEvent(state, event.getWorld(), position));
                    } else {
                        WarringStatesMod.getLogger().info("Blocking EntityPlaceEvent for non privileged citizen for BlockClaimer");
                        entity.sendMessage(new TextComponentTranslation("warring_states.messages.claim_territory.permission_denied", state.getName()));
                        if(WarringStatesConfig.shouldBlockPlacementBeBlocked)
                            event.setCanceled(true);
                    }
                } else {
                    // Player is not part of a state, so they may form a new state
                    GuiUtils.openGUI((EntityPlayer)entity, position, GuiID.STATE_CREATION_GUI);
                }
            }
        } else {
            // All non-players have permission to place blocks
            if(entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer)entity;
                if(player.isCreative() && WarringStatesConfig.allowCreativeToIgnoreProtections) {
                    return;
                }

                if(!WarringStatesAPI.doesPlayerHavePermissionForAction(event.getWorld(), entity.getPersistentID(), event.getPos(), CitizenPrivileges.MODIFY_BLOCKS)) {
                    WarringStatesMod.getLogger().info("Blocking EntityPlaceEvent: Player does not have permission to place blocks here as it is already claimed");
                    entity.sendMessage(new TextComponentTranslation("warring_states.messages.place_block.permission_denied"));
                    if(WarringStatesConfig.shouldBlockPlacementBeBlocked)
                        event.setCanceled(true);
                } else {
                    // If we are at war with this state, then we will allow it if no predicate says otherwise
                    State owningState = StateManager.getInstance().getStateAtPosition(event.getWorld(), event.getPos());

                    // But first, if somebody owns this position, and the player is not part of the that state, then they are only
                    //  allowed to place blocks if they are a part of a conflict that this state is in
                    if(owningState != null && !owningState.hasCitizen(entity.getPersistentID())) {
                        int dimension = WorldUtils.getDimensionIDForWorld((WorldServer) event.getWorld());
                        Pair<Integer, Conflict> currentConflict = WarManager.getInstance().getConflictBetween(player, owningState);
                        if(currentConflict != null) {
                            ExtendedBlockPos extendedPosition = new ExtendedBlockPos(event.getPos(), dimension);
                            if(WarManager.getInstance().shouldBlockPlaceBeIgnored(extendedPosition)) {
                                // Do not break this block even though we are at war, because a predicate says so
                                event.setCanceled(true);
                            } else {
                                if(WarringStatesConfig.shouldChangesBeRolledBack) {
                                    // Save the original block so that we can roll it back once the war ends
                                    IBlockState blockState = event.getBlockSnapshot().getReplacedBlock();
                                    TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
                                    saveChangeForRollback(currentConflict.getRight(), extendedPosition, blockState, tileEntity, event.getPlacedBlock());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBucketFillOrBucketEmpty(FillBucketEvent event) {
        RayTraceResult target = event.getTarget();

        // If target is null, then the bucket is not being filled
        if(target == null) return;

        BlockPos fillPos = WorldUtils.offsetBlockPos(target.getBlockPos(), target.sideHit);

        State owningState = StateManager.getInstance().getStateAtPosition(event.getWorld(), fillPos);

        // No protections outside of claimed territory
        if(owningState == null) return;

        Entity placer = event.getEntity();

        if(placer instanceof EntityPlayer) {
            IBlockState blockState = event.getWorld().getBlockState(fillPos);

            if(!owningState.hasCitizen(placer.getPersistentID())) {
                Pair<Integer, Conflict> conflict = WarManager.getInstance().getConflictBetween((EntityPlayer)placer, owningState);
                if(conflict == null) {
                    event.setCanceled(true);
                    WorldUtils.notifySurroundingBlocksOfStateChange(event.getWorld(), fillPos, blockState);
                } else {
                    TileEntity tileEntity = event.getWorld().getTileEntity(fillPos);
                    ExtendedBlockPos extendedPos = WorldUtils.getExtendedPosition(event.getWorld(), fillPos);
                    saveChangeForRollback(conflict.getRight(), extendedPos, blockState, tileEntity);
                }
            } else if(!owningState.hasPrivilege(placer.getPersistentID(), CitizenPrivileges.MODIFY_BLOCKS)) {
                event.setCanceled(true);
                WorldUtils.notifySurroundingBlocksOfStateChange(event.getWorld(), fillPos, blockState);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickBlock event) {
        if(event.getWorld().isRemote) return;

        EntityPlayer player = event.getEntityPlayer();

        if(player.isCreative() && WarringStatesConfig.allowCreativeToIgnoreProtections) {
            return;
        }

        // You can interact with the claimer regardless of privilege, and we will let it determine what you can do with it
        if(event.getWorld().getTileEntity(event.getPos()) instanceof TileEntityClaimer) {
            return;
        }

        // If they are trying to place a block, then return, and let this be handled by the PlaceBlock event instead
        if(player.getHeldItem(event.getHand()).getItem() instanceof ItemBlock) {
            // But we also need a special handler in case they are holding a block and are trying to interact with something
            //  that can be activated
            // Basically, if they normally would be allowed to place a block here, then return and let onBlockPlace handle it
            BlockPos pos = event.getPos();
            boolean bypass = player.getHeldItemMainhand().doesSneakBypassUse(event.getWorld(), pos, player) && player.getHeldItemOffhand().doesSneakBypassUse(event.getWorld(), pos, player);
            if(player.isSneaking() && !bypass) {
                ItemBlock itemblock = (ItemBlock)event.getItemStack().getItem();
                Block toPlace = itemblock.getBlock();
                IBlockState state = event.getWorld().getBlockState(pos);
                // You can place a block at the point you are aiming at if the block that is currently there is replaceable,
                //   and if the block you are trying to place _says_ you can place it there
                if(state.getBlock().isReplaceable(event.getWorld(), pos) && toPlace.canPlaceBlockOnSide(event.getWorld(), pos, event.getFace())) {
                    return;
                }
            }
        }

        if(!WarringStatesAPI.doesPlayerHavePermissionForAction(event.getWorld(), player.getPersistentID(), event.getPos(), event.getFace(),
                                                               CitizenPrivileges.INTERACT))
        {
            player.sendMessage(new TextComponentTranslation("warring_states.messages.interact_block_permission_denied"));
            if(WarringStatesConfig.shouldBlockInteractionBeBlocked) {
                event.setCanceled(true);
                WorldUtils.notifySurroundingBlocksOfStateChange(event.getWorld(), event.getPos(), event.getWorld().getBlockState(WorldUtils.offsetBlockPos(event.getPos(), event.getFace())));
            }
        } else {
            State owningState = StateManager.getInstance().getStateAtPosition(event.getWorld(), event.getPos());

            // If no state owns this position, then just return
            if(owningState == null) {
                return;
            }

            // If you are a citizen of this state, then you are allowed to do this regardless
            //  Note that we have already verified earlier that you have permissions to do this action, so there is no
            //  need to check that again
            if(owningState.hasCitizen(player.getPersistentID())) {
                return;
            }

            // If we are not at war with this state, or if we should protect tile entity interactions even during a war,
            //  then cancel the event
            if(!WarManager.getInstance().isAtWarWith(player, owningState) || WarringStatesConfig.shouldTileEntityInteractionsBeIgnoredDuringWar) {
                player.sendMessage(new TextComponentTranslation("warring_states.messages.interact_block_permission_denied"));
                event.setCanceled(true);
                WorldUtils.notifySurroundingBlocksOfStateChange(event.getWorld(), event.getPos(), event.getWorld().getBlockState(event.getPos()));
            }
        }
    }

    @SubscribeEvent
    public static void onTrample(BlockEvent.FarmlandTrampleEvent event) {
        Entity trampler = event.getEntity();
        BlockPos cropsPos = event.getPos();

        State owningState = StateManager.getInstance().getStateAtPosition(event.getWorld(), cropsPos);

        // No protections for crops outside of claimed territory
        if(owningState == null) return;

        if(trampler instanceof EntityPlayer) {
            // No protections for if you damage your own crops
            if(!owningState.hasCitizen(trampler.getPersistentID())) {
                // If you are at war with this state, allow trampling the crops
                Pair<Integer, Conflict> conflict = WarManager.getInstance().getConflictBetween((EntityPlayer)trampler, owningState);
                if(conflict != null) {
                    if(WarringStatesConfig.enableExperimentalCropRollbackSupport) {
                        IBlockState farmlandState = event.getState();
                        IBlockState plantsState = event.getWorld().getBlockState(cropsPos.up());

                        // Save the state of the farmland
                        saveChangeForRollback(conflict.getRight(), WorldUtils.getExtendedPosition(event.getWorld(), cropsPos), farmlandState, null);

                        // Save the state of the plants on top if there are any
                        if(plantsState.getBlock() instanceof IPlantable) {
                            saveChangeForRollback(conflict.getRight(), WorldUtils.getExtendedPosition(event.getWorld(), cropsPos.up()), plantsState, null);
                        }
                    } else if(WarringStatesConfig.shouldCropsBeProtectedEvenDuringWar) {
                        event.setCanceled(true);
                        event.getWorld().notifyBlockUpdate(cropsPos, event.getState(), event.getState(), 2);
                    }
                } else {
                    event.setCanceled(true);
                    event.getWorld().notifyBlockUpdate(cropsPos, event.getState(), event.getState(), 2);
                }
            }
        } else {
            event.setCanceled(true);
            event.getWorld().notifyBlockUpdate(cropsPos, event.getState(), event.getState(), 2);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        Entity attacker = source.getTrueSource(); // The one who actually did the naughty
        EntityLivingBase target = event.getEntityLiving(); // The one who is about to possibly have a bad time

        if(target.world.isRemote) return;

        State owningState = StateManager.getInstance().getStateAtPosition(target.world, target.getPosition());

        // No protection outside of claimed territory
        if(owningState == null) {
            return;
        }

        if(attacker instanceof EntityPlayer) {
            // If the attacker was a player
            State attackerState = StateManager.getInstance().getStateFromPlayer((EntityPlayer)attacker);

            if(WarManager.getInstance().isAtWarWith((EntityPlayer)attacker, owningState)) {
                // You can only harm other players if they are currently at war with your state
                // Anything that is not a player is fair game while you are at war here though
                //  so long as that entity doesn't have an owner you are at war with
                if(target instanceof EntityPlayer) {
                    if(!WarManager.getInstance().isAtWarWith((EntityPlayer)target, attackerState)) {
                        event.setCanceled(true);
                    }
                } else if(target instanceof IEntityOwnable){
                    // The target is ownable, so make sure we aren't at war with their owner
                    Entity owner = ((IEntityOwnable)target).getOwner();

                    if(owner instanceof EntityPlayer) {
                        if(!WarManager.getInstance().isAtWarWith((EntityPlayer)owner, attackerState)) {
                            event.setCanceled(true);
                        }
                    }
                }
            } else {
                // The attacker is not at war with the owner of this territory, so make sure they cannot damage players
                //  here
                // Even if the target is a citizen of a state attacker is at war with, as this would be neutral territory
                // However, anything that is not a player or not owned by a player we are at war with is fair game to hurt
                if(target instanceof EntityPlayer) {
                    // Do not damage players the attacker is not at war with
                    event.setCanceled(true);
                } else if(target instanceof IEntityOwnable) {
                    // The target is ownable, so make sure we aren't at war with their owner
                    Entity owner = ((IEntityOwnable)target).getOwner();

                    if(owner instanceof EntityPlayer) {
                        if(!WarManager.getInstance().isAtWarWith((EntityPlayer)owner, attackerState)) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        } else {
            // If the attacker was a mob
            if(!WarringStatesConfig.canMobsHarmInClaimedTerritory) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if(event.getWorld().isRemote) return;

        EntityLivingBase entity = event.getExplosion().getExplosivePlacedBy();
        BlockPos explosionPosition = new BlockPos(event.getExplosion().getPosition());

        List<BlockPos> affected = event.getAffectedBlocks();
        int dimension = WorldUtils.getDimensionIDForWorld((WorldServer)event.getWorld());

        for(Iterator<BlockPos> affectedPositionIter = affected.iterator(); affectedPositionIter.hasNext();) {
            BlockPos affectedPosition = affectedPositionIter.next();

            ExtendedBlockPos extendedPosition = new ExtendedBlockPos(affectedPosition, dimension);
            // Should we even bother checking everything else about if this block should be broken?
            if(WarManager.getInstance().shouldBlockBreakBeIgnored(extendedPosition)) {
                affectedPositionIter.remove();
                continue;
            }

            State owningState = StateManager.getInstance().getStateAtPosition(event.getWorld(), affectedPosition);

            if(entity instanceof EntityPlayer) {
                // If you do not have permission to modify blocks here, then remove
                if(WarringStatesConfig.shouldProtectAgainstExplosions && !WarringStatesAPI.doesPlayerHavePermissionForAction(event.getWorld(), entity.getPersistentID(), affectedPosition, CitizenPrivileges.MODIFY_BLOCKS)) {
                    affectedPositionIter.remove();
                } else {
                    // If we are at war, then save the change for rollback
                    Pair<Integer, Conflict> war = WarManager.getInstance().getConflictBetween((EntityPlayer)entity, owningState);
                    if(war != null) {
                        saveChangeForRollback(war.getRight(), extendedPosition, event.getWorld().getBlockState(affectedPosition), event.getWorld().getTileEntity(affectedPosition));
                    }
                }
            } else {
                // If we are at war, then it doesn't matter who we are at war with, prepare changes for rollback
                List<Conflict> wars = WarManager.getInstance().getAllConflictsInvolving(owningState);
                if(!wars.isEmpty()) {
                    // TODO: How do we save explosions for rollback?
                    // saveChangeForRollback();
                } else {
                    // We are not at war, so do not allow the effect to continue
                    affectedPositionIter.remove();
                }
            }
        }
    }
}

