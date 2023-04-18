package net.minecraft.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public interface RecipeSerializer {
   RecipeSerializer SHAPED = register("crafting_shaped", new ShapedRecipe.Serializer());
   RecipeSerializer SHAPELESS = register("crafting_shapeless", new ShapelessRecipe.Serializer());
   RecipeSerializer ARMOR_DYE = register("crafting_special_armordye", new SpecialRecipeSerializer(ArmorDyeRecipe::new));
   RecipeSerializer BOOK_CLONING = register("crafting_special_bookcloning", new SpecialRecipeSerializer(BookCloningRecipe::new));
   RecipeSerializer MAP_CLONING = register("crafting_special_mapcloning", new SpecialRecipeSerializer(MapCloningRecipe::new));
   RecipeSerializer MAP_EXTENDING = register("crafting_special_mapextending", new SpecialRecipeSerializer(MapExtendingRecipe::new));
   RecipeSerializer FIREWORK_ROCKET = register("crafting_special_firework_rocket", new SpecialRecipeSerializer(FireworkRocketRecipe::new));
   RecipeSerializer FIREWORK_STAR = register("crafting_special_firework_star", new SpecialRecipeSerializer(FireworkStarRecipe::new));
   RecipeSerializer FIREWORK_STAR_FADE = register("crafting_special_firework_star_fade", new SpecialRecipeSerializer(FireworkStarFadeRecipe::new));
   RecipeSerializer TIPPED_ARROW = register("crafting_special_tippedarrow", new SpecialRecipeSerializer(TippedArrowRecipe::new));
   RecipeSerializer BANNER_DUPLICATE = register("crafting_special_bannerduplicate", new SpecialRecipeSerializer(BannerDuplicateRecipe::new));
   RecipeSerializer SHIELD_DECORATION = register("crafting_special_shielddecoration", new SpecialRecipeSerializer(ShieldDecorationRecipe::new));
   RecipeSerializer SHULKER_BOX = register("crafting_special_shulkerboxcoloring", new SpecialRecipeSerializer(ShulkerBoxColoringRecipe::new));
   RecipeSerializer SUSPICIOUS_STEW = register("crafting_special_suspiciousstew", new SpecialRecipeSerializer(SuspiciousStewRecipe::new));
   RecipeSerializer REPAIR_ITEM = register("crafting_special_repairitem", new SpecialRecipeSerializer(RepairItemRecipe::new));
   RecipeSerializer SMELTING = register("smelting", new CookingRecipeSerializer(SmeltingRecipe::new, 200));
   RecipeSerializer BLASTING = register("blasting", new CookingRecipeSerializer(BlastingRecipe::new, 100));
   RecipeSerializer SMOKING = register("smoking", new CookingRecipeSerializer(SmokingRecipe::new, 100));
   RecipeSerializer CAMPFIRE_COOKING = register("campfire_cooking", new CookingRecipeSerializer(CampfireCookingRecipe::new, 100));
   RecipeSerializer STONECUTTING = register("stonecutting", new CuttingRecipe.Serializer(StonecuttingRecipe::new));
   RecipeSerializer SMITHING_TRANSFORM = register("smithing_transform", new SmithingTransformRecipe.Serializer());
   RecipeSerializer SMITHING_TRIM = register("smithing_trim", new SmithingTrimRecipe.Serializer());
   RecipeSerializer CRAFTING_DECORATED_POT = register("crafting_decorated_pot", new SpecialRecipeSerializer(CraftingDecoratedPotRecipe::new));

   Recipe read(Identifier id, JsonObject json);

   Recipe read(Identifier id, PacketByteBuf buf);

   void write(PacketByteBuf buf, Recipe recipe);

   static RecipeSerializer register(String id, RecipeSerializer serializer) {
      return (RecipeSerializer)Registry.register(Registries.RECIPE_SERIALIZER, (String)id, serializer);
   }
}
