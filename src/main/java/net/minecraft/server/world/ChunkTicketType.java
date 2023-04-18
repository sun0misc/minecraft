package net.minecraft.server.world;

import java.util.Comparator;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;

public class ChunkTicketType {
   private final String name;
   private final Comparator argumentComparator;
   private final long expiryTicks;
   public static final ChunkTicketType START = create("start", (a, b) -> {
      return 0;
   });
   public static final ChunkTicketType DRAGON = create("dragon", (a, b) -> {
      return 0;
   });
   public static final ChunkTicketType PLAYER = create("player", Comparator.comparingLong(ChunkPos::toLong));
   public static final ChunkTicketType FORCED = create("forced", Comparator.comparingLong(ChunkPos::toLong));
   public static final ChunkTicketType LIGHT = create("light", Comparator.comparingLong(ChunkPos::toLong));
   public static final ChunkTicketType PORTAL = create("portal", Vec3i::compareTo, 300);
   public static final ChunkTicketType POST_TELEPORT = create("post_teleport", Integer::compareTo, 5);
   public static final ChunkTicketType UNKNOWN = create("unknown", Comparator.comparingLong(ChunkPos::toLong), 1);

   public static ChunkTicketType create(String name, Comparator argumentComparator) {
      return new ChunkTicketType(name, argumentComparator, 0L);
   }

   public static ChunkTicketType create(String name, Comparator argumentComparator, int expiryTicks) {
      return new ChunkTicketType(name, argumentComparator, (long)expiryTicks);
   }

   protected ChunkTicketType(String name, Comparator argumentComparator, long expiryTicks) {
      this.name = name;
      this.argumentComparator = argumentComparator;
      this.expiryTicks = expiryTicks;
   }

   public String toString() {
      return this.name;
   }

   public Comparator getArgumentComparator() {
      return this.argumentComparator;
   }

   public long getExpiryTicks() {
      return this.expiryTicks;
   }
}
