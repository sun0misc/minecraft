package net.minecraft.stat;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class StatType implements Iterable {
   private final Registry registry;
   private final Map stats = new IdentityHashMap();
   @Nullable
   private Text name;

   public StatType(Registry registry) {
      this.registry = registry;
   }

   public boolean hasStat(Object key) {
      return this.stats.containsKey(key);
   }

   public Stat getOrCreateStat(Object key, StatFormatter formatter) {
      return (Stat)this.stats.computeIfAbsent(key, (value) -> {
         return new Stat(this, value, formatter);
      });
   }

   public Registry getRegistry() {
      return this.registry;
   }

   public Iterator iterator() {
      return this.stats.values().iterator();
   }

   public Stat getOrCreateStat(Object key) {
      return this.getOrCreateStat(key, StatFormatter.DEFAULT);
   }

   public String getTranslationKey() {
      String var10000 = Registries.STAT_TYPE.getId(this).toString();
      return "stat_type." + var10000.replace(':', '.');
   }

   public Text getName() {
      if (this.name == null) {
         this.name = Text.translatable(this.getTranslationKey());
      }

      return this.name;
   }
}
