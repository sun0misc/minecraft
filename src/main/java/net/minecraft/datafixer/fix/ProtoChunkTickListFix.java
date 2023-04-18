package net.minecraft.datafixer.fix;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

public class ProtoChunkTickListFix extends DataFix {
   private static final int field_35446 = 16;
   private static final ImmutableSet ALWAYS_WATERLOGGED_BLOCK_IDS = ImmutableSet.of("minecraft:bubble_column", "minecraft:kelp", "minecraft:kelp_plant", "minecraft:seagrass", "minecraft:tall_seagrass");

   public ProtoChunkTickListFix(Schema schema) {
      super(schema, false);
   }

   protected TypeRewriteRule makeRule() {
      Type type = this.getInputSchema().getType(TypeReferences.CHUNK);
      OpticFinder opticFinder = type.findField("Level");
      OpticFinder opticFinder2 = opticFinder.type().findField("Sections");
      OpticFinder opticFinder3 = ((List.ListType)opticFinder2.type()).getElement().finder();
      OpticFinder opticFinder4 = opticFinder3.type().findField("block_states");
      OpticFinder opticFinder5 = opticFinder3.type().findField("biomes");
      OpticFinder opticFinder6 = opticFinder4.type().findField("palette");
      OpticFinder opticFinder7 = opticFinder.type().findField("TileTicks");
      return this.fixTypeEverywhereTyped("ChunkProtoTickListFix", type, (typed) -> {
         return typed.updateTyped(opticFinder, (typedx) -> {
            typedx = typedx.update(DSL.remainderFinder(), (dynamicx) -> {
               return (Dynamic)DataFixUtils.orElse(dynamicx.get("LiquidTicks").result().map((dynamic2) -> {
                  return dynamicx.set("fluid_ticks", dynamic2).remove("LiquidTicks");
               }), dynamicx);
            });
            Dynamic dynamic = (Dynamic)typedx.get(DSL.remainderFinder());
            MutableInt mutableInt = new MutableInt();
            Int2ObjectMap int2ObjectMap = new Int2ObjectArrayMap();
            typedx.getOptionalTyped(opticFinder2).ifPresent((typed) -> {
               typed.getAllTyped(opticFinder3).forEach((typedx) -> {
                  Dynamic dynamic = (Dynamic)typedx.get(DSL.remainderFinder());
                  int i = dynamic.get("Y").asInt(Integer.MAX_VALUE);
                  if (i != Integer.MAX_VALUE) {
                     if (typedx.getOptionalTyped(opticFinder5).isPresent()) {
                        mutableInt.setValue(Math.min(i, mutableInt.getValue()));
                     }

                     typedx.getOptionalTyped(opticFinder4).ifPresent((typed) -> {
                        int2ObjectMap.put(i, Suppliers.memoize(() -> {
                           java.util.List list = (java.util.List)typed.getOptionalTyped(opticFinder6).map((typedx) -> {
                              return (java.util.List)typedx.write().result().map((dynamic) -> {
                                 return dynamic.asList(Function.identity());
                              }).orElse(Collections.emptyList());
                           }).orElse(Collections.emptyList());
                           long[] ls = ((Dynamic)typed.get(DSL.remainderFinder())).get("data").asLongStream().toArray();
                           return new class_6741(list, ls);
                        }));
                     });
                  }
               });
            });
            byte b = mutableInt.getValue().byteValue();
            typedx = typedx.update(DSL.remainderFinder(), (dynamicx) -> {
               return dynamicx.update("yPos", (dynamic) -> {
                  return dynamic.createByte(b);
               });
            });
            if (!typedx.getOptionalTyped(opticFinder7).isPresent() && !dynamic.get("fluid_ticks").result().isPresent()) {
               int i = dynamic.get("xPos").asInt(0);
               int j = dynamic.get("zPos").asInt(0);
               Dynamic dynamic2 = this.fixToBeTicked(dynamic, int2ObjectMap, b, i, j, "LiquidsToBeTicked", ProtoChunkTickListFix::getFluidBlockIdToBeTicked);
               Dynamic dynamic3 = this.fixToBeTicked(dynamic, int2ObjectMap, b, i, j, "ToBeTicked", ProtoChunkTickListFix::getBlockIdToBeTicked);
               Optional optional = opticFinder7.type().readTyped(dynamic3).result();
               if (optional.isPresent()) {
                  typedx = typedx.set(opticFinder7, (Typed)((Pair)optional.get()).getFirst());
               }

               return typedx.update(DSL.remainderFinder(), (dynamic2x) -> {
                  return dynamic2x.remove("ToBeTicked").remove("LiquidsToBeTicked").set("fluid_ticks", dynamic2);
               });
            } else {
               return typedx;
            }
         });
      });
   }

