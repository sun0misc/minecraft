package net.minecraft.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class RecipeManager extends JsonDataLoader {
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
   private static final Logger LOGGER = LogUtils.getLogger();
   private Map recipes = ImmutableMap.of();
   private Map recipesById = ImmutableMap.of();
   private boolean errored;

   public RecipeManager() {
      super(GSON, "recipes");
   }

   protected void apply(Map map, ResourceManager arg, Profiler arg2) {
      this.errored = false;
      Map map2 = Maps.newHashMap();
      ImmutableMap.Builder builder = ImmutableMap.builder();
      Iterator var6 = map.entrySet().iterator();

      while(var6.hasNext()) {
         Map.Entry entry = (Map.Entry)var6.next();
         Identifier lv = (Identifier)entry.getKey();

         try {
            Recipe lv2 = deserialize(lv, JsonHelper.asObject((JsonElement)entry.getValue(), "top element"));
            ((ImmutableMap.Builder)map2.computeIfAbsent(lv2.getType(), (recipeType) -> {
               return ImmutableMap.builder();
            })).put(lv, lv2);
            builder.put(lv, lv2);
         } catch (IllegalArgumentException | JsonParseException var10) {
            LOGGER.error("Parsing error loading recipe {}", lv, var10);
         }
      }

      this.recipes = (Map)map2.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (entryx) -> {
         return ((ImmutableMap.Builder)entryx.getValue()).build();
      }));
      this.recipesById = builder.build();
      LOGGER.info("Loaded {} recipes", map2.size());
   }

   public boolean isErrored() {
      return this.errored;
   }

   public Optional getFirstMatch(RecipeType type, Inventory inventory, World world) {
      return this.getAllOfType(type).values().stream().filter((recipe) -> {
         return recipe.matches(inventory, world);
      }).findFirst();
   }

   public Optional getFirstMatch(RecipeType type, Inventory inventory, World world, @Nullable Identifier id) {
      Map map = this.getAllOfType(type);
      if (id != null) {
         Recipe lv = (Recipe)map.get(id);
         if (lv != null && lv.matches(inventory, world)) {
            return Optional.of(Pair.of(id, lv));
         }
      }

      return map.entrySet().stream().filter((entry) -> {
         return ((Recipe)entry.getValue()).matches(inventory, world);
      }).findFirst().map((entry) -> {
         return Pair.of((Identifier)entry.getKey(), (Recipe)entry.getValue());
      });
   }

   public List listAllOfType(RecipeType type) {
      return List.copyOf(this.getAllOfType(type).values());
   }

   public List getAllMatches(RecipeType type, Inventory inventory, World world) {
      return (List)this.getAllOfType(type).values().stream().filter((recipe) -> {
         return recipe.matches(inventory, world);
      }).sorted(Comparator.comparing((recipe) -> {
         return recipe.getOutput(world.getRegistryManager()).getTranslationKey();
      })).collect(Collectors.toList());
   }

   private Map getAllOfType(RecipeType type) {
      return (Map)this.recipes.getOrDefault(type, Collections.emptyMap());
   }

   public DefaultedList getRemainingStacks(RecipeType type, Inventory inventory, World world) {
      Optional optional = this.getFirstMatch(type, inventory, world);
      if (optional.isPresent()) {
         return ((Recipe)optional.get()).getRemainder(inventory);
      } else {
         DefaultedList lv = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);

         for(int i = 0; i < lv.size(); ++i) {
            lv.set(i, inventory.getStack(i));
         }

         return lv;
      }
   }

   public Optional get(Identifier id) {
      return Optional.ofNullable((Recipe)this.recipesById.get(id));
   }

   public Collection values() {
      return (Collection)this.recipes.values().stream().flatMap((map) -> {
         return map.values().stream();
      }).collect(Collectors.toSet());
   }

   public Stream keys() {
      return this.recipes.values().stream().flatMap((map) -> {
         return map.keySet().stream();
      });
   }

   public static Recipe deserialize(Identifier id, JsonObject json) {
      String string = JsonHelper.getString(json, "type");
      return ((RecipeSerializer)Registries.RECIPE_SERIALIZER.getOrEmpty(new Identifier(string)).orElseThrow(() -> {
         return new JsonSyntaxException("Invalid or unsupported recipe type '" + string + "'");
      })).read(id, json);
   }

   public void setRecipes(Iterable recipes) {
      this.errored = false;
      Map map = Maps.newHashMap();
      ImmutableMap.Builder builder = ImmutableMap.builder();
      recipes.forEach((recipe) -> {
         Map map2 = (Map)map.computeIfAbsent(recipe.getType(), (t) -> {
            return Maps.newHashMap();
         });
         Identifier lv = recipe.getId();
         Recipe lv2 = (Recipe)map2.put(lv, recipe);
         builder.put(lv, recipe);
         if (lv2 != null) {
            throw new IllegalStateException("Duplicate recipe ignored with ID " + lv);
         }
      });
      this.recipes = ImmutableMap.copyOf(map);
      this.recipesById = builder.build();
   }

   public static MatchGetter createCachedMatchGetter(final RecipeType type) {
      return new MatchGetter() {
         @Nullable
         private Identifier id;

         public Optional getFirstMatch(Inventory inventory, World world) {
            RecipeManager lv = world.getRecipeManager();
            Optional optional = lv.getFirstMatch(type, inventory, world, this.id);
            if (optional.isPresent()) {
               Pair pair = (Pair)optional.get();
               this.id = (Identifier)pair.getFirst();
               return Optional.of((Recipe)pair.getSecond());
            } else {
               return Optional.empty();
            }
         }
      };
   }

   public interface MatchGetter {
      Optional getFirstMatch(Inventory inventory, World world);
   }
}
