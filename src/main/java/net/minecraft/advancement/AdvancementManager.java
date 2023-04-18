package net.minecraft.advancement;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class AdvancementManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Map advancements = Maps.newHashMap();
   private final Set roots = Sets.newLinkedHashSet();
   private final Set dependents = Sets.newLinkedHashSet();
   @Nullable
   private Listener listener;

   private void remove(Advancement advancement) {
      Iterator var2 = advancement.getChildren().iterator();

      while(var2.hasNext()) {
         Advancement lv = (Advancement)var2.next();
         this.remove(lv);
      }

      LOGGER.info("Forgot about advancement {}", advancement.getId());
      this.advancements.remove(advancement.getId());
      if (advancement.getParent() == null) {
         this.roots.remove(advancement);
         if (this.listener != null) {
            this.listener.onRootRemoved(advancement);
         }
      } else {
         this.dependents.remove(advancement);
         if (this.listener != null) {
            this.listener.onDependentRemoved(advancement);
         }
      }

   }

   public void removeAll(Set advancements) {
      Iterator var2 = advancements.iterator();

      while(var2.hasNext()) {
         Identifier lv = (Identifier)var2.next();
         Advancement lv2 = (Advancement)this.advancements.get(lv);
         if (lv2 == null) {
            LOGGER.warn("Told to remove advancement {} but I don't know what that is", lv);
         } else {
            this.remove(lv2);
         }
      }

   }

   public void load(Map advancements) {
      Map map2 = Maps.newHashMap(advancements);

      label42:
      while(!map2.isEmpty()) {
         boolean bl = false;
         Iterator iterator = map2.entrySet().iterator();

         Map.Entry entry;
         while(iterator.hasNext()) {
            entry = (Map.Entry)iterator.next();
            Identifier lv = (Identifier)entry.getKey();
            Advancement.Builder lv2 = (Advancement.Builder)entry.getValue();
            Map var10001 = this.advancements;
            Objects.requireNonNull(var10001);
            if (lv2.findParent(var10001::get)) {
               Advancement lv3 = lv2.build(lv);
               this.advancements.put(lv, lv3);
               bl = true;
               iterator.remove();
               if (lv3.getParent() == null) {
                  this.roots.add(lv3);
                  if (this.listener != null) {
                     this.listener.onRootAdded(lv3);
                  }
               } else {
                  this.dependents.add(lv3);
                  if (this.listener != null) {
                     this.listener.onDependentAdded(lv3);
                  }
               }
            }
         }

         if (!bl) {
            iterator = map2.entrySet().iterator();

            while(true) {
               if (!iterator.hasNext()) {
                  break label42;
               }

               entry = (Map.Entry)iterator.next();
               LOGGER.error("Couldn't load advancement {}: {}", entry.getKey(), entry.getValue());
            }
         }
      }

      LOGGER.info("Loaded {} advancements", this.advancements.size());
   }

   public void clear() {
      this.advancements.clear();
      this.roots.clear();
      this.dependents.clear();
      if (this.listener != null) {
         this.listener.onClear();
      }

   }

   public Iterable getRoots() {
      return this.roots;
   }

   public Collection getAdvancements() {
      return this.advancements.values();
   }

   @Nullable
   public Advancement get(Identifier id) {
      return (Advancement)this.advancements.get(id);
   }

   public void setListener(@Nullable Listener listener) {
      this.listener = listener;
      if (listener != null) {
         Iterator var2 = this.roots.iterator();

         Advancement lv;
         while(var2.hasNext()) {
            lv = (Advancement)var2.next();
            listener.onRootAdded(lv);
         }

         var2 = this.dependents.iterator();

         while(var2.hasNext()) {
            lv = (Advancement)var2.next();
            listener.onDependentAdded(lv);
         }
      }

   }

   public interface Listener {
      void onRootAdded(Advancement root);

      void onRootRemoved(Advancement root);

      void onDependentAdded(Advancement dependent);

      void onDependentRemoved(Advancement dependent);

      void onClear();
   }
}