   private Dynamic fixToBeTicked(Dynamic dynamic, Int2ObjectMap int2ObjectMap, byte b, int i, int j, String string, Function function) {
      Stream stream = Stream.empty();
      java.util.List list = dynamic.get(string).asList(Function.identity());

      for(int k = 0; k < list.size(); ++k) {
         int l = k + b;
         Supplier supplier = (Supplier)int2ObjectMap.get(l);
         Stream stream2 = ((Dynamic)list.get(k)).asStream().mapToInt((dynamicx) -> {
            return dynamicx.asShort((short)-1);
         }).filter((ix) -> {
            return ix > 0;
         }).mapToObj((lx) -> {
            return this.method_39255(dynamic, supplier, i, l, j, lx, function);
         });
         stream = Stream.concat(stream, stream2);
      }

      return dynamic.createList(stream);
   }

   private static String getBlockIdToBeTicked(@Nullable Dynamic dynamic) {
      return dynamic != null ? dynamic.get("Name").asString("minecraft:air") : "minecraft:air";
   }

   private static String getFluidBlockIdToBeTicked(@Nullable Dynamic dynamic) {
      if (dynamic == null) {
         return "minecraft:empty";
      } else {
         String string = dynamic.get("Name").asString("");
         if ("minecraft:water".equals(string)) {
            return dynamic.get("Properties").get("level").asInt(0) == 0 ? "minecraft:water" : "minecraft:flowing_water";
         } else if ("minecraft:lava".equals(string)) {
            return dynamic.get("Properties").get("level").asInt(0) == 0 ? "minecraft:lava" : "minecraft:flowing_lava";
         } else {
            return !ALWAYS_WATERLOGGED_BLOCK_IDS.contains(string) && !dynamic.get("Properties").get("waterlogged").asBoolean(false) ? "minecraft:empty" : "minecraft:water";
         }
      }
   }

   private Dynamic method_39255(Dynamic dynamic, @Nullable Supplier supplier, int i, int j, int k, int l, Function function) {
      int m = l & 15;
      int n = l >>> 4 & 15;
      int o = l >>> 8 & 15;
      String string = (String)function.apply(supplier != null ? ((class_6741)supplier.get()).method_39265(m, n, o) : null);
      return dynamic.createMap(ImmutableMap.builder().put(dynamic.createString("i"), dynamic.createString(string)).put(dynamic.createString("x"), dynamic.createInt(i * 16 + m)).put(dynamic.createString("y"), dynamic.createInt(j * 16 + n)).put(dynamic.createString("z"), dynamic.createInt(k * 16 + o)).put(dynamic.createString("t"), dynamic.createInt(0)).put(dynamic.createString("p"), dynamic.createInt(0)).build());
   }

   public static final class class_6741 {
      private static final long field_35448 = 4L;
      private final java.util.List field_35449;
      private final long[] field_35450;
      private final int field_35451;
      private final long field_35452;
      private final int field_35453;

      public class_6741(java.util.List list, long[] ls) {
         this.field_35449 = list;
         this.field_35450 = ls;
         this.field_35451 = Math.max(4, ChunkHeightAndBiomeFix.ceilLog2(list.size()));
         this.field_35452 = (1L << this.field_35451) - 1L;
         this.field_35453 = (char)(64 / this.field_35451);
      }

      @Nullable
      public Dynamic method_39265(int i, int j, int k) {
         int l = this.field_35449.size();
         if (l < 1) {
            return null;
         } else if (l == 1) {
            return (Dynamic)this.field_35449.get(0);
         } else {
            int m = this.method_39267(i, j, k);
            int n = m / this.field_35453;
            if (n >= 0 && n < this.field_35450.length) {
               long o = this.field_35450[n];
               int p = (m - n * this.field_35453) * this.field_35451;
               int q = (int)(o >> p & this.field_35452);
               return q >= 0 && q < l ? (Dynamic)this.field_35449.get(q) : null;
            } else {
               return null;
            }
         }
      }

      private int method_39267(int i, int j, int k) {
         return (j << 4 | k) << 4 | i;
      }

      public java.util.List method_39264() {
         return this.field_35449;
      }

      public long[] method_39266() {
         return this.field_35450;
      }
   }
}
