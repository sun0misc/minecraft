package net.minecraft.util.dynamic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public record CodecHolder(Codec codec) {
   public CodecHolder(Codec codec) {
      this.codec = codec;
   }

   public static CodecHolder of(Codec codec) {
      return new CodecHolder(codec);
   }

   public static CodecHolder of(MapCodec mapCodec) {
      return new CodecHolder(mapCodec.codec());
   }

   public Codec codec() {
      return this.codec;
   }
}
