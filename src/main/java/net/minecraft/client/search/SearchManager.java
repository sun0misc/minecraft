package net.minecraft.client.search;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;

@Environment(EnvType.CLIENT)
public class SearchManager implements SynchronousResourceReloader {
   public static final Key ITEM_TOOLTIP = new Key();
   public static final Key ITEM_TAG = new Key();
   public static final Key RECIPE_OUTPUT = new Key();
   private final Map instances = new HashMap();

   public void reload(ResourceManager manager) {
      Iterator var2 = this.instances.values().iterator();

      while(var2.hasNext()) {
         Instance lv = (Instance)var2.next();
         lv.reload();
      }

   }

   public void put(Key key, ProviderGetter providerGetter) {
      this.instances.put(key, new Instance(providerGetter));
   }

   private Instance getInstance(Key key) {
      Instance lv = (Instance)this.instances.get(key);
      if (lv == null) {
         throw new IllegalStateException("Tree builder not registered");
      } else {
         return lv;
      }
   }

   public void reload(Key key, List values) {
      this.getInstance(key).reload(values);
   }

   public SearchProvider get(Key key) {
      return this.getInstance(key).provider;
   }

   @Environment(EnvType.CLIENT)
   static class Instance {
      private final ProviderGetter providerGetter;
      ReloadableSearchProvider provider = ReloadableSearchProvider.empty();

      Instance(ProviderGetter providerGetter) {
         this.providerGetter = providerGetter;
      }

      void reload(List values) {
         this.provider = (ReloadableSearchProvider)this.providerGetter.apply(values);
         this.provider.reload();
      }

      void reload() {
         this.provider.reload();
      }
   }

   @Environment(EnvType.CLIENT)
   public interface ProviderGetter extends Function {
   }

   @Environment(EnvType.CLIENT)
   public static class Key {
   }
}
