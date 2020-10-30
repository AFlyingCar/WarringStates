package com.aflyingcar.warring_states.tileentities;

import com.aflyingcar.warring_states.WarringStatesBlocks;
import com.aflyingcar.warring_states.WarringStatesConfig;
import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.api.IWarGoal;
import com.aflyingcar.warring_states.blocks.BlockClaimer;
import com.aflyingcar.warring_states.client.gui.GuiID;
import com.aflyingcar.warring_states.events.ChunkCaptureBeginEvent;
import com.aflyingcar.warring_states.events.ChunkCaptureCompleteEvent;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.GuiUtils;
import com.aflyingcar.warring_states.util.PredicateUtils;
import com.aflyingcar.warring_states.util.Timer;
import com.aflyingcar.warring_states.util.WorldUtils;
import com.aflyingcar.warring_states.war.Conflict;
import com.aflyingcar.warring_states.war.WarManager;
import com.aflyingcar.warring_states.war.goals.StealChunkWarGoal;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.energy.IEnergyStorage;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
public class TileEntityClaimer extends TileEntity implements IInventory, ITickable, IEnergyStorage {
    //////////////////
    // TO SERIALIZE //
    //////////////////
    private UUID stateID = null;
    private String stateName = "";
    private String stateDesc = "";

    private int health = getMaxHealth();

    ///////////////
    // TEMPORARY //
    ///////////////

    private boolean readRequired = true;
    private Timer captureTimer; // Time since capture started. Only needed to determine if a second has passed

    private final BossInfoServer bossInfo = (new BossInfoServer(new TextComponentString(""), BossInfo.Color.BLUE, BossInfo.Overlay.PROGRESS));

    public static int getMaxHealth() {
        return WarringStatesConfig.defenderClaimerHealth + WarringStatesConfig.belligerentStealHealth;
    }

    public Timer getCaptureTimer() {
        // Initialize captureTimer if it is still null
        if(captureTimer == null)
            captureTimer = new Timer();
        return captureTimer;
    }

    public boolean getCaptureInProgress() {
        return getCaptureTimer().hasStarted();
    }

    public int getHealth() {
        return health;
    }

    public void onCapture(List<Conflict> conflicts) {
        stopCapture();
        health = getMaxHealth();
    }

    private void startCapture() {
        updateBossInfo(true);
        getCaptureTimer().start();
    }

    private void stopCapture() {
        updateBossInfo(false);
        getCaptureTimer().stop();
    }

    public String getStateName() {
        return stateName;
    }
    public String getStateDesc() {
        return stateDesc;
    }
    public UUID getStateUUID() {
        return stateID;
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState bstate, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        // Do nothing on the client
        if(worldIn.isRemote) return true;

        State playerState = StateManager.getInstance().getStateFromPlayer(playerIn);
        State ownerState = StateManager.getInstance().getStateFromUUID(stateID); // Prefer getting our state via the UUID

        if(ownerState != null) {
            if(playerState == null) {
                if(ownerState.hasApplicationFor(playerIn.getPersistentID())) {
                    playerIn.sendMessage(new TextComponentTranslation("warring_states.messages.pending_citizenship_application", ownerState.getName()));
                } else if(StateManager.getInstance().hasPendingApplication(playerIn.getPersistentID())) {
                    playerIn.sendMessage(new TextComponentTranslation("warring_states.messages.already_applied_to_different_state"));
                } else {
                    GuiUtils.openGUI(playerIn, pos, GuiID.CONFIRM_APPLY_CITIZENSHIP);
                }
            } else {
                // For current members
                if(playerState.equals(ownerState)) {
                    // TODO: Permissions should be handled inside the gui
                    //  So that any player can abandon their state
                    // if(WarringStatesAPI.doesPlayerHavePermissionForAction(worldIn, playerIn.getPersistentID(), pos, CitizenPrivileges.MANAGEMENT | CitizenPrivileges.RECRUITMENT)) {
                        GuiUtils.openGUI(playerIn, pos, GuiID.STATE_MANAGER_GUI);
                    // } else {
                        // playerIn.sendMessage(new TextComponentTranslation("warring_states.messages.interact_tec_permission_denied"));
                    // }
                } else { // For other people
                    Pair<Integer, Conflict> conflict = WarManager.getInstance().getConflictBetween(ownerState, playerState);

                    // Check if a war is going on
                    if(conflict != null) {
                        // Only begin capturing this chunk if the player's state actually has a wargoal for it
                        List<IWarGoal> goals = conflict.getRight().getWargoalsForSide(conflict.getRight().getSideOf(playerState));
                        if(goals.parallelStream().filter(StealChunkWarGoal.class::isInstance).map(StealChunkWarGoal.class::cast)
                                                 .anyMatch(goal -> WorldUtils.isBlockWithinChunk(goal.getChunk(), getPos())))
                        {
                            // If we haven't lost any health yet, start the capture process of the chunk
                            if(health == getMaxHealth()) {
                                startChunkCapture(conflict.getRight(), playerState);
                            }
                        } else {
                            playerIn.sendMessage(new TextComponentTranslation("warring_states.messages.cannot_capture_without_wargoal"));
                        }
                    } else {
                        // TODO: Open Diplomacy GUI
                        //  War can be declared via Diplomacy
                    }
                }
            }
        } else {
            WarringStatesMod.getLogger().warn("Claimer block @" + pos + " has a state of name '" + stateName + "', but the StateManager has no such state!");
        }

        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        // Note: we cannot check for this with hasKey because internally setUniqueId just adds on Most and Least to the key
        //  and hasKey does not check for this
        stateID = compound.getUniqueId("stateID");

        if(compound.hasKey("stateName")) {
            stateName = compound.getString("stateName");
        }
        if(compound.hasKey("stateDesc")) {
            stateDesc = compound.getString("stateDesc");
        }
        if(compound.hasKey("health")) {
            health = compound.getInteger("health");
        }
        if(compound.hasKey("captureTimer")) {
            getCaptureTimer().readNBT(compound.getCompoundTag("captureTimer"));
        }

        updateBossInfo(getCaptureInProgress());

        readRequired = false;
    }

