/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.spawner;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.block.enums.TrialSpawnerState;
import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.block.spawner.TrialSpawnerConfig;
import net.minecraft.block.spawner.TrialSpawnerLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class TrialSpawnerData {
    public static final String SPAWN_DATA_KEY = "spawn_data";
    private static final String NEXT_MOB_SPAWNS_AT_KEY = "next_mob_spawns_at";
    private static final int field_50190 = 20;
    private static final int field_50191 = 18000;
    public static MapCodec<TrialSpawnerData> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(Uuids.SET_CODEC.lenientOptionalFieldOf("registered_players", Sets.newHashSet()).forGetter(data -> data.players), Uuids.SET_CODEC.lenientOptionalFieldOf("current_mobs", Sets.newHashSet()).forGetter(data -> data.spawnedMobsAlive), Codec.LONG.lenientOptionalFieldOf("cooldown_ends_at", 0L).forGetter(data -> data.cooldownEnd), Codec.LONG.lenientOptionalFieldOf(NEXT_MOB_SPAWNS_AT_KEY, 0L).forGetter(data -> data.nextMobSpawnsAt), Codec.intRange(0, Integer.MAX_VALUE).lenientOptionalFieldOf("total_mobs_spawned", 0).forGetter(data -> data.totalSpawnedMobs), MobSpawnerEntry.CODEC.lenientOptionalFieldOf(SPAWN_DATA_KEY).forGetter(data -> data.spawnData), RegistryKey.createCodec(RegistryKeys.LOOT_TABLE).lenientOptionalFieldOf("ejecting_loot_table").forGetter(data -> data.rewardLootTable)).apply((Applicative<TrialSpawnerData, ?>)instance, TrialSpawnerData::new));
    protected final Set<UUID> players = new HashSet<UUID>();
    protected final Set<UUID> spawnedMobsAlive = new HashSet<UUID>();
    protected long cooldownEnd;
    protected long nextMobSpawnsAt;
    protected int totalSpawnedMobs;
    protected Optional<MobSpawnerEntry> spawnData;
    protected Optional<RegistryKey<LootTable>> rewardLootTable;
    @Nullable
    protected Entity displayEntity;
    @Nullable
    private DataPool<ItemStack> itemsToDropWhenOminous;
    protected double displayEntityRotation;
    protected double lastDisplayEntityRotation;

    public TrialSpawnerData() {
        this(Collections.emptySet(), Collections.emptySet(), 0L, 0L, 0, Optional.empty(), Optional.empty());
    }

    public TrialSpawnerData(Set<UUID> players, Set<UUID> spawnedMobsAlive, long cooldownEnd, long nextMobSpawnsAt, int totalSpawnedMobs, Optional<MobSpawnerEntry> spawnData, Optional<RegistryKey<LootTable>> rewardLootTable) {
        this.players.addAll(players);
        this.spawnedMobsAlive.addAll(spawnedMobsAlive);
        this.cooldownEnd = cooldownEnd;
        this.nextMobSpawnsAt = nextMobSpawnsAt;
        this.totalSpawnedMobs = totalSpawnedMobs;
        this.spawnData = spawnData;
        this.rewardLootTable = rewardLootTable;
    }

    public void reset() {
        this.players.clear();
        this.totalSpawnedMobs = 0;
        this.nextMobSpawnsAt = 0L;
        this.cooldownEnd = 0L;
        this.spawnedMobsAlive.clear();
    }

    public boolean hasSpawnData(TrialSpawnerLogic logic, Random random) {
        boolean bl = this.getSpawnData(logic, random).getNbt().contains("id", NbtElement.STRING_TYPE);
        return bl || !logic.getConfig().spawnPotentialsDefinition().isEmpty();
    }

    public boolean hasSpawnedAllMobs(TrialSpawnerConfig config, int additionalPlayers) {
        return this.totalSpawnedMobs >= config.getTotalMobs(additionalPlayers);
    }

    public boolean areMobsDead() {
        return this.spawnedMobsAlive.isEmpty();
    }

    public boolean canSpawnMore(ServerWorld world, TrialSpawnerConfig config, int additionalPlayers) {
        return world.getTime() >= this.nextMobSpawnsAt && this.spawnedMobsAlive.size() < config.getSimultaneousMobs(additionalPlayers);
    }

    public int getAdditionalPlayers(BlockPos pos) {
        if (this.players.isEmpty()) {
            Util.error("Trial Spawner at " + String.valueOf(pos) + " has no detected players");
        }
        return Math.max(0, this.players.size() - 1);
    }

    public void updatePlayers(ServerWorld world, BlockPos pos, TrialSpawnerLogic logic) {
        List<UUID> list2;
        boolean bl2;
        boolean bl;
        boolean bl3 = bl = (pos.asLong() + world.getTime()) % 20L != 0L;
        if (bl) {
            return;
        }
        if (logic.getSpawnerState().equals(TrialSpawnerState.COOLDOWN) && logic.isOminous()) {
            return;
        }
        List<UUID> list = logic.getEntityDetector().detect(world, logic.getEntitySelector(), pos, logic.getDetectionRadius(), true);
        if (logic.isOminous() || list.isEmpty()) {
            bl2 = false;
        } else {
            Optional<Pair<PlayerEntity, RegistryEntry<StatusEffect>>> optional = TrialSpawnerData.method_60789(world, list);
            optional.ifPresent(pair -> {
                PlayerEntity lv = (PlayerEntity)pair.getFirst();
                if (pair.getSecond() == StatusEffects.BAD_OMEN) {
                    TrialSpawnerData.applyTrialOmen(lv);
                }
                world.syncWorldEvent(WorldEvents.TRIAL_SPAWNER_TURNS_OMINOUS, BlockPos.ofFloored(lv.getEyePos()), 0);
                logic.setOminous(world, pos);
            });
            bl2 = optional.isPresent();
        }
        if (logic.getSpawnerState().equals(TrialSpawnerState.COOLDOWN) && !bl2) {
            return;
        }
        boolean bl32 = logic.getData().players.isEmpty();
        List<UUID> list3 = list2 = bl32 ? list : logic.getEntityDetector().detect(world, logic.getEntitySelector(), pos, logic.getDetectionRadius(), false);
        if (this.players.addAll(list2)) {
            this.nextMobSpawnsAt = Math.max(world.getTime() + 40L, this.nextMobSpawnsAt);
            if (!bl2) {
                int i = logic.isOminous() ? WorldEvents.OMINOUS_TRIAL_SPAWNER_DETECTS_PLAYER : WorldEvents.TRIAL_SPAWNER_DETECTS_PLAYER;
                world.syncWorldEvent(i, pos, this.players.size());
            }
        }
    }

    private static Optional<Pair<PlayerEntity, RegistryEntry<StatusEffect>>> method_60789(ServerWorld arg2, List<UUID> list) {
        PlayerEntity lv = null;
        for (UUID uUID : list) {
            PlayerEntity lv2 = arg2.getPlayerByUuid(uUID);
            if (lv2 == null) continue;
            RegistryEntry<StatusEffect> lv3 = StatusEffects.TRIAL_OMEN;
            if (lv2.hasStatusEffect(lv3)) {
                return Optional.of(Pair.of(lv2, lv3));
            }
            if (!lv2.hasStatusEffect(StatusEffects.BAD_OMEN)) continue;
            lv = lv2;
        }
        return Optional.ofNullable(lv).map(arg -> Pair.of(arg, StatusEffects.BAD_OMEN));
    }

    public void resetAndClearMobs(TrialSpawnerLogic logic, ServerWorld world) {
        this.spawnedMobsAlive.stream().map(world::getEntity).forEach(entity -> {
            if (entity == null) {
                return;
            }
            world.syncWorldEvent(WorldEvents.TRIAL_SPAWNER_SPAWNS_MOB_AT_SPAWN_POS, entity.getBlockPos(), TrialSpawnerLogic.Type.NORMAL.getIndex());
            entity.remove(Entity.RemovalReason.DISCARDED);
        });
        if (!logic.getOminousConfig().spawnPotentialsDefinition().isEmpty()) {
            this.spawnData = Optional.empty();
        }
        this.totalSpawnedMobs = 0;
        this.spawnedMobsAlive.clear();
        this.nextMobSpawnsAt = world.getTime() + (long)logic.getOminousConfig().ticksBetweenSpawn();
        logic.updateListeners();
        this.cooldownEnd = world.getTime() + logic.getOminousConfig().getCooldownLength();
    }

    private static void applyTrialOmen(PlayerEntity arg) {
        StatusEffectInstance lv = arg.getStatusEffect(StatusEffects.BAD_OMEN);
        if (lv == null) {
            return;
        }
        int i = lv.getAmplifier() + 1;
        int j = 18000 * i;
        arg.removeStatusEffect(StatusEffects.BAD_OMEN);
        arg.addStatusEffect(new StatusEffectInstance(StatusEffects.TRIAL_OMEN, j, 0));
    }

    public boolean isCooldownPast(ServerWorld world, float f, int i) {
        long l = this.cooldownEnd - (long)i;
        return (float)world.getTime() >= (float)l + f;
    }

    public boolean isCooldownAtRepeating(ServerWorld world, float f, int i) {
        long l = this.cooldownEnd - (long)i;
        return (float)(world.getTime() - l) % f == 0.0f;
    }

    public boolean isCooldownOver(ServerWorld world) {
        return world.getTime() >= this.cooldownEnd;
    }

    public void setEntityType(TrialSpawnerLogic logic, Random random, EntityType<?> type) {
        this.getSpawnData(logic, random).getNbt().putString("id", Registries.ENTITY_TYPE.getId(type).toString());
    }

    protected MobSpawnerEntry getSpawnData(TrialSpawnerLogic logic, Random random) {
        if (this.spawnData.isPresent()) {
            return this.spawnData.get();
        }
        DataPool<MobSpawnerEntry> lv = logic.getConfig().spawnPotentialsDefinition();
        Optional<MobSpawnerEntry> optional = lv.isEmpty() ? this.spawnData : lv.getOrEmpty(random).map(Weighted.Present::data);
        this.spawnData = Optional.of(optional.orElseGet(MobSpawnerEntry::new));
        logic.updateListeners();
        return this.spawnData.get();
    }

    @Nullable
    public Entity setDisplayEntity(TrialSpawnerLogic logic, World world, TrialSpawnerState state) {
        NbtCompound lv;
        if (!state.doesDisplayRotate()) {
            return null;
        }
        if (this.displayEntity == null && (lv = this.getSpawnData(logic, world.getRandom()).getNbt()).contains("id", NbtElement.STRING_TYPE)) {
            this.displayEntity = EntityType.loadEntityWithPassengers(lv, world, Function.identity());
        }
        return this.displayEntity;
    }

    public NbtCompound getSpawnDataNbt(TrialSpawnerState state) {
        NbtCompound lv = new NbtCompound();
        if (state == TrialSpawnerState.ACTIVE) {
            lv.putLong(NEXT_MOB_SPAWNS_AT_KEY, this.nextMobSpawnsAt);
        }
        this.spawnData.ifPresent(spawnData -> lv.put(SPAWN_DATA_KEY, MobSpawnerEntry.CODEC.encodeStart(NbtOps.INSTANCE, (MobSpawnerEntry)spawnData).result().orElseThrow(() -> new IllegalStateException("Invalid SpawnData"))));
        return lv;
    }

    public double getDisplayEntityRotation() {
        return this.displayEntityRotation;
    }

    public double getLastDisplayEntityRotation() {
        return this.lastDisplayEntityRotation;
    }

    DataPool<ItemStack> getItemsToDropWhenOminous(ServerWorld world, TrialSpawnerConfig config, BlockPos pos) {
        long l;
        LootContextParameterSet lv2;
        if (this.itemsToDropWhenOminous != null) {
            return this.itemsToDropWhenOminous;
        }
        LootTable lv = world.getServer().getReloadableRegistries().getLootTable(config.itemsToDropWhenOminous());
        ObjectArrayList<ItemStack> objectArrayList = lv.generateLoot(lv2 = new LootContextParameterSet.Builder(world).build(LootContextTypes.EMPTY), l = TrialSpawnerData.getLootSeed(world, pos));
        if (objectArrayList.isEmpty()) {
            return DataPool.empty();
        }
        DataPool.Builder<ItemStack> lv3 = new DataPool.Builder<ItemStack>();
        for (ItemStack lv4 : objectArrayList) {
            lv3.add(lv4.copyWithCount(1), lv4.getCount());
        }
        this.itemsToDropWhenOminous = lv3.build();
        return this.itemsToDropWhenOminous;
    }

    private static long getLootSeed(ServerWorld world, BlockPos pos) {
        BlockPos lv = new BlockPos(MathHelper.floor((float)pos.getX() / 30.0f), MathHelper.floor((float)pos.getY() / 20.0f), MathHelper.floor((float)pos.getZ() / 30.0f));
        return world.getSeed() + lv.asLong();
    }
}

