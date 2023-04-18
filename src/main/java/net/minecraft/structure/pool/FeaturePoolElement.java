package net.minecraft.structure.pool;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.enums.JigsawOrientation;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.PlacedFeature;

public class FeaturePoolElement extends StructurePoolElement {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(PlacedFeature.REGISTRY_CODEC.fieldOf("feature").forGetter((pool) -> {
         return pool.feature;
      }), projectionGetter()).apply(instance, FeaturePoolElement::new);
   });
   private final RegistryEntry feature;
   private final NbtCompound nbt;

   protected FeaturePoolElement(RegistryEntry feature, StructurePool.Projection projection) {
      super(projection);
      this.feature = feature;
      this.nbt = this.createDefaultJigsawNbt();
   }

   private NbtCompound createDefaultJigsawNbt() {
      NbtCompound lv = new NbtCompound();
      lv.putString("name", "minecraft:bottom");
      lv.putString("final_state", "minecraft:air");
      lv.putString("pool", "minecraft:empty");
      lv.putString("target", "minecraft:empty");
      lv.putString("joint", JigsawBlockEntity.Joint.ROLLABLE.asString());
      return lv;
   }

   public Vec3i getStart(StructureTemplateManager structureTemplateManager, BlockRotation rotation) {
      return Vec3i.ZERO;
   }

   public List getStructureBlockInfos(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation, Random random) {
      List list = Lists.newArrayList();
      list.add(new StructureTemplate.StructureBlockInfo(pos, (BlockState)Blocks.JIGSAW.getDefaultState().with(JigsawBlock.ORIENTATION, JigsawOrientation.byDirections(Direction.DOWN, Direction.SOUTH)), this.nbt));
      return list;
   }

   public BlockBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation) {
      Vec3i lv = this.getStart(structureTemplateManager, rotation);
      return new BlockBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + lv.getX(), pos.getY() + lv.getY(), pos.getZ() + lv.getZ());
   }

   public boolean generate(StructureTemplateManager structureTemplateManager, StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, BlockPos pos, BlockPos pivot, BlockRotation rotation, BlockBox box, Random random, boolean keepJigsaws) {
      return ((PlacedFeature)this.feature.value()).generateUnregistered(world, chunkGenerator, random, pos);
   }

   public StructurePoolElementType getType() {
      return StructurePoolElementType.FEATURE_POOL_ELEMENT;
   }

   public String toString() {
      return "Feature[" + this.feature + "]";
   }
}
