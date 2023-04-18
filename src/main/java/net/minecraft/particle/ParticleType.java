package net.minecraft.particle;

import com.mojang.serialization.Codec;

public abstract class ParticleType {
   private final boolean alwaysShow;
   private final ParticleEffect.Factory parametersFactory;

   protected ParticleType(boolean alwaysShow, ParticleEffect.Factory parametersFactory) {
      this.alwaysShow = alwaysShow;
      this.parametersFactory = parametersFactory;
   }

   public boolean shouldAlwaysSpawn() {
      return this.alwaysShow;
   }

   public ParticleEffect.Factory getParametersFactory() {
      return this.parametersFactory;
   }

   public abstract Codec getCodec();
}
