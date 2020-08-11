package com.aflyingcar.warring_states.util;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

/**
 * A {@code BlockPos} that also includes the dimension ID
 */
@MethodsReturnNonnullByDefault
public class ExtendedBlockPos extends BlockPos {
    private int dimID;

    public ExtendedBlockPos(int x, int y, int z, int dimID) {
        super(x, y, z);

        this.dimID = dimID;
    }

    public ExtendedBlockPos(double x, double y, double z, int dimID) {
        super(x, y, z);

        this.dimID = dimID;
    }

    public ExtendedBlockPos(Entity source) {
        super(source);

        this.dimID = source.dimension;
    }

    public ExtendedBlockPos(Vec3d vec, int dimID) {
        super(vec);

        this.dimID = dimID;
    }

    public ExtendedBlockPos(Vec3i source, int dimID) {
        super(source);

        this.dimID = dimID;
    }

    public int getDimID() {
        return dimID;
    }

    public void setDimID(int dimID) {
        this.dimID = dimID;
    }

    @Override
    public String toString() {
        return "Pos{" + getX() + ", " + getY() + ", " + getZ() + ", " + dimID + "}";
    }
}
