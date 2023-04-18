package net.minecraft.client.render.entity.model;

import java.util.Optional;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.AnimationHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.AnimationState;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public abstract class SinglePartEntityModel extends EntityModel {
   private static final Vector3f ZERO = new Vector3f();

   public SinglePartEntityModel() {
      this(RenderLayer::getEntityCutoutNoCull);
   }

   public SinglePartEntityModel(Function function) {
      super(function);
   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      this.getPart().render(matrices, vertices, light, overlay, red, green, blue, alpha);
   }

   public abstract ModelPart getPart();

   public Optional getChild(String name) {
      return name.equals("root") ? Optional.of(this.getPart()) : this.getPart().traverse().filter((part) -> {
         return part.hasChild(name);
      }).findFirst().map((part) -> {
         return part.getChild(name);
      });
   }

   protected void updateAnimation(AnimationState animationState, Animation animation, float animationProgress) {
      this.updateAnimation(animationState, animation, animationProgress, 1.0F);
   }

   protected void animateMovement(Animation animation, float limbAngle, float limbDistance, float h, float i) {
      long l = (long)(limbAngle * 50.0F * h);
      float j = Math.min(limbDistance * i, 1.0F);
      AnimationHelper.animate(this, animation, l, j, ZERO);
   }

   protected void updateAnimation(AnimationState animationState, Animation animation, float animationProgress, float speedMultiplier) {
      animationState.update(animationProgress, speedMultiplier);
      animationState.run((state) -> {
         AnimationHelper.animate(this, animation, state.getTimeRunning(), 1.0F, ZERO);
      });
   }
}
