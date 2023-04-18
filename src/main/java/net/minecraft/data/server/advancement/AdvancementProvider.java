package net.minecraft.data.server.advancement;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;

public class AdvancementProvider implements DataProvider {
   private final DataOutput.PathResolver pathResolver;
   private final List tabGenerators;
   private final CompletableFuture registryLookupFuture;

   public AdvancementProvider(DataOutput output, CompletableFuture registryLookupFuture, List tabGenerators) {
      this.pathResolver = output.getResolver(DataOutput.OutputType.DATA_PACK, "advancements");
      this.tabGenerators = tabGenerators;
      this.registryLookupFuture = registryLookupFuture;
   }

   public CompletableFuture run(DataWriter writer) {
      return this.registryLookupFuture.thenCompose((lookup) -> {
         Set set = new HashSet();
         List list = new ArrayList();
         Consumer consumer = (advancement) -> {
            if (!set.add(advancement.getId())) {
               throw new IllegalStateException("Duplicate advancement " + advancement.getId());
            } else {
               Path path = this.pathResolver.resolveJson(advancement.getId());
               list.add(DataProvider.writeToPath(writer, advancement.createTask().toJson(), path));
            }
         };
         Iterator var6 = this.tabGenerators.iterator();

         while(var6.hasNext()) {
            AdvancementTabGenerator lv = (AdvancementTabGenerator)var6.next();
            lv.accept(lookup, consumer);
         }

         return CompletableFuture.allOf((CompletableFuture[])list.toArray((i) -> {
            return new CompletableFuture[i];
         }));
      });
   }

   public final String getName() {
      return "Advancements";
   }
}
