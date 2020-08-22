package com.aflyingcar.warring_states.util;

import com.aflyingcar.warring_states.WarringStatesMod;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid=WarringStatesMod.MOD_ID)
public class Timer implements ISerializable {
    public static final long VANILLA_TICKS_PER_SECOND = 20;
    public static final long VANILLA_TICKS_PER_MINUTE = VANILLA_TICKS_PER_SECOND * 60;
    public static final long VANILLA_TICKS_PER_HOUR = VANILLA_TICKS_PER_MINUTE * 60;
    public static final long VANILLA_TICKS_PER_DAY = VANILLA_TICKS_PER_HOUR * 24;
    public static final long VANILLA_TICKS_PER_WEEK = VANILLA_TICKS_PER_DAY * 7;

    private static BigInteger ticks = BigInteger.ZERO;
    private static final Set<Timer> timers = new HashSet<>();

    private long currentTick = 0;
    private boolean started = false;

    public Timer() {
    }

    /**
     * Initializes data for this Timer.
     * Will not add this Timer to the timers list even if started is true.
     * @param currentTick The current tick
     * @param started Whether this Timer has been started.
     */
    public Timer(long currentTick, boolean started) {
        this.currentTick = currentTick;
        this.started = started;
    }

    public void start() {
        if(started) return;

        currentTick = 0;
        timers.add(this);
        started = true;
    }

    public void stop() {
        timers.remove(this);
        started = false;
    }

    protected void update() {
        ++currentTick;
    }


    public long getCurrentTick() {
        return currentTick;
    }

    public long getNumberOfSeconds() {
        return getCurrentTick() / VANILLA_TICKS_PER_SECOND;
    }

    public long getNumberOfMinutes() {
        return getCurrentTick() / VANILLA_TICKS_PER_MINUTE;
    }

    public long getNumberOfHours() {
        return getCurrentTick() / VANILLA_TICKS_PER_HOUR;
    }

    public long getNumberOfDays() {
        return getCurrentTick() / VANILLA_TICKS_PER_DAY;
    }

    public long getNumberOfWeeks() {
        return getCurrentTick() / VANILLA_TICKS_PER_WEEK;
    }

    public boolean hasStarted() {
        return started;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Timer timer = (Timer) o;
        return started && currentTick == timer.currentTick;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        ticks = ticks.add(BigInteger.ONE);

        for(Timer t : timers) {
            t.update();
        }
    }

    @Override
    public NBTTagCompound writeNBT(NBTTagCompound nbt) {
        nbt.setLong("currentTick", currentTick);
        nbt.setBoolean("started", started);

        return nbt;
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        currentTick = nbt.getLong("currentTick");
        started = nbt.getBoolean("started");

        if(started) {
            timers.add(this);
        }
    }
}
