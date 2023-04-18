package net.minecraft.entity.decoration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.Codecs;

public record Brightness(int block, int sky) {
   public static final Codec LIGHT_LEVEL_CODEC = Codecs.rangedInt(0, 15);
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(LIGHT_LEVEL_CODEC.fieldOf("block").forGetter(Brightness::block), LIGHT_LEVEL_CODEC.fieldOf("sky").forGetter(Brightness::sky)).apply(instance, Brightness::new);
   });
   public static Brightness FULL = new Brightness(15, 15);

   public Brightness(int i, int j) {
      this.block = i;
      this.sky = j;
   }

   public int pack() {
      return this.block << 4 | this.sky << 20;
   }

   public static Brightness unpack(int packed) {
      int j = packed >> 4 & '\uffff';
      int k = packed >> 20 & '\uffff';
      return new Brightness(j, k);
   }

   public int block() {
      return this.block;
   }

   public int sky() {
      return this.sky;
   }
}
