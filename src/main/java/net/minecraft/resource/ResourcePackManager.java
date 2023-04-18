package net.minecraft.resource;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.jetbrains.annotations.Nullable;

public class ResourcePackManager {
   private final Set providers;
   private Map profiles = ImmutableMap.of();
   private List enabled = ImmutableList.of();

   public ResourcePackManager(ResourcePackProvider... providers) {
      this.providers = ImmutableSet.copyOf(providers);
   }

   public void scanPacks() {
      List list = (List)this.enabled.stream().map(ResourcePackProfile::getName).collect(ImmutableList.toImmutableList());
      this.profiles = this.providePackProfiles();
      this.enabled = this.buildEnabledProfiles(list);
   }

   private Map providePackProfiles() {
      Map map = Maps.newTreeMap();
      Iterator var2 = this.providers.iterator();

      while(var2.hasNext()) {
         ResourcePackProvider lv = (ResourcePackProvider)var2.next();
         lv.register((profile) -> {
            map.put(profile.getName(), profile);
         });
      }

      return ImmutableMap.copyOf(map);
   }

   public void setEnabledProfiles(Collection enabled) {
      this.enabled = this.buildEnabledProfiles(enabled);
   }

   public boolean enable(String profile) {
      ResourcePackProfile lv = (ResourcePackProfile)this.profiles.get(profile);
      if (lv != null && !this.enabled.contains(lv)) {
         List list = Lists.newArrayList(this.enabled);
         list.add(lv);
         this.enabled = list;
         return true;
      } else {
         return false;
      }
   }

   public boolean disable(String profile) {
      ResourcePackProfile lv = (ResourcePackProfile)this.profiles.get(profile);
      if (lv != null && this.enabled.contains(lv)) {
         List list = Lists.newArrayList(this.enabled);
         list.remove(lv);
         this.enabled = list;
         return true;
      } else {
         return false;
      }
   }

   private List buildEnabledProfiles(Collection enabledNames) {
      List list = (List)this.streamProfilesByName(enabledNames).collect(Collectors.toList());
      Iterator var3 = this.profiles.values().iterator();

      while(var3.hasNext()) {
         ResourcePackProfile lv = (ResourcePackProfile)var3.next();
         if (lv.isAlwaysEnabled() && !list.contains(lv)) {
            lv.getInitialPosition().insert(list, lv, Functions.identity(), false);
         }
      }

      return ImmutableList.copyOf(list);
   }

   private Stream streamProfilesByName(Collection names) {
      Stream var10000 = names.stream();
      Map var10001 = this.profiles;
      Objects.requireNonNull(var10001);
      return var10000.map(var10001::get).filter(Objects::nonNull);
   }

   public Collection getNames() {
      return this.profiles.keySet();
   }

   public Collection getProfiles() {
      return this.profiles.values();
   }

   public Collection getEnabledNames() {
      return (Collection)this.enabled.stream().map(ResourcePackProfile::getName).collect(ImmutableSet.toImmutableSet());
   }

   public FeatureSet getRequestedFeatures() {
      return (FeatureSet)this.getEnabledProfiles().stream().map(ResourcePackProfile::getRequestedFeatures).reduce(FeatureSet::combine).orElse(FeatureSet.empty());
   }

   public Collection getEnabledProfiles() {
      return this.enabled;
   }

   @Nullable
   public ResourcePackProfile getProfile(String name) {
      return (ResourcePackProfile)this.profiles.get(name);
   }

   public boolean hasProfile(String name) {
      return this.profiles.containsKey(name);
   }

   public List createResourcePacks() {
      return (List)this.enabled.stream().map(ResourcePackProfile::createResourcePack).collect(ImmutableList.toImmutableList());
   }
}
