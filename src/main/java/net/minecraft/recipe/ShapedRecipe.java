package net.minecraft.recipe;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class ShapedRecipe implements CraftingRecipe {
   final int width;
   final int height;
   final DefaultedList input;
   final ItemStack output;
   private final Identifier id;
   final String group;
   final CraftingRecipeCategory category;
   final boolean showNotification;

   public ShapedRecipe(Identifier id, String group, CraftingRecipeCategory category, int width, int height, DefaultedList input, ItemStack output, boolean showNotification) {
      this.id = id;
      this.group = group;
      this.category = category;
      this.width = width;
      this.height = height;
      this.input = input;
      this.output = output;
      this.showNotification = showNotification;
   }

   public ShapedRecipe(Identifier id, String group, CraftingRecipeCategory category, int width, int height, DefaultedList input, ItemStack output) {
      this(id, group, category, width, height, input, output, true);
   }

   public Identifier getId() {
      return this.id;
   }

   public RecipeSerializer getSerializer() {
      return RecipeSerializer.SHAPED;
   }

   public String getGroup() {
      return this.group;
   }

   public CraftingRecipeCategory getCategory() {
      return this.category;
   }

   public ItemStack getOutput(DynamicRegistryManager registryManager) {
      return this.output;
   }

   public DefaultedList getIngredients() {
      return this.input;
   }

   public boolean showNotification() {
      return this.showNotification;
   }

   public boolean fits(int width, int height) {
      return width >= this.width && height >= this.height;
   }

   public boolean matches(CraftingInventory arg, World arg2) {
      for(int i = 0; i <= arg.getWidth() - this.width; ++i) {
         for(int j = 0; j <= arg.getHeight() - this.height; ++j) {
            if (this.matchesPattern(arg, i, j, true)) {
               return true;
            }

            if (this.matchesPattern(arg, i, j, false)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean matchesPattern(CraftingInventory inv, int offsetX, int offsetY, boolean flipped) {
      for(int k = 0; k < inv.getWidth(); ++k) {
         for(int l = 0; l < inv.getHeight(); ++l) {
            int m = k - offsetX;
            int n = l - offsetY;
            Ingredient lv = Ingredient.EMPTY;
            if (m >= 0 && n >= 0 && m < this.width && n < this.height) {
               if (flipped) {
                  lv = (Ingredient)this.input.get(this.width - m - 1 + n * this.width);
               } else {
                  lv = (Ingredient)this.input.get(m + n * this.width);
               }
            }

            if (!lv.test(inv.getStack(k + l * inv.getWidth()))) {
               return false;
            }
         }
      }

      return true;
   }

   public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
      return this.getOutput(arg2).copy();
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   static DefaultedList createPatternMatrix(String[] pattern, Map symbols, int width, int height) {
      DefaultedList lv = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
      Set set = Sets.newHashSet(symbols.keySet());
      set.remove(" ");

      for(int k = 0; k < pattern.length; ++k) {
         for(int l = 0; l < pattern[k].length(); ++l) {
            String string = pattern[k].substring(l, l + 1);
            Ingredient lv2 = (Ingredient)symbols.get(string);
            if (lv2 == null) {
               throw new JsonSyntaxException("Pattern references symbol '" + string + "' but it's not defined in the key");
            }

            set.remove(string);
            lv.set(l + width * k, lv2);
         }
      }

      if (!set.isEmpty()) {
         throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + set);
      } else {
         return lv;
      }
   }

   @VisibleForTesting
   static String[] removePadding(String... pattern) {
      int i = Integer.MAX_VALUE;
      int j = 0;
      int k = 0;
      int l = 0;

      for(int m = 0; m < pattern.length; ++m) {
         String string = pattern[m];
         i = Math.min(i, findFirstSymbol(string));
         int n = findLastSymbol(string);
         j = Math.max(j, n);
         if (n < 0) {
            if (k == m) {
               ++k;
            }

            ++l;
         } else {
            l = 0;
         }
      }

      if (pattern.length == l) {
         return new String[0];
      } else {
         String[] strings2 = new String[pattern.length - l - k];

         for(int o = 0; o < strings2.length; ++o) {
            strings2[o] = pattern[o + k].substring(i, j + 1);
         }

         return strings2;
      }
   }

   public boolean isEmpty() {
      DefaultedList lv = this.getIngredients();
      return lv.isEmpty() || lv.stream().filter((ingredient) -> {
         return !ingredient.isEmpty();
      }).anyMatch((ingredient) -> {
         return ingredient.getMatchingStacks().length == 0;
      });
   }

   private static int findFirstSymbol(String line) {
      int i;
      for(i = 0; i < line.length() && line.charAt(i) == ' '; ++i) {
      }

      return i;
   }

   private static int findLastSymbol(String pattern) {
      int i;
      for(i = pattern.length() - 1; i >= 0 && pattern.charAt(i) == ' '; --i) {
      }

      return i;
   }

   static String[] getPattern(JsonArray json) {
      String[] strings = new String[json.size()];
      if (strings.length > 3) {
         throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
      } else if (strings.length == 0) {
         throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
      } else {
         for(int i = 0; i < strings.length; ++i) {
            String string = JsonHelper.asString(json.get(i), "pattern[" + i + "]");
            if (string.length() > 3) {
               throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
            }

            if (i > 0 && strings[0].length() != string.length()) {
               throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
            }

            strings[i] = string;
         }

         return strings;
      }
   }

   static Map readSymbols(JsonObject json) {
      Map map = Maps.newHashMap();
      Iterator var2 = json.entrySet().iterator();

      while(var2.hasNext()) {
         Map.Entry entry = (Map.Entry)var2.next();
         if (((String)entry.getKey()).length() != 1) {
            throw new JsonSyntaxException("Invalid key entry: '" + (String)entry.getKey() + "' is an invalid symbol (must be 1 character only).");
         }

         if (" ".equals(entry.getKey())) {
            throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
         }

         map.put((String)entry.getKey(), Ingredient.fromJson((JsonElement)entry.getValue()));
      }

      map.put(" ", Ingredient.EMPTY);
      return map;
   }

   public static ItemStack outputFromJson(JsonObject json) {
      Item lv = getItem(json);
      if (json.has("data")) {
         throw new JsonParseException("Disallowed data tag found");
      } else {
         int i = JsonHelper.getInt(json, "count", 1);
         if (i < 1) {
            throw new JsonSyntaxException("Invalid output count: " + i);
         } else {
            return new ItemStack(lv, i);
         }
      }
   }

   public static Item getItem(JsonObject json) {
      String string = JsonHelper.getString(json, "item");
      Item lv = (Item)Registries.ITEM.getOrEmpty(new Identifier(string)).orElseThrow(() -> {
         return new JsonSyntaxException("Unknown item '" + string + "'");
      });
      if (lv == Items.AIR) {
         throw new JsonSyntaxException("Invalid item: " + string);
      } else {
         return lv;
      }
   }

   public static class Serializer implements RecipeSerializer {
      public ShapedRecipe read(Identifier arg, JsonObject jsonObject) {
         String string = JsonHelper.getString(jsonObject, "group", "");
         CraftingRecipeCategory lv = (CraftingRecipeCategory)CraftingRecipeCategory.CODEC.byId(JsonHelper.getString(jsonObject, "category", (String)null), CraftingRecipeCategory.MISC);
         Map map = ShapedRecipe.readSymbols(JsonHelper.getObject(jsonObject, "key"));
         String[] strings = ShapedRecipe.removePadding(ShapedRecipe.getPattern(JsonHelper.getArray(jsonObject, "pattern")));
         int i = strings[0].length();
         int j = strings.length;
         DefaultedList lv2 = ShapedRecipe.createPatternMatrix(strings, map, i, j);
         ItemStack lv3 = ShapedRecipe.outputFromJson(JsonHelper.getObject(jsonObject, "result"));
         boolean bl = JsonHelper.getBoolean(jsonObject, "show_notification", true);
         return new ShapedRecipe(arg, string, lv, i, j, lv2, lv3, bl);
      }

      public ShapedRecipe read(Identifier arg, PacketByteBuf arg2) {
         int i = arg2.readVarInt();
         int j = arg2.readVarInt();
         String string = arg2.readString();
         CraftingRecipeCategory lv = (CraftingRecipeCategory)arg2.readEnumConstant(CraftingRecipeCategory.class);
         DefaultedList lv2 = DefaultedList.ofSize(i * j, Ingredient.EMPTY);

         for(int k = 0; k < lv2.size(); ++k) {
            lv2.set(k, Ingredient.fromPacket(arg2));
         }

         ItemStack lv3 = arg2.readItemStack();
         boolean bl = arg2.readBoolean();
         return new ShapedRecipe(arg, string, lv, i, j, lv2, lv3, bl);
      }

      public void write(PacketByteBuf arg, ShapedRecipe arg2) {
         arg.writeVarInt(arg2.width);
         arg.writeVarInt(arg2.height);
         arg.writeString(arg2.group);
         arg.writeEnumConstant(arg2.category);
         Iterator var3 = arg2.input.iterator();

         while(var3.hasNext()) {
            Ingredient lv = (Ingredient)var3.next();
            lv.write(arg);
         }

         arg.writeItemStack(arg2.output);
         arg.writeBoolean(arg2.showNotification);
      }

      // $FF: synthetic method
      public Recipe read(Identifier id, PacketByteBuf buf) {
         return this.read(id, buf);
      }

      // $FF: synthetic method
      public Recipe read(Identifier id, JsonObject json) {
         return this.read(id, json);
      }
   }
}
