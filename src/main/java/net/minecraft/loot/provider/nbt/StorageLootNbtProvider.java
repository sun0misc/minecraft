package net.minecraft.loot.provider.nbt;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import org.jetbrains.annotations.Nullable;

public class StorageLootNbtProvider implements LootNbtProvider {
   final Identifier source;

   StorageLootNbtProvider(Identifier source) {
      this.source = source;
   }

   public LootNbtProviderType getType() {
      return LootNbtProviderTypes.STORAGE;
   }

   @Nullable
   public NbtElement getNbt(LootContext context) {
      return context.getWorld().getServer().getDataCommandStorage().get(this.source);
   }

   public Set getRequiredParameters() {
      return ImmutableSet.of();
   }

   public static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, StorageLootNbtProvider arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.addProperty("source", arg.source.toString());
      }

      public StorageLootNbtProvider fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         String string = JsonHelper.getString(jsonObject, "source");
         return new StorageLootNbtProvider(new Identifier(string));
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
