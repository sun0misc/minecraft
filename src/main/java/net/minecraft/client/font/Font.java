package net.minecraft.client.font;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface Font extends AutoCloseable {
   default void close() {
   }

   @Nullable
   default Glyph getGlyph(int codePoint) {
      return null;
   }

   IntSet getProvidedGlyphs();
}
