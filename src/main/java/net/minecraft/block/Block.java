package net.minecraft.block;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Block extends AbstractBlock implements ItemConvertible {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final RegistryEntry.Reference registryEntry;
   public static final IdList STATE_IDS = new IdList();
   private static final LoadingCache FULL_CUBE_SHAPE_CACHE = CacheBuilder.newBuilder().maximumSize(512L).weakKeys().build(new CacheLoader() {
      public Boolean load(VoxelShape arg) {
         return !VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), arg, BooleanBiFunction.NOT_SAME);
      }

      // $FF: synthetic method
      public Object load(Object shape) throws Exception {
         return this.load((VoxelShape)shape);
      }
   });
   public static final int NOTIFY_NEIGHBORS = 1;
   public static final int NOTIFY_LISTENERS = 2;
   public static final int NO_REDRAW = 4;
   public static final int REDRAW_ON_MAIN_THREAD = 8;
   public static final int FORCE_STATE = 16;
   public static final int SKIP_DROPS = 32;
   public static final int MOVED = 64;
   public static final int SKIP_LIGHTING_UPDATES = 128;
   public static final int field_31035 = 4;
   public static final int NOTIFY_ALL = 3;
   public static final int field_31022 = 11;
   public static final float field_31023 = -1.0F;
   public static final float field_31024 = 0.0F;
   public static final int field_31025 = 512;
   protected final StateManager stateManager;
   private BlockState defaultState;
   @Nullable
   private String translationKey;
   @Nullable
   private Item cachedItem;
   private static final int field_31026 = 2048;
   private static final ThreadLocal FACE_CULL_MAP = ThreadLocal.withInitial(() -> {
      Object2ByteLinkedOpenHashMap object2ByteLinkedOpenHashMap = new Object2ByteLinkedOpenHashMap(2048, 0.25F) {
         protected void rehash(int newN) {
         }
      };
      object2ByteLinkedOpenHashMap.defaultReturnValue((byte)127);
      return object2ByteLinkedOpenHashMap;
   });

   public static int getRawIdFromState(@Nullable BlockState state) {
      if (state == null) {
         return 0;
      } else {
         int i = STATE_IDS.getRawId(state);
         return i == -1 ? 0 : i;
      }
   }

   public static BlockState getStateFromRawId(int stateId) {
      BlockState lv = (BlockState)STATE_IDS.get(stateId);
      return lv == null ? Blocks.AIR.getDefaultState() : lv;
   }

   public static Block getBlockFromItem(@Nullable Item item) {
      return item instanceof BlockItem ? ((BlockItem)item).getBlock() : Blocks.AIR;
   }

   public static BlockState pushEntitiesUpBeforeBlockChange(BlockState from, BlockState to, WorldAccess world, BlockPos pos) {
      VoxelShape lv = VoxelShapes.combine(from.getCollisionShape(world, pos), to.getCollisionShape(world, pos), BooleanBiFunction.ONLY_SECOND).offset((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
      if (lv.isEmpty()) {
         return to;
      } else {
         List list = world.getOtherEntities((Entity)null, lv.getBoundingBox());
         Iterator var6 = list.iterator();

         while(var6.hasNext()) {
            Entity lv2 = (Entity)var6.next();
            double d = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, lv2.getBoundingBox().offset(0.0, 1.0, 0.0), List.of(lv), -1.0);
            lv2.requestTeleportOffset(0.0, 1.0 + d, 0.0);
         }

         return to;
      }
   }

   public static VoxelShape createCuboidShape(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      return VoxelShapes.cuboid(minX / 16.0, minY / 16.0, minZ / 16.0, maxX / 16.0, maxY / 16.0, maxZ / 16.0);
   }

   public static BlockState postProcessState(BlockState state, WorldAccess world, BlockPos pos) {
      BlockState lv = state;
      BlockPos.Mutable lv2 = new BlockPos.Mutable();
      Direction[] var5 = DIRECTIONS;
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         Direction lv3 = var5[var7];
         lv2.set(pos, (Direction)lv3);
         lv = lv.getStateForNeighborUpdate(lv3, world.getBlockState(lv2), world, pos, lv2);
      }

      return lv;
   }

   public static void replace(BlockState state, BlockState newState, WorldAccess world, BlockPos pos, int flags) {
      replace(state, newState, world, pos, flags, 512);
   }

   public static void replace(BlockState state, BlockState newState, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
      if (newState != state) {
         if (newState.isAir()) {
            if (!world.isClient()) {
               world.breakBlock(pos, (flags & SKIP_DROPS) == 0, (Entity)null, maxUpdateDepth);
            }
         } else {
            world.setBlockState(pos, newState, flags & ~SKIP_DROPS, maxUpdateDepth);
         }
      }

   }

   public Block(AbstractBlock.Settings arg) {
      super(arg);
      this.registryEntry = Registries.BLOCK.createEntry(this);
      StateManager.Builder lv = new StateManager.Builder(this);
      this.appendProperties(lv);
      this.stateManager = lv.build(Block::getDefaultState, BlockState::new);
      this.setDefaultState((BlockState)this.stateManager.getDefaultState());
      if (SharedConstants.isDevelopment) {
         String string = this.getClass().getSimpleName();
         if (!string.endsWith("Block")) {
            LOGGER.error("Block classes should end with Block and {} doesn't.", string);
         }
      }

   }

   public static boolean cannotConnect(BlockState state) {
      return state.getBlock() instanceof LeavesBlock || state.isOf(Blocks.BARRIER) || state.isOf(Blocks.CARVED_PUMPKIN) || state.isOf(Blocks.JACK_O_LANTERN) || state.isOf(Blocks.MELON) || state.isOf(Blocks.PUMPKIN) || state.isIn(BlockTags.SHULKER_BOXES);
   }

   public boolean hasRandomTicks(BlockState state) {
      return this.randomTicks;
   }

   public static boolean shouldDrawSide(BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos otherPos) {
      BlockState lv = world.getBlockState(otherPos);
      if (state.isSideInvisible(lv, side)) {
         return false;
      } else if (lv.isOpaque()) {
         NeighborGroup lv2 = new NeighborGroup(state, lv, side);
         Object2ByteLinkedOpenHashMap object2ByteLinkedOpenHashMap = (Object2ByteLinkedOpenHashMap)FACE_CULL_MAP.get();
         byte b = object2ByteLinkedOpenHashMap.getAndMoveToFirst(lv2);
         if (b != 127) {
            return b != 0;
         } else {
            VoxelShape lv3 = state.getCullingFace(world, pos, side);
            if (lv3.isEmpty()) {
               return true;
            } else {
               VoxelShape lv4 = lv.getCullingFace(world, otherPos, side.getOpposite());
               boolean bl = VoxelShapes.matchesAnywhere(lv3, lv4, BooleanBiFunction.ONLY_FIRST);
               if (object2ByteLinkedOpenHashMap.size() == 2048) {
                  object2ByteLinkedOpenHashMap.removeLastByte();
               }

               object2ByteLinkedOpenHashMap.putAndMoveToFirst(lv2, (byte)(bl ? 1 : 0));
               return bl;
            }
         }
      } else {
         return true;
      }
   }

   public static boolean hasTopRim(BlockView world, BlockPos pos) {
      return world.getBlockState(pos).isSideSolid(world, pos, Direction.UP, SideShapeType.RIGID);
   }

   public static boolean sideCoversSmallSquare(WorldView world, BlockPos pos, Direction side) {
      BlockState lv = world.getBlockState(pos);
      return side == Direction.DOWN && lv.isIn(BlockTags.UNSTABLE_BOTTOM_CENTER) ? false : lv.isSideSolid(world, pos, side, SideShapeType.CENTER);
   }

   public static boolean isFaceFullSquare(VoxelShape shape, Direction side) {
      VoxelShape lv = shape.getFace(side);
      return isShapeFullCube(lv);
   }

   public static boolean isShapeFullCube(VoxelShape shape) {
      return (Boolean)FULL_CUBE_SHAPE_CACHE.getUnchecked(shape);
   }

   public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
      return !isShapeFullCube(state.getOutlineShape(world, pos)) && state.getFluidState().isEmpty();
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
   }

   public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
   }

   public static List getDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity) {
      LootContext.Builder lv = (new LootContext.Builder(world)).random(world.random).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).parameter(LootContextParameters.TOOL, ItemStack.EMPTY).optionalParameter(LootContextParameters.BLOCK_ENTITY, blockEntity);
      return state.getDroppedStacks(lv);
   }

   public static List getDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack stack) {
      LootContext.Builder lv = (new LootContext.Builder(world)).random(world.random).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).parameter(LootContextParameters.TOOL, stack).optionalParameter(LootContextParameters.THIS_ENTITY, entity).optionalParameter(LootContextParameters.BLOCK_ENTITY, blockEntity);
      return state.getDroppedStacks(lv);
   }

   public static void dropStacks(BlockState state, LootContext.Builder lootContext) {
      ServerWorld lv = lootContext.getWorld();
      BlockPos lv2 = BlockPos.ofFloored((Position)lootContext.get(LootContextParameters.ORIGIN));
      state.getDroppedStacks(lootContext).forEach((stack) -> {
         dropStack(lv, (BlockPos)lv2, stack);
      });
      state.onStacksDropped(lv, lv2, ItemStack.EMPTY, true);
   }

   public static void dropStacks(BlockState state, World world, BlockPos pos) {
      if (world instanceof ServerWorld) {
         getDroppedStacks(state, (ServerWorld)world, pos, (BlockEntity)null).forEach((stack) -> {
            dropStack(world, pos, stack);
         });
         state.onStacksDropped((ServerWorld)world, pos, ItemStack.EMPTY, true);
      }

   }

   public static void dropStacks(BlockState state, WorldAccess world, BlockPos pos, @Nullable BlockEntity blockEntity) {
      if (world instanceof ServerWorld) {
         getDroppedStacks(state, (ServerWorld)world, pos, blockEntity).forEach((stack) -> {
            dropStack((ServerWorld)world, (BlockPos)pos, stack);
         });
         state.onStacksDropped((ServerWorld)world, pos, ItemStack.EMPTY, true);
      }

   }

   public static void dropStacks(BlockState state, World world, BlockPos pos, @Nullable BlockEntity blockEntity, Entity entity, ItemStack tool) {
      if (world instanceof ServerWorld) {
         getDroppedStacks(state, (ServerWorld)world, pos, blockEntity, entity, tool).forEach((stack) -> {
            dropStack(world, pos, stack);
         });
         state.onStacksDropped((ServerWorld)world, pos, tool, true);
      }

   }

   public static void dropStack(World world, BlockPos pos, ItemStack stack) {
      double d = (double)EntityType.ITEM.getHeight() / 2.0;
      double e = (double)pos.getX() + 0.5 + MathHelper.nextDouble(world.random, -0.25, 0.25);
      double f = (double)pos.getY() + 0.5 + MathHelper.nextDouble(world.random, -0.25, 0.25) - d;
      double g = (double)pos.getZ() + 0.5 + MathHelper.nextDouble(world.random, -0.25, 0.25);
      dropStack(world, () -> {
         return new ItemEntity(world, e, f, g, stack);
      }, stack);
   }

   public static void dropStack(World world, BlockPos pos, Direction direction, ItemStack stack) {
      int i = direction.getOffsetX();
      int j = direction.getOffsetY();
      int k = direction.getOffsetZ();
      double d = (double)EntityType.ITEM.getWidth() / 2.0;
      double e = (double)EntityType.ITEM.getHeight() / 2.0;
      double f = (double)pos.getX() + 0.5 + (i == 0 ? MathHelper.nextDouble(world.random, -0.25, 0.25) : (double)i * (0.5 + d));
      double g = (double)pos.getY() + 0.5 + (j == 0 ? MathHelper.nextDouble(world.random, -0.25, 0.25) : (double)j * (0.5 + e)) - e;
      double h = (double)pos.getZ() + 0.5 + (k == 0 ? MathHelper.nextDouble(world.random, -0.25, 0.25) : (double)k * (0.5 + d));
      double l = i == 0 ? MathHelper.nextDouble(world.random, -0.1, 0.1) : (double)i * 0.1;
      double m = j == 0 ? MathHelper.nextDouble(world.random, 0.0, 0.1) : (double)j * 0.1 + 0.1;
      double n = k == 0 ? MathHelper.nextDouble(world.random, -0.1, 0.1) : (double)k * 0.1;
      dropStack(world, () -> {
         return new ItemEntity(world, f, g, h, stack, l, m, n);
      }, stack);
   }

   private static void dropStack(World world, Supplier itemEntitySupplier, ItemStack stack) {
      if (!world.isClient && !stack.isEmpty() && world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
         ItemEntity lv = (ItemEntity)itemEntitySupplier.get();
         lv.setToDefaultPickupDelay();
         world.spawnEntity(lv);
      }
   }

   protected void dropExperience(ServerWorld world, BlockPos pos, int size) {
      if (world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
         ExperienceOrbEntity.spawn(world, Vec3d.ofCenter(pos), size);
      }

   }

   public float getBlastResistance() {
      return this.resistance;
   }

   public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
   }

   public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return this.getDefaultState();
   }

   public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
      player.incrementStat(Stats.MINED.getOrCreateStat(this));
      player.addExhaustion(0.005F);
      dropStacks(state, world, pos, blockEntity, player, tool);
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
   }

   public boolean canMobSpawnInside(BlockState state) {
      return !this.material.isSolid() && !state.isLiquid();
   }

   public MutableText getName() {
      return Text.translatable(this.getTranslationKey());
   }

   public String getTranslationKey() {
      if (this.translationKey == null) {
         this.translationKey = Util.createTranslationKey("block", Registries.BLOCK.getId(this));
      }

      return this.translationKey;
   }

   public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
      entity.handleFallDamage(fallDistance, 1.0F, entity.getDamageSources().fall());
   }

   public void onEntityLand(BlockView world, Entity entity) {
      entity.setVelocity(entity.getVelocity().multiply(1.0, 0.0, 1.0));
   }

   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return new ItemStack(this);
   }

   public float getSlipperiness() {
      return this.slipperiness;
   }

   public float getVelocityMultiplier() {
      return this.velocityMultiplier;
   }

   public float getJumpVelocityMultiplier() {
      return this.jumpVelocityMultiplier;
   }

   protected void spawnBreakParticles(World world, PlayerEntity player, BlockPos pos, BlockState state) {
      world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, pos, getRawIdFromState(state));
   }

   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      this.spawnBreakParticles(world, player, pos, state);
      if (state.isIn(BlockTags.GUARDED_BY_PIGLINS)) {
         PiglinBrain.onGuardedBlockInteracted(player, false);
      }

      world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(player, state));
   }

   public void precipitationTick(BlockState state, World world, BlockPos pos, Biome.Precipitation precipitation) {
   }

   public boolean shouldDropItemsOnExplosion(Explosion explosion) {
      return true;
   }

   protected void appendProperties(StateManager.Builder builder) {
   }

   public StateManager getStateManager() {
      return this.stateManager;
   }

   protected final void setDefaultState(BlockState state) {
      this.defaultState = state;
   }

   public final BlockState getDefaultState() {
      return this.defaultState;
   }

   public final BlockState getStateWithProperties(BlockState state) {
      BlockState lv = this.getDefaultState();
      Iterator var3 = state.getBlock().getStateManager().getProperties().iterator();

      while(var3.hasNext()) {
         Property lv2 = (Property)var3.next();
         if (lv.contains(lv2)) {
            lv = copyProperty(state, lv, lv2);
         }
      }

      return lv;
   }

   private static BlockState copyProperty(BlockState source, BlockState target, Property property) {
      return (BlockState)target.with(property, source.get(property));
   }

   public BlockSoundGroup getSoundGroup(BlockState state) {
      return this.soundGroup;
   }

   public Item asItem() {
      if (this.cachedItem == null) {
         this.cachedItem = Item.fromBlock(this);
      }

      return this.cachedItem;
   }

   public boolean hasDynamicBounds() {
      return this.dynamicBounds;
   }

   public String toString() {
      return "Block{" + Registries.BLOCK.getId(this) + "}";
   }

   public void appendTooltip(ItemStack stack, @Nullable BlockView world, List tooltip, TooltipContext options) {
   }

   protected Block asBlock() {
      return this;
   }

   protected ImmutableMap getShapesForStates(Function stateToShape) {
      return (ImmutableMap)this.stateManager.getStates().stream().collect(ImmutableMap.toImmutableMap(Function.identity(), stateToShape));
   }

   /** @deprecated */
   @Deprecated
   public RegistryEntry.Reference getRegistryEntry() {
      return this.registryEntry;
   }

   protected void dropExperienceWhenMined(ServerWorld world, BlockPos pos, ItemStack tool, IntProvider experience) {
      if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, tool) == 0) {
         int i = experience.get(world.random);
         if (i > 0) {
            this.dropExperience(world, pos, i);
         }
      }

   }

   public static final class NeighborGroup {
      private final BlockState self;
      private final BlockState other;
      private final Direction facing;

      public NeighborGroup(BlockState self, BlockState other, Direction facing) {
         this.self = self;
         this.other = other;
         this.facing = facing;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (!(o instanceof NeighborGroup)) {
            return false;
         } else {
            NeighborGroup lv = (NeighborGroup)o;
            return this.self == lv.self && this.other == lv.other && this.facing == lv.facing;
         }
      }

      public int hashCode() {
         int i = this.self.hashCode();
         i = 31 * i + this.other.hashCode();
         i = 31 * i + this.facing.hashCode();
         return i;
      }
   }
}