    private void updateBossInfo(boolean isVisible) {
        bossInfo.setVisible(isVisible);
        bossInfo.setName(new TextComponentTranslation("warring_states.boss_bar.capturing_claimer", stateName));
    }

    @Override
    protected void setWorldCreate(World worldIn) {
        setWorld(worldIn);
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        if(stateID != null)
            compound.setUniqueId("stateID", stateID);
        else
            compound.setUniqueId("stateID", UUID.randomUUID()); // Just generate some garbage UUID for now, to prevent
                                                                //  possible crashes in readFromNBT
        if(stateName != null) compound.setString("stateName", stateName);
        if(stateDesc != null) compound.setString("stateDesc", stateDesc);

        compound.setInteger("health", health);
        // compound.setBoolean("captureInProgress", captureInProgress);
        compound.setTag("captureTimer", getCaptureTimer().writeNBT(new NBTTagCompound()));

        return compound;
    }

    private void startChunkCapture(Conflict conflict, State attackerState) {
        // If we are currently in the process of capturing this claimer or if we have already been captured then do nothing
        if(getCaptureInProgress() || conflict.getCapturedClaimers().contains(this.pos)) return;

        startCapture();

        // TODO: Spawn a special entity for displaying a health bar

        MinecraftForge.EVENT_BUS.post(new ChunkCaptureBeginEvent(this, conflict, attackerState));

        markDirty();
    }

    @Override
    public int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    @Nullable
    public ItemStack getStackInSlot(int index) {
        return null;
    }

    @Override
    @Nullable
    public ItemStack decrStackSize(int index, int count) {
        return null;
    }

    @Override
    @Nullable
    public ItemStack removeStackFromSlot(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, @Nonnull ItemStack stack) { }

    @Override
    public int getInventoryStackLimit() {
        return 0;
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {

    }

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    @Nonnull
    public String getName() {
        return BlockClaimer.NAME;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    public boolean canBeHarvested(EntityPlayer player) {
        if(!WarringStatesConfig.enableDebugClaimerHarvesting) return false;

        State state = StateManager.getInstance().getStateFromPlayer(player);

        // Creative players can break them
        // TODO: This is a debugging feature while developing. Ideally, this should not be allowed or should be controlled
        //   with a config option
        // TODO: Check if player is OP instead of the rest of this (logic should be: CREATIVE AND OP)
        // TODO: Config option: can creative break claimers
        return player.isCreative() && state != null && state.getUUID().equals(stateID) && state.hasPrivilege(player.getPersistentID(), CitizenPrivileges.CLAIM_TERRITORY);
    }

    private void setStateName(String stateName) {
        this.stateName = stateName;
    }

    private void setStateID(UUID stateID) {
        this.stateID = stateID;
    }

    private void setStateDesc(String stateDesc) {
        this.stateDesc = stateDesc;
    }

    private void syncDataToClients() {
        this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), 0);
    }

