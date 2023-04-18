package net.minecraft.structure;

import java.util.Iterator;
import java.util.Map;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class ShipwreckGenerator {
   static final BlockPos DEFAULT_POSITION = new BlockPos(4, 0, 15);
   private static final Identifier[] BEACHED_TEMPLATES = new Identifier[]{new Identifier("shipwreck/with_mast"), new Identifier("shipwreck/sideways_full"), new Identifier("shipwreck/sideways_fronthalf"), new Identifier("shipwreck/sideways_backhalf"), new Identifier("shipwreck/rightsideup_full"), new Identifier("shipwreck/rightsideup_fronthalf"), new Identifier("shipwreck/rightsideup_backhalf"), new Identifier("shipwreck/with_mast_degraded"), new Identifier("shipwreck/rightsideup_full_degraded"), new Identifier("shipwreck/rightsideup_fronthalf_degraded"), new Identifier("shipwreck/rightsideup_backhalf_degraded")};
   private static final Identifier[] REGULAR_TEMPLATES = new Identifier[]{new Identifier("shipwreck/with_mast"), new Identifier("shipwreck/upsidedown_full"), new Identifier("shipwreck/upsidedown_fronthalf"), new Identifier("shipwreck/upsidedown_backhalf"), new Identifier("shipwreck/sideways_full"), new Identifier("shipwreck/sideways_fronthalf"), new Identifier("shipwreck/sideways_backhalf"), new Identifier("shipwreck/rightsideup_full"), new Identifier("shipwreck/rightsideup_fronthalf"), new Identifier("shipwreck/rightsideup_backhalf"), new Identifier("shipwreck/with_mast_degraded"), new Identifier("shipwreck/upsidedown_full_degraded"), new Identifier("shipwreck/upsidedown_fronthalf_degraded"), new Identifier("shipwreck/upsidedown_backhalf_degraded"), new Identifier("shipwreck/sideways_full_degraded"), new Identifier("shipwreck/sideways_fronthalf_degraded"), new Identifier("shipwreck/sideways_backhalf_degraded"), new Identifier("shipwreck/rightsideup_full_degraded"), new Identifier("shipwreck/rightsideup_fronthalf_degraded"), new Identifier("shipwreck/rightsideup_backhalf_degraded")};
   static final Map LOOT_TABLES;

   public static void addParts(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation, StructurePiecesHolder holder, Random random, boolean beached) {
      Identifier lv = (Identifier)Util.getRandom((Object[])(beached ? BEACHED_TEMPLATES : REGULAR_TEMPLATES), random);
      holder.addPiece(new Piece(structureTemplateManager, lv, pos, rotation, beached));
   }

   static {
      LOOT_TABLES = Map.of("map_chest", LootTables.SHIPWRECK_MAP_CHEST, "treasure_chest", LootTables.SHIPWRECK_TREASURE_CHEST, "supply_chest", LootTables.SHIPWRECK_SUPPLY_CHEST);
   }

   public static class Piece extends SimpleStructurePiece {
      private final boolean grounded;

      public Piece(StructureTemplateManager manager, Identifier identifier, BlockPos pos, BlockRotation rotation, boolean grounded) {
         super(StructurePieceType.SHIPWRECK, 0, manager, identifier, identifier.toString(), createPlacementData(rotation), pos);
         this.grounded = grounded;
      }

      public Piece(StructureTemplateManager manager, NbtCompound nbt) {
         super(StructurePieceType.SHIPWRECK, nbt, manager, (id) -> {
            return createPlacementData(BlockRotation.valueOf(nbt.getString("Rot")));
         });
         this.grounded = nbt.getBoolean("isBeached");
      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
         super.writeNbt(context, nbt);
         nbt.putBoolean("isBeached", this.grounded);
         nbt.putString("Rot", this.placementData.getRotation().name());
      }

      private static StructurePlacementData createPlacementData(BlockRotation rotation) {
         return (new StructurePlacementData()).setRotation(rotation).setMirror(BlockMirror.NONE).setPosition(ShipwreckGenerator.DEFAULT_POSITION).addProcessor(BlockIgnoreStructureProcessor.IGNORE_AIR_AND_STRUCTURE_BLOCKS);
      }

      protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world, Random random, BlockBox boundingBox) {
         Identifier lv = (Identifier)ShipwreckGenerator.LOOT_TABLES.get(metadata);
         if (lv != null) {
            LootableContainerBlockEntity.setLootTable(world, random, pos.down(), lv);
         }

      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         int i = world.getTopY();
         int j = 0;
         Vec3i lv = this.template.getSize();
         Heightmap.Type lv2 = this.grounded ? Heightmap.Type.WORLD_SURFACE_WG : Heightmap.Type.OCEAN_FLOOR_WG;
         int k = lv.getX() * lv.getZ();
         if (k == 0) {
            j = world.getTopY(lv2, this.pos.getX(), this.pos.getZ());
         } else {
            BlockPos lv3 = this.pos.add(lv.getX() - 1, 0, lv.getZ() - 1);

            int l;
            for(Iterator var14 = BlockPos.iterate(this.pos, lv3).iterator(); var14.hasNext(); i = Math.min(i, l)) {
               BlockPos lv4 = (BlockPos)var14.next();
               l = world.getTopY(lv2, lv4.getX(), lv4.getZ());
               j += l;
            }

            j /= k;
         }

         int m = this.grounded ? i - lv.getY() / 2 - random.nextInt(3) : j;
         this.pos = new BlockPos(this.pos.getX(), m, this.pos.getZ());
         super.generate(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, pivot);
      }
   }
}
