/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.entity;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.block.entity.CalibratedSculkSensorBlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.ComparatorBlockEntity;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.entity.SculkCatalystBlockEntity;
import net.minecraft.block.entity.SculkSensorBlockEntity;
import net.minecraft.block.entity.SculkShriekerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.block.entity.SmokerBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class BlockEntityType<T extends BlockEntity> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final BlockEntityType<FurnaceBlockEntity> FURNACE = BlockEntityType.create("furnace", Builder.create(FurnaceBlockEntity::new, Blocks.FURNACE));
    public static final BlockEntityType<ChestBlockEntity> CHEST = BlockEntityType.create("chest", Builder.create(ChestBlockEntity::new, Blocks.CHEST));
    public static final BlockEntityType<TrappedChestBlockEntity> TRAPPED_CHEST = BlockEntityType.create("trapped_chest", Builder.create(TrappedChestBlockEntity::new, Blocks.TRAPPED_CHEST));
    public static final BlockEntityType<EnderChestBlockEntity> ENDER_CHEST = BlockEntityType.create("ender_chest", Builder.create(EnderChestBlockEntity::new, Blocks.ENDER_CHEST));
    public static final BlockEntityType<JukeboxBlockEntity> JUKEBOX = BlockEntityType.create("jukebox", Builder.create(JukeboxBlockEntity::new, Blocks.JUKEBOX));
    public static final BlockEntityType<DispenserBlockEntity> DISPENSER = BlockEntityType.create("dispenser", Builder.create(DispenserBlockEntity::new, Blocks.DISPENSER));
    public static final BlockEntityType<DropperBlockEntity> DROPPER = BlockEntityType.create("dropper", Builder.create(DropperBlockEntity::new, Blocks.DROPPER));
    public static final BlockEntityType<SignBlockEntity> SIGN = BlockEntityType.create("sign", Builder.create(SignBlockEntity::new, Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.BIRCH_SIGN, Blocks.ACACIA_SIGN, Blocks.CHERRY_SIGN, Blocks.JUNGLE_SIGN, Blocks.DARK_OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.CHERRY_WALL_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN, Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN, Blocks.MANGROVE_SIGN, Blocks.MANGROVE_WALL_SIGN, Blocks.BAMBOO_SIGN, Blocks.BAMBOO_WALL_SIGN));
    public static final BlockEntityType<HangingSignBlockEntity> HANGING_SIGN = BlockEntityType.create("hanging_sign", Builder.create(HangingSignBlockEntity::new, Blocks.OAK_HANGING_SIGN, Blocks.SPRUCE_HANGING_SIGN, Blocks.BIRCH_HANGING_SIGN, Blocks.ACACIA_HANGING_SIGN, Blocks.CHERRY_HANGING_SIGN, Blocks.JUNGLE_HANGING_SIGN, Blocks.DARK_OAK_HANGING_SIGN, Blocks.CRIMSON_HANGING_SIGN, Blocks.WARPED_HANGING_SIGN, Blocks.MANGROVE_HANGING_SIGN, Blocks.BAMBOO_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN, Blocks.CRIMSON_WALL_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN, Blocks.MANGROVE_WALL_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN));
    public static final BlockEntityType<MobSpawnerBlockEntity> MOB_SPAWNER = BlockEntityType.create("mob_spawner", Builder.create(MobSpawnerBlockEntity::new, Blocks.SPAWNER));
    public static final BlockEntityType<PistonBlockEntity> PISTON = BlockEntityType.create("piston", Builder.create(PistonBlockEntity::new, Blocks.MOVING_PISTON));
    public static final BlockEntityType<BrewingStandBlockEntity> BREWING_STAND = BlockEntityType.create("brewing_stand", Builder.create(BrewingStandBlockEntity::new, Blocks.BREWING_STAND));
    public static final BlockEntityType<EnchantingTableBlockEntity> ENCHANTING_TABLE = BlockEntityType.create("enchanting_table", Builder.create(EnchantingTableBlockEntity::new, Blocks.ENCHANTING_TABLE));
    public static final BlockEntityType<EndPortalBlockEntity> END_PORTAL = BlockEntityType.create("end_portal", Builder.create(EndPortalBlockEntity::new, Blocks.END_PORTAL));
    public static final BlockEntityType<BeaconBlockEntity> BEACON = BlockEntityType.create("beacon", Builder.create(BeaconBlockEntity::new, Blocks.BEACON));
    public static final BlockEntityType<SkullBlockEntity> SKULL = BlockEntityType.create("skull", Builder.create(SkullBlockEntity::new, Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD, Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD, Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD, Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD));
    public static final BlockEntityType<DaylightDetectorBlockEntity> DAYLIGHT_DETECTOR = BlockEntityType.create("daylight_detector", Builder.create(DaylightDetectorBlockEntity::new, Blocks.DAYLIGHT_DETECTOR));
    public static final BlockEntityType<HopperBlockEntity> HOPPER = BlockEntityType.create("hopper", Builder.create(HopperBlockEntity::new, Blocks.HOPPER));
    public static final BlockEntityType<ComparatorBlockEntity> COMPARATOR = BlockEntityType.create("comparator", Builder.create(ComparatorBlockEntity::new, Blocks.COMPARATOR));
    public static final BlockEntityType<BannerBlockEntity> BANNER = BlockEntityType.create("banner", Builder.create(BannerBlockEntity::new, Blocks.WHITE_BANNER, Blocks.ORANGE_BANNER, Blocks.MAGENTA_BANNER, Blocks.LIGHT_BLUE_BANNER, Blocks.YELLOW_BANNER, Blocks.LIME_BANNER, Blocks.PINK_BANNER, Blocks.GRAY_BANNER, Blocks.LIGHT_GRAY_BANNER, Blocks.CYAN_BANNER, Blocks.PURPLE_BANNER, Blocks.BLUE_BANNER, Blocks.BROWN_BANNER, Blocks.GREEN_BANNER, Blocks.RED_BANNER, Blocks.BLACK_BANNER, Blocks.WHITE_WALL_BANNER, Blocks.ORANGE_WALL_BANNER, Blocks.MAGENTA_WALL_BANNER, Blocks.LIGHT_BLUE_WALL_BANNER, Blocks.YELLOW_WALL_BANNER, Blocks.LIME_WALL_BANNER, Blocks.PINK_WALL_BANNER, Blocks.GRAY_WALL_BANNER, Blocks.LIGHT_GRAY_WALL_BANNER, Blocks.CYAN_WALL_BANNER, Blocks.PURPLE_WALL_BANNER, Blocks.BLUE_WALL_BANNER, Blocks.BROWN_WALL_BANNER, Blocks.GREEN_WALL_BANNER, Blocks.RED_WALL_BANNER, Blocks.BLACK_WALL_BANNER));
    public static final BlockEntityType<StructureBlockBlockEntity> STRUCTURE_BLOCK = BlockEntityType.create("structure_block", Builder.create(StructureBlockBlockEntity::new, Blocks.STRUCTURE_BLOCK));
    public static final BlockEntityType<EndGatewayBlockEntity> END_GATEWAY = BlockEntityType.create("end_gateway", Builder.create(EndGatewayBlockEntity::new, Blocks.END_GATEWAY));
    public static final BlockEntityType<CommandBlockBlockEntity> COMMAND_BLOCK = BlockEntityType.create("command_block", Builder.create(CommandBlockBlockEntity::new, Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK));
    public static final BlockEntityType<ShulkerBoxBlockEntity> SHULKER_BOX = BlockEntityType.create("shulker_box", Builder.create(ShulkerBoxBlockEntity::new, Blocks.SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX));
    public static final BlockEntityType<BedBlockEntity> BED = BlockEntityType.create("bed", Builder.create(BedBlockEntity::new, Blocks.RED_BED, Blocks.BLACK_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.CYAN_BED, Blocks.GRAY_BED, Blocks.GREEN_BED, Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_GRAY_BED, Blocks.LIME_BED, Blocks.MAGENTA_BED, Blocks.ORANGE_BED, Blocks.PINK_BED, Blocks.PURPLE_BED, Blocks.WHITE_BED, Blocks.YELLOW_BED));
    public static final BlockEntityType<ConduitBlockEntity> CONDUIT = BlockEntityType.create("conduit", Builder.create(ConduitBlockEntity::new, Blocks.CONDUIT));
    public static final BlockEntityType<BarrelBlockEntity> BARREL = BlockEntityType.create("barrel", Builder.create(BarrelBlockEntity::new, Blocks.BARREL));
    public static final BlockEntityType<SmokerBlockEntity> SMOKER = BlockEntityType.create("smoker", Builder.create(SmokerBlockEntity::new, Blocks.SMOKER));
    public static final BlockEntityType<BlastFurnaceBlockEntity> BLAST_FURNACE = BlockEntityType.create("blast_furnace", Builder.create(BlastFurnaceBlockEntity::new, Blocks.BLAST_FURNACE));
    public static final BlockEntityType<LecternBlockEntity> LECTERN = BlockEntityType.create("lectern", Builder.create(LecternBlockEntity::new, Blocks.LECTERN));
    public static final BlockEntityType<BellBlockEntity> BELL = BlockEntityType.create("bell", Builder.create(BellBlockEntity::new, Blocks.BELL));
    public static final BlockEntityType<JigsawBlockEntity> JIGSAW = BlockEntityType.create("jigsaw", Builder.create(JigsawBlockEntity::new, Blocks.JIGSAW));
    public static final BlockEntityType<CampfireBlockEntity> CAMPFIRE = BlockEntityType.create("campfire", Builder.create(CampfireBlockEntity::new, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE));
    public static final BlockEntityType<BeehiveBlockEntity> BEEHIVE = BlockEntityType.create("beehive", Builder.create(BeehiveBlockEntity::new, Blocks.BEE_NEST, Blocks.BEEHIVE));
    public static final BlockEntityType<SculkSensorBlockEntity> SCULK_SENSOR = BlockEntityType.create("sculk_sensor", Builder.create(SculkSensorBlockEntity::new, Blocks.SCULK_SENSOR));
    public static final BlockEntityType<CalibratedSculkSensorBlockEntity> CALIBRATED_SCULK_SENSOR = BlockEntityType.create("calibrated_sculk_sensor", Builder.create(CalibratedSculkSensorBlockEntity::new, Blocks.CALIBRATED_SCULK_SENSOR));
    public static final BlockEntityType<SculkCatalystBlockEntity> SCULK_CATALYST = BlockEntityType.create("sculk_catalyst", Builder.create(SculkCatalystBlockEntity::new, Blocks.SCULK_CATALYST));
    public static final BlockEntityType<SculkShriekerBlockEntity> SCULK_SHRIEKER = BlockEntityType.create("sculk_shrieker", Builder.create(SculkShriekerBlockEntity::new, Blocks.SCULK_SHRIEKER));
    public static final BlockEntityType<ChiseledBookshelfBlockEntity> CHISELED_BOOKSHELF = BlockEntityType.create("chiseled_bookshelf", Builder.create(ChiseledBookshelfBlockEntity::new, Blocks.CHISELED_BOOKSHELF));
    public static final BlockEntityType<BrushableBlockEntity> BRUSHABLE_BLOCK = BlockEntityType.create("brushable_block", Builder.create(BrushableBlockEntity::new, Blocks.SUSPICIOUS_SAND, Blocks.SUSPICIOUS_GRAVEL));
    public static final BlockEntityType<DecoratedPotBlockEntity> DECORATED_POT = BlockEntityType.create("decorated_pot", Builder.create(DecoratedPotBlockEntity::new, Blocks.DECORATED_POT));
    public static final BlockEntityType<CrafterBlockEntity> CRAFTER = BlockEntityType.create("crafter", Builder.create(CrafterBlockEntity::new, Blocks.CRAFTER));
    public static final BlockEntityType<TrialSpawnerBlockEntity> TRIAL_SPAWNER = BlockEntityType.create("trial_spawner", Builder.create(TrialSpawnerBlockEntity::new, Blocks.TRIAL_SPAWNER));
    public static final BlockEntityType<VaultBlockEntity> VAULT = BlockEntityType.create("vault", Builder.create(VaultBlockEntity::new, Blocks.VAULT));
    private final BlockEntityFactory<? extends T> factory;
    private final Set<Block> blocks;
    private final Type<?> type;
    private final RegistryEntry.Reference<BlockEntityType<?>> registryEntry = Registries.BLOCK_ENTITY_TYPE.createEntry(this);

    @Nullable
    public static Identifier getId(BlockEntityType<?> type) {
        return Registries.BLOCK_ENTITY_TYPE.getId(type);
    }

    private static <T extends BlockEntity> BlockEntityType<T> create(String id, Builder<T> builder) {
        if (builder.blocks.isEmpty()) {
            LOGGER.warn("Block entity type {} requires at least one valid block to be defined!", (Object)id);
        }
        Type<?> type = Util.getChoiceType(TypeReferences.BLOCK_ENTITY, id);
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, builder.build(type));
    }

    public BlockEntityType(BlockEntityFactory<? extends T> factory, Set<Block> blocks, Type<?> type) {
        this.factory = factory;
        this.blocks = blocks;
        this.type = type;
    }

    @Nullable
    public T instantiate(BlockPos pos, BlockState state) {
        return this.factory.create(pos, state);
    }

    public boolean supports(BlockState state) {
        return this.blocks.contains(state.getBlock());
    }

    @Nullable
    public RegistryEntry.Reference<BlockEntityType<?>> getRegistryEntry() {
        return this.registryEntry;
    }

    @Nullable
    public T get(BlockView world, BlockPos pos) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv == null || lv.getType() != this) {
            return null;
        }
        return (T)lv;
    }

    public static final class Builder<T extends BlockEntity> {
        private final BlockEntityFactory<? extends T> factory;
        final Set<Block> blocks;

        private Builder(BlockEntityFactory<? extends T> factory, Set<Block> blocks) {
            this.factory = factory;
            this.blocks = blocks;
        }

        public static <T extends BlockEntity> Builder<T> create(BlockEntityFactory<? extends T> factory, Block ... blocks) {
            return new Builder<T>(factory, ImmutableSet.copyOf(blocks));
        }

        public BlockEntityType<T> build(Type<?> type) {
            return new BlockEntityType<T>(this.factory, this.blocks, type);
        }
    }

    @FunctionalInterface
    static interface BlockEntityFactory<T extends BlockEntity> {
        public T create(BlockPos var1, BlockState var2);
    }
}

