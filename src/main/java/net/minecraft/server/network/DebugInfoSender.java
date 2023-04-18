package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.Memory;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.NameGenerator;
import net.minecraft.util.Nameable;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import net.minecraft.world.poi.PointOfInterest;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class DebugInfoSender {
   private static final Logger LOGGER = LogUtils.getLogger();

   public static void addGameTestMarker(ServerWorld world, BlockPos pos, String message, int color, int duration) {
      PacketByteBuf lv = new PacketByteBuf(Unpooled.buffer());
      lv.writeBlockPos(pos);
      lv.writeInt(color);
      lv.writeString(message);
      lv.writeInt(duration);
      sendToAll(world, lv, CustomPayloadS2CPacket.DEBUG_GAME_TEST_ADD_MARKER);
   }

   public static void clearGameTestMarkers(ServerWorld world) {
      PacketByteBuf lv = new PacketByteBuf(Unpooled.buffer());
      sendToAll(world, lv, CustomPayloadS2CPacket.DEBUG_GAME_TEST_CLEAR);
   }

   public static void sendChunkWatchingChange(ServerWorld world, ChunkPos pos) {
   }

   public static void sendPoiAddition(ServerWorld world, BlockPos pos) {
      sendPoi(world, pos);
   }

   public static void sendPoiRemoval(ServerWorld world, BlockPos pos) {
      sendPoi(world, pos);
   }

   public static void sendPointOfInterest(ServerWorld world, BlockPos pos) {
      sendPoi(world, pos);
   }

   private static void sendPoi(ServerWorld world, BlockPos pos) {
   }

   public static void sendPathfindingData(World world, MobEntity mob, @Nullable Path path, float nodeReachProximity) {
   }

   public static void sendNeighborUpdate(World world, BlockPos pos) {
   }

   public static void sendStructureStart(StructureWorldAccess world, StructureStart structureStart) {
   }

   public static void sendGoalSelector(World world, MobEntity mob, GoalSelector goalSelector) {
      if (world instanceof ServerWorld) {
         ;
      }
   }

   public static void sendRaids(ServerWorld server, Collection raids) {
   }

   public static void sendBrainDebugData(LivingEntity living) {
   }

   public static void sendBeeDebugData(BeeEntity bee) {
   }

   public static void sendGameEvent(World world, GameEvent event, Vec3d pos) {
   }

   public static void sendGameEventListener(World world, GameEventListener eventListener) {
   }

   public static void sendBeehiveDebugData(World world, BlockPos pos, BlockState state, BeehiveBlockEntity blockEntity) {
   }

   private static void writeBrain(LivingEntity entity, PacketByteBuf buf) {
      Brain lv = entity.getBrain();
      long l = entity.world.getTime();
      if (entity instanceof InventoryOwner) {
         Inventory lv2 = ((InventoryOwner)entity).getInventory();
         buf.writeString(lv2.isEmpty() ? "" : lv2.toString());
      } else {
         buf.writeString("");
      }

      buf.writeOptional(lv.hasMemoryModule(MemoryModuleType.PATH) ? lv.getOptionalRegisteredMemory(MemoryModuleType.PATH) : Optional.empty(), (buf2, path) -> {
         path.toBuffer(buf2);
      });
      if (entity instanceof VillagerEntity lv3) {
         boolean bl = lv3.canSummonGolem(l);
         buf.writeBoolean(bl);
      } else {
         buf.writeBoolean(false);
      }

      if (entity.getType() == EntityType.WARDEN) {
         WardenEntity lv4 = (WardenEntity)entity;
         buf.writeInt(lv4.getAnger());
      } else {
         buf.writeInt(-1);
      }

      buf.writeCollection(lv.getPossibleActivities(), (buf2, activity) -> {
         buf2.writeString(activity.getId());
      });
      Set set = (Set)lv.getRunningTasks().stream().map(Task::getName).collect(Collectors.toSet());
      buf.writeCollection(set, PacketByteBuf::writeString);
      buf.writeCollection(listMemories(entity, l), (buf2, memory) -> {
         String string2 = StringHelper.truncate(memory, 255, true);
         buf2.writeString(string2);
      });
      Set set2;
      Stream var10000;
      if (entity instanceof VillagerEntity) {
         var10000 = Stream.of(MemoryModuleType.JOB_SITE, MemoryModuleType.HOME, MemoryModuleType.MEETING_POINT);
         Objects.requireNonNull(lv);
         set2 = (Set)var10000.map(lv::getOptionalRegisteredMemory).flatMap(Optional::stream).map(GlobalPos::getPos).collect(Collectors.toSet());
         buf.writeCollection(set2, PacketByteBuf::writeBlockPos);
      } else {
         buf.writeVarInt(0);
      }

      if (entity instanceof VillagerEntity) {
         var10000 = Stream.of(MemoryModuleType.POTENTIAL_JOB_SITE);
         Objects.requireNonNull(lv);
         set2 = (Set)var10000.map(lv::getOptionalRegisteredMemory).flatMap(Optional::stream).map(GlobalPos::getPos).collect(Collectors.toSet());
         buf.writeCollection(set2, PacketByteBuf::writeBlockPos);
      } else {
         buf.writeVarInt(0);
      }

      if (entity instanceof VillagerEntity) {
         Map map = ((VillagerEntity)entity).getGossip().getEntityReputationAssociatedGossips();
         List list = Lists.newArrayList();
         map.forEach((uuid, gossips) -> {
            String string = NameGenerator.name(uuid);
            gossips.forEach((type, value) -> {
               list.add(string + ": " + type + ": " + value);
            });
         });
         buf.writeCollection(list, PacketByteBuf::writeString);
      } else {
         buf.writeVarInt(0);
      }

   }

   private static List listMemories(LivingEntity entity, long currentTime) {
      Map map = entity.getBrain().getMemories();
      List list = Lists.newArrayList();
      Iterator var5 = map.entrySet().iterator();

      while(var5.hasNext()) {
         Map.Entry entry = (Map.Entry)var5.next();
         MemoryModuleType lv = (MemoryModuleType)entry.getKey();
         Optional optional = (Optional)entry.getValue();
         String string;
         if (optional.isPresent()) {
            Memory lv2 = (Memory)optional.get();
            Object object = lv2.getValue();
            if (lv == MemoryModuleType.HEARD_BELL_TIME) {
               long m = currentTime - (Long)object;
               string = "" + m + " ticks ago";
            } else if (lv2.isTimed()) {
               String var10000 = format((ServerWorld)entity.world, object);
               string = var10000 + " (ttl: " + lv2.getExpiry() + ")";
            } else {
               string = format((ServerWorld)entity.world, object);
            }
         } else {
            string = "-";
         }

         String var10001 = Registries.MEMORY_MODULE_TYPE.getId(lv).getPath();
         list.add(var10001 + ": " + string);
      }

      list.sort(String::compareTo);
      return list;
   }

   private static String format(ServerWorld world, @Nullable Object object) {
      if (object == null) {
         return "-";
      } else if (object instanceof UUID) {
         return format(world, world.getEntity((UUID)object));
      } else {
         Entity lv;
         if (object instanceof LivingEntity) {
            lv = (Entity)object;
            return NameGenerator.name(lv);
         } else if (object instanceof Nameable) {
            return ((Nameable)object).getName().getString();
         } else if (object instanceof WalkTarget) {
            return format(world, ((WalkTarget)object).getLookTarget());
         } else if (object instanceof EntityLookTarget) {
            return format(world, ((EntityLookTarget)object).getEntity());
         } else if (object instanceof GlobalPos) {
            return format(world, ((GlobalPos)object).getPos());
         } else if (object instanceof BlockPosLookTarget) {
            return format(world, ((BlockPosLookTarget)object).getBlockPos());
         } else if (object instanceof DamageSource) {
            lv = ((DamageSource)object).getAttacker();
            return lv == null ? object.toString() : format(world, lv);
         } else if (!(object instanceof Collection)) {
            return object.toString();
         } else {
            List list = Lists.newArrayList();
            Iterator var3 = ((Iterable)object).iterator();

            while(var3.hasNext()) {
               Object object2 = var3.next();
               list.add(format(world, object2));
            }

            return list.toString();
         }
      }
   }

   private static void sendToAll(ServerWorld world, PacketByteBuf buf, Identifier channel) {
      Packet lv = new CustomPayloadS2CPacket(channel, buf);
      Iterator var4 = world.getPlayers().iterator();

      while(var4.hasNext()) {
         PlayerEntity lv2 = (PlayerEntity)var4.next();
         ((ServerPlayerEntity)lv2).networkHandler.sendPacket(lv);
      }

   }

   // $FF: synthetic method
   private static void method_43894(PacketByteBuf arg, Path arg2) {
      arg2.toBuffer(arg);
   }

   // $FF: synthetic method
   private static void method_36163(PacketByteBuf buf, Raid raid) {
      buf.writeBlockPos(raid.getCenter());
   }

   // $FF: synthetic method
   private static void method_36162(PacketByteBuf buf, PrioritizedGoal goal) {
      buf.writeInt(goal.getPriority());
      buf.writeBoolean(goal.isRunning());
      buf.writeString(goal.getGoal().getClass().getSimpleName());
   }

   // $FF: synthetic method
   private static String method_44135(RegistryKey arg) {
      return arg.getValue().toString();
   }

   // $FF: synthetic method
   private static void method_36155(ServerWorld world, PointOfInterest poi) {
      sendPoiAddition(world, poi.getPos());
   }

   // $FF: synthetic method
   private static boolean method_36159(RegistryEntry arg) {
      return true;
   }
}
