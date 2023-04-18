package net.minecraft.test;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.Bootstrap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.data.DataWriter;
import net.minecraft.data.dev.NbtProvider;
import net.minecraft.data.validate.StructureValidatorProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class StructureTestUtil {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String TEST_STRUCTURES_DIRECTORY_NAME = "gameteststructures";
   public static String testStructuresDirectoryName = "gameteststructures";
   private static final int field_33174 = 4;

   public static BlockRotation getRotation(int steps) {
      switch (steps) {
         case 0:
            return BlockRotation.NONE;
         case 1:
            return BlockRotation.CLOCKWISE_90;
         case 2:
            return BlockRotation.CLOCKWISE_180;
         case 3:
            return BlockRotation.COUNTERCLOCKWISE_90;
         default:
            throw new IllegalArgumentException("rotationSteps must be a value from 0-3. Got value " + steps);
      }
   }

   public static int getRotationSteps(BlockRotation rotation) {
      switch (rotation) {
         case NONE:
            return 0;
         case CLOCKWISE_90:
            return 1;
         case CLOCKWISE_180:
            return 2;
         case COUNTERCLOCKWISE_90:
            return 3;
         default:
            throw new IllegalArgumentException("Unknown rotation value, don't know how many steps it represents: " + rotation);
      }
   }

   public static void main(String[] args) throws IOException {
      Bootstrap.initialize();
      Files.walk(Paths.get(testStructuresDirectoryName)).filter((path) -> {
         return path.toString().endsWith(".snbt");
      }).forEach((path) -> {
         try {
            String string = Files.readString(path);
            NbtCompound lv = NbtHelper.fromNbtProviderString(string);
            NbtCompound lv2 = StructureValidatorProvider.update(path.toString(), lv);
            NbtProvider.writeTo(DataWriter.UNCACHED, path, NbtHelper.toNbtProviderString(lv2));
         } catch (IOException | CommandSyntaxException var4) {
            LOGGER.error("Something went wrong upgrading: {}", path, var4);
         }

      });
   }

   public static Box getStructureBoundingBox(StructureBlockBlockEntity structureBlockEntity) {
      BlockPos lv = structureBlockEntity.getPos();
      BlockPos lv2 = lv.add(structureBlockEntity.getSize().add(-1, -1, -1));
      BlockPos lv3 = StructureTemplate.transformAround(lv2, BlockMirror.NONE, structureBlockEntity.getRotation(), lv);
      return new Box(lv, lv3);
   }

   public static BlockBox getStructureBlockBox(StructureBlockBlockEntity structureBlockEntity) {
      BlockPos lv = structureBlockEntity.getPos();
      BlockPos lv2 = lv.add(structureBlockEntity.getSize().add(-1, -1, -1));
      BlockPos lv3 = StructureTemplate.transformAround(lv2, BlockMirror.NONE, structureBlockEntity.getRotation(), lv);
      return BlockBox.create(lv, lv3);
   }

   public static void placeStartButton(BlockPos pos, BlockPos relativePos, BlockRotation rotation, ServerWorld world) {
      BlockPos lv = StructureTemplate.transformAround(pos.add(relativePos), BlockMirror.NONE, rotation, pos);
      world.setBlockState(lv, Blocks.COMMAND_BLOCK.getDefaultState());
      CommandBlockBlockEntity lv2 = (CommandBlockBlockEntity)world.getBlockEntity(lv);
      lv2.getCommandExecutor().setCommand("test runthis");
      BlockPos lv3 = StructureTemplate.transformAround(lv.add(0, 0, -1), BlockMirror.NONE, rotation, lv);
      world.setBlockState(lv3, Blocks.STONE_BUTTON.getDefaultState().rotate(rotation));
   }

   public static void createTestArea(String testName, BlockPos pos, Vec3i relativePos, BlockRotation rotation, ServerWorld world) {
      BlockBox lv = getStructureBlockBox(pos, relativePos, rotation);
      clearArea(lv, pos.getY(), world);
      world.setBlockState(pos, Blocks.STRUCTURE_BLOCK.getDefaultState());
      StructureBlockBlockEntity lv2 = (StructureBlockBlockEntity)world.getBlockEntity(pos);
      lv2.setIgnoreEntities(false);
      lv2.setTemplateName(new Identifier(testName));
      lv2.setSize(relativePos);
      lv2.setMode(StructureBlockMode.SAVE);
      lv2.setShowBoundingBox(true);
   }

   public static StructureBlockBlockEntity createStructureTemplate(String templateName, BlockPos pos, BlockRotation rotation, int i, ServerWorld world, boolean bl) {
      Vec3i lv = createStructureTemplate(templateName, world).getSize();
      BlockBox lv2 = getStructureBlockBox(pos, lv, rotation);
      BlockPos lv3;
      if (rotation == BlockRotation.NONE) {
         lv3 = pos;
      } else if (rotation == BlockRotation.CLOCKWISE_90) {
         lv3 = pos.add(lv.getZ() - 1, 0, 0);
      } else if (rotation == BlockRotation.CLOCKWISE_180) {
         lv3 = pos.add(lv.getX() - 1, 0, lv.getZ() - 1);
      } else {
         if (rotation != BlockRotation.COUNTERCLOCKWISE_90) {
            throw new IllegalArgumentException("Invalid rotation: " + rotation);
         }

         lv3 = pos.add(0, 0, lv.getX() - 1);
      }

      forceLoadNearbyChunks(pos, world);
      clearArea(lv2, pos.getY(), world);
      StructureBlockBlockEntity lv4 = placeStructureTemplate(templateName, lv3, rotation, world, bl);
      world.getBlockTickScheduler().clearNextTicks(lv2);
      world.clearUpdatesInArea(lv2);
      return lv4;
   }

   private static void forceLoadNearbyChunks(BlockPos pos, ServerWorld world) {
      ChunkPos lv = new ChunkPos(pos);

      for(int i = -1; i < 4; ++i) {
         for(int j = -1; j < 4; ++j) {
            int k = lv.x + i;
            int l = lv.z + j;
            world.setChunkForced(k, l, true);
         }
      }

   }

   public static void clearArea(BlockBox area, int altitude, ServerWorld world) {
      BlockBox lv = new BlockBox(area.getMinX() - 2, area.getMinY() - 3, area.getMinZ() - 3, area.getMaxX() + 3, area.getMaxY() + 20, area.getMaxZ() + 3);
      BlockPos.stream(lv).forEach((pos) -> {
         resetBlock(altitude, pos, world);
      });
      world.getBlockTickScheduler().clearNextTicks(lv);
      world.clearUpdatesInArea(lv);
      Box lv2 = new Box((double)lv.getMinX(), (double)lv.getMinY(), (double)lv.getMinZ(), (double)lv.getMaxX(), (double)lv.getMaxY(), (double)lv.getMaxZ());
      List list = world.getEntitiesByClass(Entity.class, lv2, (entity) -> {
         return !(entity instanceof PlayerEntity);
      });
      list.forEach(Entity::discard);
   }

   public static BlockBox getStructureBlockBox(BlockPos pos, Vec3i relativePos, BlockRotation rotation) {
      BlockPos lv = pos.add(relativePos).add(-1, -1, -1);
      BlockPos lv2 = StructureTemplate.transformAround(lv, BlockMirror.NONE, rotation, pos);
      BlockBox lv3 = BlockBox.create(pos, lv2);
      int i = Math.min(lv3.getMinX(), lv3.getMaxX());
      int j = Math.min(lv3.getMinZ(), lv3.getMaxZ());
      return lv3.move(pos.getX() - i, 0, pos.getZ() - j);
   }

   public static Optional findContainingStructureBlock(BlockPos pos, int radius, ServerWorld world) {
      return findStructureBlocks(pos, radius, world).stream().filter((structureBlockPos) -> {
         return isInStructureBounds(structureBlockPos, pos, world);
      }).findFirst();
   }

   @Nullable
   public static BlockPos findNearestStructureBlock(BlockPos pos, int radius, ServerWorld world) {
      Comparator comparator = Comparator.comparingInt((posx) -> {
         return posx.getManhattanDistance(pos);
      });
      Collection collection = findStructureBlocks(pos, radius, world);
      Optional optional = collection.stream().min(comparator);
      return (BlockPos)optional.orElse((Object)null);
   }

   public static Collection findStructureBlocks(BlockPos pos, int radius, ServerWorld world) {
      Collection collection = Lists.newArrayList();
      Box lv = new Box(pos);
      lv = lv.expand((double)radius);

      for(int j = (int)lv.minX; j <= (int)lv.maxX; ++j) {
         for(int k = (int)lv.minY; k <= (int)lv.maxY; ++k) {
            for(int l = (int)lv.minZ; l <= (int)lv.maxZ; ++l) {
               BlockPos lv2 = new BlockPos(j, k, l);
               BlockState lv3 = world.getBlockState(lv2);
               if (lv3.isOf(Blocks.STRUCTURE_BLOCK)) {
                  collection.add(lv2);
               }
            }
         }
      }

      return collection;
   }

   private static StructureTemplate createStructureTemplate(String templateId, ServerWorld world) {
      StructureTemplateManager lv = world.getStructureTemplateManager();
      Optional optional = lv.getTemplate(new Identifier(templateId));
      if (optional.isPresent()) {
         return (StructureTemplate)optional.get();
      } else {
         String string2 = templateId + ".snbt";
         Path path = Paths.get(testStructuresDirectoryName, string2);
         NbtCompound lv2 = loadSnbt(path);
         if (lv2 == null) {
            throw new RuntimeException("Could not find structure file " + path + ", and the structure is not available in the world structures either.");
         } else {
            return lv.createTemplate(lv2);
         }
      }
   }

   private static StructureBlockBlockEntity placeStructureTemplate(String name, BlockPos pos, BlockRotation rotation, ServerWorld world, boolean bl) {
      world.setBlockState(pos, Blocks.STRUCTURE_BLOCK.getDefaultState());
      StructureBlockBlockEntity lv = (StructureBlockBlockEntity)world.getBlockEntity(pos);
      lv.setMode(StructureBlockMode.LOAD);
      lv.setRotation(rotation);
      lv.setIgnoreEntities(false);
      lv.setTemplateName(new Identifier(name));
      lv.loadStructure(world, bl);
      if (lv.getSize() != Vec3i.ZERO) {
         return lv;
      } else {
         StructureTemplate lv2 = createStructureTemplate(name, world);
         lv.place(world, bl, lv2);
         if (lv.getSize() == Vec3i.ZERO) {
            throw new RuntimeException("Failed to load structure " + name);
         } else {
            return lv;
         }
      }
   }

   @Nullable
   private static NbtCompound loadSnbt(Path path) {
      try {
         BufferedReader bufferedReader = Files.newBufferedReader(path);
         String string = IOUtils.toString(bufferedReader);
         return NbtHelper.fromNbtProviderString(string);
      } catch (IOException var3) {
         return null;
      } catch (CommandSyntaxException var4) {
         throw new RuntimeException("Error while trying to load structure " + path, var4);
      }
   }

   private static void resetBlock(int altitude, BlockPos pos, ServerWorld world) {
      BlockState lv = null;
      DynamicRegistryManager lv2 = world.getRegistryManager();
      FlatChunkGeneratorConfig lv3 = FlatChunkGeneratorConfig.getDefaultConfig(lv2.getWrapperOrThrow(RegistryKeys.BIOME), lv2.getWrapperOrThrow(RegistryKeys.STRUCTURE_SET), lv2.getWrapperOrThrow(RegistryKeys.PLACED_FEATURE));
      List list = lv3.getLayerBlocks();
      int j = pos.getY() - world.getBottomY();
      if (pos.getY() < altitude && j > 0 && j <= list.size()) {
         lv = (BlockState)list.get(j - 1);
      }

      if (lv == null) {
         lv = Blocks.AIR.getDefaultState();
      }

      BlockStateArgument lv4 = new BlockStateArgument(lv, Collections.emptySet(), (NbtCompound)null);
      lv4.setBlockState(world, pos, Block.NOTIFY_LISTENERS);
      world.updateNeighbors(pos, lv.getBlock());
   }

   private static boolean isInStructureBounds(BlockPos structureBlockPos, BlockPos pos, ServerWorld world) {
      StructureBlockBlockEntity lv = (StructureBlockBlockEntity)world.getBlockEntity(structureBlockPos);
      Box lv2 = getStructureBoundingBox(lv).expand(1.0);
      return lv2.contains(Vec3d.ofCenter(pos));
   }
}
