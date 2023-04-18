package net.minecraft.command;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;

public interface CommandRegistryAccess {
   RegistryWrapper createWrapper(RegistryKey registryRef);

   static CommandRegistryAccess of(final RegistryWrapper.WrapperLookup wrapperLookup, final FeatureSet enabledFeatures) {
      return new CommandRegistryAccess() {
         public RegistryWrapper createWrapper(RegistryKey registryRef) {
            return wrapperLookup.getWrapperOrThrow(registryRef).withFeatureFilter(enabledFeatures);
         }
      };
   }

   static EntryListCreationPolicySettable of(final DynamicRegistryManager registryManager, final FeatureSet enabledFeatures) {
      return new EntryListCreationPolicySettable() {
         EntryListCreationPolicy entryListCreationPolicy;

         {
            this.entryListCreationPolicy = CommandRegistryAccess.EntryListCreationPolicy.FAIL;
         }

         public void setEntryListCreationPolicy(EntryListCreationPolicy entryListCreationPolicy) {
            this.entryListCreationPolicy = entryListCreationPolicy;
         }

         public RegistryWrapper createWrapper(RegistryKey registryRef) {
            Registry lv = registryManager.get(registryRef);
            final RegistryWrapper.Impl lv2 = lv.getReadOnlyWrapper();
            final RegistryWrapper.Impl lv3 = lv.getTagCreatingWrapper();
            RegistryWrapper.Impl lv4 = new RegistryWrapper.Impl.Delegating() {
               protected RegistryWrapper.Impl getBase() {
                  RegistryWrapper.Impl var10000;
                  switch (entryListCreationPolicy) {
                     case FAIL:
                        var10000 = lv2;
                        break;
                     case CREATE_NEW:
                        var10000 = lv3;
                        break;
                     default:
                        throw new IncompatibleClassChangeError();
                  }

                  return var10000;
               }
            };
            return lv4.withFeatureFilter(enabledFeatures);
         }
      };
   }

   public interface EntryListCreationPolicySettable extends CommandRegistryAccess {
      void setEntryListCreationPolicy(EntryListCreationPolicy entryListCreationPolicy);
   }

   public static enum EntryListCreationPolicy {
      CREATE_NEW,
      FAIL;

      // $FF: synthetic method
      private static EntryListCreationPolicy[] method_41701() {
         return new EntryListCreationPolicy[]{CREATE_NEW, FAIL};
      }
   }
}
