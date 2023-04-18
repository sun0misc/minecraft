package net.minecraft.structure;

import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.OceanMonumentStructure;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class StructureStart {
   public static final String INVALID = "INVALID";
   public static final StructureStart DEFAULT = new StructureStart((Structure)null, new ChunkPos(0, 0), 0, new StructurePiecesList(List.of()));
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Structure structure;
   private final StructurePiecesList children;
   private final ChunkPos pos;
   private int references;
   @Nullable
   private volatile BlockBox boundingBox;

   public StructureStart(Structure structure, ChunkPos pos, int references, StructurePiecesList children) {
      this.structure = structure;
      this.pos = pos;
      this.references = references;
      this.children = children;
   }

   @Nullable
   public static StructureStart fromNbt(StructureContext context, NbtCompound nbt, long seed) {
      String string = nbt.getString("id");
      if ("INVALID".equals(string)) {
         return DEFAULT;
      } else {
         Registry lv = context.registryManager().get(RegistryKeys.STRUCTURE);
         Structure lv2 = (Structure)lv.get(new Identifier(string));
         if (lv2 == null) {
            LOGGER.error("Unknown stucture id: {}", string);
            return null;
         } else {
            ChunkPos lv3 = new ChunkPos(nbt.getInt("ChunkX"), nbt.getInt("ChunkZ"));
            int i = nbt.getInt("references");
            NbtList lv4 = nbt.getList("Children", NbtElement.COMPOUND_TYPE);

            try {
               StructurePiecesList lv5 = StructurePiecesList.fromNbt(lv4, context);
               if (lv2 instanceof OceanMonumentStructure) {
                  lv5 = OceanMonumentStructure.modifyPiecesOnRead(lv3, seed, lv5);
               }

               return new StructureStart(lv2, lv3, i, lv5);
            } catch (Exception var11) {
               LOGGER.error("Failed Start with id {}", string, var11);
               return null;
            }
         }
      }
   }

   public BlockBox getBoundingBox() {
      BlockBox lv = this.boundingBox;
      if (lv == null) {
         lv = this.structure.expandBoxIfShouldAdaptNoise(this.children.getBoundingBox());
         this.boundingBox = lv;
      }

      return lv;
   }

   public void place(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos) {
      List list = this.children.pieces();
      if (!list.isEmpty()) {
         BlockBox lv = ((StructurePiece)list.get(0)).boundingBox;
         BlockPos lv2 = lv.getCenter();
         BlockPos lv3 = new BlockPos(lv2.getX(), lv.getMinY(), lv2.getZ());
         Iterator var11 = list.iterator();

         while(var11.hasNext()) {
            StructurePiece lv4 = (StructurePiece)var11.next();
            if (lv4.getBoundingBox().intersects(chunkBox)) {
               lv4.generate(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, lv3);
            }
         }

         this.structure.postPlace(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, this.children);
      }
   }

   public NbtCompound toNbt(StructureContext context, ChunkPos chunkPos) {
      NbtCompound lv = new NbtCompound();
      if (this.hasChildren()) {
         lv.putString("id", context.registryManager().get(RegistryKeys.STRUCTURE).getId(this.structure).toString());
         lv.putInt("ChunkX", chunkPos.x);
         lv.putInt("ChunkZ", chunkPos.z);
         lv.putInt("references", this.references);
         lv.put("Children", this.children.toNbt(context));
         return lv;
      } else {
         lv.putString("id", "INVALID");
         return lv;
      }
   }

   public boolean hasChildren() {
      return !this.children.isEmpty();
   }

   public ChunkPos getPos() {
      return this.pos;
   }

   public boolean isNeverReferenced() {
      return this.references < this.getMinReferencedStructureReferenceCount();
   }

   public void incrementReferences() {
      ++this.references;
   }

   public int getReferences() {
      return this.references;
   }

   protected int getMinReferencedStructureReferenceCount() {
      return 1;
   }

   public Structure getStructure() {
      return this.structure;
   }

   public List getChildren() {
      return this.children.pieces();
   }
}
