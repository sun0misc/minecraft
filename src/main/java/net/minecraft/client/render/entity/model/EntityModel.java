package net.minecraft.client.render.entity.model;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class EntityModel extends Model {
   public float handSwingProgress;
   public boolean riding;
   public boolean child;

   protected EntityModel() {
      this(RenderLayer::getEntityCutoutNoCull);
   }

   protected EntityModel(Function function) {
      super(function);
      this.child = true;
   }

   public abstract void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch);

   public void animateModel(Entity entity, float limbAngle, float limbDistance, float tickDelta) {
   }

   public void copyStateTo(EntityModel copy) {
      copy.handSwingProgress = this.handSwingProgress;
      copy.riding = this.riding;
      copy.child = this.child;
   }
}
