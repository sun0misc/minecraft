package net.minecraft.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class EndCityGenerator {
   private static final int MAX_DEPTH = 8;
   static final Part BUILDING = new Part() {
      public void init() {
      }

      public boolean create(StructureTemplateManager manager, int depth, Piece root, BlockPos pos, List pieces, Random random) {
         if (depth > 8) {
            return false;
         } else {
            BlockRotation lv = root.getPlacementData().getRotation();
            Piece lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, root, pos, "base_floor", lv, true));
            int j = random.nextInt(3);
            if (j == 0) {
               EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-1, 4, -1), "base_roof", lv, true));
            } else if (j == 1) {
               lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-1, 0, -1), "second_floor_2", lv, false));
               lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-1, 8, -1), "second_roof", lv, false));
               EndCityGenerator.createPart(manager, EndCityGenerator.SMALL_TOWER, depth + 1, lv2, (BlockPos)null, pieces, random);
            } else if (j == 2) {
               lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-1, 0, -1), "second_floor_2", lv, false));
               lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-1, 4, -1), "third_floor_2", lv, false));
               lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-1, 8, -1), "third_roof", lv, true));
               EndCityGenerator.createPart(manager, EndCityGenerator.SMALL_TOWER, depth + 1, lv2, (BlockPos)null, pieces, random);
            }

            return true;
         }
      }
   };
   static final List SMALL_TOWER_BRIDGE_ATTACHMENTS;
   static final Part SMALL_TOWER;
   static final Part BRIDGE_PIECE;
   static final List FAT_TOWER_BRIDGE_ATTACHMENTS;
   static final Part FAT_TOWER;

   static Piece createPiece(StructureTemplateManager structureTemplateManager, Piece lastPiece, BlockPos relativePosition, String template, BlockRotation rotation, boolean ignoreAir) {
      Piece lv = new Piece(structureTemplateManager, template, lastPiece.getPos(), rotation, ignoreAir);
      BlockPos lv2 = lastPiece.getTemplate().transformBox(lastPiece.getPlacementData(), relativePosition, lv.getPlacementData(), BlockPos.ORIGIN);
      lv.translate(lv2.getX(), lv2.getY(), lv2.getZ());
      return lv;
   }

   public static void addPieces(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation, List pieces, Random random) {
      FAT_TOWER.init();
      BUILDING.init();
      BRIDGE_PIECE.init();
      SMALL_TOWER.init();
      Piece lv = addPiece(pieces, new Piece(structureTemplateManager, "base_floor", pos, rotation, true));
      lv = addPiece(pieces, createPiece(structureTemplateManager, lv, new BlockPos(-1, 0, -1), "second_floor_1", rotation, false));
      lv = addPiece(pieces, createPiece(structureTemplateManager, lv, new BlockPos(-1, 4, -1), "third_floor_1", rotation, false));
      lv = addPiece(pieces, createPiece(structureTemplateManager, lv, new BlockPos(-1, 8, -1), "third_roof", rotation, true));
      createPart(structureTemplateManager, SMALL_TOWER, 1, lv, (BlockPos)null, pieces, random);
   }

   static Piece addPiece(List pieces, Piece piece) {
      pieces.add(piece);
      return piece;
   }

   static boolean createPart(StructureTemplateManager manager, Part piece, int depth, Piece parent, BlockPos pos, List pieces, Random random) {
      if (depth > 8) {
         return false;
      } else {
         List list2 = Lists.newArrayList();
         if (piece.create(manager, depth, parent, pos, list2, random)) {
            boolean bl = false;
            int j = random.nextInt();
            Iterator var10 = list2.iterator();

            while(var10.hasNext()) {
               StructurePiece lv = (StructurePiece)var10.next();
               lv.setChainLength(j);
               StructurePiece lv2 = StructurePiece.firstIntersecting(pieces, lv.getBoundingBox());
               if (lv2 != null && lv2.getChainLength() != parent.getChainLength()) {
                  bl = true;
                  break;
               }
            }

            if (!bl) {
               pieces.addAll(list2);
               return true;
            }
         }

         return false;
      }
   }

   static {
      SMALL_TOWER_BRIDGE_ATTACHMENTS = Lists.newArrayList(new Pair[]{new Pair(BlockRotation.NONE, new BlockPos(1, -1, 0)), new Pair(BlockRotation.CLOCKWISE_90, new BlockPos(6, -1, 1)), new Pair(BlockRotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 5)), new Pair(BlockRotation.CLOCKWISE_180, new BlockPos(5, -1, 6))});
      SMALL_TOWER = new Part() {
         public void init() {
         }

         public boolean create(StructureTemplateManager manager, int depth, Piece root, BlockPos pos, List pieces, Random random) {
            BlockRotation lv = root.getPlacementData().getRotation();
            Piece lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, root, new BlockPos(3 + random.nextInt(2), -3, 3 + random.nextInt(2)), "tower_base", lv, true));
            lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(0, 7, 0), "tower_piece", lv, true));
            Piece lv3 = random.nextInt(3) == 0 ? lv2 : null;
            int j = 1 + random.nextInt(3);

            for(int k = 0; k < j; ++k) {
               lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(0, 4, 0), "tower_piece", lv, true));
               if (k < j - 1 && random.nextBoolean()) {
                  lv3 = lv2;
               }
            }

            if (lv3 != null) {
               Iterator var14 = EndCityGenerator.SMALL_TOWER_BRIDGE_ATTACHMENTS.iterator();

               while(var14.hasNext()) {
                  Pair lv4 = (Pair)var14.next();
                  if (random.nextBoolean()) {
                     Piece lv5 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv3, (BlockPos)lv4.getRight(), "bridge_end", lv.rotate((BlockRotation)lv4.getLeft()), true));
                     EndCityGenerator.createPart(manager, EndCityGenerator.BRIDGE_PIECE, depth + 1, lv5, (BlockPos)null, pieces, random);
                  }
               }

               EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-1, 4, -1), "tower_top", lv, true));
            } else {
               if (depth != 7) {
                  return EndCityGenerator.createPart(manager, EndCityGenerator.FAT_TOWER, depth + 1, lv2, (BlockPos)null, pieces, random);
               }

               EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-1, 4, -1), "tower_top", lv, true));
            }

            return true;
         }
      };
      BRIDGE_PIECE = new Part() {
         public boolean shipGenerated;

         public void init() {
            this.shipGenerated = false;
         }

         public boolean create(StructureTemplateManager manager, int depth, Piece root, BlockPos pos, List pieces, Random random) {
            BlockRotation lv = root.getPlacementData().getRotation();
            int j = random.nextInt(4) + 1;
            Piece lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, root, new BlockPos(0, 0, -4), "bridge_piece", lv, true));
            lv2.setChainLength(-1);
            int k = 0;

            for(int l = 0; l < j; ++l) {
               if (random.nextBoolean()) {
                  lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(0, k, -4), "bridge_piece", lv, true));
                  k = 0;
               } else {
                  if (random.nextBoolean()) {
                     lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(0, k, -4), "bridge_steep_stairs", lv, true));
                  } else {
                     lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(0, k, -8), "bridge_gentle_stairs", lv, true));
                  }

                  k = 4;
               }
            }

            if (!this.shipGenerated && random.nextInt(10 - depth) == 0) {
               EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-8 + random.nextInt(8), k, -70 + random.nextInt(10)), "ship", lv, true));
               this.shipGenerated = true;
            } else if (!EndCityGenerator.createPart(manager, EndCityGenerator.BUILDING, depth + 1, lv2, new BlockPos(-3, k + 1, -11), pieces, random)) {
               return false;
            }

            lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(4, k, 0), "bridge_end", lv.rotate(BlockRotation.CLOCKWISE_180), true));
            lv2.setChainLength(-1);
            return true;
         }
      };
      FAT_TOWER_BRIDGE_ATTACHMENTS = Lists.newArrayList(new Pair[]{new Pair(BlockRotation.NONE, new BlockPos(4, -1, 0)), new Pair(BlockRotation.CLOCKWISE_90, new BlockPos(12, -1, 4)), new Pair(BlockRotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 8)), new Pair(BlockRotation.CLOCKWISE_180, new BlockPos(8, -1, 12))});
      FAT_TOWER = new Part() {
         public void init() {
         }

         public boolean create(StructureTemplateManager manager, int depth, Piece root, BlockPos pos, List pieces, Random random) {
            BlockRotation lv = root.getPlacementData().getRotation();
            Piece lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, root, new BlockPos(-3, 4, -3), "fat_tower_base", lv, true));
            lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(0, 4, 0), "fat_tower_middle", lv, true));

            for(int j = 0; j < 2 && random.nextInt(3) != 0; ++j) {
               lv2 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(0, 8, 0), "fat_tower_middle", lv, true));
               Iterator var10 = EndCityGenerator.FAT_TOWER_BRIDGE_ATTACHMENTS.iterator();

               while(var10.hasNext()) {
                  Pair lv3 = (Pair)var10.next();
                  if (random.nextBoolean()) {
                     Piece lv4 = EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, (BlockPos)lv3.getRight(), "bridge_end", lv.rotate((BlockRotation)lv3.getLeft()), true));
                     EndCityGenerator.createPart(manager, EndCityGenerator.BRIDGE_PIECE, depth + 1, lv4, (BlockPos)null, pieces, random);
                  }
               }
            }

            EndCityGenerator.addPiece(pieces, EndCityGenerator.createPiece(manager, lv2, new BlockPos(-2, 8, -2), "fat_tower_top", lv, true));
            return true;
         }
      };
   }

   public static class Piece extends SimpleStructurePiece {
      public Piece(StructureTemplateManager manager, String template, BlockPos pos, BlockRotation rotation, boolean includeAir) {
         super(StructurePieceType.END_CITY, 0, manager, getId(template), template, createPlacementData(includeAir, rotation), pos);
      }

      public Piece(StructureTemplateManager manager, NbtCompound nbt) {
         super(StructurePieceType.END_CITY, nbt, manager, (id) -> {
            return createPlacementData(nbt.getBoolean("OW"), BlockRotation.valueOf(nbt.getString("Rot")));
         });
      }

      private static StructurePlacementData createPlacementData(boolean includeAir, BlockRotation rotation) {
         BlockIgnoreStructureProcessor lv = includeAir ? BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS : BlockIgnoreStructureProcessor.IGNORE_AIR_AND_STRUCTURE_BLOCKS;
         return (new StructurePlacementData()).setIgnoreEntities(true).addProcessor(lv).setRotation(rotation);
      }

      protected Identifier getId() {
         return getId(this.templateIdString);
      }

      private static Identifier getId(String template) {
         return new Identifier("end_city/" + template);
      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
         super.writeNbt(context, nbt);
         nbt.putString("Rot", this.placementData.getRotation().name());
         nbt.putBoolean("OW", this.placementData.getProcessors().get(0) == BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
      }

      protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world, Random random, BlockBox boundingBox) {
         if (metadata.startsWith("Chest")) {
            BlockPos lv = pos.down();
            if (boundingBox.contains(lv)) {
               LootableContainerBlockEntity.setLootTable(world, random, lv, LootTables.END_CITY_TREASURE_CHEST);
            }
         } else if (boundingBox.contains(pos) && World.isValid(pos)) {
            if (metadata.startsWith("Sentry")) {
               ShulkerEntity lv2 = (ShulkerEntity)EntityType.SHULKER.create(world.toServerWorld());
               if (lv2 != null) {
                  lv2.setPosition((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5);
                  world.spawnEntity(lv2);
               }
            } else if (metadata.startsWith("Elytra")) {
               ItemFrameEntity lv3 = new ItemFrameEntity(world.toServerWorld(), pos, this.placementData.getRotation().rotate(Direction.SOUTH));
               lv3.setHeldItemStack(new ItemStack(Items.ELYTRA), false);
               world.spawnEntity(lv3);
            }
         }

      }
   }

   interface Part {
      void init();

      boolean create(StructureTemplateManager manager, int depth, Piece root, BlockPos pos, List pieces, Random random);
   }
}
