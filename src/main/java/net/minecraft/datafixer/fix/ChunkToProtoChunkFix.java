package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.datafixer.TypeReferences;

public class ChunkToProtoChunkFix extends DataFix {
   private static final int field_29881 = 16;

   public ChunkToProtoChunkFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   public TypeRewriteRule makeRule() {
      return this.writeFixAndRead("ChunkToProtoChunkFix", this.getInputSchema().getType(TypeReferences.CHUNK), this.getOutputSchema().getType(TypeReferences.CHUNK), (dynamic) -> {
         return dynamic.update("Level", ChunkToProtoChunkFix::fixLevel);
      });
   }

   private static Dynamic fixLevel(Dynamic dynamic) {
      boolean bl = dynamic.get("TerrainPopulated").asBoolean(false);
      boolean bl2 = dynamic.get("LightPopulated").asNumber().result().isEmpty() || dynamic.get("LightPopulated").asBoolean(false);
      String string;
      if (bl) {
         if (bl2) {
            string = "mobs_spawned";
         } else {
            string = "decorated";
         }
      } else {
         string = "carved";
      }

      return fixTileTicks(fixBiomes(dynamic)).set("Status", dynamic.createString(string)).set("hasLegacyStructureData", dynamic.createBoolean(true));
   }

   private static Dynamic fixBiomes(Dynamic dynamic) {
      return dynamic.update("Biomes", (dynamic2) -> {
         return (Dynamic)DataFixUtils.orElse(dynamic2.asByteBufferOpt().result().map((byteBuffer) -> {
            int[] is = new int[256];

            for(int i = 0; i < is.length; ++i) {
               if (i < byteBuffer.capacity()) {
                  is[i] = byteBuffer.get(i) & 255;
               }
            }

            return dynamic.createIntList(Arrays.stream(is));
         }), dynamic2);
      });
   }

   private static Dynamic fixTileTicks(Dynamic dynamic) {
      return (Dynamic)DataFixUtils.orElse(dynamic.get("TileTicks").asStreamOpt().result().map((stream) -> {
         List list = (List)IntStream.range(0, 16).mapToObj((i) -> {
            return new ShortArrayList();
         }).collect(Collectors.toList());
         stream.forEach((dynamicx) -> {
            int i = dynamicx.get("x").asInt(0);
            int j = dynamicx.get("y").asInt(0);
            int k = dynamicx.get("z").asInt(0);
            short s = packChunkSectionPos(i, j, k);
            ((ShortList)list.get(j >> 4)).add(s);
         });
         return dynamic.remove("TileTicks").set("ToBeTicked", dynamic.createList(list.stream().map((shortList) -> {
            return dynamic.createList(shortList.intStream().mapToObj((i) -> {
               return dynamic.createShort((short)i);
            }));
         })));
      }), dynamic);
   }

   private static short packChunkSectionPos(int x, int y, int z) {
      return (short)(x & 15 | (y & 15) << 4 | (z & 15) << 8);
   }
}
