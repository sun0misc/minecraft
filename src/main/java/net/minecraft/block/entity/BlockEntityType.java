package net.minecraft.block.entity;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class BlockEntityType {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final BlockEntityType FURNACE;
   public static final BlockEntityType CHEST;
   public static final BlockEntityType TRAPPED_CHEST;
   public static final BlockEntityType ENDER_CHEST;
   public static final BlockEntityType JUKEBOX;
   public static final BlockEntityType DISPENSER;
   public static final BlockEntityType DROPPER;
   public static final BlockEntityType SIGN;
   public static final BlockEntityType HANGING_SIGN;
   public static final BlockEntityType MOB_SPAWNER;
   public static final BlockEntityType PISTON;
   public static final BlockEntityType BREWING_STAND;
   public static final BlockEntityType ENCHANTING_TABLE;
   public static final BlockEntityType END_PORTAL;
   public static final BlockEntityType BEACON;
   public static final BlockEntityType SKULL;
   public static final BlockEntityType DAYLIGHT_DETECTOR;
   public static final BlockEntityType HOPPER;
   public static final BlockEntityType COMPARATOR;
   public static final BlockEntityType BANNER;
   public static final BlockEntityType STRUCTURE_BLOCK;
   public static final BlockEntityType END_GATEWAY;
   public static final BlockEntityType COMMAND_BLOCK;
   public static final BlockEntityType SHULKER_BOX;
   public static final BlockEntityType BED;
   public static final BlockEntityType CONDUIT;
   public static final BlockEntityType BARREL;
   public static final BlockEntityType SMOKER;
   public static final BlockEntityType BLAST_FURNACE;
   public static final BlockEntityType LECTERN;
   public static final BlockEntityType BELL;
   public static final BlockEntityType JIGSAW;
   public static final BlockEntityType CAMPFIRE;
   public static final BlockEntityType BEEHIVE;
   public static final BlockEntityType SCULK_SENSOR;
   public static final BlockEntityType CALIBRATED_SCULK_SENSOR;
   public static final BlockEntityType SCULK_CATALYST;
   public static final BlockEntityType SCULK_SHRIEKER;
   public static final BlockEntityType CHISELED_BOOKSHELF;
   public static final BlockEntityType BRUSHABLE_BLOCK;
   public static final BlockEntityType DECORATED_POT;
   private final BlockEntityFactory factory;
   private final Set blocks;
   private final Type type;

   @Nullable
   public static Identifier getId(BlockEntityType type) {
      return Registries.BLOCK_ENTITY_TYPE.getId(type);
   }

   private static BlockEntityType create(String id, Builder builder) {
      if (builder.blocks.isEmpty()) {
         LOGGER.warn("Block entity type {} requires at least one valid block to be defined!", id);
      }

      Type type = Util.getChoiceType(TypeReferences.BLOCK_ENTITY, id);
      return (BlockEntityType)Registry.register(Registries.BLOCK_ENTITY_TYPE, (String)id, builder.build(type));
   }

   public BlockEntityType(BlockEntityFactory factory, Set blocks, Type type) {
      this.factory = factory;
      this.blocks = blocks;
      this.type = type;
   }

   @Nullable
   public BlockEntity instantiate(BlockPos pos, BlockState state) {
      return this.factory.create(pos, state);
   }

   public boolean supports(BlockState state) {
      return this.blocks.contains(state.getBlock());
   }

   @Nullable
   public BlockEntity get(BlockView world, BlockPos pos) {
      BlockEntity lv = world.getBlockEntity(pos);
      return lv != null && lv.getType() == this ? lv : null;
   }

   static {
      FURNACE = create("furnace", BlockEntityType.Builder.create(FurnaceBlockEntity::new, Blocks.FURNACE));
      CHEST = create("chest", BlockEntityType.Builder.create(ChestBlockEntity::new, Blocks.CHEST));
      TRAPPED_CHEST = create("trapped_chest", BlockEntityType.Builder.create(TrappedChestBlockEntity::new, Blocks.TRAPPED_CHEST));
      ENDER_CHEST = create("ender_chest", BlockEntityType.Builder.create(EnderChestBlockEntity::new, Blocks.ENDER_CHEST));
      JUKEBOX = create("jukebox", BlockEntityType.Builder.create(JukeboxBlockEntity::new, Blocks.JUKEBOX));
      DISPENSER = create("dispenser", BlockEntityType.Builder.create(DispenserBlockEntity::new, Blocks.DISPENSER));
      DROPPER = create("dropper", BlockEntityType.Builder.create(DropperBlockEntity::new, Blocks.DROPPER));
      SIGN = create("sign", BlockEntityType.Builder.create(SignBlockEntity::new, Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.BIRCH_SIGN, Blocks.ACACIA_SIGN, Blocks.CHERRY_SIGN, Blocks.JUNGLE_SIGN, Blocks.DARK_OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.CHERRY_WALL_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN, Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN, Blocks.MANGROVE_SIGN, Blocks.MANGROVE_WALL_SIGN, Blocks.BAMBOO_SIGN, Blocks.BAMBOO_WALL_SIGN));
      HANGING_SIGN = create("hanging_sign", BlockEntityType.Builder.create(HangingSignBlockEntity::new, Blocks.OAK_HANGING_SIGN, Blocks.SPRUCE_HANGING_SIGN, Blocks.BIRCH_HANGING_SIGN, Blocks.ACACIA_HANGING_SIGN, Blocks.CHERRY_HANGING_SIGN, Blocks.JUNGLE_HANGING_SIGN, Blocks.DARK_OAK_HANGING_SIGN, Blocks.CRIMSON_HANGING_SIGN, Blocks.WARPED_HANGING_SIGN, Blocks.MANGROVE_HANGING_SIGN, Blocks.BAMBOO_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN, Blocks.CRIMSON_WALL_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN, Blocks.MANGROVE_WALL_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN));
      MOB_SPAWNER = create("mob_spawner", BlockEntityType.Builder.create(MobSpawnerBlockEntity::new, Blocks.SPAWNER));
      PISTON = create("piston", BlockEntityType.Builder.create(PistonBlockEntity::new, Blocks.MOVING_PISTON));
      BREWING_STAND = create("brewing_stand", BlockEntityType.Builder.create(BrewingStandBlockEntity::new, Blocks.BREWING_STAND));
      ENCHANTING_TABLE = create("enchanting_table", BlockEntityType.Builder.create(EnchantingTableBlockEntity::new, Blocks.ENCHANTING_TABLE));
      END_PORTAL = create("end_portal", BlockEntityType.Builder.create(EndPortalBlockEntity::new, Blocks.END_PORTAL));
      BEACON = create("beacon", BlockEntityType.Builder.create(BeaconBlockEntity::new, Blocks.BEACON));
      SKULL = create("skull", BlockEntityType.Builder.create(SkullBlockEntity::new, Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD, Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD, Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD, Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD));
      DAYLIGHT_DETECTOR = create("daylight_detector", BlockEntityType.Builder.create(DaylightDetectorBlockEntity::new, Blocks.DAYLIGHT_DETECTOR));
      HOPPER = create("hopper", BlockEntityType.Builder.create(HopperBlockEntity::new, Blocks.HOPPER));
      COMPARATOR = create("comparator", BlockEntityType.Builder.create(ComparatorBlockEntity::new, Blocks.COMPARATOR));
      BANNER = create("banner", BlockEntityType.Builder.create(BannerBlockEntity::new, Blocks.WHITE_BANNER, Blocks.ORANGE_BANNER, Blocks.MAGENTA_BANNER, Blocks.LIGHT_BLUE_BANNER, Blocks.YELLOW_BANNER, Blocks.LIME_BANNER, Blocks.PINK_BANNER, Blocks.GRAY_BANNER, Blocks.LIGHT_GRAY_BANNER, Blocks.CYAN_BANNER, Blocks.PURPLE_BANNER, Blocks.BLUE_BANNER, Blocks.BROWN_BANNER, Blocks.GREEN_BANNER, Blocks.RED_BANNER, Blocks.BLACK_BANNER, Blocks.WHITE_WALL_BANNER, Blocks.ORANGE_WALL_BANNER, Blocks.MAGENTA_WALL_BANNER, Blocks.LIGHT_BLUE_WALL_BANNER, Blocks.YELLOW_WALL_BANNER, Blocks.LIME_WALL_BANNER, Blocks.PINK_WALL_BANNER, Blocks.GRAY_WALL_BANNER, Blocks.LIGHT_GRAY_WALL_BANNER, Blocks.CYAN_WALL_BANNER, Blocks.PURPLE_WALL_BANNER, Blocks.BLUE_WALL_BANNER, Blocks.BROWN_WALL_BANNER, Blocks.GREEN_WALL_BANNER, Blocks.RED_WALL_BANNER, Blocks.BLACK_WALL_BANNER));
      STRUCTURE_BLOCK = create("structure_block", BlockEntityType.Builder.create(StructureBlockBlockEntity::new, Blocks.STRUCTURE_BLOCK));
      END_GATEWAY = create("end_gateway", BlockEntityType.Builder.create(EndGatewayBlockEntity::new, Blocks.END_GATEWAY));
      COMMAND_BLOCK = create("command_block", BlockEntityType.Builder.create(CommandBlockBlockEntity::new, Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK));
      SHULKER_BOX = create("shulker_box", BlockEntityType.Builder.create(ShulkerBoxBlockEntity::new, Blocks.SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX));
      BED = create("bed", BlockEntityType.Builder.create(BedBlockEntity::new, Blocks.RED_BED, Blocks.BLACK_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.CYAN_BED, Blocks.GRAY_BED, Blocks.GREEN_BED, Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_GRAY_BED, Blocks.LIME_BED, Blocks.MAGENTA_BED, Blocks.ORANGE_BED, Blocks.PINK_BED, Blocks.PURPLE_BED, Blocks.WHITE_BED, Blocks.YELLOW_BED));
      CONDUIT = create("conduit", BlockEntityType.Builder.create(ConduitBlockEntity::new, Blocks.CONDUIT));
      BARREL = create("barrel", BlockEntityType.Builder.create(BarrelBlockEntity::new, Blocks.BARREL));
      SMOKER = create("smoker", BlockEntityType.Builder.create(SmokerBlockEntity::new, Blocks.SMOKER));
      BLAST_FURNACE = create("blast_furnace", BlockEntityType.Builder.create(BlastFurnaceBlockEntity::new, Blocks.BLAST_FURNACE));
      LECTERN = create("lectern", BlockEntityType.Builder.create(LecternBlockEntity::new, Blocks.LECTERN));
      BELL = create("bell", BlockEntityType.Builder.create(BellBlockEntity::new, Blocks.BELL));
      JIGSAW = create("jigsaw", BlockEntityType.Builder.create(JigsawBlockEntity::new, Blocks.JIGSAW));
      CAMPFIRE = create("campfire", BlockEntityType.Builder.create(CampfireBlockEntity::new, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE));
      BEEHIVE = create("beehive", BlockEntityType.Builder.create(BeehiveBlockEntity::new, Blocks.BEE_NEST, Blocks.BEEHIVE));
      SCULK_SENSOR = create("sculk_sensor", BlockEntityType.Builder.create(SculkSensorBlockEntity::new, Blocks.SCULK_SENSOR));
      CALIBRATED_SCULK_SENSOR = create("calibrated_sculk_sensor", BlockEntityType.Builder.create(CalibratedSculkSensorBlockEntity::new, Blocks.CALIBRATED_SCULK_SENSOR));
      SCULK_CATALYST = create("sculk_catalyst", BlockEntityType.Builder.create(SculkCatalystBlockEntity::new, Blocks.SCULK_CATALYST));
      SCULK_SHRIEKER = create("sculk_shrieker", BlockEntityType.Builder.create(SculkShriekerBlockEntity::new, Blocks.SCULK_SHRIEKER));
      CHISELED_BOOKSHELF = create("chiseled_bookshelf", BlockEntityType.Builder.create(ChiseledBookshelfBlockEntity::new, Blocks.CHISELED_BOOKSHELF));
      BRUSHABLE_BLOCK = create("brushable_block", BlockEntityType.Builder.create(BrushableBlockEntity::new, Blocks.SUSPICIOUS_SAND, Blocks.SUSPICIOUS_GRAVEL));
      DECORATED_POT = create("decorated_pot", BlockEntityType.Builder.create(DecoratedPotBlockEntity::new, Blocks.DECORATED_POT));
   }

   public static final class Builder {
      private final BlockEntityFactory factory;
      final Set blocks;

      private Builder(BlockEntityFactory factory, Set blocks) {
         this.factory = factory;
         this.blocks = blocks;
      }

      public static Builder create(BlockEntityFactory factory, Block... blocks) {
         return new Builder(factory, ImmutableSet.copyOf(blocks));
      }

      public BlockEntityType build(Type type) {
         return new BlockEntityType(this.factory, this.blocks, type);
      }
   }

   @FunctionalInterface
   private interface BlockEntityFactory {
      BlockEntity create(BlockPos pos, BlockState state);
   }
}
