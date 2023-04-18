package net.minecraft.village.raid;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.PointOfInterestTypeTags;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.PersistentState;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;

public class RaidManager extends PersistentState {
   private static final String RAIDS = "raids";
   private final Map raids = Maps.newHashMap();
   private final ServerWorld world;
   private int nextAvailableId;
   private int currentTime;

   public RaidManager(ServerWorld world) {
      this.world = world;
      this.nextAvailableId = 1;
      this.markDirty();
   }

   public Raid getRaid(int id) {
      return (Raid)this.raids.get(id);
   }

   public void tick() {
      ++this.currentTime;
      Iterator iterator = this.raids.values().iterator();

      while(iterator.hasNext()) {
         Raid lv = (Raid)iterator.next();
         if (this.world.getGameRules().getBoolean(GameRules.DISABLE_RAIDS)) {
            lv.invalidate();
         }

         if (lv.hasStopped()) {
            iterator.remove();
            this.markDirty();
         } else {
            lv.tick();
         }
      }

      if (this.currentTime % 200 == 0) {
         this.markDirty();
      }

      DebugInfoSender.sendRaids(this.world, this.raids.values());
   }

   public static boolean isValidRaiderFor(RaiderEntity raider, Raid raid) {
      if (raider != null && raid != null && raid.getWorld() != null) {
         return raider.isAlive() && raider.canJoinRaid() && raider.getDespawnCounter() <= 2400 && raider.world.getDimension() == raid.getWorld().getDimension();
      } else {
         return false;
      }
   }

   @Nullable
   public Raid startRaid(ServerPlayerEntity player) {
      if (player.isSpectator()) {
         return null;
      } else if (this.world.getGameRules().getBoolean(GameRules.DISABLE_RAIDS)) {
         return null;
      } else {
         DimensionType lv = player.world.getDimension();
         if (!lv.hasRaids()) {
            return null;
         } else {
            BlockPos lv2 = player.getBlockPos();
            List list = this.world.getPointOfInterestStorage().getInCircle((poiType) -> {
               return poiType.isIn(PointOfInterestTypeTags.VILLAGE);
            }, lv2, 64, PointOfInterestStorage.OccupationStatus.IS_OCCUPIED).toList();
            int i = 0;
            Vec3d lv3 = Vec3d.ZERO;

            for(Iterator var8 = list.iterator(); var8.hasNext(); ++i) {
               PointOfInterest lv4 = (PointOfInterest)var8.next();
               BlockPos lv5 = lv4.getPos();
               lv3 = lv3.add((double)lv5.getX(), (double)lv5.getY(), (double)lv5.getZ());
            }

            BlockPos lv6;
            if (i > 0) {
               lv3 = lv3.multiply(1.0 / (double)i);
               lv6 = BlockPos.ofFloored(lv3);
            } else {
               lv6 = lv2;
            }

            Raid lv7 = this.getOrCreateRaid(player.getWorld(), lv6);
            boolean bl = false;
            if (!lv7.hasStarted()) {
               if (!this.raids.containsKey(lv7.getRaidId())) {
                  this.raids.put(lv7.getRaidId(), lv7);
               }

               bl = true;
            } else if (lv7.getBadOmenLevel() < lv7.getMaxAcceptableBadOmenLevel()) {
               bl = true;
            } else {
               player.removeStatusEffect(StatusEffects.BAD_OMEN);
               player.networkHandler.sendPacket(new EntityStatusS2CPacket(player, EntityStatuses.ADD_CLOUD_PARTICLES));
            }

            if (bl) {
               lv7.start(player);
               player.networkHandler.sendPacket(new EntityStatusS2CPacket(player, EntityStatuses.ADD_CLOUD_PARTICLES));
               if (!lv7.hasSpawned()) {
                  player.incrementStat(Stats.RAID_TRIGGER);
                  Criteria.VOLUNTARY_EXILE.trigger(player);
               }
            }

            this.markDirty();
            return lv7;
         }
      }
   }

   private Raid getOrCreateRaid(ServerWorld world, BlockPos pos) {
      Raid lv = world.getRaidAt(pos);
      return lv != null ? lv : new Raid(this.nextId(), world, pos);
   }

   public static RaidManager fromNbt(ServerWorld world, NbtCompound nbt) {
      RaidManager lv = new RaidManager(world);
      lv.nextAvailableId = nbt.getInt("NextAvailableID");
      lv.currentTime = nbt.getInt("Tick");
      NbtList lv2 = nbt.getList("Raids", NbtElement.COMPOUND_TYPE);

      for(int i = 0; i < lv2.size(); ++i) {
         NbtCompound lv3 = lv2.getCompound(i);
         Raid lv4 = new Raid(world, lv3);
         lv.raids.put(lv4.getRaidId(), lv4);
      }

      return lv;
   }

   public NbtCompound writeNbt(NbtCompound nbt) {
      nbt.putInt("NextAvailableID", this.nextAvailableId);
      nbt.putInt("Tick", this.currentTime);
      NbtList lv = new NbtList();
      Iterator var3 = this.raids.values().iterator();

      while(var3.hasNext()) {
         Raid lv2 = (Raid)var3.next();
         NbtCompound lv3 = new NbtCompound();
         lv2.writeNbt(lv3);
         lv.add(lv3);
      }

      nbt.put("Raids", lv);
      return nbt;
   }

   public static String nameFor(RegistryEntry dimensionTypeEntry) {
      return dimensionTypeEntry.matchesKey(DimensionTypes.THE_END) ? "raids_end" : "raids";
   }

   private int nextId() {
      return ++this.nextAvailableId;
   }

   @Nullable
   public Raid getRaidAt(BlockPos pos, int searchDistance) {
      Raid lv = null;
      double d = (double)searchDistance;
      Iterator var6 = this.raids.values().iterator();

      while(var6.hasNext()) {
         Raid lv2 = (Raid)var6.next();
         double e = lv2.getCenter().getSquaredDistance(pos);
         if (lv2.isActive() && e < d) {
            lv = lv2;
            d = e;
         }
      }

      return lv;
   }
}
