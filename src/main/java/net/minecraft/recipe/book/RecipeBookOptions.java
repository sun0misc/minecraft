package net.minecraft.recipe.book;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Util;

public final class RecipeBookOptions {
   private static final Map CATEGORY_OPTION_NAMES;
   private final Map categoryOptions;

   private RecipeBookOptions(Map categoryOptions) {
      this.categoryOptions = categoryOptions;
   }

   public RecipeBookOptions() {
      this((Map)Util.make(Maps.newEnumMap(RecipeBookCategory.class), (categoryOptions) -> {
         RecipeBookCategory[] var1 = RecipeBookCategory.values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            RecipeBookCategory lv = var1[var3];
            categoryOptions.put(lv, new CategoryOption(false, false));
         }

      }));
   }

   public boolean isGuiOpen(RecipeBookCategory category) {
      return ((CategoryOption)this.categoryOptions.get(category)).guiOpen;
   }

   public void setGuiOpen(RecipeBookCategory category, boolean open) {
      ((CategoryOption)this.categoryOptions.get(category)).guiOpen = open;
   }

   public boolean isFilteringCraftable(RecipeBookCategory category) {
      return ((CategoryOption)this.categoryOptions.get(category)).filteringCraftable;
   }

   public void setFilteringCraftable(RecipeBookCategory category, boolean filtering) {
      ((CategoryOption)this.categoryOptions.get(category)).filteringCraftable = filtering;
   }

   public static RecipeBookOptions fromPacket(PacketByteBuf buf) {
      Map map = Maps.newEnumMap(RecipeBookCategory.class);
      RecipeBookCategory[] var2 = RecipeBookCategory.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         RecipeBookCategory lv = var2[var4];
         boolean bl = buf.readBoolean();
         boolean bl2 = buf.readBoolean();
         map.put(lv, new CategoryOption(bl, bl2));
      }

      return new RecipeBookOptions(map);
   }

   public void toPacket(PacketByteBuf buf) {
      RecipeBookCategory[] var2 = RecipeBookCategory.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         RecipeBookCategory lv = var2[var4];
         CategoryOption lv2 = (CategoryOption)this.categoryOptions.get(lv);
         if (lv2 == null) {
            buf.writeBoolean(false);
            buf.writeBoolean(false);
         } else {
            buf.writeBoolean(lv2.guiOpen);
            buf.writeBoolean(lv2.filteringCraftable);
         }
      }

   }

   public static RecipeBookOptions fromNbt(NbtCompound nbt) {
      Map map = Maps.newEnumMap(RecipeBookCategory.class);
      CATEGORY_OPTION_NAMES.forEach((category, pair) -> {
         boolean bl = nbt.getBoolean((String)pair.getFirst());
         boolean bl2 = nbt.getBoolean((String)pair.getSecond());
         map.put(category, new CategoryOption(bl, bl2));
      });
      return new RecipeBookOptions(map);
   }

   public void writeNbt(NbtCompound nbt) {
      CATEGORY_OPTION_NAMES.forEach((category, pair) -> {
         CategoryOption lv = (CategoryOption)this.categoryOptions.get(category);
         nbt.putBoolean((String)pair.getFirst(), lv.guiOpen);
         nbt.putBoolean((String)pair.getSecond(), lv.filteringCraftable);
      });
   }

   public RecipeBookOptions copy() {
      Map map = Maps.newEnumMap(RecipeBookCategory.class);
      RecipeBookCategory[] var2 = RecipeBookCategory.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         RecipeBookCategory lv = var2[var4];
         CategoryOption lv2 = (CategoryOption)this.categoryOptions.get(lv);
         map.put(lv, lv2.copy());
      }

      return new RecipeBookOptions(map);
   }

   public void copyFrom(RecipeBookOptions other) {
      this.categoryOptions.clear();
      RecipeBookCategory[] var2 = RecipeBookCategory.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         RecipeBookCategory lv = var2[var4];
         CategoryOption lv2 = (CategoryOption)other.categoryOptions.get(lv);
         this.categoryOptions.put(lv, lv2.copy());
      }

   }

   public boolean equals(Object o) {
      return this == o || o instanceof RecipeBookOptions && this.categoryOptions.equals(((RecipeBookOptions)o).categoryOptions);
   }

   public int hashCode() {
      return this.categoryOptions.hashCode();
   }

   static {
      CATEGORY_OPTION_NAMES = ImmutableMap.of(RecipeBookCategory.CRAFTING, Pair.of("isGuiOpen", "isFilteringCraftable"), RecipeBookCategory.FURNACE, Pair.of("isFurnaceGuiOpen", "isFurnaceFilteringCraftable"), RecipeBookCategory.BLAST_FURNACE, Pair.of("isBlastingFurnaceGuiOpen", "isBlastingFurnaceFilteringCraftable"), RecipeBookCategory.SMOKER, Pair.of("isSmokerGuiOpen", "isSmokerFilteringCraftable"));
   }

   private static final class CategoryOption {
      boolean guiOpen;
      boolean filteringCraftable;

      public CategoryOption(boolean guiOpen, boolean filteringCraftable) {
         this.guiOpen = guiOpen;
         this.filteringCraftable = filteringCraftable;
      }

      public CategoryOption copy() {
         return new CategoryOption(this.guiOpen, this.filteringCraftable);
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (!(o instanceof CategoryOption)) {
            return false;
         } else {
            CategoryOption lv = (CategoryOption)o;
            return this.guiOpen == lv.guiOpen && this.filteringCraftable == lv.filteringCraftable;
         }
      }

      public int hashCode() {
         int i = this.guiOpen ? 1 : 0;
         i = 31 * i + (this.filteringCraftable ? 1 : 0);
         return i;
      }

      public String toString() {
         return "[open=" + this.guiOpen + ", filtering=" + this.filteringCraftable + "]";
      }
   }
}
