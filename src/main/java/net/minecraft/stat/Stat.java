/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.stat;

import java.util.Objects;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class Stat<T>
extends ScoreboardCriterion {
    public static final PacketCodec<RegistryByteBuf, Stat<?>> PACKET_CODEC = PacketCodecs.registryValue(RegistryKeys.STAT_TYPE).dispatch(Stat::getType, StatType::getPacketCodec);
    private final StatFormatter formatter;
    private final T value;
    private final StatType<T> type;

    protected Stat(StatType<T> type, T value, StatFormatter formatter) {
        super(Stat.getName(type, value));
        this.type = type;
        this.formatter = formatter;
        this.value = value;
    }

    public static <T> String getName(StatType<T> type, T value) {
        return Stat.getName(Registries.STAT_TYPE.getId(type)) + ":" + Stat.getName(type.getRegistry().getId(value));
    }

    private static <T> String getName(@Nullable Identifier id) {
        return id.toString().replace(':', '.');
    }

    public StatType<T> getType() {
        return this.type;
    }

    public T getValue() {
        return this.value;
    }

    public String format(int value) {
        return this.formatter.format(value);
    }

    public boolean equals(Object o) {
        return this == o || o instanceof Stat && Objects.equals(this.getName(), ((Stat)o).getName());
    }

    public int hashCode() {
        return this.getName().hashCode();
    }

    public String toString() {
        return "Stat{name=" + this.getName() + ", formatter=" + String.valueOf(this.formatter) + "}";
    }
}

