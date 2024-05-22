/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.world;

import java.util.Comparator;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;

public class ChunkTicketType<T> {
    private final String name;
    private final Comparator<T> argumentComparator;
    private final long expiryTicks;
    public static final ChunkTicketType<Unit> START = ChunkTicketType.create("start", (a, b) -> 0);
    public static final ChunkTicketType<Unit> DRAGON = ChunkTicketType.create("dragon", (a, b) -> 0);
    public static final ChunkTicketType<ChunkPos> PLAYER = ChunkTicketType.create("player", Comparator.comparingLong(ChunkPos::toLong));
    public static final ChunkTicketType<ChunkPos> FORCED = ChunkTicketType.create("forced", Comparator.comparingLong(ChunkPos::toLong));
    public static final ChunkTicketType<BlockPos> PORTAL = ChunkTicketType.create("portal", Vec3i::compareTo, 300);
    public static final ChunkTicketType<Integer> POST_TELEPORT = ChunkTicketType.create("post_teleport", Integer::compareTo, 5);
    public static final ChunkTicketType<ChunkPos> UNKNOWN = ChunkTicketType.create("unknown", Comparator.comparingLong(ChunkPos::toLong), 1);

    public static <T> ChunkTicketType<T> create(String name, Comparator<T> argumentComparator) {
        return new ChunkTicketType<T>(name, argumentComparator, 0L);
    }

    public static <T> ChunkTicketType<T> create(String name, Comparator<T> argumentComparator, int expiryTicks) {
        return new ChunkTicketType<T>(name, argumentComparator, expiryTicks);
    }

    protected ChunkTicketType(String name, Comparator<T> argumentComparator, long expiryTicks) {
        this.name = name;
        this.argumentComparator = argumentComparator;
        this.expiryTicks = expiryTicks;
    }

    public String toString() {
        return this.name;
    }

    public Comparator<T> getArgumentComparator() {
        return this.argumentComparator;
    }

    public long getExpiryTicks() {
        return this.expiryTicks;
    }
}

