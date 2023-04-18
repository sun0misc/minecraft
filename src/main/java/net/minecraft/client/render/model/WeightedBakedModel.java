package net.minecraft.client.render.model;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.collection.Weighting;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WeightedBakedModel implements BakedModel {
   private final int totalWeight;
   private final List models;
   private final BakedModel defaultModel;

   public WeightedBakedModel(List models) {
      this.models = models;
      this.totalWeight = Weighting.getWeightSum(models);
      this.defaultModel = (BakedModel)((Weighted.Present)models.get(0)).getData();
   }

   public List getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
      return (List)Weighting.getAt(this.models, Math.abs((int)random.nextLong()) % this.totalWeight).map((arg4) -> {
         return ((BakedModel)arg4.getData()).getQuads(state, face, random);
      }).orElse(Collections.emptyList());
   }

   public boolean useAmbientOcclusion() {
      return this.defaultModel.useAmbientOcclusion();
   }

   public boolean hasDepth() {
      return this.defaultModel.hasDepth();
   }

   public boolean isSideLit() {
      return this.defaultModel.isSideLit();
   }

   public boolean isBuiltin() {
      return this.defaultModel.isBuiltin();
   }

   public Sprite getParticleSprite() {
      return this.defaultModel.getParticleSprite();
   }

   public ModelTransformation getTransformation() {
      return this.defaultModel.getTransformation();
   }

   public ModelOverrideList getOverrides() {
      return this.defaultModel.getOverrides();
   }

   @Environment(EnvType.CLIENT)
   public static class Builder {
      private final List models = Lists.newArrayList();

      public Builder add(@Nullable BakedModel model, int weight) {
         if (model != null) {
            this.models.add(Weighted.of(model, weight));
         }

         return this;
      }

      @Nullable
      public BakedModel build() {
         if (this.models.isEmpty()) {
            return null;
         } else {
            return (BakedModel)(this.models.size() == 1 ? (BakedModel)((Weighted.Present)this.models.get(0)).getData() : new WeightedBakedModel(this.models));
         }
      }
   }
}
