package com.aflyingcar.warring_states.util;

import com.aflyingcar.warring_states.WarringStatesMod;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A base class to represent managerial classes
 */
public abstract class BaseManager {
    private boolean dirty;
    private File rootGameDir;
    private Side side;

    protected abstract void readFromNBT(NBTTagCompound compound);
    @SuppressWarnings("UnusedReturnValue")
    protected abstract NBTTagCompound writeToNBT(NBTTagCompound compound);
    protected abstract File getDataFile(File rootGameDir);
    public abstract void resetAllData();
    public abstract void update();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isDirty() {
        return dirty;
    }

    public final void markDirty() {
        dirty = true;
    }

    @SuppressWarnings("SameParameterValue")
    protected final void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void setRootGameDirectory(File rootGameDir) {
        this.rootGameDir = rootGameDir;
    }

    /**
     * Loads data from this manager's world data file
     */
    public final void loadInfoFromFile() {
        if(rootGameDir == null) {
            WarringStatesMod.getLogger().error("rootGameDir is null! Cannot load information as we don't know where to look. Make sure it didn't get unset early.");
            return;
        }

        File stateInfoFile = getDataFile(rootGameDir);

        if(stateInfoFile.exists()) {
            try {
                FileInputStream fileinputstream = new FileInputStream(stateInfoFile);
                NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();
                readFromNBT(nbttagcompound);
                setDirty(false); // We have just loaded data, no need to write it again
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Writes data to this manager's world data file
     */
    public final void writeInfoToFile() {
        if(!isDirty()) return;

        if(rootGameDir == null) {
            WarringStatesMod.getLogger().error("rootGameDir is null! Cannot write state information as we don't know where to write to. Make sure it didn't get unset early.");
            return;
        }

        File stateInfoFile = getDataFile(rootGameDir);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(stateInfoFile);
            NBTTagCompound compound = new NBTTagCompound();
            writeToNBT(compound);
            CompressedStreamTools.writeCompressed(compound, fileOutputStream);
            fileOutputStream.close();
            setDirty(false);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public Side getSide() {
        return side;
    }
}
