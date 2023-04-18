package net.minecraft.structure.pool;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.block.JigsawBlock;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class StructurePoolBasedGenerator {
   static final Logger LOGGER = LogUtils.getLogger();

   public static Optional generate(Structure.Context context, RegistryEntry structurePool, Optional id, int size, BlockPos pos, boolean useExpansionHack, Optional projectStartToHeightmap, int maxDistanceFromCenter) {
      DynamicRegistryManager lv = context.dynamicRegistryManager();
      ChunkGenerator lv2 = context.chunkGenerator();
      StructureTemplateManager lv3 = context.structureTemplateManager();
      HeightLimitView lv4 = context.world();
      ChunkRandom lv5 = context.random();
      Registry lv6 = lv.get(RegistryKeys.TEMPLATE_POOL);
      BlockRotation lv7 = BlockRotation.random(lv5);
      StructurePool lv8 = (StructurePool)structurePool.value();
      StructurePoolElement lv9 = lv8.getRandomElement(lv5);
      if (lv9 == EmptyPoolElement.INSTANCE) {
         return Optional.empty();
      } else {
         BlockPos lv11;
         if (id.isPresent()) {
            Identifier lv10 = (Identifier)id.get();
            Optional optional3 = findStartingJigsawPos(lv9, lv10, pos, lv7, lv3, lv5);
            if (optional3.isEmpty()) {
               LOGGER.error("No starting jigsaw {} found in start pool {}", lv10, structurePool.getKey().map((key) -> {
                  return key.getValue().toString();
               }).orElse("<unregistered>"));
               return Optional.empty();
            }

            lv11 = (BlockPos)optional3.get();
         } else {
            lv11 = pos;
         }

         Vec3i lv12 = lv11.subtract(pos);
         BlockPos lv13 = pos.subtract(lv12);
         PoolStructurePiece lv14 = new PoolStructurePiece(lv3, lv9, lv13, lv9.getGroundLevelDelta(), lv7, lv9.getBoundingBox(lv3, lv13, lv7));
         BlockBox lv15 = lv14.getBoundingBox();
         int k = (lv15.getMaxX() + lv15.getMinX()) / 2;
         int l = (lv15.getMaxZ() + lv15.getMinZ()) / 2;
         int m;
         if (projectStartToHeightmap.isPresent()) {
            m = pos.getY() + lv2.getHeightOnGround(k, l, (Heightmap.Type)projectStartToHeightmap.get(), lv4, context.noiseConfig());
         } else {
            m = lv13.getY();
         }

         int n = lv15.getMinY() + lv14.getGroundLevelDelta();
         lv14.translate(0, m - n, 0);
         int o = m + lv12.getY();
         return Optional.of(new Structure.StructurePosition(new BlockPos(k, o, l), (collector) -> {
            List list = Lists.newArrayList();
            list.add(lv14);
            if (size > 0) {
               Box lv = new Box((double)(k - maxDistanceFromCenter), (double)(o - maxDistanceFromCenter), (double)(l - maxDistanceFromCenter), (double)(k + maxDistanceFromCenter + 1), (double)(o + maxDistanceFromCenter + 1), (double)(l + maxDistanceFromCenter + 1));
               VoxelShape lv2x = VoxelShapes.combineAndSimplify(VoxelShapes.cuboid(lv), VoxelShapes.cuboid(Box.from(lv15)), BooleanBiFunction.ONLY_FIRST);
               generate(context.noiseConfig(), size, useExpansionHack, lv2, lv3, lv4, lv5, lv6, lv14, list, lv2x);
               Objects.requireNonNull(collector);
               list.forEach(collector::addPiece);
            }
         }));
      }
   }

   private static Optional findStartingJigsawPos(StructurePoolElement pool, Identifier id, BlockPos pos, BlockRotation rotation, StructureTemplateManager structureManager, ChunkRandom random) {
      List list = pool.getStructureBlockInfos(structureManager, pos, rotation, random);
      Optional optional = Optional.empty();
      Iterator var8 = list.iterator();

      while(var8.hasNext()) {
         StructureTemplate.StructureBlockInfo lv = (StructureTemplate.StructureBlockInfo)var8.next();
         Identifier lv2 = Identifier.tryParse(lv.nbt().getString("name"));
         if (id.equals(lv2)) {
            optional = Optional.of(lv.pos());
            break;
         }
      }

      return optional;
   }

   private static void generate(NoiseConfig noiseConfig, int maxSize, boolean modifyBoundingBox, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, HeightLimitView heightLimitView, Random random, Registry structurePoolRegistry, PoolStructurePiece firstPiece, List pieces, VoxelShape pieceShape) {
      StructurePoolGenerator lv = new StructurePoolGenerator(structurePoolRegistry, maxSize, chunkGenerator, structureTemplateManager, pieces, random);
      lv.structurePieces.addLast(new ShapedPoolStructurePiece(firstPiece, new MutableObject(pieceShape), 0));

      while(!lv.structurePieces.isEmpty()) {
         ShapedPoolStructurePiece lv2 = (ShapedPoolStructurePiece)lv.structurePieces.removeFirst();
         lv.generatePiece(lv2.piece, lv2.pieceShape, lv2.currentSize, modifyBoundingBox, heightLimitView, noiseConfig);
      }

   }

   public static boolean generate(ServerWorld world, RegistryEntry structurePool, Identifier id, int size, BlockPos pos, boolean keepJigsaws) {
      ChunkGenerator lv = world.getChunkManager().getChunkGenerator();
      StructureTemplateManager lv2 = world.getStructureTemplateManager();
      StructureAccessor lv3 = world.getStructureAccessor();
      Random lv4 = world.getRandom();
      Structure.Context lv5 = new Structure.Context(world.getRegistryManager(), lv, lv.getBiomeSource(), world.getChunkManager().getNoiseConfig(), lv2, world.getSeed(), new ChunkPos(pos), world, (biome) -> {
         return true;
      });
      Optional optional = generate(lv5, structurePool, Optional.of(id), size, pos, false, Optional.empty(), 128);
      if (optional.isPresent()) {
         StructurePiecesCollector lv6 = ((Structure.StructurePosition)optional.get()).generate();
         Iterator var13 = lv6.toList().pieces().iterator();

         while(var13.hasNext()) {
            StructurePiece lv7 = (StructurePiece)var13.next();
            if (lv7 instanceof PoolStructurePiece) {
               PoolStructurePiece lv8 = (PoolStructurePiece)lv7;
               lv8.generate(world, lv3, lv, lv4, BlockBox.infinite(), pos, keepJigsaws);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   static final class StructurePoolGenerator {
      private final Registry registry;
      private final int maxSize;
      private final ChunkGenerator chunkGenerator;
      private final StructureTemplateManager structureTemplateManager;
      private final List children;
      private final Random random;
      final Deque structurePieces = Queues.newArrayDeque();

      StructurePoolGenerator(Registry registry, int maxSize, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, List children, Random random) {
         this.registry = registry;
         this.maxSize = maxSize;
         this.chunkGenerator = chunkGenerator;
         this.structureTemplateManager = structureTemplateManager;
         this.children = children;
         this.random = random;
      }

      void generatePiece(PoolStructurePiece piece, MutableObject pieceShape, int minY, boolean modifyBoundingBox, HeightLimitView world, NoiseConfig noiseConfig) {
         StructurePoolElement lv = piece.getPoolElement();
         BlockPos lv2 = piece.getPos();
         BlockRotation lv3 = piece.getRotation();
         StructurePool.Projection lv4 = lv.getProjection();
         boolean bl2 = lv4 == StructurePool.Projection.RIGID;
         MutableObject mutableObject2 = new MutableObject();
         BlockBox lv5 = piece.getBoundingBox();
         int j = lv5.getMinY();
         Iterator var15 = lv.getStructureBlockInfos(this.structureTemplateManager, lv2, lv3, this.random).iterator();

         while(true) {
            label129:
            while(var15.hasNext()) {
               StructureTemplate.StructureBlockInfo lv6 = (StructureTemplate.StructureBlockInfo)var15.next();
               Direction lv7 = JigsawBlock.getFacing(lv6.state());
               BlockPos lv8 = lv6.pos();
               BlockPos lv9 = lv8.offset(lv7);
               int k = lv8.getY() - j;
               int l = -1;
               RegistryKey lv10 = getPoolKey(lv6);
               Optional optional = this.registry.getEntry(lv10);
               if (optional.isEmpty()) {
                  StructurePoolBasedGenerator.LOGGER.warn("Empty or non-existent pool: {}", lv10.getValue());
               } else {
                  RegistryEntry lv11 = (RegistryEntry)optional.get();
                  if (((StructurePool)lv11.value()).getElementCount() == 0 && !lv11.matchesKey(StructurePools.EMPTY)) {
                     StructurePoolBasedGenerator.LOGGER.warn("Empty or non-existent pool: {}", lv10.getValue());
                  } else {
                     RegistryEntry lv12 = ((StructurePool)lv11.value()).getFallback();
                     if (((StructurePool)lv12.value()).getElementCount() == 0 && !lv12.matchesKey(StructurePools.EMPTY)) {
                        StructurePoolBasedGenerator.LOGGER.warn("Empty or non-existent fallback pool: {}", lv12.getKey().map((key) -> {
                           return key.getValue().toString();
                        }).orElse("<unregistered>"));
                     } else {
                        boolean bl3 = lv5.contains(lv9);
                        MutableObject mutableObject3;
                        if (bl3) {
                           mutableObject3 = mutableObject2;
                           if (mutableObject2.getValue() == null) {
                              mutableObject2.setValue(VoxelShapes.cuboid(Box.from(lv5)));
                           }
                        } else {
                           mutableObject3 = pieceShape;
                        }

                        List list = Lists.newArrayList();
                        if (minY != this.maxSize) {
                           list.addAll(((StructurePool)lv11.value()).getElementIndicesInRandomOrder(this.random));
                        }

                        list.addAll(((StructurePool)lv12.value()).getElementIndicesInRandomOrder(this.random));
                        Iterator var29 = list.iterator();

                        while(var29.hasNext()) {
                           StructurePoolElement lv13 = (StructurePoolElement)var29.next();
                           if (lv13 == EmptyPoolElement.INSTANCE) {
                              break;
                           }

                           Iterator var31 = BlockRotation.randomRotationOrder(this.random).iterator();

                           label125:
                           while(var31.hasNext()) {
                              BlockRotation lv14 = (BlockRotation)var31.next();
                              List list2 = lv13.getStructureBlockInfos(this.structureTemplateManager, BlockPos.ORIGIN, lv14, this.random);
                              BlockBox lv15 = lv13.getBoundingBox(this.structureTemplateManager, BlockPos.ORIGIN, lv14);
                              int m;
                              if (modifyBoundingBox && lv15.getBlockCountY() <= 16) {
                                 m = list2.stream().mapToInt((blockInfo) -> {
                                    if (!lv15.contains(blockInfo.pos().offset(JigsawBlock.getFacing(blockInfo.state())))) {
                                       return 0;
                                    } else {
                                       RegistryKey lv = getPoolKey(blockInfo);
                                       Optional optional = this.registry.getEntry(lv);
                                       Optional optional2 = optional.map((entry) -> {
                                          return ((StructurePool)entry.value()).getFallback();
                                       });
                                       int i = (Integer)optional.map((entry) -> {
                                          return ((StructurePool)entry.value()).getHighestY(this.structureTemplateManager);
                                       }).orElse(0);
                                       int j = (Integer)optional2.map((entry) -> {
                                          return ((StructurePool)entry.value()).getHighestY(this.structureTemplateManager);
                                       }).orElse(0);
                                       return Math.max(i, j);
                                    }
                                 }).max().orElse(0);
                              } else {
                                 m = 0;
                              }

                              Iterator var36 = list2.iterator();

                              StructurePool.Projection lv20;
                              boolean bl4;
                              int o;
                              int p;
                              int q;
                              BlockBox lv21;
                              BlockPos lv22;
                              int s;
                              do {
                                 StructureTemplate.StructureBlockInfo lv16;
                                 do {
                                    if (!var36.hasNext()) {
                                       continue label125;
                                    }

                                    lv16 = (StructureTemplate.StructureBlockInfo)var36.next();
                                 } while(!JigsawBlock.attachmentMatches(lv6, lv16));

                                 BlockPos lv17 = lv16.pos();
                                 BlockPos lv18 = lv9.subtract(lv17);
                                 BlockBox lv19 = lv13.getBoundingBox(this.structureTemplateManager, lv18, lv14);
                                 int n = lv19.getMinY();
                                 lv20 = lv13.getProjection();
                                 bl4 = lv20 == StructurePool.Projection.RIGID;
                                 o = lv17.getY();
                                 p = k - o + JigsawBlock.getFacing(lv6.state()).getOffsetY();
                                 if (bl2 && bl4) {
                                    q = j + p;
                                 } else {
                                    if (l == -1) {
                                       l = this.chunkGenerator.getHeightOnGround(lv8.getX(), lv8.getZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);
                                    }

                                    q = l - o;
                                 }

                                 int r = q - n;
                                 lv21 = lv19.offset(0, r, 0);
                                 lv22 = lv18.add(0, r, 0);
                                 if (m > 0) {
                                    s = Math.max(m + 1, lv21.getMaxY() - lv21.getMinY());
                                    lv21.encompass(new BlockPos(lv21.getMinX(), lv21.getMinY() + s, lv21.getMinZ()));
                                 }
                              } while(VoxelShapes.matchesAnywhere((VoxelShape)mutableObject3.getValue(), VoxelShapes.cuboid(Box.from(lv21).contract(0.25)), BooleanBiFunction.ONLY_SECOND));

                              mutableObject3.setValue(VoxelShapes.combine((VoxelShape)mutableObject3.getValue(), VoxelShapes.cuboid(Box.from(lv21)), BooleanBiFunction.ONLY_FIRST));
                              s = piece.getGroundLevelDelta();
                              int t;
                              if (bl4) {
                                 t = s - p;
                              } else {
                                 t = lv13.getGroundLevelDelta();
                              }

                              PoolStructurePiece lv23 = new PoolStructurePiece(this.structureTemplateManager, lv13, lv22, t, lv14, lv21);
                              int u;
                              if (bl2) {
                                 u = j + k;
                              } else if (bl4) {
                                 u = q + o;
                              } else {
                                 if (l == -1) {
                                    l = this.chunkGenerator.getHeightOnGround(lv8.getX(), lv8.getZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);
                                 }

                                 u = l + p / 2;
                              }

                              piece.addJunction(new JigsawJunction(lv9.getX(), u - k + s, lv9.getZ(), p, lv20));
                              lv23.addJunction(new JigsawJunction(lv8.getX(), u - o + t, lv8.getZ(), -p, lv4));
                              this.children.add(lv23);
                              if (minY + 1 <= this.maxSize) {
                                 this.structurePieces.addLast(new ShapedPoolStructurePiece(lv23, mutableObject3, minY + 1));
                              }
                              continue label129;
                           }
                        }
                     }
                  }
               }
            }

            return;
         }
      }

      private static RegistryKey getPoolKey(StructureTemplate.StructureBlockInfo blockInfo) {
         return RegistryKey.of(RegistryKeys.TEMPLATE_POOL, new Identifier(blockInfo.nbt().getString("pool")));
      }
   }

   private static final class ShapedPoolStructurePiece {
      final PoolStructurePiece piece;
      final MutableObject pieceShape;
      final int currentSize;

      ShapedPoolStructurePiece(PoolStructurePiece piece, MutableObject pieceShape, int currentSize) {
         this.piece = piece;
         this.pieceShape = pieceShape;
         this.currentSize = currentSize;
      }
   }
}
