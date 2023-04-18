package net.minecraft.fluid;

import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.StateManager;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public abstract class Fluid {
   public static final IdList STATE_IDS = new IdList();
   protected final StateManager stateManager;
   private FluidState defaultState;
   private final RegistryEntry.Reference registryEntry;

   protected Fluid() {
      this.registryEntry = Registries.FLUID.createEntry(this);
      StateManager.Builder lv = new StateManager.Builder(this);
      this.appendProperties(lv);
      this.stateManager = lv.build(Fluid::getDefaultState, FluidState::new);
      this.setDefaultState((FluidState)this.stateManager.getDefaultState());
   }

   protected void appendProperties(StateManager.Builder builder) {
   }

   public StateManager getStateManager() {
      return this.stateManager;
   }

   protected final void setDefaultState(FluidState state) {
      this.defaultState = state;
   }

   public final FluidState getDefaultState() {
      return this.defaultState;
   }

   public abstract Item getBucketItem();

   protected void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random) {
   }

   protected void onScheduledTick(World world, BlockPos pos, FluidState state) {
   }

   protected void onRandomTick(World world, BlockPos pos, FluidState state, Random random) {
   }

   @Nullable
   protected ParticleEffect getParticle() {
      return null;
   }

   protected abstract boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction);

   protected abstract Vec3d getVelocity(BlockView world, BlockPos pos, FluidState state);

   public abstract int getTickRate(WorldView world);

   protected boolean hasRandomTicks() {
      return false;
   }

   protected boolean isEmpty() {
      return false;
   }

   protected abstract float getBlastResistance();

   public abstract float getHeight(FluidState state, BlockView world, BlockPos pos);

   public abstract float getHeight(FluidState state);

   protected abstract BlockState toBlockState(FluidState state);

   public abstract boolean isStill(FluidState state);

   public abstract int getLevel(FluidState state);

   public boolean matchesType(Fluid fluid) {
      return fluid == this;
   }

   /** @deprecated */
   @Deprecated
   public boolean isIn(TagKey tag) {
      return this.registryEntry.isIn(tag);
   }

   public abstract VoxelShape getShape(FluidState state, BlockView world, BlockPos pos);

   public Optional getBucketFillSound() {
      return Optional.empty();
   }

   /** @deprecated */
   @Deprecated
   public RegistryEntry.Reference getRegistryEntry() {
      return this.registryEntry;
   }
}
