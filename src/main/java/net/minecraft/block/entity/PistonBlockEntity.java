package net.minecraft.block.entity;

import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.enums.PistonType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Boxes;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class PistonBlockEntity extends BlockEntity {
   private static final int field_31382 = 2;
   private static final double field_31383 = 0.01;
   public static final double field_31381 = 0.51;
   private BlockState pushedBlock;
   private Direction facing;
   private boolean extending;
   private boolean source;
   private static final ThreadLocal field_12205 = ThreadLocal.withInitial(() -> {
      return null;
   });
   private float progress;
   private float lastProgress;
   private long savedWorldTime;
   private int field_26705;

   public PistonBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.PISTON, pos, state);
      this.pushedBlock = Blocks.AIR.getDefaultState();
   }

   public PistonBlockEntity(BlockPos pos, BlockState state, BlockState pushedBlock, Direction facing, boolean extending, boolean source) {
      this(pos, state);
      this.pushedBlock = pushedBlock;
      this.facing = facing;
      this.extending = extending;
      this.source = source;
   }

   public NbtCompound toInitialChunkDataNbt() {
      return this.createNbt();
   }

   public boolean isExtending() {
      return this.extending;
   }

   public Direction getFacing() {
      return this.facing;
   }

   public boolean isSource() {
      return this.source;
   }

   public float getProgress(float tickDelta) {
      if (tickDelta > 1.0F) {
         tickDelta = 1.0F;
      }

      return MathHelper.lerp(tickDelta, this.lastProgress, this.progress);
   }

   public float getRenderOffsetX(float tickDelta) {
      return (float)this.facing.getOffsetX() * this.getAmountExtended(this.getProgress(tickDelta));
   }

   public float getRenderOffsetY(float tickDelta) {
      return (float)this.facing.getOffsetY() * this.getAmountExtended(this.getProgress(tickDelta));
   }

   public float getRenderOffsetZ(float tickDelta) {
      return (float)this.facing.getOffsetZ() * this.getAmountExtended(this.getProgress(tickDelta));
   }

   private float getAmountExtended(float progress) {
      return this.extending ? progress - 1.0F : 1.0F - progress;
   }

   private BlockState getHeadBlockState() {
      return !this.isExtending() && this.isSource() && this.pushedBlock.getBlock() instanceof PistonBlock ? (BlockState)((BlockState)((BlockState)Blocks.PISTON_HEAD.getDefaultState().with(PistonHeadBlock.SHORT, this.progress > 0.25F)).with(PistonHeadBlock.TYPE, this.pushedBlock.isOf(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT)).with(PistonHeadBlock.FACING, (Direction)this.pushedBlock.get(PistonBlock.FACING)) : this.pushedBlock;
   }

   private static void pushEntities(World world, BlockPos pos, float f, PistonBlockEntity blockEntity) {
      Direction lv = blockEntity.getMovementDirection();
      double d = (double)(f - blockEntity.progress);
      VoxelShape lv2 = blockEntity.getHeadBlockState().getCollisionShape(world, pos);
      if (!lv2.isEmpty()) {
         Box lv3 = offsetHeadBox(pos, lv2.getBoundingBox(), blockEntity);
         List list = world.getOtherEntities((Entity)null, Boxes.stretch(lv3, lv, d).union(lv3));
         if (!list.isEmpty()) {
            List list2 = lv2.getBoundingBoxes();
            boolean bl = blockEntity.pushedBlock.isOf(Blocks.SLIME_BLOCK);
            Iterator var12 = list.iterator();

            while(true) {
               Entity lv4;
               while(true) {
                  do {
                     if (!var12.hasNext()) {
                        return;
                     }

                     lv4 = (Entity)var12.next();
                  } while(lv4.getPistonBehavior() == PistonBehavior.IGNORE);

                  if (!bl) {
                     break;
                  }

                  if (!(lv4 instanceof ServerPlayerEntity)) {
                     Vec3d lv5 = lv4.getVelocity();
                     double e = lv5.x;
                     double g = lv5.y;
                     double h = lv5.z;
                     switch (lv.getAxis()) {
                        case X:
                           e = (double)lv.getOffsetX();
                           break;
                        case Y:
                           g = (double)lv.getOffsetY();
                           break;
                        case Z:
                           h = (double)lv.getOffsetZ();
                     }

                     lv4.setVelocity(e, g, h);
                     break;
                  }
               }

               double i = 0.0;
               Iterator var16 = list2.iterator();

               while(var16.hasNext()) {
                  Box lv6 = (Box)var16.next();
                  Box lv7 = Boxes.stretch(offsetHeadBox(pos, lv6, blockEntity), lv, d);
                  Box lv8 = lv4.getBoundingBox();
                  if (lv7.intersects(lv8)) {
                     i = Math.max(i, getIntersectionSize(lv7, lv, lv8));
                     if (i >= d) {
                        break;
                     }
                  }
               }

               if (!(i <= 0.0)) {
                  i = Math.min(i, d) + 0.01;
                  moveEntity(lv, lv4, i, lv);
                  if (!blockEntity.extending && blockEntity.source) {
                     push(pos, lv4, lv, d);
                  }
               }
            }
         }
      }
   }

   private static void moveEntity(Direction direction, Entity entity, double d, Direction movementDirection) {
      field_12205.set(direction);
      entity.move(MovementType.PISTON, new Vec3d(d * (double)movementDirection.getOffsetX(), d * (double)movementDirection.getOffsetY(), d * (double)movementDirection.getOffsetZ()));
      field_12205.set((Object)null);
   }

   private static void moveEntitiesInHoneyBlock(World world, BlockPos pos, float f, PistonBlockEntity blockEntity) {
      if (blockEntity.isPushingHoneyBlock()) {
         Direction lv = blockEntity.getMovementDirection();
         if (lv.getAxis().isHorizontal()) {
            double d = blockEntity.pushedBlock.getCollisionShape(world, pos).getMax(Direction.Axis.Y);
            Box lv2 = offsetHeadBox(pos, new Box(0.0, d, 0.0, 1.0, 1.5000000999999998, 1.0), blockEntity);
            double e = (double)(f - blockEntity.progress);
            List list = world.getOtherEntities((Entity)null, lv2, (entity) -> {
               return canMoveEntity(lv2, entity);
            });
            Iterator var11 = list.iterator();

            while(var11.hasNext()) {
               Entity lv3 = (Entity)var11.next();
               moveEntity(lv, lv3, e, lv);
            }

         }
      }
   }

   private static boolean canMoveEntity(Box box, Entity entity) {
      return entity.getPistonBehavior() == PistonBehavior.NORMAL && entity.isOnGround() && entity.getX() >= box.minX && entity.getX() <= box.maxX && entity.getZ() >= box.minZ && entity.getZ() <= box.maxZ;
   }

   private boolean isPushingHoneyBlock() {
      return this.pushedBlock.isOf(Blocks.HONEY_BLOCK);
   }

   public Direction getMovementDirection() {
      return this.extending ? this.facing : this.facing.getOpposite();
   }

   private static double getIntersectionSize(Box arg, Direction arg2, Box arg3) {
      switch (arg2) {
         case EAST:
            return arg.maxX - arg3.minX;
         case WEST:
            return arg3.maxX - arg.minX;
         case UP:
         default:
            return arg.maxY - arg3.minY;
         case DOWN:
            return arg3.maxY - arg.minY;
         case SOUTH:
            return arg.maxZ - arg3.minZ;
         case NORTH:
            return arg3.maxZ - arg.minZ;
      }
   }

   private static Box offsetHeadBox(BlockPos pos, Box box, PistonBlockEntity blockEntity) {
      double d = (double)blockEntity.getAmountExtended(blockEntity.progress);
      return box.offset((double)pos.getX() + d * (double)blockEntity.facing.getOffsetX(), (double)pos.getY() + d * (double)blockEntity.facing.getOffsetY(), (double)pos.getZ() + d * (double)blockEntity.facing.getOffsetZ());
   }

   private static void push(BlockPos pos, Entity entity, Direction direction, double amount) {
      Box lv = entity.getBoundingBox();
      Box lv2 = VoxelShapes.fullCube().getBoundingBox().offset(pos);
      if (lv.intersects(lv2)) {
         Direction lv3 = direction.getOpposite();
         double e = getIntersectionSize(lv2, lv3, lv) + 0.01;
         double f = getIntersectionSize(lv2, lv3, lv.intersection(lv2)) + 0.01;
         if (Math.abs(e - f) < 0.01) {
            e = Math.min(e, amount) + 0.01;
            moveEntity(direction, entity, e, lv3);
         }
      }

   }

   public BlockState getPushedBlock() {
      return this.pushedBlock;
   }

   public void finish() {
      if (this.world != null && (this.lastProgress < 1.0F || this.world.isClient)) {
         this.progress = 1.0F;
         this.lastProgress = this.progress;
         this.world.removeBlockEntity(this.pos);
         this.markRemoved();
         if (this.world.getBlockState(this.pos).isOf(Blocks.MOVING_PISTON)) {
            BlockState lv;
            if (this.source) {
               lv = Blocks.AIR.getDefaultState();
            } else {
               lv = Block.postProcessState(this.pushedBlock, this.world, this.pos);
            }

            this.world.setBlockState(this.pos, lv, Block.NOTIFY_ALL);
            this.world.updateNeighbor(this.pos, lv.getBlock(), this.pos);
         }
      }

   }

   public static void tick(World world, BlockPos pos, BlockState state, PistonBlockEntity blockEntity) {
      blockEntity.savedWorldTime = world.getTime();
      blockEntity.lastProgress = blockEntity.progress;
      if (blockEntity.lastProgress >= 1.0F) {
         if (world.isClient && blockEntity.field_26705 < 5) {
            ++blockEntity.field_26705;
         } else {
            world.removeBlockEntity(pos);
            blockEntity.markRemoved();
            if (world.getBlockState(pos).isOf(Blocks.MOVING_PISTON)) {
               BlockState lv = Block.postProcessState(blockEntity.pushedBlock, world, pos);
               if (lv.isAir()) {
                  world.setBlockState(pos, blockEntity.pushedBlock, Block.NO_REDRAW | Block.FORCE_STATE | Block.MOVED);
                  Block.replace(blockEntity.pushedBlock, lv, world, pos, 3);
               } else {
                  if (lv.contains(Properties.WATERLOGGED) && (Boolean)lv.get(Properties.WATERLOGGED)) {
                     lv = (BlockState)lv.with(Properties.WATERLOGGED, false);
                  }

                  world.setBlockState(pos, lv, Block.NOTIFY_ALL | Block.MOVED);
                  world.updateNeighbor(pos, lv.getBlock(), pos);
               }
            }

         }
      } else {
         float f = blockEntity.progress + 0.5F;
         pushEntities(world, pos, f, blockEntity);
         moveEntitiesInHoneyBlock(world, pos, f, blockEntity);
         blockEntity.progress = f;
         if (blockEntity.progress >= 1.0F) {
            blockEntity.progress = 1.0F;
         }

      }
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      RegistryEntryLookup lv = this.world != null ? this.world.createCommandRegistryWrapper(RegistryKeys.BLOCK) : Registries.BLOCK.getReadOnlyWrapper();
      this.pushedBlock = NbtHelper.toBlockState((RegistryEntryLookup)lv, nbt.getCompound("blockState"));
      this.facing = Direction.byId(nbt.getInt("facing"));
      this.progress = nbt.getFloat("progress");
      this.lastProgress = this.progress;
      this.extending = nbt.getBoolean("extending");
      this.source = nbt.getBoolean("source");
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      nbt.put("blockState", NbtHelper.fromBlockState(this.pushedBlock));
      nbt.putInt("facing", this.facing.getId());
      nbt.putFloat("progress", this.lastProgress);
      nbt.putBoolean("extending", this.extending);
      nbt.putBoolean("source", this.source);
   }

   public VoxelShape getCollisionShape(BlockView world, BlockPos pos) {
      VoxelShape lv;
      if (!this.extending && this.source && this.pushedBlock.getBlock() instanceof PistonBlock) {
         lv = ((BlockState)this.pushedBlock.with(PistonBlock.EXTENDED, true)).getCollisionShape(world, pos);
      } else {
         lv = VoxelShapes.empty();
      }

      Direction lv2 = (Direction)field_12205.get();
      if ((double)this.progress < 1.0 && lv2 == this.getMovementDirection()) {
         return lv;
      } else {
         BlockState lv3;
         if (this.isSource()) {
            lv3 = (BlockState)((BlockState)Blocks.PISTON_HEAD.getDefaultState().with(PistonHeadBlock.FACING, this.facing)).with(PistonHeadBlock.SHORT, this.extending != 1.0F - this.progress < 0.25F);
         } else {
            lv3 = this.pushedBlock;
         }

         float f = this.getAmountExtended(this.progress);
         double d = (double)((float)this.facing.getOffsetX() * f);
         double e = (double)((float)this.facing.getOffsetY() * f);
         double g = (double)((float)this.facing.getOffsetZ() * f);
         return VoxelShapes.union(lv, lv3.getCollisionShape(world, pos).offset(d, e, g));
      }
   }

   public long getSavedWorldTime() {
      return this.savedWorldTime;
   }

   public void setWorld(World world) {
      super.setWorld(world);
      if (world.createCommandRegistryWrapper(RegistryKeys.BLOCK).getOptional(this.pushedBlock.getBlock().getRegistryEntry().registryKey()).isEmpty()) {
         this.pushedBlock = Blocks.AIR.getDefaultState();
      }

   }
}
