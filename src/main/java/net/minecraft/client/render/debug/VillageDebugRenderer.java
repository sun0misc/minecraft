package net.minecraft.client.render.debug;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.NameGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class VillageDebugRenderer implements DebugRenderer.Renderer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final boolean field_32874 = true;
   private static final boolean field_32875 = false;
   private static final boolean field_32876 = false;
   private static final boolean field_32877 = false;
   private static final boolean field_32878 = false;
   private static final boolean field_32879 = false;
   private static final boolean field_32880 = false;
   private static final boolean field_32881 = false;
   private static final boolean field_32882 = true;
   private static final boolean field_38346 = false;
   private static final boolean field_32883 = true;
   private static final boolean field_32884 = true;
   private static final boolean field_32885 = true;
   private static final boolean field_32886 = true;
   private static final boolean field_32887 = true;
   private static final boolean field_32888 = true;
   private static final boolean field_32889 = true;
   private static final boolean field_32890 = true;
   private static final boolean field_32891 = true;
   private static final boolean field_32892 = true;
   private static final boolean field_38347 = true;
   private static final boolean field_32893 = true;
   private static final int POI_RANGE = 30;
   private static final int BRAIN_RANGE = 30;
   private static final int TARGET_ENTITY_RANGE = 8;
   private static final float DEFAULT_DRAWN_STRING_SIZE = 0.02F;
   private static final int WHITE = -1;
   private static final int YELLOW = -256;
   private static final int AQUA = -16711681;
   private static final int GREEN = -16711936;
   private static final int GRAY = -3355444;
   private static final int PINK = -98404;
   private static final int RED = -65536;
   private static final int ORANGE = -23296;
   private final MinecraftClient client;
   private final Map pointsOfInterest = Maps.newHashMap();
   private final Map brains = Maps.newHashMap();
   @Nullable
   private UUID targetedEntity;

   public VillageDebugRenderer(MinecraftClient client) {
      this.client = client;
   }

   public void clear() {
      this.pointsOfInterest.clear();
      this.brains.clear();
      this.targetedEntity = null;
   }

   public void addPointOfInterest(PointOfInterest poi) {
      this.pointsOfInterest.put(poi.pos, poi);
   }

   public void removePointOfInterest(BlockPos pos) {
      this.pointsOfInterest.remove(pos);
   }

   public void setFreeTicketCount(BlockPos pos, int freeTicketCount) {
      PointOfInterest lv = (PointOfInterest)this.pointsOfInterest.get(pos);
      if (lv == null) {
         LOGGER.warn("Strange, setFreeTicketCount was called for an unknown POI: {}", pos);
      } else {
         lv.freeTicketCount = freeTicketCount;
      }
   }

   public void addBrain(Brain brain) {
      this.brains.put(brain.uuid, brain);
   }

   public void removeBrain(int entityId) {
      this.brains.values().removeIf((brain) -> {
         return brain.entityId == entityId;
      });
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      this.removeRemovedBrains();
      this.draw(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
      if (!this.client.player.isSpectator()) {
         this.updateTargetedEntity();
      }

   }

   private void removeRemovedBrains() {
      this.brains.entrySet().removeIf((entry) -> {
         Entity lv = this.client.world.getEntityById(((Brain)entry.getValue()).entityId);
         return lv == null || lv.isRemoved();
      });
   }

   private void draw(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double x, double y, double z) {
      BlockPos lv = BlockPos.ofFloored(x, y, z);
      this.brains.values().forEach((brain) -> {
         if (this.isClose(brain)) {
            this.drawBrain(matrices, vertexConsumers, brain, x, y, z);
         }

      });
      Iterator var10 = this.pointsOfInterest.keySet().iterator();

      while(var10.hasNext()) {
         BlockPos lv2 = (BlockPos)var10.next();
         if (lv.isWithinDistance(lv2, 30.0)) {
            drawPointOfInterest(matrices, vertexConsumers, lv2);
         }
      }

      this.pointsOfInterest.values().forEach((poi) -> {
         if (lv.isWithinDistance(poi.pos, 30.0)) {
            this.drawPointOfInterestInfo(matrices, vertexConsumers, poi);
         }

      });
      this.getGhostPointsOfInterest().forEach((pos, brains) -> {
         if (lv.isWithinDistance(pos, 30.0)) {
            this.drawGhostPointOfInterest(matrices, vertexConsumers, pos, brains);
         }

      });
   }

   private static void drawPointOfInterest(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockPos pos) {
      float f = 0.05F;
      DebugRenderer.drawBox(matrices, vertexConsumers, pos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
   }

   private void drawGhostPointOfInterest(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockPos pos, List brains) {
      float f = 0.05F;
      DebugRenderer.drawBox(matrices, vertexConsumers, pos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
      drawString(matrices, vertexConsumers, "" + brains, (BlockPos)pos, 0, -256);
      drawString(matrices, vertexConsumers, "Ghost POI", (BlockPos)pos, 1, -65536);
   }

   private void drawPointOfInterestInfo(MatrixStack matrices, VertexConsumerProvider vertexConsumers, PointOfInterest pointOfInterest) {
      int i = 0;
      Set set = this.getNamesOfPointOfInterestTicketHolders(pointOfInterest);
      if (set.size() < 4) {
         drawString(matrices, vertexConsumers, "Owners: " + set, (PointOfInterest)pointOfInterest, i, -256);
      } else {
         drawString(matrices, vertexConsumers, set.size() + " ticket holders", (PointOfInterest)pointOfInterest, i, -256);
      }

      ++i;
      Set set2 = this.getNamesOfJobSitePotentialOwners(pointOfInterest);
      if (set2.size() < 4) {
         drawString(matrices, vertexConsumers, "Candidates: " + set2, (PointOfInterest)pointOfInterest, i, -23296);
      } else {
         drawString(matrices, vertexConsumers, set2.size() + " potential owners", (PointOfInterest)pointOfInterest, i, -23296);
      }

      ++i;
      drawString(matrices, vertexConsumers, "Free tickets: " + pointOfInterest.freeTicketCount, (PointOfInterest)pointOfInterest, i, -256);
      ++i;
      drawString(matrices, vertexConsumers, pointOfInterest.field_18932, (PointOfInterest)pointOfInterest, i, -1);
   }

   private void drawPath(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Brain brain, double cameraX, double cameraY, double cameraZ) {
      if (brain.path != null) {
         PathfindingDebugRenderer.drawPath(matrices, vertexConsumers, brain.path, 0.5F, false, false, cameraX, cameraY, cameraZ);
      }

   }

   private void drawBrain(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Brain brain, double cameraX, double cameraY, double cameraZ) {
      boolean bl = this.isTargeted(brain);
      int i = 0;
      drawString(matrices, vertexConsumers, brain.pos, i, brain.name, -1, 0.03F);
      ++i;
      if (bl) {
         drawString(matrices, vertexConsumers, brain.pos, i, brain.profession + " " + brain.xp + " xp", -1, 0.02F);
         ++i;
      }

      if (bl) {
         int j = brain.health < brain.maxHealth ? -23296 : -1;
         Position var10002 = brain.pos;
         String var10004 = String.format(Locale.ROOT, "%.1f", brain.health);
         drawString(matrices, vertexConsumers, var10002, i, "health: " + var10004 + " / " + String.format(Locale.ROOT, "%.1f", brain.maxHealth), j, 0.02F);
         ++i;
      }

      if (bl && !brain.inventory.equals("")) {
         drawString(matrices, vertexConsumers, brain.pos, i, brain.inventory, -98404, 0.02F);
         ++i;
      }

      String string;
      Iterator var14;
      if (bl) {
         for(var14 = brain.runningTasks.iterator(); var14.hasNext(); ++i) {
            string = (String)var14.next();
            drawString(matrices, vertexConsumers, brain.pos, i, string, -16711681, 0.02F);
         }
      }

      if (bl) {
         for(var14 = brain.possibleActivities.iterator(); var14.hasNext(); ++i) {
            string = (String)var14.next();
            drawString(matrices, vertexConsumers, brain.pos, i, string, -16711936, 0.02F);
         }
      }

      if (brain.wantsGolem) {
         drawString(matrices, vertexConsumers, brain.pos, i, "Wants Golem", -23296, 0.02F);
         ++i;
      }

      if (bl && brain.angerLevel != -1) {
         drawString(matrices, vertexConsumers, brain.pos, i, "Anger Level: " + brain.angerLevel, -98404, 0.02F);
         ++i;
      }

      if (bl) {
         for(var14 = brain.gossips.iterator(); var14.hasNext(); ++i) {
            string = (String)var14.next();
            if (string.startsWith(brain.name)) {
               drawString(matrices, vertexConsumers, brain.pos, i, string, -1, 0.02F);
            } else {
               drawString(matrices, vertexConsumers, brain.pos, i, string, -23296, 0.02F);
            }
         }
      }

      if (bl) {
         for(var14 = Lists.reverse(brain.memories).iterator(); var14.hasNext(); ++i) {
            string = (String)var14.next();
            drawString(matrices, vertexConsumers, brain.pos, i, string, -3355444, 0.02F);
         }
      }

      if (bl) {
         this.drawPath(matrices, vertexConsumers, brain, cameraX, cameraY, cameraZ);
      }

   }

   private static void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, String string, PointOfInterest pointOfInterest, int offsetY, int color) {
      drawString(matrices, vertexConsumers, string, pointOfInterest.pos, offsetY, color);
   }

   private static void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, String string, BlockPos pos, int offsetY, int color) {
      double d = 1.3;
      double e = 0.2;
      double f = (double)pos.getX() + 0.5;
      double g = (double)pos.getY() + 1.3 + (double)offsetY * 0.2;
      double h = (double)pos.getZ() + 0.5;
      DebugRenderer.drawString(matrices, vertexConsumers, string, f, g, h, color, 0.02F, true, 0.0F, true);
   }

   private static void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Position pos, int offsetY, String string, int color, float size) {
      double d = 2.4;
      double e = 0.25;
      BlockPos lv = BlockPos.ofFloored(pos);
      double g = (double)lv.getX() + 0.5;
      double h = pos.getY() + 2.4 + (double)offsetY * 0.25;
      double k = (double)lv.getZ() + 0.5;
      float l = 0.5F;
      DebugRenderer.drawString(matrices, vertexConsumers, string, g, h, k, color, size, false, 0.5F, true);
   }

   private Set getNamesOfPointOfInterestTicketHolders(PointOfInterest pointOfInterest) {
      return (Set)this.getBrainsContainingPointOfInterest(pointOfInterest.pos).stream().map(NameGenerator::name).collect(Collectors.toSet());
   }

   private Set getNamesOfJobSitePotentialOwners(PointOfInterest potentialJobSite) {
      return (Set)this.getBrainsContainingPotentialJobSite(potentialJobSite.pos).stream().map(NameGenerator::name).collect(Collectors.toSet());
   }

   private boolean isTargeted(Brain brain) {
      return Objects.equals(this.targetedEntity, brain.uuid);
   }

   private boolean isClose(Brain brain) {
      PlayerEntity lv = this.client.player;
      BlockPos lv2 = BlockPos.ofFloored(lv.getX(), brain.pos.getY(), lv.getZ());
      BlockPos lv3 = BlockPos.ofFloored(brain.pos);
      return lv2.isWithinDistance(lv3, 30.0);
   }

   private Collection getBrainsContainingPointOfInterest(BlockPos pointOfInterest) {
      return (Collection)this.brains.values().stream().filter((brain) -> {
         return brain.isPointOfInterest(pointOfInterest);
      }).map(Brain::getUuid).collect(Collectors.toSet());
   }

   private Collection getBrainsContainingPotentialJobSite(BlockPos potentialJobSite) {
      return (Collection)this.brains.values().stream().filter((brain) -> {
         return brain.isPotentialJobSite(potentialJobSite);
      }).map(Brain::getUuid).collect(Collectors.toSet());
   }

   private Map getGhostPointsOfInterest() {
      Map map = Maps.newHashMap();
      Iterator var2 = this.brains.values().iterator();

      while(var2.hasNext()) {
         Brain lv = (Brain)var2.next();
         Iterator var4 = Iterables.concat(lv.pointsOfInterest, lv.potentialJobSites).iterator();

         while(var4.hasNext()) {
            BlockPos lv2 = (BlockPos)var4.next();
            if (!this.pointsOfInterest.containsKey(lv2)) {
               ((List)map.computeIfAbsent(lv2, (pos) -> {
                  return Lists.newArrayList();
               })).add(lv.name);
            }
         }
      }

      return map;
   }

   private void updateTargetedEntity() {
      DebugRenderer.getTargetedEntity(this.client.getCameraEntity(), 8).ifPresent((entity) -> {
         this.targetedEntity = entity.getUuid();
      });
   }

   @Environment(EnvType.CLIENT)
   public static class PointOfInterest {
      public final BlockPos pos;
      public String field_18932;
      public int freeTicketCount;

      public PointOfInterest(BlockPos pos, String string, int freeTicketCount) {
         this.pos = pos;
         this.field_18932 = string;
         this.freeTicketCount = freeTicketCount;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Brain {
      public final UUID uuid;
      public final int entityId;
      public final String name;
      public final String profession;
      public final int xp;
      public final float health;
      public final float maxHealth;
      public final Position pos;
      public final String inventory;
      public final Path path;
      public final boolean wantsGolem;
      public final int angerLevel;
      public final List possibleActivities = Lists.newArrayList();
      public final List runningTasks = Lists.newArrayList();
      public final List memories = Lists.newArrayList();
      public final List gossips = Lists.newArrayList();
      public final Set pointsOfInterest = Sets.newHashSet();
      public final Set potentialJobSites = Sets.newHashSet();

      public Brain(UUID uuid, int entityId, String name, String profession, int xp, float health, float maxHealth, Position pos, String inventory, @Nullable Path path, boolean wantsGolem, int angerLevel) {
         this.uuid = uuid;
         this.entityId = entityId;
         this.name = name;
         this.profession = profession;
         this.xp = xp;
         this.health = health;
         this.maxHealth = maxHealth;
         this.pos = pos;
         this.inventory = inventory;
         this.path = path;
         this.wantsGolem = wantsGolem;
         this.angerLevel = angerLevel;
      }

      boolean isPointOfInterest(BlockPos pos) {
         Stream var10000 = this.pointsOfInterest.stream();
         Objects.requireNonNull(pos);
         return var10000.anyMatch(pos::equals);
      }

      boolean isPotentialJobSite(BlockPos pos) {
         return this.potentialJobSites.contains(pos);
      }

      public UUID getUuid() {
         return this.uuid;
      }
   }
}
