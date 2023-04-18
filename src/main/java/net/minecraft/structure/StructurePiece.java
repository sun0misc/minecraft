package net.minecraft.structure;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class StructurePiece {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected static final BlockState AIR;
   protected BlockBox boundingBox;
   @Nullable
   private Direction facing;
   private BlockMirror mirror;
   private BlockRotation rotation;
   protected int chainLength;
   private final StructurePieceType type;
   private static final Set BLOCKS_NEEDING_POST_PROCESSING;

   protected StructurePiece(StructurePieceType type, int length, BlockBox boundingBox) {
      this.type = type;
      this.chainLength = length;
      this.boundingBox = boundingBox;
   }

   public StructurePiece(StructurePieceType type, NbtCompound nbt) {
      int var10002 = nbt.getInt("GD");
      DataResult var10003 = BlockBox.CODEC.parse(NbtOps.INSTANCE, nbt.get("BB"));
      Logger var10004 = LOGGER;
      Objects.requireNonNull(var10004);
      this(type, var10002, (BlockBox)var10003.resultOrPartial(var10004::error).orElseThrow(() -> {
         return new IllegalArgumentException("Invalid boundingbox");
      }));
      int i = nbt.getInt("O");
      this.setOrientation(i == -1 ? null : Direction.fromHorizontal(i));
   }

   protected static BlockBox createBox(int x, int y, int z, Direction orientation, int width, int height, int depth) {
      return orientation.getAxis() == Direction.Axis.Z ? new BlockBox(x, y, z, x + width - 1, y + height - 1, z + depth - 1) : new BlockBox(x, y, z, x + depth - 1, y + height - 1, z + width - 1);
   }

   protected static Direction getRandomHorizontalDirection(Random random) {
      return Direction.Type.HORIZONTAL.random(random);
   }

   public final NbtCompound toNbt(StructureContext context) {
      NbtCompound lv = new NbtCompound();
      lv.putString("id", Registries.STRUCTURE_PIECE.getId(this.getType()).toString());
      DataResult var10000 = BlockBox.CODEC.encodeStart(NbtOps.INSTANCE, this.boundingBox);
      Logger var10001 = LOGGER;
      Objects.requireNonNull(var10001);
      var10000.resultOrPartial(var10001::error).ifPresent((arg2) -> {
         lv.put("BB", arg2);
      });
      Direction lv2 = this.getFacing();
      lv.putInt("O", lv2 == null ? -1 : lv2.getHorizontal());
      lv.putInt("GD", this.chainLength);
      this.writeNbt(context, lv);
      return lv;
   }

   protected abstract void writeNbt(StructureContext context, NbtCompound nbt);

   public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
   }

   public abstract void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot);

   public BlockBox getBoundingBox() {
      return this.boundingBox;
   }

   public int getChainLength() {
      return this.chainLength;
   }

   public void setChainLength(int chainLength) {
      this.chainLength = chainLength;
   }

   public boolean intersectsChunk(ChunkPos pos, int offset) {
      int j = pos.getStartX();
      int k = pos.getStartZ();
      return this.boundingBox.intersectsXZ(j - offset, k - offset, j + 15 + offset, k + 15 + offset);
   }

   public BlockPos getCenter() {
      return new BlockPos(this.boundingBox.getCenter());
   }

   protected BlockPos.Mutable offsetPos(int x, int y, int z) {
      return new BlockPos.Mutable(this.applyXTransform(x, z), this.applyYTransform(y), this.applyZTransform(x, z));
   }

   protected int applyXTransform(int x, int z) {
      Direction lv = this.getFacing();
      if (lv == null) {
         return x;
      } else {
         switch (lv) {
            case NORTH:
            case SOUTH:
               return this.boundingBox.getMinX() + x;
            case WEST:
               return this.boundingBox.getMaxX() - z;
            case EAST:
               return this.boundingBox.getMinX() + z;
            default:
               return x;
         }
      }
   }

   protected int applyYTransform(int y) {
      return this.getFacing() == null ? y : y + this.boundingBox.getMinY();
   }

   protected int applyZTransform(int x, int z) {
      Direction lv = this.getFacing();
      if (lv == null) {
         return z;
      } else {
         switch (lv) {
            case NORTH:
               return this.boundingBox.getMaxZ() - z;
            case SOUTH:
               return this.boundingBox.getMinZ() + z;
            case WEST:
            case EAST:
               return this.boundingBox.getMinZ() + x;
            default:
               return z;
         }
      }
   }

   protected void addBlock(StructureWorldAccess world, BlockState block, int x, int y, int z, BlockBox box) {
      BlockPos lv = this.offsetPos(x, y, z);
      if (box.contains(lv)) {
         if (this.canAddBlock(world, x, y, z, box)) {
            if (this.mirror != BlockMirror.NONE) {
               block = block.mirror(this.mirror);
            }

            if (this.rotation != BlockRotation.NONE) {
               block = block.rotate(this.rotation);
            }

            world.setBlockState(lv, block, Block.NOTIFY_LISTENERS);
            FluidState lv2 = world.getFluidState(lv);
            if (!lv2.isEmpty()) {
               world.scheduleFluidTick(lv, lv2.getFluid(), 0);
            }

            if (BLOCKS_NEEDING_POST_PROCESSING.contains(block.getBlock())) {
               world.getChunk(lv).markBlockForPostProcessing(lv);
            }

         }
      }
   }

   protected boolean canAddBlock(WorldView world, int x, int y, int z, BlockBox box) {
      return true;
   }

   protected BlockState getBlockAt(BlockView world, int x, int y, int z, BlockBox box) {
      BlockPos lv = this.offsetPos(x, y, z);
      return !box.contains(lv) ? Blocks.AIR.getDefaultState() : world.getBlockState(lv);
   }

   protected boolean isUnderSeaLevel(WorldView world, int x, int z, int y, BlockBox box) {
      BlockPos lv = this.offsetPos(x, z + 1, y);
      if (!box.contains(lv)) {
         return false;
      } else {
         return lv.getY() < world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, lv.getX(), lv.getZ());
      }
   }

   protected void fill(StructureWorldAccess world, BlockBox bounds, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      for(int o = minY; o <= maxY; ++o) {
         for(int p = minX; p <= maxX; ++p) {
            for(int q = minZ; q <= maxZ; ++q) {
               this.addBlock(world, Blocks.AIR.getDefaultState(), p, o, q, bounds);
            }
         }
      }

   }

   protected void fillWithOutline(StructureWorldAccess world, BlockBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState outline, BlockState inside, boolean cantReplaceAir) {
      for(int o = minY; o <= maxY; ++o) {
         for(int p = minX; p <= maxX; ++p) {
            for(int q = minZ; q <= maxZ; ++q) {
               if (!cantReplaceAir || !this.getBlockAt(world, p, o, q, box).isAir()) {
                  if (o != minY && o != maxY && p != minX && p != maxX && q != minZ && q != maxZ) {
                     this.addBlock(world, inside, p, o, q, box);
                  } else {
                     this.addBlock(world, outline, p, o, q, box);
                  }
               }
            }
         }
      }

   }

   protected void fillWithOutline(StructureWorldAccess world, BlockBox box, BlockBox fillBox, BlockState outline, BlockState inside, boolean cantReplaceAir) {
      this.fillWithOutline(world, box, fillBox.getMinX(), fillBox.getMinY(), fillBox.getMinZ(), fillBox.getMaxX(), fillBox.getMaxY(), fillBox.getMaxZ(), outline, inside, cantReplaceAir);
   }

   protected void fillWithOutline(StructureWorldAccess world, BlockBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean cantReplaceAir, Random random, BlockRandomizer randomizer) {
      for(int o = minY; o <= maxY; ++o) {
         for(int p = minX; p <= maxX; ++p) {
            for(int q = minZ; q <= maxZ; ++q) {
               if (!cantReplaceAir || !this.getBlockAt(world, p, o, q, box).isAir()) {
                  randomizer.setBlock(random, p, o, q, o == minY || o == maxY || p == minX || p == maxX || q == minZ || q == maxZ);
                  this.addBlock(world, randomizer.getBlock(), p, o, q, box);
               }
            }
         }
      }

   }

   protected void fillWithOutline(StructureWorldAccess world, BlockBox box, BlockBox fillBox, boolean cantReplaceAir, Random random, BlockRandomizer randomizer) {
      this.fillWithOutline(world, box, fillBox.getMinX(), fillBox.getMinY(), fillBox.getMinZ(), fillBox.getMaxX(), fillBox.getMaxY(), fillBox.getMaxZ(), cantReplaceAir, random, randomizer);
   }

   protected void fillWithOutlineUnderSeaLevel(StructureWorldAccess world, BlockBox box, Random random, float blockChance, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState outline, BlockState inside, boolean cantReplaceAir, boolean stayBelowSeaLevel) {
      for(int o = minY; o <= maxY; ++o) {
         for(int p = minX; p <= maxX; ++p) {
            for(int q = minZ; q <= maxZ; ++q) {
               if (!(random.nextFloat() > blockChance) && (!cantReplaceAir || !this.getBlockAt(world, p, o, q, box).isAir()) && (!stayBelowSeaLevel || this.isUnderSeaLevel(world, p, o, q, box))) {
                  if (o != minY && o != maxY && p != minX && p != maxX && q != minZ && q != maxZ) {
                     this.addBlock(world, inside, p, o, q, box);
                  } else {
                     this.addBlock(world, outline, p, o, q, box);
                  }
               }
            }
         }
      }

   }

   protected void addBlockWithRandomThreshold(StructureWorldAccess world, BlockBox bounds, Random random, float threshold, int x, int y, int z, BlockState state) {
      if (random.nextFloat() < threshold) {
         this.addBlock(world, state, x, y, z, bounds);
      }

   }

   protected void fillHalfEllipsoid(StructureWorldAccess world, BlockBox bounds, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState block, boolean cantReplaceAir) {
      float f = (float)(maxX - minX + 1);
      float g = (float)(maxY - minY + 1);
      float h = (float)(maxZ - minZ + 1);
      float o = (float)minX + f / 2.0F;
      float p = (float)minZ + h / 2.0F;

      for(int q = minY; q <= maxY; ++q) {
         float r = (float)(q - minY) / g;

         for(int s = minX; s <= maxX; ++s) {
            float t = ((float)s - o) / (f * 0.5F);

            for(int u = minZ; u <= maxZ; ++u) {
               float v = ((float)u - p) / (h * 0.5F);
               if (!cantReplaceAir || !this.getBlockAt(world, s, q, u, bounds).isAir()) {
                  float w = t * t + r * r + v * v;
                  if (w <= 1.05F) {
                     this.addBlock(world, block, s, q, u, bounds);
                  }
               }
            }
         }
      }

   }

   protected void fillDownwards(StructureWorldAccess world, BlockState state, int x, int y, int z, BlockBox box) {
      BlockPos.Mutable lv = this.offsetPos(x, y, z);
      if (box.contains(lv)) {
         while(this.canReplace(world.getBlockState(lv)) && lv.getY() > world.getBottomY() + 1) {
            world.setBlockState(lv, state, Block.NOTIFY_LISTENERS);
            lv.move(Direction.DOWN);
         }

      }
   }

   protected boolean canReplace(BlockState state) {
      return state.isAir() || state.isLiquid() || state.isOf(Blocks.GLOW_LICHEN) || state.isOf(Blocks.SEAGRASS) || state.isOf(Blocks.TALL_SEAGRASS);
   }

   protected boolean addChest(StructureWorldAccess world, BlockBox boundingBox, Random random, int x, int y, int z, Identifier lootTableId) {
      return this.addChest(world, boundingBox, random, this.offsetPos(x, y, z), lootTableId, (BlockState)null);
   }

   public static BlockState orientateChest(BlockView world, BlockPos pos, BlockState state) {
      Direction lv = null;
      Iterator var4 = Direction.Type.HORIZONTAL.iterator();

      while(var4.hasNext()) {
         Direction lv2 = (Direction)var4.next();
         BlockPos lv3 = pos.offset(lv2);
         BlockState lv4 = world.getBlockState(lv3);
         if (lv4.isOf(Blocks.CHEST)) {
            return state;
         }

         if (lv4.isOpaqueFullCube(world, lv3)) {
            if (lv != null) {
               lv = null;
               break;
            }

            lv = lv2;
         }
      }

      if (lv != null) {
         return (BlockState)state.with(HorizontalFacingBlock.FACING, lv.getOpposite());
      } else {
         Direction lv5 = (Direction)state.get(HorizontalFacingBlock.FACING);
         BlockPos lv6 = pos.offset(lv5);
         if (world.getBlockState(lv6).isOpaqueFullCube(world, lv6)) {
            lv5 = lv5.getOpposite();
            lv6 = pos.offset(lv5);
         }

         if (world.getBlockState(lv6).isOpaqueFullCube(world, lv6)) {
            lv5 = lv5.rotateYClockwise();
            lv6 = pos.offset(lv5);
         }

         if (world.getBlockState(lv6).isOpaqueFullCube(world, lv6)) {
            lv5 = lv5.getOpposite();
            pos.offset(lv5);
         }

         return (BlockState)state.with(HorizontalFacingBlock.FACING, lv5);
      }
   }

   protected boolean addChest(ServerWorldAccess world, BlockBox boundingBox, Random random, BlockPos pos, Identifier lootTableId, @Nullable BlockState block) {
      if (boundingBox.contains(pos) && !world.getBlockState(pos).isOf(Blocks.CHEST)) {
         if (block == null) {
            block = orientateChest(world, pos, Blocks.CHEST.getDefaultState());
         }

         world.setBlockState(pos, block, Block.NOTIFY_LISTENERS);
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof ChestBlockEntity) {
            ((ChestBlockEntity)lv).setLootTable(lootTableId, random.nextLong());
         }

         return true;
      } else {
         return false;
      }
   }

   protected boolean addDispenser(StructureWorldAccess world, BlockBox boundingBox, Random random, int x, int y, int z, Direction facing, Identifier lootTableId) {
      BlockPos lv = this.offsetPos(x, y, z);
      if (boundingBox.contains(lv) && !world.getBlockState(lv).isOf(Blocks.DISPENSER)) {
         this.addBlock(world, (BlockState)Blocks.DISPENSER.getDefaultState().with(DispenserBlock.FACING, facing), x, y, z, boundingBox);
         BlockEntity lv2 = world.getBlockEntity(lv);
         if (lv2 instanceof DispenserBlockEntity) {
            ((DispenserBlockEntity)lv2).setLootTable(lootTableId, random.nextLong());
         }

         return true;
      } else {
         return false;
      }
   }

   public void translate(int x, int y, int z) {
      this.boundingBox.move(x, y, z);
   }

   public static BlockBox boundingBox(Stream pieces) {
      Stream var10000 = pieces.map(StructurePiece::getBoundingBox);
      Objects.requireNonNull(var10000);
      return (BlockBox)BlockBox.encompass(var10000::iterator).orElseThrow(() -> {
         return new IllegalStateException("Unable to calculate boundingbox without pieces");
      });
   }

   @Nullable
   public static StructurePiece firstIntersecting(List pieces, BlockBox box) {
      Iterator var2 = pieces.iterator();

      StructurePiece lv;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         lv = (StructurePiece)var2.next();
      } while(!lv.getBoundingBox().intersects(box));

      return lv;
   }

   @Nullable
   public Direction getFacing() {
      return this.facing;
   }

   public void setOrientation(@Nullable Direction orientation) {
      this.facing = orientation;
      if (orientation == null) {
         this.rotation = BlockRotation.NONE;
         this.mirror = BlockMirror.NONE;
      } else {
         switch (orientation) {
            case SOUTH:
               this.mirror = BlockMirror.LEFT_RIGHT;
               this.rotation = BlockRotation.NONE;
               break;
            case WEST:
               this.mirror = BlockMirror.LEFT_RIGHT;
               this.rotation = BlockRotation.CLOCKWISE_90;
               break;
            case EAST:
               this.mirror = BlockMirror.NONE;
               this.rotation = BlockRotation.CLOCKWISE_90;
               break;
            default:
               this.mirror = BlockMirror.NONE;
               this.rotation = BlockRotation.NONE;
         }
      }

   }

   public BlockRotation getRotation() {
      return this.rotation;
   }

   public BlockMirror getMirror() {
      return this.mirror;
   }

   public StructurePieceType getType() {
      return this.type;
   }

   static {
      AIR = Blocks.CAVE_AIR.getDefaultState();
      BLOCKS_NEEDING_POST_PROCESSING = ImmutableSet.builder().add(Blocks.NETHER_BRICK_FENCE).add(Blocks.TORCH).add(Blocks.WALL_TORCH).add(Blocks.OAK_FENCE).add(Blocks.SPRUCE_FENCE).add(Blocks.DARK_OAK_FENCE).add(Blocks.ACACIA_FENCE).add(Blocks.BIRCH_FENCE).add(Blocks.JUNGLE_FENCE).add(Blocks.LADDER).add(Blocks.IRON_BARS).build();
   }

   public abstract static class BlockRandomizer {
      protected BlockState block;

      public BlockRandomizer() {
         this.block = Blocks.AIR.getDefaultState();
      }

      public abstract void setBlock(Random random, int x, int y, int z, boolean placeBlock);

      public BlockState getBlock() {
         return this.block;
      }
   }
}
