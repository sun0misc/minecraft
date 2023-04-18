package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.mob.ZombieEntity;

@Environment(EnvType.CLIENT)
public class ZombieEntityModel extends AbstractZombieModel {
   public ZombieEntityModel(ModelPart arg) {
      super(arg);
   }

   public boolean isAttacking(ZombieEntity arg) {
      return arg.isAttacking();
   }
}
