package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.serialization.Dynamic;
import java.util.stream.LongStream;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.math.MathHelper;

public class BitStorageAlignFix extends DataFix {
   private static final int ELEMENT_BIT_SHIFT = 6;
   private static final int CHUNK_WIDTH = 16;
   private static final int CHUNK_LENGTH = 16;
   private static final int MAX_BLOCK_STATE_ID = 4096;
   private static final int HEIGHT_VALUE_BITS = 9;
   private static final int MAX_HEIGHT_VALUE = 256;

   public BitStorageAlignFix(Schema outputSchema) {
      super(outputSchema, false);
   }

   protected TypeRewriteRule makeRule() {
      Type type = this.getInputSchema().getType(TypeReferences.CHUNK);
      Type type2 = type.findFieldType("Level");
      OpticFinder opticFinder = DSL.fieldFinder("Level", type2);
      OpticFinder opticFinder2 = opticFinder.type().findField("Sections");
      Type type3 = ((List.ListType)opticFinder2.type()).getElement();
      OpticFinder opticFinder3 = DSL.typeFinder(type3);
      Type type4 = DSL.named(TypeReferences.BLOCK_STATE.typeName(), DSL.remainderType());
      OpticFinder opticFinder4 = DSL.fieldFinder("Palette", DSL.list(type4));
      return this.fixTypeEverywhereTyped("BitStorageAlignFix", type, this.getOutputSchema().getType(TypeReferences.CHUNK), (chunk) -> {
         return chunk.updateTyped(opticFinder, (level) -> {
            return this.fixHeightmaps(fixLevel(opticFinder2, opticFinder3, opticFinder4, level));
         });
      });
   }

   private Typed fixHeightmaps(Typed fixedLevel) {
      return fixedLevel.update(DSL.remainderFinder(), (levelDynamic) -> {
         return levelDynamic.update("Heightmaps", (heightmapsDynamic) -> {
            return heightmapsDynamic.updateMapValues((heightmap) -> {
               return heightmap.mapSecond((heightmapDynamic) -> {
                  return fixBitStorageArray(levelDynamic, heightmapDynamic, 256, 9);
               });
            });
         });
      });
   }

   private static Typed fixLevel(OpticFinder levelSectionsFinder, OpticFinder sectionFinder, OpticFinder paletteFinder, Typed level) {
      return level.updateTyped(levelSectionsFinder, (levelSection) -> {
         return levelSection.updateTyped(sectionFinder, (section) -> {
            int i = (Integer)section.getOptional(paletteFinder).map((palette) -> {
               return Math.max(4, DataFixUtils.ceillog2(palette.size()));
            }).orElse(0);
            return i != 0 && !MathHelper.isPowerOfTwo(i) ? section.update(DSL.remainderFinder(), (sectionDynamic) -> {
               return sectionDynamic.update("BlockStates", (statesDynamic) -> {
                  return fixBitStorageArray(sectionDynamic, statesDynamic, 4096, i);
               });
            }) : section;
         });
      });
   }

   private static Dynamic fixBitStorageArray(Dynamic sectionDynamic, Dynamic statesDynamic, int maxValue, int elementBits) {
      long[] ls = statesDynamic.asLongStream().toArray();
      long[] ms = resizePackedIntArray(maxValue, elementBits, ls);
      return sectionDynamic.createLongList(LongStream.of(ms));
   }

   public static long[] resizePackedIntArray(int maxValue, int elementBits, long[] elements) {
      int k = elements.length;
      if (k == 0) {
         return elements;
      } else {
         long l = (1L << elementBits) - 1L;
         int m = 64 / elementBits;
         int n = (maxValue + m - 1) / m;
         long[] ms = new long[n];
         int o = 0;
         int p = 0;
         long q = 0L;
         int r = 0;
         long s = elements[0];
         long t = k > 1 ? elements[1] : 0L;

         for(int u = 0; u < maxValue; ++u) {
            int v = u * elementBits;
            int w = v >> 6;
            int x = (u + 1) * elementBits - 1 >> 6;
            int y = v ^ w << 6;
            if (w != r) {
               s = t;
               t = w + 1 < k ? elements[w + 1] : 0L;
               r = w;
            }

            long z;
            int aa;
            if (w == x) {
               z = s >>> y & l;
            } else {
               aa = 64 - y;
               z = (s >>> y | t << aa) & l;
            }

            aa = p + elementBits;
            if (aa >= 64) {
               ms[o++] = q;
               q = z;
               p = elementBits;
            } else {
               q |= z << p;
               p = aa;
            }
         }

         if (q != 0L) {
            ms[o] = q;
         }

         return ms;
      }
   }
}
