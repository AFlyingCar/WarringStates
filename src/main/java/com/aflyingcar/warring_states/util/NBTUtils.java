package com.aflyingcar.warring_states.util;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class NBTUtils {
    /**
     * Serializes a collection into an {@code NBTTagList}
     * @param collection The collection of objects to serialize
     * @param <T> A type that can be serialized
     * @return A new {@code NBTTagList} with an {@code NBTTagCompound} for every element in the collection
     */
    public static <T extends ISerializable> NBTTagList serializeCollection(Collection<T> collection) {
        return serializeCollection(collection, t -> t.writeNBT(new NBTTagCompound()));
    }

    /**
     * Serializes a collection into an {@code NBTTagList}
     * @param collection The collection of objects to serialize
     * @param writer A {@code Function} which describes how to write a {@code <T>} into a {@code NBTBase}
     * @param <T> The type to write
     * @return A new {@code NBTTagList} with a {@code NBTBase} for every element in the collection
     */
    public static <T> NBTTagList serializeCollection(Collection<T> collection, Function<T, NBTBase> writer) {
        NBTTagList nbtList = new NBTTagList();

        for(T t : collection) {
            nbtList.appendTag(writer.apply(t));
        }

        return nbtList;
    }

    /**
     * Serializes a map into an {@code NBTTagList}
     * @param map The map to serialize
     * @param <K> The key type
     * @param <V> The value type
     * @return A new {@code NBTTagList} with a {@code NBTTagCompound} for every element in the map
     */
    public static <K extends ISerializable, V extends ISerializable> NBTTagList serializeMap(Map<K, V> map) {
        return serializeMap(map, entry -> {
            NBTTagCompound pair = new NBTTagCompound();

            pair.setTag("key", entry.getKey().writeNBT(new NBTTagCompound()));
            pair.setTag("val", entry.getValue().writeNBT(new NBTTagCompound()));

            return pair;
        });
    }

    /**
     * Serializes a map into an {@code NBTTagList}
     * @param map The map to serialize
     * @param writer A {@code Function} which describes how to write a {@code Map.Entry<K, V>} into a {@code NBTBase}
     * @param <K> The key type
     * @param <V> The value type
     * @return A new {@code NBTTagList} with a {@code NBTBase} for every element in the map
     */
    public static <K, V> NBTTagList serializeMap(Map<K, V> map, Function<Map.Entry<K, V>, NBTBase> writer) {
        NBTTagList nbtEntries = new NBTTagList();

        for(Map.Entry<K, V> entry : map.entrySet()) {
            nbtEntries.appendTag(writer.apply(entry));
        }

        return nbtEntries;
    }

    /**
     * Deserializes a collection from an {@code NBTTagList}. Assumes that said list was serialized with {@code serializeCollection}.
     * @param nbtList The {@code NBTTagList} to deserialize from.
     * @param constructor A method that can create new instances of {@code <T>}.
     * @param <T> The type to deserialize into
     * @return A {@code NonNullList} of deserialized {@code <T>} instances. Will be instantiated for each one using {@code constructor}
     */
    public static <T extends ISerializable> NonNullList<T> deserializeList(NBTTagList nbtList, Function<NBTTagCompound, T> constructor) {
        return deserializeGenericList(nbtList, nbtBase -> {
            try {
                T newT = constructor.apply((NBTTagCompound)nbtBase);

                newT.readNBT((NBTTagCompound)nbtBase);

                return newT;
            } catch(Exception e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    /**
     * Deserializes a collection from an {@code NBTTagList}. Assumes that said list was serialized with {@code serializeCollection}.
     * If the call to {@code reader} fails, then the element is skipped
     * @param nbtList The {@code NBTTagList} to deserialize from.
     * @param reader A {@code Function} which knows how to convert from an NBTBase into a new instance of {@code <T>}
     * @param <T> The type to deserialize into
     * @return A {@code NonNullList} of deserialized {@code <T>} instances. Will be instantiated for each one using {@code reader}.
     */
    public static <T> NonNullList<T> deserializeGenericList(NBTTagList nbtList, Function<NBTBase, T> reader) {
        // TODO: We should probably initialize using withSize()
        NonNullList<T> list = NonNullList.create();

        int i = 0;
        for(NBTBase base : nbtList) {
            T t = reader.apply(base);
            if(t == null) {
                WarringStatesMod.getLogger().warn("Failed to deserialize element #" + i);
                continue;
            }

            list.add(t);

            ++i;
        }

        return list;
    }


    /**
     * Deserializes all entries for a map from an {@code NBTTagList}. Assumes that said map was serialized with {@code serializeMap}.
     * @param nbtEntries The {@code NBTTagList} to deserialize map entries from.
     * @param keyConstructor A method that can create new instances of {@code <K>}.
     * @param valueConstructor A method that can create new instances of {@code <V>}.
     * @param <K> The type to deserialize keys into
     * @param <V> The type to deserialize values into
     * @return A {@code Map} of deserialized {@code <K>} and {@code <V>} instances. Will be instantiated for each one using {@code keyConstructor} and {@code valueConstructor}
     */
    public static <K extends ISerializable, V extends ISerializable> Map<K, V> deserializeMap(NBTTagList nbtEntries, Function<NBTTagCompound, K> keyConstructor, Function<NBTTagCompound, V> valueConstructor) {
        return deserializeMap(nbtEntries, nbtBase -> {
            try {
                NBTTagCompound nbtKey = ((NBTTagCompound)nbtBase).getCompoundTag("key");
                NBTTagCompound nbtVal = ((NBTTagCompound)nbtBase).getCompoundTag("val");

                K newK = keyConstructor.apply(nbtKey);
                V newV = valueConstructor.apply(nbtVal);

                newK.readNBT(nbtKey);
                newV.readNBT(nbtVal);

                return Pair.of(newK, newV);
            } catch(Exception e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    /**
     * Deserializes all entries for a map from an {@code NBTTagList}. Assumes that said list was serialized with {@code serializeMap}.
     * If the call to {@code reader} fails, then the element is skipped
     * @param nbtEntries The {@code NBTTagList} to deserialize map entries from.
     * @param reader A {@code Function} which knows how to convert from an NBTBase into a new {@code Pair} instance of {@code <K>} and {@code <V>}
     * @param <K> The type to deserialize keys into
     * @param <V> The type to deserialize values into
     * @return A {@code Map} of deserialized {@code <K>} and {@code <V>} instances. Will be instantiated for each one using {@code reader}.
     */
    public static <K, V> Map<K, V> deserializeMap(NBTTagList nbtEntries, Function<NBTBase, Pair<K, V>> reader) {
        Map<K, V> map = Maps.newHashMap();

        int i = 0;
        for(NBTBase base : nbtEntries) {
            Pair<K, V> entry = reader.apply(base);

            if(entry == null) {
                WarringStatesMod.getLogger().warn("Failed to deserialize element #" + i);
                continue;
            }

            map.put(entry.getKey(), entry.getValue());

            ++i;
        }

        return map;
    }

    public static NBTTagCompound serializeUUID(UUID uuid) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setUniqueId("uuid", uuid);
        return nbt;
    }

    public static UUID deserializeUUID(NBTTagCompound uuidTag) {
        return uuidTag.getUniqueId("uuid");
    }
}
