package net.minecraft.client.render.model.json;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ModelOverrideList {
   public static final ModelOverrideList EMPTY = new ModelOverrideList();
   public static final float field_42072 = Float.NEGATIVE_INFINITY;
   private final BakedOverride[] overrides;
   private final Identifier[] conditionTypes;

   private ModelOverrideList() {
      this.overrides = new BakedOverride[0];
      this.conditionTypes = new Identifier[0];
   }

   public ModelOverrideList(Baker baker, JsonUnbakedModel parent, List overrides) {
      this.conditionTypes = (Identifier[])overrides.stream().flatMap(ModelOverride::streamConditions).map(ModelOverride.Condition::getType).distinct().toArray((ix) -> {
         return new Identifier[ix];
      });
      Object2IntMap object2IntMap = new Object2IntOpenHashMap();

      for(int i = 0; i < this.conditionTypes.length; ++i) {
         object2IntMap.put(this.conditionTypes[i], i);
      }

      List list2 = Lists.newArrayList();

      for(int j = overrides.size() - 1; j >= 0; --j) {
         ModelOverride lv = (ModelOverride)overrides.get(j);
         BakedModel lv2 = this.bakeOverridingModel(baker, parent, lv);
         InlinedCondition[] lvs = (InlinedCondition[])lv.streamConditions().map((condition) -> {
            int i = object2IntMap.getInt(condition.getType());
            return new InlinedCondition(i, condition.getThreshold());
         }).toArray((ix) -> {
            return new InlinedCondition[ix];
         });
         list2.add(new BakedOverride(lvs, lv2));
      }

      this.overrides = (BakedOverride[])list2.toArray(new BakedOverride[0]);
   }

   @Nullable
   private BakedModel bakeOverridingModel(Baker baker, JsonUnbakedModel parent, ModelOverride override) {
      UnbakedModel lv = baker.getOrLoadModel(override.getModelId());
      return Objects.equals(lv, parent) ? null : baker.bake(override.getModelId(), net.minecraft.client.render.model.ModelRotation.X0_Y0);
   }

   @Nullable
   public BakedModel apply(BakedModel model, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
      if (this.overrides.length != 0) {
         Item lv = stack.getItem();
         int j = this.conditionTypes.length;
         float[] fs = new float[j];

         for(int k = 0; k < j; ++k) {
            Identifier lv2 = this.conditionTypes[k];
            ModelPredicateProvider lv3 = ModelPredicateProviderRegistry.get(lv, lv2);
            if (lv3 != null) {
               fs[k] = lv3.call(stack, world, entity, seed);
            } else {
               fs[k] = Float.NEGATIVE_INFINITY;
            }
         }

         BakedOverride[] var16 = this.overrides;
         int var14 = var16.length;

         for(int var15 = 0; var15 < var14; ++var15) {
            BakedOverride lv4 = var16[var15];
            if (lv4.test(fs)) {
               BakedModel lv5 = lv4.model;
               if (lv5 == null) {
                  return model;
               }

               return lv5;
            }
         }
      }

      return model;
   }

   @Environment(EnvType.CLIENT)
   static class BakedOverride {
      private final InlinedCondition[] conditions;
      @Nullable
      final BakedModel model;

      BakedOverride(InlinedCondition[] conditions, @Nullable BakedModel model) {
         this.conditions = conditions;
         this.model = model;
      }

      boolean test(float[] values) {
         InlinedCondition[] var2 = this.conditions;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            InlinedCondition lv = var2[var4];
            float f = values[lv.index];
            if (f < lv.threshold) {
               return false;
            }
         }

         return true;
      }
   }

   @Environment(EnvType.CLIENT)
   private static class InlinedCondition {
      public final int index;
      public final float threshold;

      InlinedCondition(int index, float threshold) {
         this.index = index;
         this.threshold = threshold;
      }
   }
}
