package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class EntityModelLayer {
   private final Identifier id;
   private final String name;

   public EntityModelLayer(Identifier id, String name) {
      this.id = id;
      this.name = name;
   }

   public Identifier getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof EntityModelLayer)) {
         return false;
      } else {
         EntityModelLayer lv = (EntityModelLayer)o;
         return this.id.equals(lv.id) && this.name.equals(lv.name);
      }
   }

   public int hashCode() {
      int i = this.id.hashCode();
      i = 31 * i + this.name.hashCode();
      return i;
   }

   public String toString() {
      return this.id + "#" + this.name;
   }
}
