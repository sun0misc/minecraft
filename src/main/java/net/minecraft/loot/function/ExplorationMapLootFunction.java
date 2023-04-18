package net.minecraft.loot.function;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import java.util.Locale;
import java.util.Set;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;

public class ExplorationMapLootFunction extends ConditionalLootFunction {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final TagKey DEFAULT_DESTINATION;
   public static final String MANSION = "mansion";
   public static final MapIcon.Type DEFAULT_DECORATION;
   public static final byte field_31851 = 2;
   public static final int field_31852 = 50;
   public static final boolean field_31853 = true;
   final TagKey destination;
   final MapIcon.Type decoration;
   final byte zoom;
   final int searchRadius;
   final boolean skipExistingChunks;

   ExplorationMapLootFunction(LootCondition[] conditions, TagKey destination, MapIcon.Type decoration, byte zoom, int searchRadius, boolean skipExistingChunks) {
      super(conditions);
      this.destination = destination;
      this.decoration = decoration;
      this.zoom = zoom;
      this.searchRadius = searchRadius;
      this.skipExistingChunks = skipExistingChunks;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.EXPLORATION_MAP;
   }

   public Set getRequiredParameters() {
      return ImmutableSet.of(LootContextParameters.ORIGIN);
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      if (!stack.isOf(Items.MAP)) {
         return stack;
      } else {
         Vec3d lv = (Vec3d)context.get(LootContextParameters.ORIGIN);
         if (lv != null) {
            ServerWorld lv2 = context.getWorld();
            BlockPos lv3 = lv2.locateStructure(this.destination, BlockPos.ofFloored(lv), this.searchRadius, this.skipExistingChunks);
            if (lv3 != null) {
               ItemStack lv4 = FilledMapItem.createMap(lv2, lv3.getX(), lv3.getZ(), this.zoom, true, true);
               FilledMapItem.fillExplorationMap(lv2, lv4);
               MapState.addDecorationsNbt(lv4, lv3, "+", this.decoration);
               return lv4;
            }
         }

         return stack;
      }
   }

   public static Builder builder() {
      return new Builder();
   }

   static {
      DEFAULT_DESTINATION = StructureTags.ON_TREASURE_MAPS;
      DEFAULT_DECORATION = MapIcon.Type.MANSION;
   }

   public static class Builder extends ConditionalLootFunction.Builder {
      private TagKey destination;
      private MapIcon.Type decoration;
      private byte zoom;
      private int searchRadius;
      private boolean skipExistingChunks;

      public Builder() {
         this.destination = ExplorationMapLootFunction.DEFAULT_DESTINATION;
         this.decoration = ExplorationMapLootFunction.DEFAULT_DECORATION;
         this.zoom = 2;
         this.searchRadius = 50;
         this.skipExistingChunks = true;
      }

      protected Builder getThisBuilder() {
         return this;
      }

      public Builder withDestination(TagKey destination) {
         this.destination = destination;
         return this;
      }

      public Builder withDecoration(MapIcon.Type decoration) {
         this.decoration = decoration;
         return this;
      }

      public Builder withZoom(byte zoom) {
         this.zoom = zoom;
         return this;
      }

      public Builder searchRadius(int searchRadius) {
         this.searchRadius = searchRadius;
         return this;
      }

      public Builder withSkipExistingChunks(boolean skipExistingChunks) {
         this.skipExistingChunks = skipExistingChunks;
         return this;
      }

      public LootFunction build() {
         return new ExplorationMapLootFunction(this.getConditions(), this.destination, this.decoration, this.zoom, this.searchRadius, this.skipExistingChunks);
      }

      // $FF: synthetic method
      protected ConditionalLootFunction.Builder getThisBuilder() {
         return this.getThisBuilder();
      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, ExplorationMapLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         if (!arg.destination.equals(ExplorationMapLootFunction.DEFAULT_DESTINATION)) {
            jsonObject.addProperty("destination", arg.destination.id().toString());
         }

         if (arg.decoration != ExplorationMapLootFunction.DEFAULT_DECORATION) {
            jsonObject.add("decoration", jsonSerializationContext.serialize(arg.decoration.toString().toLowerCase(Locale.ROOT)));
         }

         if (arg.zoom != 2) {
            jsonObject.addProperty("zoom", arg.zoom);
         }

         if (arg.searchRadius != 50) {
            jsonObject.addProperty("search_radius", arg.searchRadius);
         }

         if (!arg.skipExistingChunks) {
            jsonObject.addProperty("skip_existing_chunks", arg.skipExistingChunks);
         }

      }

      public ExplorationMapLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         TagKey lv = getDestination(jsonObject);
         String string = jsonObject.has("decoration") ? JsonHelper.getString(jsonObject, "decoration") : "mansion";
         MapIcon.Type lv2 = ExplorationMapLootFunction.DEFAULT_DECORATION;

         try {
            lv2 = MapIcon.Type.valueOf(string.toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var10) {
            ExplorationMapLootFunction.LOGGER.error("Error while parsing loot table decoration entry. Found {}. Defaulting to {}", string, ExplorationMapLootFunction.DEFAULT_DECORATION);
         }

         byte b = JsonHelper.getByte(jsonObject, "zoom", (byte)2);
         int i = JsonHelper.getInt(jsonObject, "search_radius", 50);
         boolean bl = JsonHelper.getBoolean(jsonObject, "skip_existing_chunks", true);
         return new ExplorationMapLootFunction(args, lv, lv2, b, i, bl);
      }

      private static TagKey getDestination(JsonObject json) {
         if (json.has("destination")) {
            String string = JsonHelper.getString(json, "destination");
            return TagKey.of(RegistryKeys.STRUCTURE, new Identifier(string));
         } else {
            return ExplorationMapLootFunction.DEFAULT_DESTINATION;
         }
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
