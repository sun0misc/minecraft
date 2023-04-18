package net.minecraft.entity.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public abstract class AmbientEntity extends MobEntity {
   protected AmbientEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public boolean canBeLeashedBy(PlayerEntity player) {
      return false;
   }
}
