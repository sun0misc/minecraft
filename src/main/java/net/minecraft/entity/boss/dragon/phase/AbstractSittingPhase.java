package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;

public abstract class AbstractSittingPhase extends AbstractPhase {
   public AbstractSittingPhase(EnderDragonEntity arg) {
      super(arg);
   }

   public boolean isSittingOrHovering() {
      return true;
   }

   public float modifyDamageTaken(DamageSource damageSource, float damage) {
      if (damageSource.getSource() instanceof PersistentProjectileEntity) {
         damageSource.getSource().setOnFireFor(1);
         return 0.0F;
      } else {
         return super.modifyDamageTaken(damageSource, damage);
      }
   }
}
