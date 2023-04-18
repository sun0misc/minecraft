package net.minecraft.fluid;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.State;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class FluidState extends State {
   public static final Codec CODEC;
   public static final int field_31727 = 9;
   public static final int field_31728 = 8;

   public FluidState(Fluid fluid, ImmutableMap propertiesMap, MapCodec codec) {
      super(fluid, propertiesMap, codec);
   }

   public Fluid getFluid() {
      return (Fluid)this.owner;
   }

   public boolean isStill() {
      return this.getFluid().isStill(this);
   }

   public boolean isEqualAndStill(Fluid fluid) {
      return this.owner == fluid && ((Fluid)this.owner).isStill(this);
   }

   public boolean isEmpty() {
      return this.getFluid().isEmpty();
   }

   public float getHeight(BlockView world, BlockPos pos) {
      return this.getFluid().getHeight(this, world, pos);
   }

   public float getHeight() {
      return this.getFluid().getHeight(this);
   }

   public int getLevel() {
      return this.getFluid().getLevel(this);
   }

   public boolean method_15756(BlockView world, BlockPos pos) {
      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            BlockPos lv = pos.add(i, 0, j);
            FluidState lv2 = world.getFluidState(lv);
            if (!lv2.getFluid().matchesType(this.getFluid()) && !world.getBlockState(lv).isOpaqueFullCube(world, lv)) {
               return true;
            }
         }
      }

      return false;
   }

   public void onScheduledTick(World world, BlockPos pos) {
      this.getFluid().onScheduledTick(world, pos, this);
   }

   public void randomDisplayTick(World world, BlockPos pos, Random random) {
      this.getFluid().randomDisplayTick(world, pos, this, random);
   }

   public boolean hasRandomTicks() {
      return this.getFluid().hasRandomTicks();
   }

   public void onRandomTick(World world, BlockPos pos, Random random) {
      this.getFluid().onRandomTick(world, pos, this, random);
   }

   public Vec3d getVelocity(BlockView world, BlockPos pos) {
      return this.getFluid().getVelocity(world, pos, this);
   }

   public BlockState getBlockState() {
      return this.getFluid().toBlockState(this);
   }

   @Nullable
   public ParticleEffect getParticle() {
      return this.getFluid().getParticle();
   }

   public boolean isIn(TagKey tag) {
      return this.getFluid().getRegistryEntry().isIn(tag);
   }

   public boolean isIn(RegistryEntryList fluids) {
      return fluids.contains(this.getFluid().getRegistryEntry());
   }

   public boolean isOf(Fluid fluid) {
      return this.getFluid() == fluid;
   }

   public float getBlastResistance() {
      return this.getFluid().getBlastResistance();
   }

   public boolean canBeReplacedWith(BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
      return this.getFluid().canBeReplacedWith(this, world, pos, fluid, direction);
   }

   public VoxelShape getShape(BlockView world, BlockPos pos) {
      return this.getFluid().getShape(this, world, pos);
   }

   public RegistryEntry getRegistryEntry() {
      return ((Fluid)this.owner).getRegistryEntry();
   }

   public Stream streamTags() {
      return ((Fluid)this.owner).getRegistryEntry().streamTags();
   }

   static {
      CODEC = createCodec(Registries.FLUID.getCodec(), Fluid::getDefaultState).stable();
   }
}
