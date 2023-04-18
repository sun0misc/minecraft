package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.featuretoggle.ToggleableFeature;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.State;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBlock implements ToggleableFeature {
   protected static final Direction[] DIRECTIONS;
   protected final Material material;
   protected final boolean collidable;
   protected final float resistance;
   protected final boolean randomTicks;
   protected final BlockSoundGroup soundGroup;
   protected final float slipperiness;
   protected final float velocityMultiplier;
   protected final float jumpVelocityMultiplier;
   protected final boolean dynamicBounds;
   protected final FeatureSet requiredFeatures;
   protected final Settings settings;
   @Nullable
   protected Identifier lootTableId;

   public AbstractBlock(Settings settings) {
      this.material = settings.material;
      this.collidable = settings.collidable;
      this.lootTableId = settings.lootTableId;
      this.resistance = settings.resistance;
      this.randomTicks = settings.randomTicks;
      this.soundGroup = settings.soundGroup;
      this.slipperiness = settings.slipperiness;
      this.velocityMultiplier = settings.velocityMultiplier;
      this.jumpVelocityMultiplier = settings.jumpVelocityMultiplier;
      this.dynamicBounds = settings.dynamicBounds;
      this.requiredFeatures = settings.requiredFeatures;
      this.settings = settings;
   }

   /** @deprecated */
   @Deprecated
   public void prepare(BlockState state, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
   }

   /** @deprecated */
   @Deprecated
   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      switch (type) {
         case LAND:
            return !state.isFullCube(world, pos);
         case WATER:
            return world.getFluidState(pos).isIn(FluidTags.WATER);
         case AIR:
            return !state.isFullCube(world, pos);
         default:
            return false;
      }
   }

   /** @deprecated */
   @Deprecated
   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      return state;
   }

   /** @deprecated */
   @Deprecated
   public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      DebugInfoSender.sendNeighborUpdate(world, pos);
   }

   /** @deprecated */
   @Deprecated
   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
   }

   /** @deprecated */
   @Deprecated
   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (state.hasBlockEntity() && !state.isOf(newState.getBlock())) {
         world.removeBlockEntity(pos);
      }

   }

   /** @deprecated */
   @Deprecated
   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      return ActionResult.PASS;
   }

   /** @deprecated */
   @Deprecated
   public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   /** @deprecated */
   @Deprecated
   public boolean hasSidedTransparency(BlockState state) {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public boolean emitsRedstonePower(BlockState state) {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public FluidState getFluidState(BlockState state) {
      return Fluids.EMPTY.getDefaultState();
   }

   /** @deprecated */
   @Deprecated
   public boolean hasComparatorOutput(BlockState state) {
      return false;
   }

   public float getMaxHorizontalModelOffset() {
      return 0.25F;
   }

   public float getVerticalModelOffsetMultiplier() {
      return 0.2F;
   }

   public FeatureSet getRequiredFeatures() {
      return this.requiredFeatures;
   }

   /** @deprecated */
   @Deprecated
   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return state;
   }

   /** @deprecated */
   @Deprecated
   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state;
   }

   /** @deprecated */
   @Deprecated
   public boolean canReplace(BlockState state, ItemPlacementContext context) {
      return this.material.isReplaceable() && (context.getStack().isEmpty() || !context.getStack().isOf(this.asItem()));
   }

   /** @deprecated */
   @Deprecated
   public boolean canBucketPlace(BlockState state, Fluid fluid) {
      return this.material.isReplaceable() || !this.material.isSolid();
   }

   /** @deprecated */
   @Deprecated
   public List getDroppedStacks(BlockState state, LootContext.Builder builder) {
      Identifier lv = this.getLootTableId();
      if (lv == LootTables.EMPTY) {
         return Collections.emptyList();
      } else {
         LootContext lv2 = builder.parameter(LootContextParameters.BLOCK_STATE, state).build(LootContextTypes.BLOCK);
         ServerWorld lv3 = lv2.getWorld();
         LootTable lv4 = lv3.getServer().getLootManager().getLootTable(lv);
         return lv4.generateLoot(lv2);
      }
   }

   /** @deprecated */
   @Deprecated
   public long getRenderingSeed(BlockState state, BlockPos pos) {
      return MathHelper.hashCode(pos);
   }

   /** @deprecated */
   @Deprecated
   public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
      return state.getOutlineShape(world, pos);
   }

   /** @deprecated */
   @Deprecated
   public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
      return this.getCollisionShape(state, world, pos, ShapeContext.absent());
   }

   /** @deprecated */
   @Deprecated
   public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
      return VoxelShapes.empty();
   }

   /** @deprecated */
   @Deprecated
   public int getOpacity(BlockState state, BlockView world, BlockPos pos) {
      if (state.isOpaqueFullCube(world, pos)) {
         return world.getMaxLightLevel();
      } else {
         return state.isTransparent(world, pos) ? 0 : 1;
      }
   }

   /** @deprecated */
   @Nullable
   @Deprecated
   public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
      return null;
   }

   /** @deprecated */
   @Deprecated
   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return true;
   }

   /** @deprecated */
   @Deprecated
   public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
      return state.isFullCube(world, pos) ? 0.2F : 1.0F;
   }

   /** @deprecated */
   @Deprecated
   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return 0;
   }

   /** @deprecated */
   @Deprecated
   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return VoxelShapes.fullCube();
   }

   /** @deprecated */
   @Deprecated
   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.collidable ? state.getOutlineShape(world, pos) : VoxelShapes.empty();
   }

   /** @deprecated */
   @Deprecated
   public boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
      return Block.isShapeFullCube(state.getCollisionShape(world, pos));
   }

   /** @deprecated */
   @Deprecated
   public boolean isCullingShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
      return Block.isShapeFullCube(state.getCullingShape(world, pos));
   }

   /** @deprecated */
   @Deprecated
   public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getCollisionShape(state, world, pos, context);
   }

   /** @deprecated */
   @Deprecated
   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      this.scheduledTick(state, world, pos, random);
   }

   /** @deprecated */
   @Deprecated
   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
   }

   /** @deprecated */
   @Deprecated
   public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
      float f = state.getHardness(world, pos);
      if (f == -1.0F) {
         return 0.0F;
      } else {
         int i = player.canHarvest(state) ? 30 : 100;
         return player.getBlockBreakingSpeed(state) / f / (float)i;
      }
   }

   /** @deprecated */
   @Deprecated
   public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
   }

   /** @deprecated */
   @Deprecated
   public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
   }

   /** @deprecated */
   @Deprecated
   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return 0;
   }

   /** @deprecated */
   @Deprecated
   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
   }

   /** @deprecated */
   @Deprecated
   public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return 0;
   }

   public final Identifier getLootTableId() {
      if (this.lootTableId == null) {
         Identifier lv = Registries.BLOCK.getId(this.asBlock());
         this.lootTableId = lv.withPrefixedPath("blocks/");
      }

      return this.lootTableId;
   }

   /** @deprecated */
   @Deprecated
   public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
   }

   public abstract Item asItem();

   protected abstract Block asBlock();

   public MapColor getDefaultMapColor() {
      return (MapColor)this.settings.mapColorProvider.apply(this.asBlock().getDefaultState());
   }

   public float getHardness() {
      return this.settings.hardness;
   }

   static {
      DIRECTIONS = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP};
   }

   public static class Settings {
      Material material;
      Function mapColorProvider;
      boolean collidable;
      BlockSoundGroup soundGroup;
      ToIntFunction luminance;
      float resistance;
      float hardness;
      boolean toolRequired;
      boolean randomTicks;
      float slipperiness;
      float velocityMultiplier;
      float jumpVelocityMultiplier;
      Identifier lootTableId;
      boolean opaque;
      boolean isAir;
      boolean burnable;
      /** @deprecated */
      @Deprecated
      boolean liquid;
      PistonBehavior pistonBehavior;
      boolean blockBreakParticles;
      TypedContextPredicate allowsSpawningPredicate;
      ContextPredicate solidBlockPredicate;
      ContextPredicate suffocationPredicate;
      ContextPredicate blockVisionPredicate;
      ContextPredicate postProcessPredicate;
      ContextPredicate emissiveLightingPredicate;
      boolean dynamicBounds;
      FeatureSet requiredFeatures;
      Optional offsetter;

      private Settings(Material material, MapColor mapColorProvider) {
         this(material, (state) -> {
            return mapColorProvider;
         });
      }

      private Settings(Material material, Function mapColorProvider) {
         this.collidable = true;
         this.soundGroup = BlockSoundGroup.STONE;
         this.luminance = (state) -> {
            return 0;
         };
         this.slipperiness = 0.6F;
         this.velocityMultiplier = 1.0F;
         this.jumpVelocityMultiplier = 1.0F;
         this.opaque = true;
         this.pistonBehavior = PistonBehavior.NORMAL;
         this.blockBreakParticles = true;
         this.allowsSpawningPredicate = (state, world, pos, type) -> {
            return state.isSideSolidFullSquare(world, pos, Direction.UP) && state.getLuminance() < 14;
         };
         this.solidBlockPredicate = (state, world, pos) -> {
            return state.getMaterial().blocksLight() && state.isFullCube(world, pos);
         };
         this.suffocationPredicate = (state, world, pos) -> {
            return this.material.blocksMovement() && state.isFullCube(world, pos);
         };
         this.blockVisionPredicate = this.suffocationPredicate;
         this.postProcessPredicate = (state, world, pos) -> {
            return false;
         };
         this.emissiveLightingPredicate = (state, world, pos) -> {
            return false;
         };
         this.requiredFeatures = FeatureFlags.VANILLA_FEATURES;
         this.offsetter = Optional.empty();
         this.material = material;
         this.mapColorProvider = mapColorProvider;
      }

      public static Settings of(Material material) {
         return of(material, material.getColor());
      }

      public static Settings of(Material material, DyeColor color) {
         return of(material, color.getMapColor());
      }

      public static Settings of(Material material, MapColor color) {
         return new Settings(material, color);
      }

      public static Settings of(Material material, Function mapColor) {
         return new Settings(material, mapColor);
      }

      public static Settings copy(AbstractBlock block) {
         Settings lv = new Settings(block.material, block.settings.mapColorProvider);
         lv.material = block.settings.material;
         lv.hardness = block.settings.hardness;
         lv.resistance = block.settings.resistance;
         lv.collidable = block.settings.collidable;
         lv.randomTicks = block.settings.randomTicks;
         lv.luminance = block.settings.luminance;
         lv.mapColorProvider = block.settings.mapColorProvider;
         lv.soundGroup = block.settings.soundGroup;
         lv.slipperiness = block.settings.slipperiness;
         lv.velocityMultiplier = block.settings.velocityMultiplier;
         lv.dynamicBounds = block.settings.dynamicBounds;
         lv.opaque = block.settings.opaque;
         lv.isAir = block.settings.isAir;
         lv.burnable = block.settings.burnable;
         lv.liquid = block.settings.liquid;
         lv.pistonBehavior = block.settings.pistonBehavior;
         lv.toolRequired = block.settings.toolRequired;
         lv.offsetter = block.settings.offsetter;
         lv.blockBreakParticles = block.settings.blockBreakParticles;
         lv.requiredFeatures = block.settings.requiredFeatures;
         lv.emissiveLightingPredicate = block.settings.emissiveLightingPredicate;
         return lv;
      }

      public Settings noCollision() {
         this.collidable = false;
         this.opaque = false;
         return this;
      }

      public Settings nonOpaque() {
         this.opaque = false;
         return this;
      }

      public Settings slipperiness(float slipperiness) {
         this.slipperiness = slipperiness;
         return this;
      }

      public Settings velocityMultiplier(float velocityMultiplier) {
         this.velocityMultiplier = velocityMultiplier;
         return this;
      }

      public Settings jumpVelocityMultiplier(float jumpVelocityMultiplier) {
         this.jumpVelocityMultiplier = jumpVelocityMultiplier;
         return this;
      }

      public Settings sounds(BlockSoundGroup soundGroup) {
         this.soundGroup = soundGroup;
         return this;
      }

      public Settings luminance(ToIntFunction luminance) {
         this.luminance = luminance;
         return this;
      }

      public Settings strength(float hardness, float resistance) {
         return this.hardness(hardness).resistance(resistance);
      }

      public Settings breakInstantly() {
         return this.strength(0.0F);
      }

      public Settings strength(float strength) {
         this.strength(strength, strength);
         return this;
      }

      public Settings ticksRandomly() {
         this.randomTicks = true;
         return this;
      }

      public Settings dynamicBounds() {
         this.dynamicBounds = true;
         return this;
      }

      public Settings dropsNothing() {
         this.lootTableId = LootTables.EMPTY;
         return this;
      }

      public Settings dropsLike(Block source) {
         this.lootTableId = source.getLootTableId();
         return this;
      }

      public Settings burnable() {
         this.burnable = true;
         return this;
      }

      public Settings liquid() {
         this.liquid = true;
         return this;
      }

      public Settings pistonBehavior(PistonBehavior pistonBehavior) {
         this.pistonBehavior = pistonBehavior;
         return this;
      }

      public Settings air() {
         this.isAir = true;
         return this;
      }

      public Settings allowsSpawning(TypedContextPredicate predicate) {
         this.allowsSpawningPredicate = predicate;
         return this;
      }

      public Settings solidBlock(ContextPredicate predicate) {
         this.solidBlockPredicate = predicate;
         return this;
      }

      public Settings suffocates(ContextPredicate predicate) {
         this.suffocationPredicate = predicate;
         return this;
      }

      public Settings blockVision(ContextPredicate predicate) {
         this.blockVisionPredicate = predicate;
         return this;
      }

      public Settings postProcess(ContextPredicate predicate) {
         this.postProcessPredicate = predicate;
         return this;
      }

      public Settings emissiveLighting(ContextPredicate predicate) {
         this.emissiveLightingPredicate = predicate;
         return this;
      }

      public Settings requiresTool() {
         this.toolRequired = true;
         return this;
      }

      public Settings mapColor(MapColor color) {
         this.mapColorProvider = (state) -> {
            return color;
         };
         return this;
      }

      public Settings hardness(float hardness) {
         this.hardness = hardness;
         return this;
      }

      public Settings resistance(float resistance) {
         this.resistance = Math.max(0.0F, resistance);
         return this;
      }

      public Settings offset(OffsetType offsetType) {
         switch (offsetType) {
            case XYZ:
               this.offsetter = Optional.of((state, world, pos) -> {
                  Block lv = state.getBlock();
                  long l = MathHelper.hashCode(pos.getX(), 0, pos.getZ());
                  double d = ((double)((float)(l >> 4 & 15L) / 15.0F) - 1.0) * (double)lv.getVerticalModelOffsetMultiplier();
                  float f = lv.getMaxHorizontalModelOffset();
                  double e = MathHelper.clamp(((double)((float)(l & 15L) / 15.0F) - 0.5) * 0.5, (double)(-f), (double)f);
                  double g = MathHelper.clamp(((double)((float)(l >> 8 & 15L) / 15.0F) - 0.5) * 0.5, (double)(-f), (double)f);
                  return new Vec3d(e, d, g);
               });
               break;
            case XZ:
               this.offsetter = Optional.of((state, world, pos) -> {
                  Block lv = state.getBlock();
                  long l = MathHelper.hashCode(pos.getX(), 0, pos.getZ());
                  float f = lv.getMaxHorizontalModelOffset();
                  double d = MathHelper.clamp(((double)((float)(l & 15L) / 15.0F) - 0.5) * 0.5, (double)(-f), (double)f);
                  double e = MathHelper.clamp(((double)((float)(l >> 8 & 15L) / 15.0F) - 0.5) * 0.5, (double)(-f), (double)f);
                  return new Vec3d(d, 0.0, e);
               });
               break;
            default:
               this.offsetter = Optional.empty();
         }

         return this;
      }

      public Settings noBlockBreakParticles() {
         this.blockBreakParticles = false;
         return this;
      }

      public Settings requires(FeatureFlag... features) {
         this.requiredFeatures = FeatureFlags.FEATURE_MANAGER.featureSetOf(features);
         return this;
      }
   }

   public interface TypedContextPredicate {
      boolean test(BlockState state, BlockView world, BlockPos pos, Object type);
   }

   public interface Offsetter {
      Vec3d evaluate(BlockState state, BlockView world, BlockPos pos);
   }

   public interface ContextPredicate {
      boolean test(BlockState state, BlockView world, BlockPos pos);
   }

   public abstract static class AbstractBlockState extends State {
      private final int luminance;
      private final boolean hasSidedTransparency;
      private final boolean isAir;
      private final boolean burnable;
      /** @deprecated */
      @Deprecated
      private final boolean liquid;
      private final PistonBehavior pistonBehavior;
      private final Material material;
      private final MapColor mapColor;
      private final float hardness;
      private final boolean toolRequired;
      private final boolean opaque;
      private final ContextPredicate solidBlockPredicate;
      private final ContextPredicate suffocationPredicate;
      private final ContextPredicate blockVisionPredicate;
      private final ContextPredicate postProcessPredicate;
      private final ContextPredicate emissiveLightingPredicate;
      private final Optional offsetter;
      private final boolean blockBreakParticles;
      @Nullable
      protected ShapeCache shapeCache;
      private FluidState fluidState;
      private boolean ticksRandomly;

      protected AbstractBlockState(Block block, ImmutableMap propertyMap, MapCodec codec) {
         super(block, propertyMap, codec);
         this.fluidState = Fluids.EMPTY.getDefaultState();
         Settings lv = block.settings;
         this.luminance = lv.luminance.applyAsInt(this.asBlockState());
         this.hasSidedTransparency = block.hasSidedTransparency(this.asBlockState());
         this.isAir = lv.isAir;
         this.burnable = lv.burnable;
         this.liquid = lv.liquid;
         this.pistonBehavior = lv.pistonBehavior;
         this.material = lv.material;
         this.mapColor = (MapColor)lv.mapColorProvider.apply(this.asBlockState());
         this.hardness = lv.hardness;
         this.toolRequired = lv.toolRequired;
         this.opaque = lv.opaque;
         this.solidBlockPredicate = lv.solidBlockPredicate;
         this.suffocationPredicate = lv.suffocationPredicate;
         this.blockVisionPredicate = lv.blockVisionPredicate;
         this.postProcessPredicate = lv.postProcessPredicate;
         this.emissiveLightingPredicate = lv.emissiveLightingPredicate;
         this.offsetter = lv.offsetter;
         this.blockBreakParticles = lv.blockBreakParticles;
      }

      public void initShapeCache() {
         this.fluidState = ((Block)this.owner).getFluidState(this.asBlockState());
         this.ticksRandomly = ((Block)this.owner).hasRandomTicks(this.asBlockState());
         if (!this.getBlock().hasDynamicBounds()) {
            this.shapeCache = new ShapeCache(this.asBlockState());
         }

      }

      public Block getBlock() {
         return (Block)this.owner;
      }

      public RegistryEntry getRegistryEntry() {
         return ((Block)this.owner).getRegistryEntry();
      }

      public Material getMaterial() {
         return this.material;
      }

      public boolean allowsSpawning(BlockView world, BlockPos pos, EntityType type) {
         return this.getBlock().settings.allowsSpawningPredicate.test(this.asBlockState(), world, pos, type);
      }

      public boolean isTransparent(BlockView world, BlockPos pos) {
         return this.shapeCache != null ? this.shapeCache.transparent : this.getBlock().isTransparent(this.asBlockState(), world, pos);
      }

      public int getOpacity(BlockView world, BlockPos pos) {
         return this.shapeCache != null ? this.shapeCache.lightSubtracted : this.getBlock().getOpacity(this.asBlockState(), world, pos);
      }

      public VoxelShape getCullingFace(BlockView world, BlockPos pos, Direction direction) {
         return this.shapeCache != null && this.shapeCache.extrudedFaces != null ? this.shapeCache.extrudedFaces[direction.ordinal()] : VoxelShapes.extrudeFace(this.getCullingShape(world, pos), direction);
      }

      public VoxelShape getCullingShape(BlockView world, BlockPos pos) {
         return this.getBlock().getCullingShape(this.asBlockState(), world, pos);
      }

      public boolean exceedsCube() {
         return this.shapeCache == null || this.shapeCache.exceedsCube;
      }

      public boolean hasSidedTransparency() {
         return this.hasSidedTransparency;
      }

      public int getLuminance() {
         return this.luminance;
      }

      public boolean isAir() {
         return this.isAir;
      }

      public boolean isBurnable() {
         return this.burnable;
      }

      /** @deprecated */
      @Deprecated
      public boolean isLiquid() {
         return this.liquid;
      }

      public MapColor getMapColor(BlockView world, BlockPos pos) {
         return this.mapColor;
      }

      public BlockState rotate(BlockRotation rotation) {
         return this.getBlock().rotate(this.asBlockState(), rotation);
      }

      public BlockState mirror(BlockMirror mirror) {
         return this.getBlock().mirror(this.asBlockState(), mirror);
      }

      public BlockRenderType getRenderType() {
         return this.getBlock().getRenderType(this.asBlockState());
      }

      public boolean hasEmissiveLighting(BlockView world, BlockPos pos) {
         return this.emissiveLightingPredicate.test(this.asBlockState(), world, pos);
      }

      public float getAmbientOcclusionLightLevel(BlockView world, BlockPos pos) {
         return this.getBlock().getAmbientOcclusionLightLevel(this.asBlockState(), world, pos);
      }

      public boolean isSolidBlock(BlockView world, BlockPos pos) {
         return this.solidBlockPredicate.test(this.asBlockState(), world, pos);
      }

      public boolean emitsRedstonePower() {
         return this.getBlock().emitsRedstonePower(this.asBlockState());
      }

      public int getWeakRedstonePower(BlockView world, BlockPos pos, Direction direction) {
         return this.getBlock().getWeakRedstonePower(this.asBlockState(), world, pos, direction);
      }

      public boolean hasComparatorOutput() {
         return this.getBlock().hasComparatorOutput(this.asBlockState());
      }

      public int getComparatorOutput(World world, BlockPos pos) {
         return this.getBlock().getComparatorOutput(this.asBlockState(), world, pos);
      }

      public float getHardness(BlockView world, BlockPos pos) {
         return this.hardness;
      }

      public float calcBlockBreakingDelta(PlayerEntity player, BlockView world, BlockPos pos) {
         return this.getBlock().calcBlockBreakingDelta(this.asBlockState(), player, world, pos);
      }

      public int getStrongRedstonePower(BlockView world, BlockPos pos, Direction direction) {
         return this.getBlock().getStrongRedstonePower(this.asBlockState(), world, pos, direction);
      }

      public PistonBehavior getPistonBehavior() {
         return this.pistonBehavior;
      }

      public boolean isOpaqueFullCube(BlockView world, BlockPos pos) {
         if (this.shapeCache != null) {
            return this.shapeCache.fullOpaque;
         } else {
            BlockState lv = this.asBlockState();
            return lv.isOpaque() ? Block.isShapeFullCube(lv.getCullingShape(world, pos)) : false;
         }
      }

      public boolean isOpaque() {
         return this.opaque;
      }

      public boolean isSideInvisible(BlockState state, Direction direction) {
         return this.getBlock().isSideInvisible(this.asBlockState(), state, direction);
      }

      public VoxelShape getOutlineShape(BlockView world, BlockPos pos) {
         return this.getOutlineShape(world, pos, ShapeContext.absent());
      }

      public VoxelShape getOutlineShape(BlockView world, BlockPos pos, ShapeContext context) {
         return this.getBlock().getOutlineShape(this.asBlockState(), world, pos, context);
      }

      public VoxelShape getCollisionShape(BlockView world, BlockPos pos) {
         return this.shapeCache != null ? this.shapeCache.collisionShape : this.getCollisionShape(world, pos, ShapeContext.absent());
      }

      public VoxelShape getCollisionShape(BlockView world, BlockPos pos, ShapeContext context) {
         return this.getBlock().getCollisionShape(this.asBlockState(), world, pos, context);
      }

      public VoxelShape getSidesShape(BlockView world, BlockPos pos) {
         return this.getBlock().getSidesShape(this.asBlockState(), world, pos);
      }

      public VoxelShape getCameraCollisionShape(BlockView world, BlockPos pos, ShapeContext context) {
         return this.getBlock().getCameraCollisionShape(this.asBlockState(), world, pos, context);
      }

      public VoxelShape getRaycastShape(BlockView world, BlockPos pos) {
         return this.getBlock().getRaycastShape(this.asBlockState(), world, pos);
      }

      public final boolean hasSolidTopSurface(BlockView world, BlockPos pos, Entity entity) {
         return this.isSolidSurface(world, pos, entity, Direction.UP);
      }

      public final boolean isSolidSurface(BlockView world, BlockPos pos, Entity entity, Direction direction) {
         return Block.isFaceFullSquare(this.getCollisionShape(world, pos, ShapeContext.of(entity)), direction);
      }

      public Vec3d getModelOffset(BlockView world, BlockPos pos) {
         return (Vec3d)this.offsetter.map((offsetter) -> {
            return offsetter.evaluate(this.asBlockState(), world, pos);
         }).orElse(Vec3d.ZERO);
      }

      public boolean hasModelOffset() {
         return !this.offsetter.isEmpty();
      }

      public boolean onSyncedBlockEvent(World world, BlockPos pos, int type, int data) {
         return this.getBlock().onSyncedBlockEvent(this.asBlockState(), world, pos, type, data);
      }

      /** @deprecated */
      @Deprecated
      public void neighborUpdate(World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
         this.getBlock().neighborUpdate(this.asBlockState(), world, pos, sourceBlock, sourcePos, notify);
      }

      public final void updateNeighbors(WorldAccess world, BlockPos pos, int flags) {
         this.updateNeighbors(world, pos, flags, 512);
      }

      public final void updateNeighbors(WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
         BlockPos.Mutable lv = new BlockPos.Mutable();
         Direction[] var6 = AbstractBlock.DIRECTIONS;
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            Direction lv2 = var6[var8];
            lv.set(pos, (Direction)lv2);
            world.replaceWithStateForNeighborUpdate(lv2.getOpposite(), this.asBlockState(), lv, pos, flags, maxUpdateDepth);
         }

      }

      public final void prepare(WorldAccess world, BlockPos pos, int flags) {
         this.prepare(world, pos, flags, 512);
      }

      public void prepare(WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
         this.getBlock().prepare(this.asBlockState(), world, pos, flags, maxUpdateDepth);
      }

      public void onBlockAdded(World world, BlockPos pos, BlockState state, boolean notify) {
         this.getBlock().onBlockAdded(this.asBlockState(), world, pos, state, notify);
      }

      public void onStateReplaced(World world, BlockPos pos, BlockState state, boolean moved) {
         this.getBlock().onStateReplaced(this.asBlockState(), world, pos, state, moved);
      }

      public void scheduledTick(ServerWorld world, BlockPos pos, Random random) {
         this.getBlock().scheduledTick(this.asBlockState(), world, pos, random);
      }

      public void randomTick(ServerWorld world, BlockPos pos, Random random) {
         this.getBlock().randomTick(this.asBlockState(), world, pos, random);
      }

      public void onEntityCollision(World world, BlockPos pos, Entity entity) {
         this.getBlock().onEntityCollision(this.asBlockState(), world, pos, entity);
      }

      public void onStacksDropped(ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
         this.getBlock().onStacksDropped(this.asBlockState(), world, pos, tool, dropExperience);
      }

      public List getDroppedStacks(LootContext.Builder builder) {
         return this.getBlock().getDroppedStacks(this.asBlockState(), builder);
      }

      public ActionResult onUse(World world, PlayerEntity player, Hand hand, BlockHitResult hit) {
         return this.getBlock().onUse(this.asBlockState(), world, hit.getBlockPos(), player, hand, hit);
      }

      public void onBlockBreakStart(World world, BlockPos pos, PlayerEntity player) {
         this.getBlock().onBlockBreakStart(this.asBlockState(), world, pos, player);
      }

      public boolean shouldSuffocate(BlockView world, BlockPos pos) {
         return this.suffocationPredicate.test(this.asBlockState(), world, pos);
      }

      public boolean shouldBlockVision(BlockView world, BlockPos pos) {
         return this.blockVisionPredicate.test(this.asBlockState(), world, pos);
      }

      public BlockState getStateForNeighborUpdate(Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
         return this.getBlock().getStateForNeighborUpdate(this.asBlockState(), direction, neighborState, world, pos, neighborPos);
      }

      public boolean canPathfindThrough(BlockView world, BlockPos pos, NavigationType type) {
         return this.getBlock().canPathfindThrough(this.asBlockState(), world, pos, type);
      }

      public boolean canReplace(ItemPlacementContext context) {
         return this.getBlock().canReplace(this.asBlockState(), context);
      }

      public boolean canBucketPlace(Fluid fluid) {
         return this.getBlock().canBucketPlace(this.asBlockState(), fluid);
      }

      public boolean isReplaceable() {
         return this.getMaterial().isReplaceable();
      }

      public boolean canPlaceAt(WorldView world, BlockPos pos) {
         return this.getBlock().canPlaceAt(this.asBlockState(), world, pos);
      }

      public boolean shouldPostProcess(BlockView world, BlockPos pos) {
         return this.postProcessPredicate.test(this.asBlockState(), world, pos);
      }

      @Nullable
      public NamedScreenHandlerFactory createScreenHandlerFactory(World world, BlockPos pos) {
         return this.getBlock().createScreenHandlerFactory(this.asBlockState(), world, pos);
      }

      public boolean isIn(TagKey tag) {
         return this.getBlock().getRegistryEntry().isIn(tag);
      }

      public boolean isIn(TagKey tag, Predicate predicate) {
         return this.isIn(tag) && predicate.test(this);
      }

      public boolean isIn(RegistryEntryList blocks) {
         return blocks.contains(this.getBlock().getRegistryEntry());
      }

      public Stream streamTags() {
         return this.getBlock().getRegistryEntry().streamTags();
      }

      public boolean hasBlockEntity() {
         return this.getBlock() instanceof BlockEntityProvider;
      }

      @Nullable
      public BlockEntityTicker getBlockEntityTicker(World world, BlockEntityType blockEntityType) {
         return this.getBlock() instanceof BlockEntityProvider ? ((BlockEntityProvider)this.getBlock()).getTicker(world, this.asBlockState(), blockEntityType) : null;
      }

      public boolean isOf(Block block) {
         return this.getBlock() == block;
      }

      public FluidState getFluidState() {
         return this.fluidState;
      }

      public boolean hasRandomTicks() {
         return this.ticksRandomly;
      }

      public long getRenderingSeed(BlockPos pos) {
         return this.getBlock().getRenderingSeed(this.asBlockState(), pos);
      }

      public BlockSoundGroup getSoundGroup() {
         return this.getBlock().getSoundGroup(this.asBlockState());
      }

      public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
         this.getBlock().onProjectileHit(world, state, hit, projectile);
      }

      public boolean isSideSolidFullSquare(BlockView world, BlockPos pos, Direction direction) {
         return this.isSideSolid(world, pos, direction, SideShapeType.FULL);
      }

      public boolean isSideSolid(BlockView world, BlockPos pos, Direction direction, SideShapeType shapeType) {
         return this.shapeCache != null ? this.shapeCache.isSideSolid(direction, shapeType) : shapeType.matches(this.asBlockState(), world, pos, direction);
      }

      public boolean isFullCube(BlockView world, BlockPos pos) {
         return this.shapeCache != null ? this.shapeCache.isFullCube : this.getBlock().isShapeFullCube(this.asBlockState(), world, pos);
      }

      protected abstract BlockState asBlockState();

      public boolean isToolRequired() {
         return this.toolRequired;
      }

      public boolean hasBlockBreakParticles() {
         return this.blockBreakParticles;
      }

      private static final class ShapeCache {
         private static final Direction[] DIRECTIONS = Direction.values();
         private static final int SHAPE_TYPE_LENGTH = SideShapeType.values().length;
         protected final boolean fullOpaque;
         final boolean transparent;
         final int lightSubtracted;
         @Nullable
         final VoxelShape[] extrudedFaces;
         protected final VoxelShape collisionShape;
         protected final boolean exceedsCube;
         private final boolean[] solidSides;
         protected final boolean isFullCube;

         ShapeCache(BlockState state) {
            Block lv = state.getBlock();
            this.fullOpaque = state.isOpaqueFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
            this.transparent = lv.isTransparent(state, EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
            this.lightSubtracted = lv.getOpacity(state, EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
            int var5;
            if (!state.isOpaque()) {
               this.extrudedFaces = null;
            } else {
               this.extrudedFaces = new VoxelShape[DIRECTIONS.length];
               VoxelShape lv2 = lv.getCullingShape(state, EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
               Direction[] var4 = DIRECTIONS;
               var5 = var4.length;

               for(int var6 = 0; var6 < var5; ++var6) {
                  Direction lv3 = var4[var6];
                  this.extrudedFaces[lv3.ordinal()] = VoxelShapes.extrudeFace(lv2, lv3);
               }
            }

            this.collisionShape = lv.getCollisionShape(state, EmptyBlockView.INSTANCE, BlockPos.ORIGIN, ShapeContext.absent());
            if (!this.collisionShape.isEmpty() && state.hasModelOffset()) {
               throw new IllegalStateException(String.format(Locale.ROOT, "%s has a collision shape and an offset type, but is not marked as dynamicShape in its properties.", Registries.BLOCK.getId(lv)));
            } else {
               this.exceedsCube = Arrays.stream(Direction.Axis.values()).anyMatch((axis) -> {
                  return this.collisionShape.getMin(axis) < 0.0 || this.collisionShape.getMax(axis) > 1.0;
               });
               this.solidSides = new boolean[DIRECTIONS.length * SHAPE_TYPE_LENGTH];
               Direction[] var11 = DIRECTIONS;
               int var12 = var11.length;

               for(var5 = 0; var5 < var12; ++var5) {
                  Direction lv4 = var11[var5];
                  SideShapeType[] var14 = SideShapeType.values();
                  int var8 = var14.length;

                  for(int var9 = 0; var9 < var8; ++var9) {
                     SideShapeType lv5 = var14[var9];
                     this.solidSides[indexSolidSide(lv4, lv5)] = lv5.matches(state, EmptyBlockView.INSTANCE, BlockPos.ORIGIN, lv4);
                  }
               }

               this.isFullCube = Block.isShapeFullCube(state.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN));
            }
         }

         public boolean isSideSolid(Direction direction, SideShapeType shapeType) {
            return this.solidSides[indexSolidSide(direction, shapeType)];
         }

         private static int indexSolidSide(Direction direction, SideShapeType shapeType) {
            return direction.ordinal() * SHAPE_TYPE_LENGTH + shapeType.ordinal();
         }
      }
   }

   public static enum OffsetType {
      NONE,
      XZ,
      XYZ;

      // $FF: synthetic method
      private static OffsetType[] method_36719() {
         return new OffsetType[]{NONE, XZ, XYZ};
      }
   }
}
