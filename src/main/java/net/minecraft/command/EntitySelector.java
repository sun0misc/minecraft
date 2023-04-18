package net.minecraft.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class EntitySelector {
   public static final int MAX_VALUE = Integer.MAX_VALUE;
   public static final BiConsumer ARBITRARY = (pos, entities) -> {
   };
   private static final TypeFilter PASSTHROUGH_FILTER = new TypeFilter() {
      public Entity downcast(Entity arg) {
         return arg;
      }

      public Class getBaseClass() {
         return Entity.class;
      }
   };
   private final int limit;
   private final boolean includesNonPlayers;
   private final boolean localWorldOnly;
   private final Predicate basePredicate;
   private final NumberRange.FloatRange distance;
   private final Function positionOffset;
   @Nullable
   private final Box box;
   private final BiConsumer sorter;
   private final boolean senderOnly;
   @Nullable
   private final String playerName;
   @Nullable
   private final UUID uuid;
   private final TypeFilter entityFilter;
   private final boolean usesAt;

   public EntitySelector(int count, boolean includesNonPlayers, boolean localWorldOnly, Predicate basePredicate, NumberRange.FloatRange distance, Function positionOffset, @Nullable Box box, BiConsumer sorter, boolean senderOnly, @Nullable String playerName, @Nullable UUID uuid, @Nullable EntityType type, boolean usesAt) {
      this.limit = count;
      this.includesNonPlayers = includesNonPlayers;
      this.localWorldOnly = localWorldOnly;
      this.basePredicate = basePredicate;
      this.distance = distance;
      this.positionOffset = positionOffset;
      this.box = box;
      this.sorter = sorter;
      this.senderOnly = senderOnly;
      this.playerName = playerName;
      this.uuid = uuid;
      this.entityFilter = (TypeFilter)(type == null ? PASSTHROUGH_FILTER : type);
      this.usesAt = usesAt;
   }

   public int getLimit() {
      return this.limit;
   }

   public boolean includesNonPlayers() {
      return this.includesNonPlayers;
   }

   public boolean isSenderOnly() {
      return this.senderOnly;
   }

   public boolean isLocalWorldOnly() {
      return this.localWorldOnly;
   }

   public boolean usesAt() {
      return this.usesAt;
   }

   private void checkSourcePermission(ServerCommandSource source) throws CommandSyntaxException {
      if (this.usesAt && !source.hasPermissionLevel(2)) {
         throw EntityArgumentType.NOT_ALLOWED_EXCEPTION.create();
      }
   }

   public Entity getEntity(ServerCommandSource source) throws CommandSyntaxException {
      this.checkSourcePermission(source);
      List list = this.getEntities(source);
      if (list.isEmpty()) {
         throw EntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create();
      } else if (list.size() > 1) {
         throw EntityArgumentType.TOO_MANY_ENTITIES_EXCEPTION.create();
      } else {
         return (Entity)list.get(0);
      }
   }

   public List getEntities(ServerCommandSource source) throws CommandSyntaxException {
      return this.getUnfilteredEntities(source).stream().filter((entity) -> {
         return entity.getType().isEnabled(source.getEnabledFeatures());
      }).toList();
   }

   private List getUnfilteredEntities(ServerCommandSource source) throws CommandSyntaxException {
      this.checkSourcePermission(source);
      if (!this.includesNonPlayers) {
         return this.getPlayers(source);
      } else if (this.playerName != null) {
         ServerPlayerEntity lv = source.getServer().getPlayerManager().getPlayer(this.playerName);
         return (List)(lv == null ? Collections.emptyList() : Lists.newArrayList(new ServerPlayerEntity[]{lv}));
      } else if (this.uuid != null) {
         Iterator var7 = source.getServer().getWorlds().iterator();

         Entity lv3;
         do {
            if (!var7.hasNext()) {
               return Collections.emptyList();
            }

            ServerWorld lv2 = (ServerWorld)var7.next();
            lv3 = lv2.getEntity(this.uuid);
         } while(lv3 == null);

         return Lists.newArrayList(new Entity[]{lv3});
      } else {
         Vec3d lv4 = (Vec3d)this.positionOffset.apply(source.getPosition());
         Predicate predicate = this.getPositionPredicate(lv4);
         if (this.senderOnly) {
            return (List)(source.getEntity() != null && predicate.test(source.getEntity()) ? Lists.newArrayList(new Entity[]{source.getEntity()}) : Collections.emptyList());
         } else {
            List list = Lists.newArrayList();
            if (this.isLocalWorldOnly()) {
               this.appendEntitiesFromWorld(list, source.getWorld(), lv4, predicate);
            } else {
               Iterator var5 = source.getServer().getWorlds().iterator();

               while(var5.hasNext()) {
                  ServerWorld lv5 = (ServerWorld)var5.next();
                  this.appendEntitiesFromWorld(list, lv5, lv4, predicate);
               }
            }

            return this.getEntities(lv4, list);
         }
      }
   }

   private void appendEntitiesFromWorld(List entities, ServerWorld world, Vec3d pos, Predicate predicate) {
      int i = this.getAppendLimit();
      if (entities.size() < i) {
         if (this.box != null) {
            world.collectEntitiesByType(this.entityFilter, this.box.offset(pos), predicate, entities, i);
         } else {
            world.collectEntitiesByType(this.entityFilter, predicate, entities, i);
         }

      }
   }

   private int getAppendLimit() {
      return this.sorter == ARBITRARY ? this.limit : Integer.MAX_VALUE;
   }

   public ServerPlayerEntity getPlayer(ServerCommandSource source) throws CommandSyntaxException {
      this.checkSourcePermission(source);
      List list = this.getPlayers(source);
      if (list.size() != 1) {
         throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
      } else {
         return (ServerPlayerEntity)list.get(0);
      }
   }

   public List getPlayers(ServerCommandSource source) throws CommandSyntaxException {
      this.checkSourcePermission(source);
      ServerPlayerEntity lv;
      if (this.playerName != null) {
         lv = source.getServer().getPlayerManager().getPlayer(this.playerName);
         return (List)(lv == null ? Collections.emptyList() : Lists.newArrayList(new ServerPlayerEntity[]{lv}));
      } else if (this.uuid != null) {
         lv = source.getServer().getPlayerManager().getPlayer(this.uuid);
         return (List)(lv == null ? Collections.emptyList() : Lists.newArrayList(new ServerPlayerEntity[]{lv}));
      } else {
         Vec3d lv2 = (Vec3d)this.positionOffset.apply(source.getPosition());
         Predicate predicate = this.getPositionPredicate(lv2);
         if (this.senderOnly) {
            if (source.getEntity() instanceof ServerPlayerEntity) {
               ServerPlayerEntity lv3 = (ServerPlayerEntity)source.getEntity();
               if (predicate.test(lv3)) {
                  return Lists.newArrayList(new ServerPlayerEntity[]{lv3});
               }
            }

            return Collections.emptyList();
         } else {
            int i = this.getAppendLimit();
            Object list;
            if (this.isLocalWorldOnly()) {
               list = source.getWorld().getPlayers(predicate, i);
            } else {
               list = Lists.newArrayList();
               Iterator var6 = source.getServer().getPlayerManager().getPlayerList().iterator();

               while(var6.hasNext()) {
                  ServerPlayerEntity lv4 = (ServerPlayerEntity)var6.next();
                  if (predicate.test(lv4)) {
                     ((List)list).add(lv4);
                     if (((List)list).size() >= i) {
                        return (List)list;
                     }
                  }
               }
            }

            return this.getEntities(lv2, (List)list);
         }
      }
   }

   private Predicate getPositionPredicate(Vec3d pos) {
      Predicate predicate = this.basePredicate;
      if (this.box != null) {
         Box lv = this.box.offset(pos);
         predicate = predicate.and((entity) -> {
            return lv.intersects(entity.getBoundingBox());
         });
      }

      if (!this.distance.isDummy()) {
         predicate = predicate.and((entity) -> {
            return this.distance.testSqrt(entity.squaredDistanceTo(pos));
         });
      }

      return predicate;
   }

   private List getEntities(Vec3d pos, List entities) {
      if (entities.size() > 1) {
         this.sorter.accept(pos, entities);
      }

      return entities.subList(0, Math.min(this.limit, entities.size()));
   }

   public static Text getNames(List entities) {
      return Texts.join(entities, (Function)(Entity::getDisplayName));
   }
}
