package com.aflyingcar.warring_states.util;

import net.minecraft.nbt.NBTTagCompound;

public interface ISerializable {
    NBTTagCompound writeNBT(NBTTagCompound nbt);
    void readNBT(NBTTagCompound nbt);
}