    public void changeOwner(State state) {
        changeOwner(state.getName(), state.getDesc(), state.getUUID());
    }

    public void changeOwner(String name, String desc, UUID uuid) {
        setStateName(name);
        setStateDesc(desc);
        setStateID(uuid);

        readRequired = false;

        // Only sync data to clients if we are a server
        if(!world.isRemote)
            syncDataToClients();
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        NBTTagCompound updateTag = new NBTTagCompound();

        writeToNBT(updateTag);
        tag.setTag("updateTag", updateTag);

        return tag;
    }

    @Override
    public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
        super.handleUpdateTag(tag);

        if(tag.hasKey("updateTag")) {
            NBTTagCompound updateTag = tag.getCompoundTag("updateTag");

            readFromNBT(updateTag);
        }
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();

        writeToNBT(tag);

        return new SPacketUpdateTileEntity(pos, -1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public void update() {
        // Sanity check
        if(world == null) return;

        if(world.isRemote) {
            // TODO: We should show some client rendering stuff here maybe?
            return;
        }

        //////////////////////
        // SERVER SIDE CODE //
        //////////////////////

        // If we do not have a valid State, destroy self
        //  But only do so after we have read for the first time
        State ownerState = StateManager.getInstance().getStateFromUUID(stateID);
        if(!readRequired) {
            if(stateID == null || ownerState == null) {
                WarringStatesMod.getLogger().error("TileEntityClaimer found to be missing a stateID, or it's stateID is referring to a non-existant state. Self-destructing");
                if(WorldUtils.destroyClaimer(WorldUtils.getExtendedPosition(world, getPos()))) {
                    WarringStatesMod.getLogger().warn("No tile entity was destroyed!");
                }
                return;
            } else {
                // Additional sanity check
                State stateAtPosition = StateManager.getInstance().getStateAtPosition(world, pos);
                if(stateAtPosition == null || !stateAtPosition.equals(ownerState)) {
                    WarringStatesMod.getLogger().error("TileEntityClaimer belongs to " + stateName + " but the StateManager says that " + (stateAtPosition != null ? stateAtPosition.getName() : "nobody") + " owns it!");
                    if(WarringStatesConfig.correctClaimerManagerOwnershipDisparity) {
                        if(stateAtPosition != null) {
                            WarringStatesMod.getLogger().warn("Attempting to correct ownership and deferring to StateManager.");
                            changeOwner(stateAtPosition);
                        } else {
                            WarringStatesMod.getLogger().warn("Attempting to correct ownership and deferring to the TileEntityClaimer");
                            ownerState.claimTerritory(world.getChunk(getPos()).getPos(), WorldUtils.getDimensionIDForWorld((WorldServer) world));
                        }
                    } else {
                        return;
                    }
                }
            }
        } else {
            // If a read is still required, then do nothing for this update loop
            return;
        }

        // If our health is 0, then we have been fully captured
        if(health <= 0) {
            MinecraftForge.EVENT_BUS.post(new ChunkCaptureCompleteEvent(this));
            markDirty();
        }

        if(getCaptureInProgress()) {
            State selfState = StateManager.getInstance().getStateFromName(stateName);

            if(selfState == null) {
                WarringStatesMod.getLogger().warn("TileEntityClaimer is being captured, but is not a part of a state!");
                return;
            }

            // All players who are at war with the owner and are in this chunk
            List<EntityPlayerMP> players = WorldUtils.getEntitiesWithinChunk(EntityPlayerMP.class, world, getPos(),
                    (player -> WarManager.getInstance().isAtWarWith(selfState, StateManager.getInstance().getStateFromPlayer(Objects.requireNonNull(player)))));
            // All defenders of this territory who are in the chunk
            List<EntityPlayerMP> defenders = WorldUtils.getEntitiesWithinChunk(EntityPlayerMP.class, world, getPos(),
                    (player -> selfState.hasCitizen(Objects.requireNonNull(player).getPersistentID())));

            // Update the players tracked by the bossInfo
            //  Remove all that are in bossInfo, but not in players
            List<EntityPlayerMP> toRemove = bossInfo.getPlayers().stream().filter(PredicateUtils.not(PredicateUtils.or(players::contains, defenders::contains))).collect(Collectors.toList());
            toRemove.forEach(bossInfo::removePlayer);
            //  Add all players to the bossInfo
            players.forEach(bossInfo::addPlayer);
            defenders.forEach(bossInfo::addPlayer);

            // If there are any players who are at war with us in this chunk, then we can go ahead and continue
            //  lowering the health of this TileEntity
            if(!players.isEmpty()) {
                // Decrease health every second
                if(getTicksSinceCaptureStarted() % Timer.VANILLA_TICKS_PER_SECOND == 0) {
                    --health;

                    // TODO: This should deplete until "defenderClaimerHealth" points have been subtracted, then start
                    //  filling back up until "belligerentStealHealth" points have been subtracted
                    // float remainingDefenderHealth = health - WarringStatesConfig.belligerentStealHealth;
                    // boolean passedCriticalPoint = remainingDefenderHealth < 0;
                    // float fauxHealth = Math.abs((remainingDefenderHealth % getMaxHealth()) - (getMaxHealth() * Boolean.compare(passedCriticalPoint, false)));
                    // bossInfo.setPercent(fauxHealth / (passedCriticalPoint ? WarringStatesConfig.belligerentStealHealth : WarringStatesConfig.defenderClaimerHealth));
                    bossInfo.setPercent((float)health / getMaxHealth());
                }
                markDirty();
            }
        }
    }

    private long getTicksSinceCaptureStarted() {
        return getCaptureTimer().getCurrentTick();
    }

    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if(worldIn.isRemote) return;

        State s = StateManager.getInstance().getStateFromUUID(stateID);

        if(s == null) {
            WarringStatesMod.getLogger().error("Tried to harvest a Claimer which says it belongs to a state we couldn't find (UUID=" + stateID);
            return;
        }

        s.unclaimTerritory(worldIn.getChunk(pos).getPos(), WorldUtils.getDimensionIDForWorld((WorldServer)worldIn));
    }

