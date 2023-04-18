package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.mob.GiantEntity;

@Environment(EnvType.CLIENT)
public class GiantEntityModel extends AbstractZombieModel {
   public GiantEntityModel(ModelPart arg) {
      super(arg);
   }

   public boolean isAttacking(GiantEntity arg) {
      return false;
   }
}
