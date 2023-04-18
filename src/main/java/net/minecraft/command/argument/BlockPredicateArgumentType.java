package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.Nullable;

public class BlockPredicateArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "#stone", "#stone[foo=bar]{baz=nbt}");
   private final RegistryWrapper registryWrapper;

   public BlockPredicateArgumentType(CommandRegistryAccess commandRegistryAccess) {
      this.registryWrapper = commandRegistryAccess.createWrapper(RegistryKeys.BLOCK);
   }

   public static BlockPredicateArgumentType blockPredicate(CommandRegistryAccess commandRegistryAccess) {
      return new BlockPredicateArgumentType(commandRegistryAccess);
   }

   public BlockPredicate parse(StringReader stringReader) throws CommandSyntaxException {
      return parse(this.registryWrapper, stringReader);
   }

   public static BlockPredicate parse(RegistryWrapper registryWrapper, StringReader reader) throws CommandSyntaxException {
      return (BlockPredicate)BlockArgumentParser.blockOrTag(registryWrapper, reader, true).map((result) -> {
         return new StatePredicate(result.blockState(), result.properties().keySet(), result.nbt());
      }, (result) -> {
         return new TagPredicate(result.tag(), result.vagueProperties(), result.nbt());
      });
   }

   public static Predicate getBlockPredicate(CommandContext context, String name) throws CommandSyntaxException {
      return (Predicate)context.getArgument(name, BlockPredicate.class);
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      return BlockArgumentParser.getSuggestions(this.registryWrapper, builder, true, true);
   }

   public Collection getExamples() {
      return EXAMPLES;
   }

   // $FF: synthetic method
   public Object parse(StringReader reader) throws CommandSyntaxException {
      return this.parse(reader);
   }

   public interface BlockPredicate extends Predicate {
      boolean hasNbt();
   }

   static class TagPredicate implements BlockPredicate {
      private final RegistryEntryList tag;
      @Nullable
      private final NbtCompound nbt;
      private final Map properties;

      TagPredicate(RegistryEntryList tag, Map properties, @Nullable NbtCompound nbt) {
         this.tag = tag;
         this.properties = properties;
         this.nbt = nbt;
      }

      public boolean test(CachedBlockPosition arg) {
         BlockState lv = arg.getBlockState();
         if (!lv.isIn(this.tag)) {
            return false;
         } else {
            Iterator var3 = this.properties.entrySet().iterator();

            while(var3.hasNext()) {
               Map.Entry entry = (Map.Entry)var3.next();
               Property lv2 = lv.getBlock().getStateManager().getProperty((String)entry.getKey());
               if (lv2 == null) {
                  return false;
               }

               Comparable comparable = (Comparable)lv2.parse((String)entry.getValue()).orElse((Object)null);
               if (comparable == null) {
                  return false;
               }

               if (lv.get(lv2) != comparable) {
                  return false;
               }
            }

            if (this.nbt == null) {
               return true;
            } else {
               BlockEntity lv3 = arg.getBlockEntity();
               return lv3 != null && NbtHelper.matches(this.nbt, lv3.createNbtWithIdentifyingData(), true);
            }
         }
      }

      public boolean hasNbt() {
         return this.nbt != null;
      }

      // $FF: synthetic method
      public boolean test(Object context) {
         return this.test((CachedBlockPosition)context);
      }
   }

   static class StatePredicate implements BlockPredicate {
      private final BlockState state;
      private final Set properties;
      @Nullable
      private final NbtCompound nbt;

      public StatePredicate(BlockState state, Set properties, @Nullable NbtCompound nbt) {
         this.state = state;
         this.properties = properties;
         this.nbt = nbt;
      }

      public boolean test(CachedBlockPosition arg) {
         BlockState lv = arg.getBlockState();
         if (!lv.isOf(this.state.getBlock())) {
            return false;
         } else {
            Iterator var3 = this.properties.iterator();

            while(var3.hasNext()) {
               Property lv2 = (Property)var3.next();
               if (lv.get(lv2) != this.state.get(lv2)) {
                  return false;
               }
            }

            if (this.nbt == null) {
               return true;
            } else {
               BlockEntity lv3 = arg.getBlockEntity();
               return lv3 != null && NbtHelper.matches(this.nbt, lv3.createNbtWithIdentifyingData(), true);
            }
         }
      }

      public boolean hasNbt() {
         return this.nbt != null;
      }

      // $FF: synthetic method
      public boolean test(Object context) {
         return this.test((CachedBlockPosition)context);
      }
   }
}
