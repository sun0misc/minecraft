/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.debug;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.debug.PathfindingDebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.custom.DebugBrainCustomPayload;
import net.minecraft.util.NameGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class VillageDebugRenderer
implements DebugRenderer.Renderer {
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
    private static final float DEFAULT_DRAWN_STRING_SIZE = 0.02f;
    private static final int WHITE = -1;
    private static final int YELLOW = -256;
    private static final int AQUA = -16711681;
    private static final int GREEN = -16711936;
    private static final int GRAY = -3355444;
    private static final int PINK = -98404;
    private static final int RED = -65536;
    private static final int ORANGE = -23296;
    private final MinecraftClient client;
    private final Map<BlockPos, PointOfInterest> pointsOfInterest = Maps.newHashMap();
    private final Map<UUID, DebugBrainCustomPayload.Brain> brains = Maps.newHashMap();
    @Nullable
    private UUID targetedEntity;

    public VillageDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
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
        PointOfInterest lv = this.pointsOfInterest.get(pos);
        if (lv == null) {
            LOGGER.warn("Strange, setFreeTicketCount was called for an unknown POI: {}", (Object)pos);
            return;
        }
        lv.freeTicketCount = freeTicketCount;
    }

    public void addBrain(DebugBrainCustomPayload.Brain brain) {
        this.brains.put(brain.uuid(), brain);
    }

    public void removeBrain(int entityId) {
        this.brains.values().removeIf(brain -> brain.entityId() == entityId);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        this.removeRemovedBrains();
        this.draw(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
        if (!this.client.player.isSpectator()) {
            this.updateTargetedEntity();
        }
    }

    private void removeRemovedBrains() {
        this.brains.entrySet().removeIf(entry -> {
            Entity lv = this.client.world.getEntityById(((DebugBrainCustomPayload.Brain)entry.getValue()).entityId());
            return lv == null || lv.isRemoved();
        });
    }

    private void draw(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double x, double y, double z) {
        BlockPos lv = BlockPos.ofFloored(x, y, z);
        this.brains.values().forEach(brain -> {
            if (this.isClose((DebugBrainCustomPayload.Brain)brain)) {
                this.drawBrain(matrices, vertexConsumers, (DebugBrainCustomPayload.Brain)brain, x, y, z);
            }
        });
        for (BlockPos lv2 : this.pointsOfInterest.keySet()) {
            if (!lv.isWithinDistance(lv2, 30.0)) continue;
            VillageDebugRenderer.drawPointOfInterest(matrices, vertexConsumers, lv2);
        }
        this.pointsOfInterest.values().forEach(poi -> {
            if (lv.isWithinDistance(poi.pos, 30.0)) {
                this.drawPointOfInterestInfo(matrices, vertexConsumers, (PointOfInterest)poi);
            }
        });
        this.getGhostPointsOfInterest().forEach((pos, brains) -> {
            if (lv.isWithinDistance((Vec3i)pos, 30.0)) {
                this.drawGhostPointOfInterest(matrices, vertexConsumers, (BlockPos)pos, (List<String>)brains);
            }
        });
    }

    private static void drawPointOfInterest(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockPos pos) {
        float f = 0.05f;
        DebugRenderer.drawBox(matrices, vertexConsumers, pos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
    }

    private void drawGhostPointOfInterest(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockPos pos, List<String> brains) {
        float f = 0.05f;
        DebugRenderer.drawBox(matrices, vertexConsumers, pos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
        VillageDebugRenderer.drawString(matrices, vertexConsumers, String.valueOf(brains), pos, 0, -256);
        VillageDebugRenderer.drawString(matrices, vertexConsumers, "Ghost POI", pos, 1, -65536);
    }

    private void drawPointOfInterestInfo(MatrixStack matrices, VertexConsumerProvider vertexConsumers, PointOfInterest pointOfInterest) {
        int i = 0;
        Set<String> set = this.getNamesOfPointOfInterestTicketHolders(pointOfInterest);
        if (set.size() < 4) {
            VillageDebugRenderer.drawString(matrices, vertexConsumers, "Owners: " + String.valueOf(set), pointOfInterest, i, -256);
        } else {
            VillageDebugRenderer.drawString(matrices, vertexConsumers, set.size() + " ticket holders", pointOfInterest, i, -256);
        }
        ++i;
        Set<String> set2 = this.getNamesOfJobSitePotentialOwners(pointOfInterest);
        if (set2.size() < 4) {
            VillageDebugRenderer.drawString(matrices, vertexConsumers, "Candidates: " + String.valueOf(set2), pointOfInterest, i, -23296);
        } else {
            VillageDebugRenderer.drawString(matrices, vertexConsumers, set2.size() + " potential owners", pointOfInterest, i, -23296);
        }
        VillageDebugRenderer.drawString(matrices, vertexConsumers, "Free tickets: " + pointOfInterest.freeTicketCount, pointOfInterest, ++i, -256);
        VillageDebugRenderer.drawString(matrices, vertexConsumers, pointOfInterest.type, pointOfInterest, ++i, -1);
    }

    private void drawPath(MatrixStack matrices, VertexConsumerProvider vertexConsumers, DebugBrainCustomPayload.Brain brain, double cameraX, double cameraY, double cameraZ) {
        if (brain.path() != null) {
            PathfindingDebugRenderer.drawPath(matrices, vertexConsumers, brain.path(), 0.5f, false, false, cameraX, cameraY, cameraZ);
        }
    }

    private void drawBrain(MatrixStack matrices, VertexConsumerProvider vertexConsumers, DebugBrainCustomPayload.Brain brain, double cameraX, double cameraY, double cameraZ) {
        boolean bl = this.isTargeted(brain);
        int i = 0;
        VillageDebugRenderer.drawString(matrices, vertexConsumers, brain.pos(), i, brain.name(), -1, 0.03f);
        ++i;
        if (bl) {
            VillageDebugRenderer.drawString(matrices, vertexConsumers, brain.pos(), i, brain.profession() + " " + brain.xp() + " xp", -1, 0.02f);
            ++i;
        }
        if (bl) {
            int j = brain.health() < brain.maxHealth() ? -23296 : -1;
            VillageDebugRenderer.drawString(matrices, vertexConsumers, brain.pos(), i, "health: " + String.format(Locale.ROOT, "%.1f", Float.valueOf(brain.health())) + " / " + String.format(Locale.ROOT, "%.1f", Float.valueOf(brain.maxHealth())), j, 0.02f);
            ++i;
        }
        if (bl && !brain.inventory().equals("")) {
            VillageDebugRenderer.drawString(matrices, vertexConsumers, brain.pos(), i, brain.inventory(), -98404, 0.02f);
            ++i;
        }
        if (bl) {
            for (String string : brain.runningTasks()) {
                VillageDebugRenderer.drawString(matrices, vertexConsumers, brain.pos(), i, string, -16711681, 0.02f);
                ++i;
            }
        }
        if (bl) {
            for (String string : brain.possibleActivities()) {
                VillageDebugRenderer.drawString(matrices, vertexConsumers, brain.pos(), i, string, -16711936, 0.02f);
                ++i;
            }
        }
        if (brain.wantsGolem()) {
            VillageDebugRenderer.drawString(matrices, vertexConsumers, brain.pos(), i, "Wants Golem", -23296, 0.02f);
            ++i;
        }
        if (bl && brain.angerLevel() != -1) {
            VillageDebugRenderer.drawString(matrices, vertexConsumers, brain.pos(), i, "Anger Level: " + brain.angerLevel(), -98404, 0.02f);
            ++i;
        }
        if (bl) {
            for (String string : brain.gossips()) {
                if (string.startsWith(brain.name())) {
                    VillageDebugRenderer.drawString(matrices, vertexConsumers, brain.pos(), i, string, -1, 0.02f);
                } else {
                    VillageDebugRenderer.drawString(matrices, vertexConsumers, brain.pos(), i, string, -23296, 0.02f);
                }
                ++i;
            }
        }
        if (bl) {
            for (String string : Lists.reverse(brain.memories())) {
                VillageDebugRenderer.drawString(matrices, vertexConsumers, brain.pos(), i, string, -3355444, 0.02f);
                ++i;
            }
        }
        if (bl) {
            this.drawPath(matrices, vertexConsumers, brain, cameraX, cameraY, cameraZ);
        }
    }

    private static void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, String string, PointOfInterest pointOfInterest, int offsetY, int color) {
        VillageDebugRenderer.drawString(matrices, vertexConsumers, string, pointOfInterest.pos, offsetY, color);
    }

    private static void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, String string, BlockPos pos, int offsetY, int color) {
        double d = 1.3;
        double e = 0.2;
        double f = (double)pos.getX() + 0.5;
        double g = (double)pos.getY() + 1.3 + (double)offsetY * 0.2;
        double h = (double)pos.getZ() + 0.5;
        DebugRenderer.drawString(matrices, vertexConsumers, string, f, g, h, color, 0.02f, true, 0.0f, true);
    }

    private static void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Position pos, int offsetY, String string, int color, float size) {
        double d = 2.4;
        double e = 0.25;
        BlockPos lv = BlockPos.ofFloored(pos);
        double g = (double)lv.getX() + 0.5;
        double h = pos.getY() + 2.4 + (double)offsetY * 0.25;
        double k = (double)lv.getZ() + 0.5;
        float l = 0.5f;
        DebugRenderer.drawString(matrices, vertexConsumers, string, g, h, k, color, size, false, 0.5f, true);
    }

    private Set<String> getNamesOfPointOfInterestTicketHolders(PointOfInterest pointOfInterest) {
        return this.getBrainsContainingPointOfInterest(pointOfInterest.pos).stream().map(NameGenerator::name).collect(Collectors.toSet());
    }

    private Set<String> getNamesOfJobSitePotentialOwners(PointOfInterest potentialJobSite) {
        return this.getBrainsContainingPotentialJobSite(potentialJobSite.pos).stream().map(NameGenerator::name).collect(Collectors.toSet());
    }

    private boolean isTargeted(DebugBrainCustomPayload.Brain brain) {
        return Objects.equals(this.targetedEntity, brain.uuid());
    }

    private boolean isClose(DebugBrainCustomPayload.Brain brain) {
        ClientPlayerEntity lv = this.client.player;
        BlockPos lv2 = BlockPos.ofFloored(lv.getX(), brain.pos().getY(), lv.getZ());
        BlockPos lv3 = BlockPos.ofFloored(brain.pos());
        return lv2.isWithinDistance(lv3, 30.0);
    }

    private Collection<UUID> getBrainsContainingPointOfInterest(BlockPos pointOfInterest) {
        return this.brains.values().stream().filter(brain -> brain.isPointOfInterest(pointOfInterest)).map(DebugBrainCustomPayload.Brain::uuid).collect(Collectors.toSet());
    }

    private Collection<UUID> getBrainsContainingPotentialJobSite(BlockPos potentialJobSite) {
        return this.brains.values().stream().filter(brain -> brain.isPotentialJobSite(potentialJobSite)).map(DebugBrainCustomPayload.Brain::uuid).collect(Collectors.toSet());
    }

    private Map<BlockPos, List<String>> getGhostPointsOfInterest() {
        HashMap<BlockPos, List<String>> map = Maps.newHashMap();
        for (DebugBrainCustomPayload.Brain lv : this.brains.values()) {
            for (BlockPos lv2 : Iterables.concat(lv.pois(), lv.potentialPois())) {
                if (this.pointsOfInterest.containsKey(lv2)) continue;
                map.computeIfAbsent(lv2, pos -> Lists.newArrayList()).add(lv.name());
            }
        }
        return map;
    }

    private void updateTargetedEntity() {
        DebugRenderer.getTargetedEntity(this.client.getCameraEntity(), 8).ifPresent(entity -> {
            this.targetedEntity = entity.getUuid();
        });
    }

    @Environment(value=EnvType.CLIENT)
    public static class PointOfInterest {
        public final BlockPos pos;
        public final String type;
        public int freeTicketCount;

        public PointOfInterest(BlockPos pos, String type, int freeTicketCount) {
            this.pos = pos;
            this.type = type;
            this.freeTicketCount = freeTicketCount;
        }
    }
}

