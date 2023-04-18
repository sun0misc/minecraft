package net.minecraft.data.server.loottable;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.loot.LootDataKey;
import net.minecraft.loot.LootDataLookup;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class LootTableProvider implements DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final DataOutput.PathResolver pathResolver;
   private final Set lootTableIds;
   private final List lootTypeGenerators;

   public LootTableProvider(DataOutput output, Set lootTableIds, List lootTypeGenerators) {
      this.pathResolver = output.getResolver(DataOutput.OutputType.DATA_PACK, "loot_tables");
      this.lootTypeGenerators = lootTypeGenerators;
      this.lootTableIds = lootTableIds;
   }

   public CompletableFuture run(DataWriter writer) {
      final Map map = Maps.newHashMap();
      this.lootTypeGenerators.forEach((lootTypeGenerator) -> {
         ((LootTableGenerator)lootTypeGenerator.provider().get()).accept((id, builder) -> {
            if (map.put(id, builder.type(lootTypeGenerator.paramSet).build()) != null) {
               throw new IllegalStateException("Duplicate loot table " + id);
            }
         });
      });
      LootTableReporter lv = new LootTableReporter(LootContextTypes.GENERIC, new LootDataLookup() {
         @Nullable
         public Object getElement(LootDataKey arg) {
            return arg.type() == LootDataType.LOOT_TABLES ? map.get(arg.id()) : null;
         }
      });
      Set set = Sets.difference(this.lootTableIds, map.keySet());
      Iterator var5 = set.iterator();

      while(var5.hasNext()) {
         Identifier lv2 = (Identifier)var5.next();
         lv.report("Missing built-in table: " + lv2);
      }

      map.forEach((id, table) -> {
         table.validate(lv.withContextType(table.getType()).makeChild("{" + id + "}", new LootDataKey(LootDataType.LOOT_TABLES, id)));
      });
      Multimap multimap = lv.getMessages();
      if (!multimap.isEmpty()) {
         multimap.forEach((name, message) -> {
            LOGGER.warn("Found validation problem in {}: {}", name, message);
         });
         throw new IllegalStateException("Failed to validate loot tables, see logs");
      } else {
         return CompletableFuture.allOf((CompletableFuture[])map.entrySet().stream().map((entry) -> {
            Identifier lv = (Identifier)entry.getKey();
            LootTable lv2 = (LootTable)entry.getValue();
            Path path = this.pathResolver.resolveJson(lv);
            return DataProvider.writeToPath(writer, LootDataType.LOOT_TABLES.getGson().toJsonTree(lv2), path);
         }).toArray((i) -> {
            return new CompletableFuture[i];
         }));
      }
   }

   public final String getName() {
      return "Loot Tables";
   }

   public static record LootTypeGenerator(Supplier provider, LootContextType paramSet) {
      final LootContextType paramSet;

      public LootTypeGenerator(Supplier supplier, LootContextType arg) {
         this.provider = supplier;
         this.paramSet = arg;
      }

      public Supplier provider() {
         return this.provider;
      }

      public LootContextType paramSet() {
         return this.paramSet;
      }
   }
}
