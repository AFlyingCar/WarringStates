package com.aflyingcar.warring_states.util;

import io.netty.buffer.ByteBuf;

public interface INetSerializable {
    void writeToBuf(ByteBuf buf);
    void readFromBuf(ByteBuf buf);
}
