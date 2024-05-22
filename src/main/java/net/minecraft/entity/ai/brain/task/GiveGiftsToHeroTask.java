/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerProfession;

public class GiveGiftsToHeroTask
extends MultiTickTask<VillagerEntity> {
    private static final int MAX_DISTANCE = 5;
    private static final int DEFAULT_DURATION = 600;
    private static final int MAX_NEXT_GIFT_DELAY = 6600;
    private static final int RUN_TIME = 20;
    private static final Map<VillagerProfession, RegistryKey<LootTable>> GIFTS = Util.make(Maps.newHashMap(), gifts -> {
        gifts.put(VillagerProfession.ARMORER, LootTables.HERO_OF_THE_VILLAGE_ARMORER_GIFT_GAMEPLAY);
        gifts.put(VillagerProfession.BUTCHER, LootTables.HERO_OF_THE_VILLAGE_BUTCHER_GIFT_GAMEPLAY);
        gifts.put(VillagerProfession.CARTOGRAPHER, LootTables.HERO_OF_THE_VILLAGE_CARTOGRAPHER_GIFT_GAMEPLAY);
        gifts.put(VillagerProfession.CLERIC, LootTables.HERO_OF_THE_VILLAGE_CLERIC_GIFT_GAMEPLAY);
        gifts.put(VillagerProfession.FARMER, LootTables.HERO_OF_THE_VILLAGE_FARMER_GIFT_GAMEPLAY);
        gifts.put(VillagerProfession.FISHERMAN, LootTables.HERO_OF_THE_VILLAGE_FISHERMAN_GIFT_GAMEPLAY);
        gifts.put(VillagerProfession.FLETCHER, LootTables.HERO_OF_THE_VILLAGE_FLETCHER_GIFT_GAMEPLAY);
        gifts.put(VillagerProfession.LEATHERWORKER, LootTables.HERO_OF_THE_VILLAGE_LEATHERWORKER_GIFT_GAMEPLAY);
        gifts.put(VillagerProfession.LIBRARIAN, LootTables.HERO_OF_THE_VILLAGE_LIBRARIAN_GIFT_GAMEPLAY);
        gifts.put(VillagerProfession.MASON, LootTables.HERO_OF_THE_VILLAGE_MASON_GIFT_GAMEPLAY);
        gifts.put(VillagerProfession.SHEPHERD, LootTables.HERO_OF_THE_VILLAGE_SHEPHERD_GIFT_GAMEPLAY);
        gifts.put(VillagerProfession.TOOLSMITH, LootTables.HERO_OF_THE_VILLAGE_TOOLSMITH_GIFT_GAMEPLAY);
        gifts.put(VillagerProfession.WEAPONSMITH, LootTables.HERO_OF_THE_VILLAGE_WEAPONSMITH_GIFT_GAMEPLAY);
    });
    private static final float WALK_SPEED = 0.5f;
    private int ticksLeft = 600;
    private boolean done;
    private long startTime;

    public GiveGiftsToHeroTask(int delay) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.INTERACTION_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleState.VALUE_PRESENT), delay);
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, VillagerEntity arg2) {
        if (!this.isNearestPlayerHero(arg2)) {
            return false;
        }
        if (this.ticksLeft > 0) {
            --this.ticksLeft;
            return false;
        }
        return true;
    }

    @Override
    protected void run(ServerWorld arg, VillagerEntity arg2, long l) {
        this.done = false;
        this.startTime = l;
        PlayerEntity lv = this.getNearestPlayerIfHero(arg2).get();
        arg2.getBrain().remember(MemoryModuleType.INTERACTION_TARGET, lv);
        LookTargetUtil.lookAt(arg2, lv);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        return this.isNearestPlayerHero(arg2) && !this.done;
    }

    @Override
    protected void keepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        PlayerEntity lv = this.getNearestPlayerIfHero(arg2).get();
        LookTargetUtil.lookAt(arg2, lv);
        if (this.isCloseEnough(arg2, lv)) {
            if (l - this.startTime > 20L) {
                this.giveGifts(arg2, lv);
                this.done = true;
            }
        } else {
            LookTargetUtil.walkTowards((LivingEntity)arg2, lv, 0.5f, 5);
        }
    }

    @Override
    protected void finishRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        this.ticksLeft = GiveGiftsToHeroTask.getNextGiftDelay(arg);
        arg2.getBrain().forget(MemoryModuleType.INTERACTION_TARGET);
        arg2.getBrain().forget(MemoryModuleType.WALK_TARGET);
        arg2.getBrain().forget(MemoryModuleType.LOOK_TARGET);
    }

    private void giveGifts(VillagerEntity villager, LivingEntity recipient) {
        List<ItemStack> list = this.getGifts(villager);
        for (ItemStack lv : list) {
            LookTargetUtil.give(villager, lv, recipient.getPos());
        }
    }

    private List<ItemStack> getGifts(VillagerEntity villager) {
        if (villager.isBaby()) {
            return ImmutableList.of(new ItemStack(Items.POPPY));
        }
        VillagerProfession lv = villager.getVillagerData().getProfession();
        if (GIFTS.containsKey(lv)) {
            LootTable lv2 = villager.getWorld().getServer().getReloadableRegistries().getLootTable(GIFTS.get(lv));
            LootContextParameterSet lv3 = new LootContextParameterSet.Builder((ServerWorld)villager.getWorld()).add(LootContextParameters.ORIGIN, villager.getPos()).add(LootContextParameters.THIS_ENTITY, villager).build(LootContextTypes.GIFT);
            return lv2.generateLoot(lv3);
        }
        return ImmutableList.of(new ItemStack(Items.WHEAT_SEEDS));
    }

    private boolean isNearestPlayerHero(VillagerEntity villager) {
        return this.getNearestPlayerIfHero(villager).isPresent();
    }

    private Optional<PlayerEntity> getNearestPlayerIfHero(VillagerEntity villager) {
        return villager.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).filter(this::isHero);
    }

    private boolean isHero(PlayerEntity player) {
        return player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE);
    }

    private boolean isCloseEnough(VillagerEntity villager, PlayerEntity player) {
        BlockPos lv = player.getBlockPos();
        BlockPos lv2 = villager.getBlockPos();
        return lv2.isWithinDistance(lv, 5.0);
    }

    private static int getNextGiftDelay(ServerWorld world) {
        return 600 + world.random.nextInt(6001);
    }

    @Override
    protected /* synthetic */ boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
        return this.shouldKeepRunning(world, (VillagerEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (VillagerEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (VillagerEntity)entity, time);
    }
}

