package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;

@Environment(EnvType.CLIENT)
public abstract class AbstractZombieModel extends BipedEntityModel {
   protected AbstractZombieModel(ModelPart arg) {
      super(arg);
   }

   public void setAngles(HostileEntity arg, float f, float g, float h, float i, float j) {
      super.setAngles((LivingEntity)arg, f, g, h, i, j);
      CrossbowPosing.meleeAttack(this.leftArm, this.rightArm, this.isAttacking(arg), this.handSwingProgress, h);
   }

   public abstract boolean isAttacking(HostileEntity entity);
}
