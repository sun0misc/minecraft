package net.minecraft.client.render.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.NameGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BeeDebugRenderer implements DebugRenderer.Renderer {
   private static final boolean field_32841 = true;
   private static final boolean field_32842 = true;
   private static final boolean field_32843 = true;
   private static final boolean field_32844 = true;
   private static final boolean field_32845 = true;
   private static final boolean field_32846 = false;
   private static final boolean field_32847 = true;
   private static final boolean field_32848 = true;
   private static final boolean field_32849 = true;
   private static final boolean field_32850 = true;
   private static final boolean field_32851 = true;
   private static final boolean field_32852 = true;
   private static final boolean field_32853 = true;
   private static final boolean field_32854 = true;
   private static final int HIVE_RANGE = 30;
   private static final int BEE_RANGE = 30;
   private static final int TARGET_ENTITY_RANGE = 8;
   private static final int field_32858 = 20;
   private static final float DEFAULT_DRAWN_STRING_SIZE = 0.02F;
   private static final int WHITE = -1;
   private static final int YELLOW = -256;
   private static final int ORANGE = -23296;
   private static final int GREEN = -16711936;
   private static final int GRAY = -3355444;
   private static final int PINK = -98404;
   private static final int RED = -65536;
   private final MinecraftClient client;
   private final Map hives = Maps.newHashMap();
   private final Map bees = Maps.newHashMap();
   private UUID targetedEntity;

   public BeeDebugRenderer(MinecraftClient client) {
      this.client = client;
   }

   public void clear() {
      this.hives.clear();
      this.bees.clear();
      this.targetedEntity = null;
   }

   public void addHive(Hive hive) {
      this.hives.put(hive.pos, hive);
   }

   public void addBee(Bee bee) {
      this.bees.put(bee.uuid, bee);
   }

   public void removeBee(int id) {
      this.bees.values().removeIf((bee) -> {
         return bee.entityId == id;
      });
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      this.removeOutdatedHives();
      this.removeInvalidBees();
      this.render(matrices, vertexConsumers);
      if (!this.client.player.isSpectator()) {
         this.updateTargetedEntity();
      }

   }

   private void removeInvalidBees() {
      this.bees.entrySet().removeIf((bee) -> {
         return this.client.world.getEntityById(((Bee)bee.getValue()).entityId) == null;
      });
   }

   private void removeOutdatedHives() {
      long l = this.client.world.getTime() - 20L;
      this.hives.entrySet().removeIf((hive) -> {
         return ((Hive)hive.getValue()).time < l;
      });
   }

   private void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
      BlockPos lv = this.getCameraPos().getBlockPos();
      this.bees.values().forEach((bee) -> {
         if (this.isInRange(bee)) {
            this.drawBee(matrices, vertexConsumers, bee);
         }

      });
      this.drawFlowers(matrices, vertexConsumers);
      Iterator var4 = this.hives.keySet().iterator();

      while(var4.hasNext()) {
         BlockPos lv2 = (BlockPos)var4.next();
         if (lv.isWithinDistance(lv2, 30.0)) {
            drawHive(matrices, vertexConsumers, lv2);
         }
      }

      Map map = this.getBlacklistingBees();
      this.hives.values().forEach((hive) -> {
         if (lv.isWithinDistance(hive.pos, 30.0)) {
            Set set = (Set)map.get(hive.pos);
            this.drawHiveInfo(matrices, vertexConsumers, hive, (Collection)(set == null ? Sets.newHashSet() : set));
         }

      });
      this.getBeesByHive().forEach((hive, bees) -> {
         if (lv.isWithinDistance(hive, 30.0)) {
            this.drawHiveBees(matrices, vertexConsumers, hive, bees);
         }

      });
   }

   private Map getBlacklistingBees() {
      Map map = Maps.newHashMap();
      this.bees.values().forEach((bee) -> {
         bee.blacklist.forEach((pos) -> {
            ((Set)map.computeIfAbsent(pos, (pos2) -> {
               return Sets.newHashSet();
            })).add(bee.getUuid());
         });
      });
      return map;
   }

   private void drawFlowers(MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
      Map map = Maps.newHashMap();
      this.bees.values().stream().filter(Bee::hasFlower).forEach((bee) -> {
         ((Set)map.computeIfAbsent(bee.flower, (flower) -> {
            return Sets.newHashSet();
         })).add(bee.getUuid());
      });
      map.entrySet().forEach((entry) -> {
         BlockPos lv = (BlockPos)entry.getKey();
         Set set = (Set)entry.getValue();
         Set set2 = (Set)set.stream().map(NameGenerator::name).collect(Collectors.toSet());
         int i = 1;
         drawString(matrices, vertexConsumers, set2.toString(), (BlockPos)lv, i++, -256);
         drawString(matrices, vertexConsumers, "Flower", (BlockPos)lv, i++, -1);
         float f = 0.05F;
         DebugRenderer.drawBox(matrices, vertexConsumers, lv, 0.05F, 0.8F, 0.8F, 0.0F, 0.3F);
      });
   }

   private static String toString(Collection bees) {
      if (bees.isEmpty()) {
         return "-";
      } else {
         return bees.size() > 3 ? bees.size() + " bees" : ((Set)bees.stream().map(NameGenerator::name).collect(Collectors.toSet())).toString();
      }
   }

   private static void drawHive(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockPos pos) {
      float f = 0.05F;
      DebugRenderer.drawBox(matrices, vertexConsumers, pos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
   }

   private void drawHiveBees(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockPos pos, List bees) {
      float f = 0.05F;
      DebugRenderer.drawBox(matrices, vertexConsumers, pos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
      drawString(matrices, vertexConsumers, "" + bees, (BlockPos)pos, 0, -256);
      drawString(matrices, vertexConsumers, "Ghost Hive", (BlockPos)pos, 1, -65536);
   }

   private void drawHiveInfo(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Hive hive, Collection blacklistingBees) {
      int i = 0;
      if (!blacklistingBees.isEmpty()) {
         drawString(matrices, vertexConsumers, "Blacklisted by " + toString(blacklistingBees), hive, i++, -65536);
      }

      drawString(matrices, vertexConsumers, "Out: " + toString(this.getBeesForHive(hive.pos)), hive, i++, -3355444);
      if (hive.beeCount == 0) {
         drawString(matrices, vertexConsumers, "In: -", (Hive)hive, i++, -256);
      } else if (hive.beeCount == 1) {
         drawString(matrices, vertexConsumers, "In: 1 bee", (Hive)hive, i++, -256);
      } else {
         drawString(matrices, vertexConsumers, "In: " + hive.beeCount + " bees", (Hive)hive, i++, -256);
      }

      int var6 = hive.honeyLevel;
      drawString(matrices, vertexConsumers, "Honey: " + var6, (Hive)hive, i++, -23296);
      drawString(matrices, vertexConsumers, hive.label + (hive.sedated ? " (sedated)" : ""), (Hive)hive, i++, -1);
   }

   private void drawPath(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Bee bee) {
      if (bee.path != null) {
         PathfindingDebugRenderer.drawPath(matrices, vertexConsumers, bee.path, 0.5F, false, false, this.getCameraPos().getPos().getX(), this.getCameraPos().getPos().getY(), this.getCameraPos().getPos().getZ());
      }

   }

   private void drawBee(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Bee bee) {
      boolean bl = this.isTargeted(bee);
      int i = 0;
      drawString(matrices, vertexConsumers, bee.position, i++, bee.toString(), -1, 0.03F);
      if (bee.hive == null) {
         drawString(matrices, vertexConsumers, bee.position, i++, "No hive", -98404, 0.02F);
      } else {
         drawString(matrices, vertexConsumers, bee.position, i++, "Hive: " + this.getPositionString(bee, bee.hive), -256, 0.02F);
      }

      if (bee.flower == null) {
         drawString(matrices, vertexConsumers, bee.position, i++, "No flower", -98404, 0.02F);
      } else {
         drawString(matrices, vertexConsumers, bee.position, i++, "Flower: " + this.getPositionString(bee, bee.flower), -256, 0.02F);
      }

      Iterator var6 = bee.labels.iterator();

      while(var6.hasNext()) {
         String string = (String)var6.next();
         drawString(matrices, vertexConsumers, bee.position, i++, string, -16711936, 0.02F);
      }

      if (bl) {
         this.drawPath(matrices, vertexConsumers, bee);
      }

      if (bee.travelTicks > 0) {
         int j = bee.travelTicks < 600 ? -3355444 : -23296;
         drawString(matrices, vertexConsumers, bee.position, i++, "Travelling: " + bee.travelTicks + " ticks", j, 0.02F);
      }

   }

   private static void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, String string, Hive hive, int line, int color) {
      BlockPos lv = hive.pos;
      drawString(matrices, vertexConsumers, string, lv, line, color);
   }

   private static void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, String string, BlockPos pos, int line, int color) {
      double d = 1.3;
      double e = 0.2;
      double f = (double)pos.getX() + 0.5;
      double g = (double)pos.getY() + 1.3 + (double)line * 0.2;
      double h = (double)pos.getZ() + 0.5;
      DebugRenderer.drawString(matrices, vertexConsumers, string, f, g, h, color, 0.02F, true, 0.0F, true);
   }

   private static void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Position pos, int line, String string, int color, float size) {
      double d = 2.4;
      double e = 0.25;
      BlockPos lv = BlockPos.ofFloored(pos);
      double g = (double)lv.getX() + 0.5;
      double h = pos.getY() + 2.4 + (double)line * 0.25;
      double k = (double)lv.getZ() + 0.5;
      float l = 0.5F;
      DebugRenderer.drawString(matrices, vertexConsumers, string, g, h, k, color, size, false, 0.5F, true);
   }

   private Camera getCameraPos() {
      return this.client.gameRenderer.getCamera();
   }

   private Set getBeeNamesForHive(Hive hive) {
      return (Set)this.getBeesForHive(hive.pos).stream().map(NameGenerator::name).collect(Collectors.toSet());
   }

   private String getPositionString(Bee bee, BlockPos pos) {
      double d = Math.sqrt(pos.getSquaredDistance(bee.position));
      double e = (double)Math.round(d * 10.0) / 10.0;
      String var10000 = pos.toShortString();
      return var10000 + " (dist " + e + ")";
   }

   private boolean isTargeted(Bee bee) {
      return Objects.equals(this.targetedEntity, bee.uuid);
   }

   private boolean isInRange(Bee bee) {
      PlayerEntity lv = this.client.player;
      BlockPos lv2 = BlockPos.ofFloored(lv.getX(), bee.position.getY(), lv.getZ());
      BlockPos lv3 = BlockPos.ofFloored(bee.position);
      return lv2.isWithinDistance(lv3, 30.0);
   }

   private Collection getBeesForHive(BlockPos hivePos) {
      return (Collection)this.bees.values().stream().filter((bee) -> {
         return bee.isHiveAt(hivePos);
      }).map(Bee::getUuid).collect(Collectors.toSet());
   }

   private Map getBeesByHive() {
      Map map = Maps.newHashMap();
      Iterator var2 = this.bees.values().iterator();

      while(var2.hasNext()) {
         Bee lv = (Bee)var2.next();
         if (lv.hive != null && !this.hives.containsKey(lv.hive)) {
            ((List)map.computeIfAbsent(lv.hive, (hive) -> {
               return Lists.newArrayList();
            })).add(lv.getName());
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
   public static class Hive {
      public final BlockPos pos;
      public final String label;
      public final int beeCount;
      public final int honeyLevel;
      public final boolean sedated;
      public final long time;

      public Hive(BlockPos pos, String label, int beeCount, int honeyLevel, boolean sedated, long time) {
         this.pos = pos;
         this.label = label;
         this.beeCount = beeCount;
         this.honeyLevel = honeyLevel;
         this.sedated = sedated;
         this.time = time;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Bee {
      public final UUID uuid;
      public final int entityId;
      public final Position position;
      @Nullable
      public final Path path;
      @Nullable
      public final BlockPos hive;
      @Nullable
      public final BlockPos flower;
      public final int travelTicks;
      public final List labels = Lists.newArrayList();
      public final Set blacklist = Sets.newHashSet();

      public Bee(UUID uuid, int entityId, Position position, @Nullable Path path, @Nullable BlockPos hive, @Nullable BlockPos flower, int travelTicks) {
         this.uuid = uuid;
         this.entityId = entityId;
         this.position = position;
         this.path = path;
         this.hive = hive;
         this.flower = flower;
         this.travelTicks = travelTicks;
      }

      public boolean isHiveAt(BlockPos pos) {
         return this.hive != null && this.hive.equals(pos);
      }

      public UUID getUuid() {
         return this.uuid;
      }

      public String getName() {
         return NameGenerator.name(this.uuid);
      }

      public String toString() {
         return this.getName();
      }

      public boolean hasFlower() {
         return this.flower != null;
      }
   }
}
