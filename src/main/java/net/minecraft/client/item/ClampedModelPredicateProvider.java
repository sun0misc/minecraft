package net.minecraft.client.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface ClampedModelPredicateProvider extends ModelPredicateProvider {
   /** @deprecated */
   @Deprecated
   default float call(ItemStack arg, @Nullable ClientWorld arg2, @Nullable LivingEntity arg3, int i) {
      return MathHelper.clamp(this.unclampedCall(arg, arg2, arg3, i), 0.0F, 1.0F);
   }

   float unclampedCall(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed);
}
