package com.aflyingcar.warring_states.util;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.WarringStatesNetwork;
import com.aflyingcar.warring_states.states.DummyState;
import com.aflyingcar.warring_states.war.DummyConflict;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NetworkUtils {
    /**
     * Gets the thread listener for the given {@code MessageContext}
     * @param ctx The {@code MessageContext} to get the thread listener for
     * @return The thread listener for {@code ctx}
     */
    public static IThreadListener getThreadListener(@Nonnull MessageContext ctx) {
        return ctx.side == Side.SERVER ? ctx.getServerHandler().player.getServerWorld() : Minecraft.getMinecraft();
    }

    @Nonnull
    public static UUID readUUID(@Nonnull ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    public static void writeUUID(@Nonnull ByteBuf buf, @Nonnull UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public static void writeExtendedBlockPos(@Nonnull ByteBuf buf, @Nonnull ExtendedBlockPos extendedBlockPos) {
        buf.writeInt(extendedBlockPos.getX());
        buf.writeInt(extendedBlockPos.getY());
        buf.writeInt(extendedBlockPos.getZ());
        buf.writeInt(extendedBlockPos.getDimID());
    }

    @Nonnull
    public static ExtendedBlockPos readExtendedBlockPos(@Nonnull ByteBuf buf) {
        return new ExtendedBlockPos(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void writeString(@Nonnull ByteBuf buf, @Nonnull String string) {
        buf.writeInt(string.length());
        buf.writeCharSequence(string, Charset.defaultCharset());
    }

    public static String readString(@Nonnull ByteBuf buf) {
        return buf.readCharSequence(buf.readInt(), Charset.defaultCharset()).toString();
    }

    /**
     * Writes a Collection to the given ByteBuf.
     * @param buf The ByteBuf to write to.
     * @param collection The List to write
     * @param writer A function which knows how to write a T to a ByteBuf
     * @param <T> The type contained in the collection
     */
    public static <T> void writeCollection(@Nonnull ByteBuf buf, @Nonnull Collection<T> collection, @Nonnull BiConsumer<ByteBuf, T> writer) {
        buf.writeInt(collection.size());
        collection.forEach(t -> writer.accept(buf, t));
    }

    /**
     * Reads a List from the given ByteBuf. Assumes that said list had been previously written with {@code writeList}
     * @param buf The ByteBuf to read from.
     * @param reader A function which knows how to read a T from a ByteBuf
     * @param <T> The type contained in the list
     * @return A new List of all elements read from ByteBuf
     */
    public static <T> List<T> readList(@Nonnull ByteBuf buf, @Nonnull Function<ByteBuf, T> reader) {
        int size = buf.readInt();

        return IntStream.range(0, size).mapToObj(i -> reader.apply(buf)).collect(Collectors.toList());
    }

    /**
     * Reads a Set from the given ByteBuf. Assumes that said set had been previously written with {@code writeList}
     * @param buf The ByteBuf to read from.
     * @param reader A function which knows how to read a T from a ByteBuf
     * @param <T> The type contained in the set
     * @return A new List of all elements read from ByteBuf
     */
    public static <T> Set<T> readSet(@Nonnull ByteBuf buf, @Nonnull Function<ByteBuf, T> reader) {
        int size = buf.readInt();

        return IntStream.range(0, size).mapToObj(i -> reader.apply(buf)).collect(Collectors.toSet());
    }

    /**
     * Writes a Map to a ByteBuf.
     * @param buf The ByteBuf to write to
     * @param map The map to write
     * @param keyWriter A function which knows how to write K to a given ByteBuf
     * @param valWriter A function which knows how to write V to a given ByteBuf
     * @param <K> The key type
     * @param <V> The value type
     */
    public static <K, V> void writeMap(@Nonnull ByteBuf buf, @Nonnull Map<K, V> map, @Nonnull BiConsumer<ByteBuf, K> keyWriter, @Nonnull BiConsumer<ByteBuf, V> valWriter) {
        buf.writeInt(map.size());

        for(Map.Entry<K, V> entry : map.entrySet()) {
            keyWriter.accept(buf, entry.getKey());
            valWriter.accept(buf, entry.getValue());
        }
    }

    /**
     * Reads a Map from a ByteBuf. Assumes said Map has been previously written with {@code writeMap}
     * @param buf The ByteBuf to read from
     * @param keyReader A function which knows how to read K from the given ByteBuf
     * @param valReader A function which knows how to read V from the given ByteBuf
     * @param <K> The key type
     * @param <V> The value type
     * @return A new Map containing all serialized values in ByteBuf
     */
    public static <K, V> Map<K, V> readMap(@Nonnull ByteBuf buf, @Nonnull Function<ByteBuf, K> keyReader, @Nonnull Function<ByteBuf, V> valReader) {
        int numEntries = buf.readInt();

        // TODO: This can easily be replaced with a collect, but i'm worried about how readable it is
        Map<K, V> map = new HashMap<>();

        for(int i = 0; i < numEntries; ++i) {
            map.put(keyReader.apply(buf), valReader.apply(buf));
        }

        return map;
    }

    private static final Map<UUID, Consumer<TrackedMessage>> trackedMessages = new HashMap<>();

    /**
     * Sends a message to the server, and stores a Consumer to be called when the server responds to that message
     * @param message The message to send
     * @param serverResponseConsumer The consumer to call when the server responds
     */
    public static void sendTrackedMessage(@Nonnull TrackedMessage message, @Nonnull Consumer<TrackedMessage> serverResponseConsumer) {
        trackedMessages.put(message.getUUID(), serverResponseConsumer);

        WarringStatesNetwork.NETWORK.sendToServer(message);
    }

    /**
     * Pops the consumer callback for the given UUID, if found.
     * @param uuid The UUID of the TrackedMessage that was earlier sent with sendTrackedMessage
     * @return The consumer callback if found, or null otherwise
     */
    @Nullable
    public static Consumer<TrackedMessage> popConsumerForResponse(UUID uuid) {
        Consumer<TrackedMessage> consumer = trackedMessages.getOrDefault(uuid, null);
        trackedMessages.remove(uuid);

        return consumer;
    }

    /**
     * Receives a Tracked response from the server.
     * @param message The message received
     * @param ctx The context of the message
     */
    public static void receivedTrackedResponse(@Nonnull TrackedMessage message, @Nonnull MessageContext ctx) {
        NetworkUtils.getThreadListener(ctx).addScheduledTask(() -> {
            Consumer<TrackedMessage> consumer = NetworkUtils.popConsumerForResponse(message.getUUID());
            if(consumer == null) {
                WarringStatesMod.getLogger().warn("Received message with UUID " + message.getUUID() + " however no Consumer was found for that message.");
                return;
            }

            consumer.accept(message);
        });
    }

    public static void writeTimer(ByteBuf buf, Timer decayTimer) {
        buf.writeLong(decayTimer.getCurrentTick());
        buf.writeBoolean(decayTimer.hasStarted());
    }

    public static Timer readTimer(ByteBuf buf) {
        return new Timer(buf.readLong(), buf.readBoolean());
    }

    public static void writeChunkPos(ByteBuf buf, ChunkPos pos) {
        buf.writeInt(pos.x);
        buf.writeInt(pos.z);
    }

    public static ChunkPos readChunkPos(ByteBuf buf) {
        return new ChunkPos(buf.readInt(), buf.readInt());
    }

    public static void writeChunkGroup(ByteBuf buf, ChunkGroup group) {
        buf.writeInt(group.getDimension());
        writeCollection(buf, group.getChunks(), NetworkUtils::writeChunkPos);
    }

    public static ChunkGroup readChunkGroup(ByteBuf buf) {
        ChunkGroup group = new ChunkGroup(buf.readInt());

        group.getChunks().addAll(NetworkUtils.readSet(buf, NetworkUtils::readChunkPos));

        return group;
    }

    public static void writeConflict(ByteBuf byteBuf, DummyConflict war) {
        writeMap(byteBuf, war.getDefenders(), (byteBuf1, dummyState) -> dummyState.writeData(byteBuf1), ByteBuf::writeInt);
        writeMap(byteBuf, war.getBelligerents(), (byteBuf1, dummyState) -> dummyState.writeData(byteBuf1), ByteBuf::writeInt);
    }

    public static DummyConflict readConflict(ByteBuf byteBuf) {
        return new DummyConflict(readMap(byteBuf, DummyState::readStateData, ByteBuf::readInt), readMap(byteBuf, DummyState::readStateData, ByteBuf::readInt));
    }

    public static void writeNetSerializable(ByteBuf byteBuf, INetSerializable serializable) {
        serializable.writeToBuf(byteBuf);
    }
}

