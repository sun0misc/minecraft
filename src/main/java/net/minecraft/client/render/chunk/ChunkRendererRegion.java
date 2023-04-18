package net.minecraft.client.render.chunk;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ChunkRendererRegion implements BlockRenderView {
   private final int chunkXOffset;
   private final int chunkZOffset;
   protected final RenderedChunk[][] chunks;
   protected final World world;

   ChunkRendererRegion(World world, int chunkX, int chunkZ, RenderedChunk[][] chunks) {
      this.world = world;
      this.chunkXOffset = chunkX;
      this.chunkZOffset = chunkZ;
      this.chunks = chunks;
   }

   public BlockState getBlockState(BlockPos pos) {
      int i = ChunkSectionPos.getSectionCoord(pos.getX()) - this.chunkXOffset;
      int j = ChunkSectionPos.getSectionCoord(pos.getZ()) - this.chunkZOffset;
      return this.chunks[i][j].getBlockState(pos);
   }

   public FluidState getFluidState(BlockPos pos) {
      int i = ChunkSectionPos.getSectionCoord(pos.getX()) - this.chunkXOffset;
      int j = ChunkSectionPos.getSectionCoord(pos.getZ()) - this.chunkZOffset;
      return this.chunks[i][j].getBlockState(pos).getFluidState();
   }

   public float getBrightness(Direction direction, boolean shaded) {
      return this.world.getBrightness(direction, shaded);
   }

   public LightingProvider getLightingProvider() {
      return this.world.getLightingProvider();
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos) {
      int i = ChunkSectionPos.getSectionCoord(pos.getX()) - this.chunkXOffset;
      int j = ChunkSectionPos.getSectionCoord(pos.getZ()) - this.chunkZOffset;
      return this.chunks[i][j].getBlockEntity(pos);
   }

   public int getColor(BlockPos pos, ColorResolver colorResolver) {
      return this.world.getColor(pos, colorResolver);
   }

   public int getBottomY() {
      return this.world.getBottomY();
   }

   public int getHeight() {
      return this.world.getHeight();
   }
}
