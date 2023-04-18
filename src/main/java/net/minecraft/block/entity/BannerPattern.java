package net.minecraft.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Iterator;
import java.util.List;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BannerPattern {
   final String id;

   public BannerPattern(String id) {
      this.id = id;
   }

   public static Identifier getSpriteId(RegistryKey pattern, boolean banner) {
      String string = banner ? "banner" : "shield";
      return pattern.getValue().withPrefixedPath("entity/" + string + "/");
   }

   public String getId() {
      return this.id;
   }

   @Nullable
   public static RegistryEntry byId(String id) {
      return (RegistryEntry)Registries.BANNER_PATTERN.streamEntries().filter((pattern) -> {
         return ((BannerPattern)pattern.value()).id.equals(id);
      }).findAny().orElse((Object)null);
   }

   public static class Patterns {
      private final List entries = Lists.newArrayList();

      public Patterns add(RegistryKey pattern, DyeColor color) {
         return this.add((RegistryEntry)Registries.BANNER_PATTERN.entryOf(pattern), color);
      }

      public Patterns add(RegistryEntry pattern, DyeColor color) {
         return this.add(Pair.of(pattern, color));
      }

      public Patterns add(Pair pattern) {
         this.entries.add(pattern);
         return this;
      }

      public NbtList toNbt() {
         NbtList lv = new NbtList();
         Iterator var2 = this.entries.iterator();

         while(var2.hasNext()) {
            Pair pair = (Pair)var2.next();
            NbtCompound lv2 = new NbtCompound();
            lv2.putString("Pattern", ((BannerPattern)((RegistryEntry)pair.getFirst()).value()).id);
            lv2.putInt("Color", ((DyeColor)pair.getSecond()).getId());
            lv.add(lv2);
         }

         return lv;
      }
   }
}
