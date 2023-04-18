package net.minecraft.predicate.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class FishingHookPredicate implements TypeSpecificPredicate {
   public static final FishingHookPredicate ALL = new FishingHookPredicate(false);
   private static final String IN_OPEN_WATER = "in_open_water";
   private final boolean inOpenWater;

   private FishingHookPredicate(boolean inOpenWater) {
      this.inOpenWater = inOpenWater;
   }

   public static FishingHookPredicate of(boolean inOpenWater) {
      return new FishingHookPredicate(inOpenWater);
   }

   public static FishingHookPredicate fromJson(JsonObject json) {
      JsonElement jsonElement = json.get("in_open_water");
      return jsonElement != null ? new FishingHookPredicate(JsonHelper.asBoolean(jsonElement, "in_open_water")) : ALL;
   }

   public JsonObject typeSpecificToJson() {
      if (this == ALL) {
         return new JsonObject();
      } else {
         JsonObject jsonObject = new JsonObject();
         jsonObject.add("in_open_water", new JsonPrimitive(this.inOpenWater));
         return jsonObject;
      }
   }

   public TypeSpecificPredicate.Deserializer getDeserializer() {
      return TypeSpecificPredicate.Deserializers.FISHING_HOOK;
   }

   public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
      if (this == ALL) {
         return true;
      } else if (!(entity instanceof FishingBobberEntity)) {
         return false;
      } else {
         FishingBobberEntity lv = (FishingBobberEntity)entity;
         return this.inOpenWater == lv.isInOpenWater();
      }
   }
}
