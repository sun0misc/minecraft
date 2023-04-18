package net.minecraft.structure;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Clearable;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class StructureTemplate {
   public static final String PALETTE_KEY = "palette";
   public static final String PALETTES_KEY = "palettes";
   public static final String ENTITIES_KEY = "entities";
   public static final String BLOCKS_KEY = "blocks";
   public static final String BLOCKS_POS_KEY = "pos";
   public static final String BLOCKS_STATE_KEY = "state";
   public static final String BLOCKS_NBT_KEY = "nbt";
   public static final String ENTITIES_POS_KEY = "pos";
   public static final String ENTITIES_BLOCK_POS_KEY = "blockPos";
   public static final String ENTITIES_NBT_KEY = "nbt";
   public static final String SIZE_KEY = "size";
   private final List blockInfoLists = Lists.newArrayList();
   private final List entities = Lists.newArrayList();
   private Vec3i size;
   private String author;

   public StructureTemplate() {
      this.size = Vec3i.ZERO;
      this.author = "?";
   }

   public Vec3i getSize() {
      return this.size;
   }

   public void setAuthor(String author) {
      this.author = author;
   }

   public String getAuthor() {
      return this.author;
   }

   public void saveFromWorld(World world, BlockPos start, Vec3i dimensions, boolean includeEntities, @Nullable Block ignoredBlock) {
      if (dimensions.getX() >= 1 && dimensions.getY() >= 1 && dimensions.getZ() >= 1) {
         BlockPos lv = start.add(dimensions).add(-1, -1, -1);
         List list = Lists.newArrayList();
         List list2 = Lists.newArrayList();
         List list3 = Lists.newArrayList();
         BlockPos lv2 = new BlockPos(Math.min(start.getX(), lv.getX()), Math.min(start.getY(), lv.getY()), Math.min(start.getZ(), lv.getZ()));
         BlockPos lv3 = new BlockPos(Math.max(start.getX(), lv.getX()), Math.max(start.getY(), lv.getY()), Math.max(start.getZ(), lv.getZ()));
         this.size = dimensions;
         Iterator var12 = BlockPos.iterate(lv2, lv3).iterator();

         while(true) {
            BlockPos lv4;
            BlockPos lv5;
            BlockState lv6;
            do {
               if (!var12.hasNext()) {
                  List list4 = combineSorted(list, list2, list3);
                  this.blockInfoLists.clear();
                  this.blockInfoLists.add(new PalettedBlockInfoList(list4));
                  if (includeEntities) {
                     this.addEntitiesFromWorld(world, lv2, lv3.add(1, 1, 1));
                  } else {
                     this.entities.clear();
                  }

                  return;
               }

               lv4 = (BlockPos)var12.next();
               lv5 = lv4.subtract(lv2);
               lv6 = world.getBlockState(lv4);
            } while(ignoredBlock != null && lv6.isOf(ignoredBlock));

            BlockEntity lv7 = world.getBlockEntity(lv4);
            StructureBlockInfo lv8;
            if (lv7 != null) {
               lv8 = new StructureBlockInfo(lv5, lv6, lv7.createNbtWithId());
            } else {
               lv8 = new StructureBlockInfo(lv5, lv6, (NbtCompound)null);
            }

            categorize(lv8, list, list2, list3);
         }
      }
   }

   private static void categorize(StructureBlockInfo blockInfo, List fullBlocks, List blocksWithNbt, List otherBlocks) {
      if (blockInfo.nbt != null) {
         blocksWithNbt.add(blockInfo);
      } else if (!blockInfo.state.getBlock().hasDynamicBounds() && blockInfo.state.isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)) {
         fullBlocks.add(blockInfo);
      } else {
         otherBlocks.add(blockInfo);
      }

   }

   private static List combineSorted(List fullBlocks, List blocksWithNbt, List otherBlocks) {
      Comparator comparator = Comparator.comparingInt((blockInfo) -> {
         return blockInfo.pos.getY();
      }).thenComparingInt((blockInfo) -> {
         return blockInfo.pos.getX();
      }).thenComparingInt((blockInfo) -> {
         return blockInfo.pos.getZ();
      });
      fullBlocks.sort(comparator);
      otherBlocks.sort(comparator);
      blocksWithNbt.sort(comparator);
      List list4 = Lists.newArrayList();
      list4.addAll(fullBlocks);
      list4.addAll(otherBlocks);
      list4.addAll(blocksWithNbt);
      return list4;
   }

   private void addEntitiesFromWorld(World world, BlockPos firstCorner, BlockPos secondCorner) {
      List list = world.getEntitiesByClass(Entity.class, new Box(firstCorner, secondCorner), (entity) -> {
         return !(entity instanceof PlayerEntity);
      });
      this.entities.clear();

      Vec3d lv2;
      NbtCompound lv3;
      BlockPos lv4;
      for(Iterator var5 = list.iterator(); var5.hasNext(); this.entities.add(new StructureEntityInfo(lv2, lv4, lv3.copy()))) {
         Entity lv = (Entity)var5.next();
         lv2 = new Vec3d(lv.getX() - (double)firstCorner.getX(), lv.getY() - (double)firstCorner.getY(), lv.getZ() - (double)firstCorner.getZ());
         lv3 = new NbtCompound();
         lv.saveNbt(lv3);
         if (lv instanceof PaintingEntity) {
            lv4 = ((PaintingEntity)lv).getDecorationBlockPos().subtract(firstCorner);
         } else {
            lv4 = BlockPos.ofFloored(lv2);
         }
      }

   }

   public List getInfosForBlock(BlockPos pos, StructurePlacementData placementData, Block block) {
      return this.getInfosForBlock(pos, placementData, block, true);
   }

   public ObjectArrayList getInfosForBlock(BlockPos pos, StructurePlacementData placementData, Block block, boolean transformed) {
      ObjectArrayList objectArrayList = new ObjectArrayList();
      BlockBox lv = placementData.getBoundingBox();
      if (this.blockInfoLists.isEmpty()) {
         return objectArrayList;
      } else {
         Iterator var7 = placementData.getRandomBlockInfos(this.blockInfoLists, pos).getAllOf(block).iterator();

         while(true) {
            StructureBlockInfo lv2;
            BlockPos lv3;
            do {
               if (!var7.hasNext()) {
                  return objectArrayList;
               }

               lv2 = (StructureBlockInfo)var7.next();
               lv3 = transformed ? transform(placementData, lv2.pos).add(pos) : lv2.pos;
            } while(lv != null && !lv.contains(lv3));

            objectArrayList.add(new StructureBlockInfo(lv3, lv2.state.rotate(placementData.getRotation()), lv2.nbt));
         }
      }
   }

   public BlockPos transformBox(StructurePlacementData placementData1, BlockPos pos1, StructurePlacementData placementData2, BlockPos pos2) {
      BlockPos lv = transform(placementData1, pos1);
      BlockPos lv2 = transform(placementData2, pos2);
      return lv.subtract(lv2);
   }

   public static BlockPos transform(StructurePlacementData placementData, BlockPos pos) {
      return transformAround(pos, placementData.getMirror(), placementData.getRotation(), placementData.getPosition());
   }

   public boolean place(ServerWorldAccess world, BlockPos pos, BlockPos pivot, StructurePlacementData placementData, Random random, int flags) {
      if (this.blockInfoLists.isEmpty()) {
         return false;
      } else {
         List list = placementData.getRandomBlockInfos(this.blockInfoLists, pos).getAll();
         if ((!list.isEmpty() || !placementData.shouldIgnoreEntities() && !this.entities.isEmpty()) && this.size.getX() >= 1 && this.size.getY() >= 1 && this.size.getZ() >= 1) {
            BlockBox lv = placementData.getBoundingBox();
            List list2 = Lists.newArrayListWithCapacity(placementData.shouldPlaceFluids() ? list.size() : 0);
            List list3 = Lists.newArrayListWithCapacity(placementData.shouldPlaceFluids() ? list.size() : 0);
            List list4 = Lists.newArrayListWithCapacity(list.size());
            int j = Integer.MAX_VALUE;
            int k = Integer.MAX_VALUE;
            int l = Integer.MAX_VALUE;
            int m = Integer.MIN_VALUE;
            int n = Integer.MIN_VALUE;
            int o = Integer.MIN_VALUE;
            List list5 = process(world, pos, pivot, placementData, list);
            Iterator var19 = list5.iterator();

            while(true) {
               StructureBlockInfo lv2;
               BlockPos lv3;
               BlockEntity lv6;
               do {
                  if (!var19.hasNext()) {
                     boolean bl = true;
                     Direction[] lvs = new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

                     Iterator iterator;
                     int p;
                     BlockState lv11;
                     while(bl && !list2.isEmpty()) {
                        bl = false;
                        iterator = list2.iterator();

                        while(iterator.hasNext()) {
                           BlockPos lv7 = (BlockPos)iterator.next();
                           FluidState lv8 = world.getFluidState(lv7);

                           for(p = 0; p < lvs.length && !lv8.isStill(); ++p) {
                              BlockPos lv9 = lv7.offset(lvs[p]);
                              FluidState lv10 = world.getFluidState(lv9);
                              if (lv10.isStill() && !list3.contains(lv9)) {
                                 lv8 = lv10;
                              }
                           }

                           if (lv8.isStill()) {
                              lv11 = world.getBlockState(lv7);
                              Block lv12 = lv11.getBlock();
                              if (lv12 instanceof FluidFillable) {
                                 ((FluidFillable)lv12).tryFillWithFluid(world, lv7, lv11, lv8);
                                 bl = true;
                                 iterator.remove();
                              }
                           }
                        }
                     }

                     if (j <= m) {
                        if (!placementData.shouldUpdateNeighbors()) {
                           VoxelSet lv13 = new BitSetVoxelSet(m - j + 1, n - k + 1, o - l + 1);
                           int q = j;
                           int r = k;
                           p = l;
                           Iterator var41 = list4.iterator();

                           while(var41.hasNext()) {
                              Pair pair = (Pair)var41.next();
                              BlockPos lv14 = (BlockPos)pair.getFirst();
                              lv13.set(lv14.getX() - q, lv14.getY() - r, lv14.getZ() - p);
                           }

                           updateCorner(world, flags, lv13, q, r, p);
                        }

                        iterator = list4.iterator();

                        while(iterator.hasNext()) {
                           Pair pair2 = (Pair)iterator.next();
                           BlockPos lv15 = (BlockPos)pair2.getFirst();
                           if (!placementData.shouldUpdateNeighbors()) {
                              lv11 = world.getBlockState(lv15);
                              BlockState lv16 = Block.postProcessState(lv11, world, lv15);
                              if (lv11 != lv16) {
                                 world.setBlockState(lv15, lv16, flags & ~Block.NOTIFY_NEIGHBORS | Block.FORCE_STATE);
                              }

                              world.updateNeighbors(lv15, lv16.getBlock());
                           }

                           if (pair2.getSecond() != null) {
                              lv6 = world.getBlockEntity(lv15);
                              if (lv6 != null) {
                                 lv6.markDirty();
                              }
                           }
                        }
                     }

                     if (!placementData.shouldIgnoreEntities()) {
                        this.spawnEntities(world, pos, placementData.getMirror(), placementData.getRotation(), placementData.getPosition(), lv, placementData.shouldInitializeMobs());
                     }

                     return true;
                  }

                  lv2 = (StructureBlockInfo)var19.next();
                  lv3 = lv2.pos;
               } while(lv != null && !lv.contains(lv3));

               FluidState lv4 = placementData.shouldPlaceFluids() ? world.getFluidState(lv3) : null;
               BlockState lv5 = lv2.state.mirror(placementData.getMirror()).rotate(placementData.getRotation());
               if (lv2.nbt != null) {
                  lv6 = world.getBlockEntity(lv3);
                  Clearable.clear(lv6);
                  world.setBlockState(lv3, Blocks.BARRIER.getDefaultState(), Block.NO_REDRAW | Block.FORCE_STATE);
               }

               if (world.setBlockState(lv3, lv5, flags)) {
                  j = Math.min(j, lv3.getX());
                  k = Math.min(k, lv3.getY());
                  l = Math.min(l, lv3.getZ());
                  m = Math.max(m, lv3.getX());
                  n = Math.max(n, lv3.getY());
                  o = Math.max(o, lv3.getZ());
                  list4.add(Pair.of(lv3, lv2.nbt));
                  if (lv2.nbt != null) {
                     lv6 = world.getBlockEntity(lv3);
                     if (lv6 != null) {
                        if (lv6 instanceof LootableContainerBlockEntity) {
                           lv2.nbt.putLong("LootTableSeed", random.nextLong());
                        }

                        lv6.readNbt(lv2.nbt);
                     }
                  }

                  if (lv4 != null) {
                     if (lv5.getFluidState().isStill()) {
                        list3.add(lv3);
                     } else if (lv5.getBlock() instanceof FluidFillable) {
                        ((FluidFillable)lv5.getBlock()).tryFillWithFluid(world, lv3, lv5, lv4);
                        if (!lv4.isStill()) {
                           list2.add(lv3);
                        }
                     }
                  }
               }
            }
         } else {
            return false;
         }
      }
   }

   public static void updateCorner(WorldAccess world, int flags, VoxelSet set, int startX, int startY, int startZ) {
      set.forEachDirection((direction, x, y, z) -> {
         BlockPos lv = new BlockPos(startX + x, startY + y, startZ + z);
         BlockPos lv2 = lv.offset(direction);
         BlockState lv3 = world.getBlockState(lv);
         BlockState lv4 = world.getBlockState(lv2);
         BlockState lv5 = lv3.getStateForNeighborUpdate(direction, lv4, world, lv, lv2);
         if (lv3 != lv5) {
            world.setBlockState(lv, lv5, flags & ~Block.NOTIFY_NEIGHBORS);
         }

         BlockState lv6 = lv4.getStateForNeighborUpdate(direction.getOpposite(), lv5, world, lv2, lv);
         if (lv4 != lv6) {
            world.setBlockState(lv2, lv6, flags & ~Block.NOTIFY_NEIGHBORS);
         }

      });
   }

   public static List process(ServerWorldAccess world, BlockPos pos, BlockPos pivot, StructurePlacementData placementData, List infos) {
      List list2 = new ArrayList();
      List list3 = new ArrayList();
      Iterator var7 = infos.iterator();

      while(var7.hasNext()) {
         StructureBlockInfo lv = (StructureBlockInfo)var7.next();
         BlockPos lv2 = transform(placementData, lv.pos).add(pos);
         StructureBlockInfo lv3 = new StructureBlockInfo(lv2, lv.state, lv.nbt != null ? lv.nbt.copy() : null);

         for(Iterator iterator = placementData.getProcessors().iterator(); lv3 != null && iterator.hasNext(); lv3 = ((StructureProcessor)iterator.next()).process(world, pos, pivot, lv, lv3, placementData)) {
         }

         if (lv3 != null) {
            ((List)list3).add(lv3);
            list2.add(lv);
         }
      }

      StructureProcessor lv4;
      for(var7 = placementData.getProcessors().iterator(); var7.hasNext(); list3 = lv4.reprocess(world, pos, pivot, list2, (List)list3, placementData)) {
         lv4 = (StructureProcessor)var7.next();
      }

      return (List)list3;
   }

   private void spawnEntities(ServerWorldAccess world, BlockPos pos, BlockMirror mirror, BlockRotation rotation, BlockPos pivot, @Nullable BlockBox area, boolean initializeMobs) {
      Iterator var8 = this.entities.iterator();

      while(true) {
         StructureEntityInfo lv;
         BlockPos lv2;
         do {
            if (!var8.hasNext()) {
               return;
            }

            lv = (StructureEntityInfo)var8.next();
            lv2 = transformAround(lv.blockPos, mirror, rotation, pivot).add(pos);
         } while(area != null && !area.contains(lv2));

         NbtCompound lv3 = lv.nbt.copy();
         Vec3d lv4 = transformAround(lv.pos, mirror, rotation, pivot);
         Vec3d lv5 = lv4.add((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
         NbtList lv6 = new NbtList();
         lv6.add(NbtDouble.of(lv5.x));
         lv6.add(NbtDouble.of(lv5.y));
         lv6.add(NbtDouble.of(lv5.z));
         lv3.put("Pos", lv6);
         lv3.remove("UUID");
         getEntity(world, lv3).ifPresent((entity) -> {
            float f = entity.applyRotation(rotation);
            f += entity.applyMirror(mirror) - entity.getYaw();
            entity.refreshPositionAndAngles(lv5.x, lv5.y, lv5.z, f, entity.getPitch());
            if (initializeMobs && entity instanceof MobEntity) {
               ((MobEntity)entity).initialize(world, world.getLocalDifficulty(BlockPos.ofFloored(lv5)), SpawnReason.STRUCTURE, (EntityData)null, lv3);
            }

            world.spawnEntityAndPassengers(entity);
         });
      }
   }

   private static Optional getEntity(ServerWorldAccess world, NbtCompound nbt) {
      try {
         return EntityType.getEntityFromNbt(nbt, world.toServerWorld());
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public Vec3i getRotatedSize(BlockRotation rotation) {
      switch (rotation) {
         case COUNTERCLOCKWISE_90:
         case CLOCKWISE_90:
            return new Vec3i(this.size.getZ(), this.size.getY(), this.size.getX());
         default:
            return this.size;
      }
   }

   public static BlockPos transformAround(BlockPos pos, BlockMirror mirror, BlockRotation rotation, BlockPos pivot) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      boolean bl = true;
      switch (mirror) {
         case LEFT_RIGHT:
            k = -k;
            break;
         case FRONT_BACK:
            i = -i;
            break;
         default:
            bl = false;
      }

      int l = pivot.getX();
      int m = pivot.getZ();
      switch (rotation) {
         case COUNTERCLOCKWISE_90:
            return new BlockPos(l - m + k, j, l + m - i);
         case CLOCKWISE_90:
            return new BlockPos(l + m - k, j, m - l + i);
         case CLOCKWISE_180:
            return new BlockPos(l + l - i, j, m + m - k);
         default:
            return bl ? new BlockPos(i, j, k) : pos;
      }
   }

   public static Vec3d transformAround(Vec3d point, BlockMirror mirror, BlockRotation rotation, BlockPos pivot) {
      double d = point.x;
      double e = point.y;
      double f = point.z;
      boolean bl = true;
      switch (mirror) {
         case LEFT_RIGHT:
            f = 1.0 - f;
            break;
         case FRONT_BACK:
            d = 1.0 - d;
            break;
         default:
            bl = false;
      }

      int i = pivot.getX();
      int j = pivot.getZ();
      switch (rotation) {
         case COUNTERCLOCKWISE_90:
            return new Vec3d((double)(i - j) + f, e, (double)(i + j + 1) - d);
         case CLOCKWISE_90:
            return new Vec3d((double)(i + j + 1) - f, e, (double)(j - i) + d);
         case CLOCKWISE_180:
            return new Vec3d((double)(i + i + 1) - d, e, (double)(j + j + 1) - f);
         default:
            return bl ? new Vec3d(d, e, f) : point;
      }
   }

   public BlockPos offsetByTransformedSize(BlockPos pos, BlockMirror mirror, BlockRotation rotation) {
      return applyTransformedOffset(pos, mirror, rotation, this.getSize().getX(), this.getSize().getZ());
   }

   public static BlockPos applyTransformedOffset(BlockPos pos, BlockMirror mirror, BlockRotation rotation, int offsetX, int offsetZ) {
      --offsetX;
      --offsetZ;
      int k = mirror == BlockMirror.FRONT_BACK ? offsetX : 0;
      int l = mirror == BlockMirror.LEFT_RIGHT ? offsetZ : 0;
      BlockPos lv = pos;
      switch (rotation) {
         case COUNTERCLOCKWISE_90:
            lv = pos.add(l, 0, offsetX - k);
            break;
         case CLOCKWISE_90:
            lv = pos.add(offsetZ - l, 0, k);
            break;
         case CLOCKWISE_180:
            lv = pos.add(offsetX - k, 0, offsetZ - l);
            break;
         case NONE:
            lv = pos.add(k, 0, l);
      }

      return lv;
   }

   public BlockBox calculateBoundingBox(StructurePlacementData placementData, BlockPos pos) {
      return this.calculateBoundingBox(pos, placementData.getRotation(), placementData.getPosition(), placementData.getMirror());
   }

   public BlockBox calculateBoundingBox(BlockPos pos, BlockRotation rotation, BlockPos pivot, BlockMirror mirror) {
      return createBox(pos, rotation, pivot, mirror, this.size);
   }

   @VisibleForTesting
   protected static BlockBox createBox(BlockPos pos, BlockRotation rotation, BlockPos pivot, BlockMirror mirror, Vec3i dimensions) {
      Vec3i lv = dimensions.add(-1, -1, -1);
      BlockPos lv2 = transformAround(BlockPos.ORIGIN, mirror, rotation, pivot);
      BlockPos lv3 = transformAround(BlockPos.ORIGIN.add(lv), mirror, rotation, pivot);
      return BlockBox.create(lv2, lv3).move(pos);
   }

   public NbtCompound writeNbt(NbtCompound nbt) {
      if (this.blockInfoLists.isEmpty()) {
         nbt.put("blocks", new NbtList());
         nbt.put("palette", new NbtList());
      } else {
         List list = Lists.newArrayList();
         Palette lv = new Palette();
         list.add(lv);

         for(int i = 1; i < this.blockInfoLists.size(); ++i) {
            list.add(new Palette());
         }

         NbtList lv2 = new NbtList();
         List list2 = ((PalettedBlockInfoList)this.blockInfoLists.get(0)).getAll();

         for(int j = 0; j < list2.size(); ++j) {
            StructureBlockInfo lv3 = (StructureBlockInfo)list2.get(j);
            NbtCompound lv4 = new NbtCompound();
            lv4.put("pos", this.createNbtIntList(lv3.pos.getX(), lv3.pos.getY(), lv3.pos.getZ()));
            int k = lv.getId(lv3.state);
            lv4.putInt("state", k);
            if (lv3.nbt != null) {
               lv4.put("nbt", lv3.nbt);
            }

            lv2.add(lv4);

            for(int l = 1; l < this.blockInfoLists.size(); ++l) {
               Palette lv5 = (Palette)list.get(l);
               lv5.set(((StructureBlockInfo)((PalettedBlockInfoList)this.blockInfoLists.get(l)).getAll().get(j)).state, k);
            }
         }

         nbt.put("blocks", lv2);
         NbtList lv6;
         Iterator var18;
         if (list.size() == 1) {
            lv6 = new NbtList();
            var18 = lv.iterator();

            while(var18.hasNext()) {
               BlockState lv7 = (BlockState)var18.next();
               lv6.add(NbtHelper.fromBlockState(lv7));
            }

            nbt.put("palette", lv6);
         } else {
            lv6 = new NbtList();
            var18 = list.iterator();

            while(var18.hasNext()) {
               Palette lv8 = (Palette)var18.next();
               NbtList lv9 = new NbtList();
               Iterator var22 = lv8.iterator();

               while(var22.hasNext()) {
                  BlockState lv10 = (BlockState)var22.next();
                  lv9.add(NbtHelper.fromBlockState(lv10));
               }

               lv6.add(lv9);
            }

            nbt.put("palettes", lv6);
         }
      }

      NbtList lv11 = new NbtList();

      NbtCompound lv13;
      for(Iterator var13 = this.entities.iterator(); var13.hasNext(); lv11.add(lv13)) {
         StructureEntityInfo lv12 = (StructureEntityInfo)var13.next();
         lv13 = new NbtCompound();
         lv13.put("pos", this.createNbtDoubleList(lv12.pos.x, lv12.pos.y, lv12.pos.z));
         lv13.put("blockPos", this.createNbtIntList(lv12.blockPos.getX(), lv12.blockPos.getY(), lv12.blockPos.getZ()));
         if (lv12.nbt != null) {
            lv13.put("nbt", lv12.nbt);
         }
      }

      nbt.put("entities", lv11);
      nbt.put("size", this.createNbtIntList(this.size.getX(), this.size.getY(), this.size.getZ()));
      return NbtHelper.putDataVersion(nbt);
   }

   public void readNbt(RegistryEntryLookup blockLookup, NbtCompound nbt) {
      this.blockInfoLists.clear();
      this.entities.clear();
      NbtList lv = nbt.getList("size", NbtElement.INT_TYPE);
      this.size = new Vec3i(lv.getInt(0), lv.getInt(1), lv.getInt(2));
      NbtList lv2 = nbt.getList("blocks", NbtElement.COMPOUND_TYPE);
      NbtList lv3;
      int i;
      if (nbt.contains("palettes", NbtElement.LIST_TYPE)) {
         lv3 = nbt.getList("palettes", NbtElement.LIST_TYPE);

         for(i = 0; i < lv3.size(); ++i) {
            this.loadPalettedBlockInfo(blockLookup, lv3.getList(i), lv2);
         }
      } else {
         this.loadPalettedBlockInfo(blockLookup, nbt.getList("palette", NbtElement.COMPOUND_TYPE), lv2);
      }

      lv3 = nbt.getList("entities", NbtElement.COMPOUND_TYPE);

      for(i = 0; i < lv3.size(); ++i) {
         NbtCompound lv4 = lv3.getCompound(i);
         NbtList lv5 = lv4.getList("pos", NbtElement.DOUBLE_TYPE);
         Vec3d lv6 = new Vec3d(lv5.getDouble(0), lv5.getDouble(1), lv5.getDouble(2));
         NbtList lv7 = lv4.getList("blockPos", NbtElement.INT_TYPE);
         BlockPos lv8 = new BlockPos(lv7.getInt(0), lv7.getInt(1), lv7.getInt(2));
         if (lv4.contains("nbt")) {
            NbtCompound lv9 = lv4.getCompound("nbt");
            this.entities.add(new StructureEntityInfo(lv6, lv8, lv9));
         }
      }

   }

   private void loadPalettedBlockInfo(RegistryEntryLookup blockLookup, NbtList palette, NbtList blocks) {
      Palette lv = new Palette();

      for(int i = 0; i < palette.size(); ++i) {
         lv.set(NbtHelper.toBlockState(blockLookup, palette.getCompound(i)), i);
      }

      List list = Lists.newArrayList();
      List list2 = Lists.newArrayList();
      List list3 = Lists.newArrayList();

      for(int j = 0; j < blocks.size(); ++j) {
         NbtCompound lv2 = blocks.getCompound(j);
         NbtList lv3 = lv2.getList("pos", NbtElement.INT_TYPE);
         BlockPos lv4 = new BlockPos(lv3.getInt(0), lv3.getInt(1), lv3.getInt(2));
         BlockState lv5 = lv.getState(lv2.getInt("state"));
         NbtCompound lv6;
         if (lv2.contains("nbt")) {
            lv6 = lv2.getCompound("nbt");
         } else {
            lv6 = null;
         }

         StructureBlockInfo lv7 = new StructureBlockInfo(lv4, lv5, lv6);
         categorize(lv7, list, list2, list3);
      }

      List list4 = combineSorted(list, list2, list3);
      this.blockInfoLists.add(new PalettedBlockInfoList(list4));
   }

   private NbtList createNbtIntList(int... ints) {
      NbtList lv = new NbtList();
      int[] var3 = ints;
      int var4 = ints.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         int i = var3[var5];
         lv.add(NbtInt.of(i));
      }

      return lv;
   }

   private NbtList createNbtDoubleList(double... doubles) {
      NbtList lv = new NbtList();
      double[] var3 = doubles;
      int var4 = doubles.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         double d = var3[var5];
         lv.add(NbtDouble.of(d));
      }

      return lv;
   }

   public static record StructureBlockInfo(BlockPos pos, BlockState state, @Nullable NbtCompound nbt) {
      final BlockPos pos;
      final BlockState state;
      @Nullable
      final NbtCompound nbt;

      public StructureBlockInfo(BlockPos pos, BlockState state, @Nullable NbtCompound nbt) {
         this.pos = pos;
         this.state = state;
         this.nbt = nbt;
      }

      public String toString() {
         return String.format(Locale.ROOT, "<StructureBlockInfo | %s | %s | %s>", this.pos, this.state, this.nbt);
      }

      public BlockPos pos() {
         return this.pos;
      }

      public BlockState state() {
         return this.state;
      }

      @Nullable
      public NbtCompound nbt() {
         return this.nbt;
      }
   }

   public static final class PalettedBlockInfoList {
      private final List infos;
      private final Map blockToInfos = Maps.newHashMap();

      PalettedBlockInfoList(List infos) {
         this.infos = infos;
      }

      public List getAll() {
         return this.infos;
      }

      public List getAllOf(Block block) {
         return (List)this.blockToInfos.computeIfAbsent(block, (block2) -> {
            return (List)this.infos.stream().filter((info) -> {
               return info.state.isOf(block2);
            }).collect(Collectors.toList());
         });
      }
   }

   public static class StructureEntityInfo {
      public final Vec3d pos;
      public final BlockPos blockPos;
      public final NbtCompound nbt;

      public StructureEntityInfo(Vec3d pos, BlockPos blockPos, NbtCompound nbt) {
         this.pos = pos;
         this.blockPos = blockPos;
         this.nbt = nbt;
      }
   }

   static class Palette implements Iterable {
      public static final BlockState AIR;
      private final IdList ids = new IdList(16);
      private int currentIndex;

      public int getId(BlockState state) {
         int i = this.ids.getRawId(state);
         if (i == -1) {
            i = this.currentIndex++;
            this.ids.set(state, i);
         }

         return i;
      }

      @Nullable
      public BlockState getState(int id) {
         BlockState lv = (BlockState)this.ids.get(id);
         return lv == null ? AIR : lv;
      }

      public Iterator iterator() {
         return this.ids.iterator();
      }

      public void set(BlockState state, int id) {
         this.ids.set(state, id);
      }

      static {
         AIR = Blocks.AIR.getDefaultState();
      }
   }
}
