package net.minecraft.util.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public final class GlobalPos {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(World.CODEC.fieldOf("dimension").forGetter(GlobalPos::getDimension), BlockPos.CODEC.fieldOf("pos").forGetter(GlobalPos::getPos)).apply(instance, GlobalPos::create);
   });
   private final RegistryKey dimension;
   private final BlockPos pos;

   private GlobalPos(RegistryKey dimension, BlockPos pos) {
      this.dimension = dimension;
      this.pos = pos;
   }

   public static GlobalPos create(RegistryKey dimension, BlockPos pos) {
      return new GlobalPos(dimension, pos);
   }

   public RegistryKey getDimension() {
      return this.dimension;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         GlobalPos lv = (GlobalPos)o;
         return Objects.equals(this.dimension, lv.dimension) && Objects.equals(this.pos, lv.pos);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.dimension, this.pos});
   }

   public String toString() {
      return this.dimension + " " + this.pos;
   }
}
