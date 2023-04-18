package net.minecraft.data.client;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ModelProvider implements DataProvider {
   private final DataOutput.PathResolver blockstatesPathResolver;
   private final DataOutput.PathResolver modelsPathResolver;

   public ModelProvider(DataOutput output) {
      this.blockstatesPathResolver = output.getResolver(DataOutput.OutputType.RESOURCE_PACK, "blockstates");
      this.modelsPathResolver = output.getResolver(DataOutput.OutputType.RESOURCE_PACK, "models");
   }

   public CompletableFuture run(DataWriter writer) {
      Map map = Maps.newHashMap();
      Consumer consumer = (blockStateSupplier) -> {
         Block lv = blockStateSupplier.getBlock();
         BlockStateSupplier lv2 = (BlockStateSupplier)map.put(lv, blockStateSupplier);
         if (lv2 != null) {
            throw new IllegalStateException("Duplicate blockstate definition for " + lv);
         }
      };
      Map map2 = Maps.newHashMap();
      Set set = Sets.newHashSet();
      BiConsumer biConsumer = (id, jsonSupplier) -> {
         Supplier supplier2 = (Supplier)map2.put(id, jsonSupplier);
         if (supplier2 != null) {
            throw new IllegalStateException("Duplicate model definition for " + id);
         }
      };
      Objects.requireNonNull(set);
      Consumer consumer2 = set::add;
      (new BlockStateModelGenerator(consumer, biConsumer, consumer2)).register();
      (new ItemModelGenerator(biConsumer)).register();
      List list = Registries.BLOCK.stream().filter((block) -> {
         return !map.containsKey(block);
      }).toList();
      if (!list.isEmpty()) {
         throw new IllegalStateException("Missing blockstate definitions for: " + list);
      } else {
         Registries.BLOCK.forEach((block) -> {
            Item lv = (Item)Item.BLOCK_ITEMS.get(block);
            if (lv != null) {
               if (set.contains(lv)) {
                  return;
               }

               Identifier lv2 = ModelIds.getItemModelId(lv);
               if (!map2.containsKey(lv2)) {
                  map2.put(lv2, new SimpleModelSupplier(ModelIds.getBlockModelId(block)));
               }
            }

         });
         CompletableFuture[] var10000 = new CompletableFuture[]{this.writeJsons(writer, map, (block) -> {
            return this.blockstatesPathResolver.resolveJson(block.getRegistryEntry().registryKey().getValue());
         }), null};
         DataOutput.PathResolver var10006 = this.modelsPathResolver;
         Objects.requireNonNull(var10006);
         var10000[1] = this.writeJsons(writer, map2, var10006::resolveJson);
         return CompletableFuture.allOf(var10000);
      }
   }

   private CompletableFuture writeJsons(DataWriter cache, Map models, Function pathGetter) {
      return CompletableFuture.allOf((CompletableFuture[])models.entrySet().stream().map((entry) -> {
         Path path = (Path)pathGetter.apply(entry.getKey());
         JsonElement jsonElement = (JsonElement)((Supplier)entry.getValue()).get();
         return DataProvider.writeToPath(cache, jsonElement, path);
      }).toArray((i) -> {
         return new CompletableFuture[i];
      }));
   }

   public final String getName() {
      return "Model Definitions";
   }
}
