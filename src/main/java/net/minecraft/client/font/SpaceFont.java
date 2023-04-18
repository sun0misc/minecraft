package net.minecraft.client.font;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMaps;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SpaceFont implements Font {
   private final Int2ObjectMap codePointsToGlyphs;

   public SpaceFont(Int2FloatMap codePointsToAdvances) {
      this.codePointsToGlyphs = new Int2ObjectOpenHashMap(codePointsToAdvances.size());
      Int2FloatMaps.fastForEach(codePointsToAdvances, (entry) -> {
         float f = entry.getFloatValue();
         this.codePointsToGlyphs.put(entry.getIntKey(), () -> {
            return f;
         });
      });
   }

   @Nullable
   public Glyph getGlyph(int codePoint) {
      return (Glyph)this.codePointsToGlyphs.get(codePoint);
   }

   public IntSet getProvidedGlyphs() {
      return IntSets.unmodifiable(this.codePointsToGlyphs.keySet());
   }

   public static FontLoader fromJson(JsonObject json) {
      Int2FloatMap int2FloatMap = new Int2FloatOpenHashMap();
      JsonObject jsonObject2 = JsonHelper.getObject(json, "advances");
      Iterator var3 = jsonObject2.entrySet().iterator();

      while(var3.hasNext()) {
         Map.Entry entry = (Map.Entry)var3.next();
         int[] is = ((String)entry.getKey()).codePoints().toArray();
         if (is.length != 1) {
            throw new JsonParseException("Expected single codepoint, got " + Arrays.toString(is));
         }

         float f = JsonHelper.asFloat((JsonElement)entry.getValue(), "advance");
         int2FloatMap.put(is[0], f);
      }

      return (arg) -> {
         return new SpaceFont(int2FloatMap);
      };
   }
}
