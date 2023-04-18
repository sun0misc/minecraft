package net.minecraft.structure;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;

public record StructurePiecesList(List pieces) {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Identifier JIGSAW = new Identifier("jigsaw");
   private static final Map ID_UPDATES;

   public StructurePiecesList(List pieces) {
      this.pieces = List.copyOf(pieces);
   }

   public boolean isEmpty() {
      return this.pieces.isEmpty();
   }

   public boolean contains(BlockPos pos) {
      Iterator var2 = this.pieces.iterator();

      StructurePiece lv;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         lv = (StructurePiece)var2.next();
      } while(!lv.getBoundingBox().contains(pos));

      return true;
   }

   public NbtElement toNbt(StructureContext context) {
      NbtList lv = new NbtList();
      Iterator var3 = this.pieces.iterator();

      while(var3.hasNext()) {
         StructurePiece lv2 = (StructurePiece)var3.next();
         lv.add(lv2.toNbt(context));
      }

      return lv;
   }

   public static StructurePiecesList fromNbt(NbtList list, StructureContext context) {
      List list = Lists.newArrayList();

      for(int i = 0; i < list.size(); ++i) {
         NbtCompound lv = list.getCompound(i);
         String string = lv.getString("id").toLowerCase(Locale.ROOT);
         Identifier lv2 = new Identifier(string);
         Identifier lv3 = (Identifier)ID_UPDATES.getOrDefault(lv2, lv2);
         StructurePieceType lv4 = (StructurePieceType)Registries.STRUCTURE_PIECE.get(lv3);
         if (lv4 == null) {
            LOGGER.error("Unknown structure piece id: {}", lv3);
         } else {
            try {
               StructurePiece lv5 = lv4.load(context, lv);
               list.add(lv5);
            } catch (Exception var10) {
               LOGGER.error("Exception loading structure piece with id {}", lv3, var10);
            }
         }
      }

      return new StructurePiecesList(list);
   }

   public BlockBox getBoundingBox() {
      return StructurePiece.boundingBox(this.pieces.stream());
   }

   public List pieces() {
      return this.pieces;
   }

   static {
      ID_UPDATES = ImmutableMap.builder().put(new Identifier("nvi"), JIGSAW).put(new Identifier("pcp"), JIGSAW).put(new Identifier("bastionremnant"), JIGSAW).put(new Identifier("runtime"), JIGSAW).build();
   }
}