    private int calculateEnergyToProvide() {
        // TODO
        return 0;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        // We do not accept energy at all
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return calculateEnergyToProvide();
    }

    @Override
    public int getEnergyStored() {
        return calculateEnergyToProvide();
    }

    @Override
    public int getMaxEnergyStored() {
        return calculateEnergyToProvide();
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    /**
     * Performs a sanity check on this particular block+tile entity location
     *
     * @param fixProblems Whether or not problems should be fixed when encountered
     *
     * @return Number of problems detected and fixed
     */
    public int performSanityCheck(boolean fixProblems) {
        IBlockState state = world.getBlockState(pos);
        IBlockState state2 = world.getBlockState(pos.up());

        int problems = 0;
        boolean topIsNotClaimer = false;

        // Nothing we can do about this one, kill it anyway
        if(!(state.getBlock() instanceof BlockClaimer)) {
            return 0;
        }

        // Tile Entity must _always_ be the bottom part of the block
        if(state.getValue(BlockClaimer.HALF) == BlockClaimer.ClaimerHalf.TOP) {
            if(fixProblems) {
                world.setBlockState(pos, WarringStatesBlocks.BLOCK_CLAIMER.getDefaultState().withProperty(BlockClaimer.HALF, BlockClaimer.ClaimerHalf.BOTTOM));
            }
            ++problems;
        }

        if(!(state2.getBlock() instanceof BlockClaimer)) {
            topIsNotClaimer = true;
            if(fixProblems) {
                // Break the upper block if it's not a top-claimer
                state2.getBlock().dropBlockAsItem(world, pos.up(), state2, 0);
                world.setBlockToAir(pos.up());
            }
            ++problems;
        }

        // Top block must _always_ be the top part of the block
        if(topIsNotClaimer || state2.getValue(BlockClaimer.HALF) != BlockClaimer.ClaimerHalf.TOP) {
            if(fixProblems) {
                world.setBlockState(pos.up(), WarringStatesBlocks.BLOCK_CLAIMER.getDefaultState().withProperty(BlockClaimer.HALF, BlockClaimer.ClaimerHalf.TOP));
            }
            ++problems;
        }

        return problems;
    }
}
