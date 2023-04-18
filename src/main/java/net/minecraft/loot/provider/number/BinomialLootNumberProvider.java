package net.minecraft.loot.provider.number;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.math.random.Random;

public final class BinomialLootNumberProvider implements LootNumberProvider {
   final LootNumberProvider n;
   final LootNumberProvider p;

   BinomialLootNumberProvider(LootNumberProvider n, LootNumberProvider p) {
      this.n = n;
      this.p = p;
   }

   public LootNumberProviderType getType() {
      return LootNumberProviderTypes.BINOMIAL;
   }

   public int nextInt(LootContext context) {
      int i = this.n.nextInt(context);
      float f = this.p.nextFloat(context);
      Random lv = context.getRandom();
      int j = 0;

      for(int k = 0; k < i; ++k) {
         if (lv.nextFloat() < f) {
            ++j;
         }
      }

      return j;
   }

   public float nextFloat(LootContext context) {
      return (float)this.nextInt(context);
   }

   public static BinomialLootNumberProvider create(int n, float p) {
      return new BinomialLootNumberProvider(ConstantLootNumberProvider.create((float)n), ConstantLootNumberProvider.create(p));
   }

   public Set getRequiredParameters() {
      return Sets.union(this.n.getRequiredParameters(), this.p.getRequiredParameters());
   }

   public static class Serializer implements JsonSerializer {
      public BinomialLootNumberProvider fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         LootNumberProvider lv = (LootNumberProvider)JsonHelper.deserialize(jsonObject, "n", jsonDeserializationContext, LootNumberProvider.class);
         LootNumberProvider lv2 = (LootNumberProvider)JsonHelper.deserialize(jsonObject, "p", jsonDeserializationContext, LootNumberProvider.class);
         return new BinomialLootNumberProvider(lv, lv2);
      }

      public void toJson(JsonObject jsonObject, BinomialLootNumberProvider arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.add("n", jsonSerializationContext.serialize(arg.n));
         jsonObject.add("p", jsonSerializationContext.serialize(arg.p));
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
