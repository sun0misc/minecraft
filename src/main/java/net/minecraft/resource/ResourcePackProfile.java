package net.minecraft.resource;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.metadata.PackFeatureSetMetadata;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ResourcePackProfile {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String name;
   private final PackFactory packFactory;
   private final Text displayName;
   private final Text description;
   private final ResourcePackCompatibility compatibility;
   private final FeatureSet requestedFeatures;
   private final InsertionPosition position;
   private final boolean alwaysEnabled;
   private final boolean pinned;
   private final ResourcePackSource source;

   @Nullable
   public static ResourcePackProfile create(String name, Text displayName, boolean alwaysEnabled, PackFactory packFactory, ResourceType type, InsertionPosition position, ResourcePackSource source) {
      Metadata lv = loadMetadata(name, packFactory);
      return lv != null ? of(name, displayName, alwaysEnabled, packFactory, lv, type, position, false, source) : null;
   }

   public static ResourcePackProfile of(String name, Text displayName, boolean alwaysEnabled, PackFactory packFactory, Metadata metadata, ResourceType type, InsertionPosition position, boolean pinned, ResourcePackSource source) {
      return new ResourcePackProfile(name, alwaysEnabled, packFactory, displayName, metadata, metadata.getCompatibility(type), position, pinned, source);
   }

   private ResourcePackProfile(String name, boolean alwaysEnabled, PackFactory packFactory, Text displayName, Metadata metadata, ResourcePackCompatibility compatibility, InsertionPosition position, boolean pinned, ResourcePackSource source) {
      this.name = name;
      this.packFactory = packFactory;
      this.displayName = displayName;
      this.description = metadata.description();
      this.compatibility = compatibility;
      this.requestedFeatures = metadata.requestedFeatures();
      this.alwaysEnabled = alwaysEnabled;
      this.position = position;
      this.pinned = pinned;
      this.source = source;
   }

   @Nullable
   public static Metadata loadMetadata(String name, PackFactory packFactory) {
      try {
         ResourcePack lv = packFactory.open(name);

         PackFeatureSetMetadata lv3;
         label52: {
            Metadata var6;
            try {
               PackResourceMetadata lv2 = (PackResourceMetadata)lv.parseMetadata(PackResourceMetadata.SERIALIZER);
               if (lv2 == null) {
                  LOGGER.warn("Missing metadata in pack {}", name);
                  lv3 = null;
                  break label52;
               }

               lv3 = (PackFeatureSetMetadata)lv.parseMetadata(PackFeatureSetMetadata.SERIALIZER);
               FeatureSet lv4 = lv3 != null ? lv3.flags() : FeatureSet.empty();
               var6 = new Metadata(lv2.getDescription(), lv2.getPackFormat(), lv4);
            } catch (Throwable var8) {
               if (lv != null) {
                  try {
                     lv.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (lv != null) {
               lv.close();
            }

            return var6;
         }

         if (lv != null) {
            lv.close();
         }

         return lv3;
      } catch (Exception var9) {
         LOGGER.warn("Failed to read pack metadata", var9);
         return null;
      }
   }

   public Text getDisplayName() {
      return this.displayName;
   }

   public Text getDescription() {
      return this.description;
   }

   public Text getInformationText(boolean enabled) {
      return Texts.bracketed(this.source.decorate(Text.literal(this.name))).styled((style) -> {
         return style.withColor(enabled ? Formatting.GREEN : Formatting.RED).withInsertion(StringArgumentType.escapeIfRequired(this.name)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.empty().append(this.displayName).append("\n").append(this.description)));
      });
   }

   public ResourcePackCompatibility getCompatibility() {
      return this.compatibility;
   }

   public FeatureSet getRequestedFeatures() {
      return this.requestedFeatures;
   }

   public ResourcePack createResourcePack() {
      return this.packFactory.open(this.name);
   }

   public String getName() {
      return this.name;
   }

   public boolean isAlwaysEnabled() {
      return this.alwaysEnabled;
   }

   public boolean isPinned() {
      return this.pinned;
   }

   public InsertionPosition getInitialPosition() {
      return this.position;
   }

   public ResourcePackSource getSource() {
      return this.source;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof ResourcePackProfile)) {
         return false;
      } else {
         ResourcePackProfile lv = (ResourcePackProfile)o;
         return this.name.equals(lv.name);
      }
   }

   public int hashCode() {
      return this.name.hashCode();
   }

   @FunctionalInterface
   public interface PackFactory {
      ResourcePack open(String name);
   }

   public static record Metadata(Text description, int format, FeatureSet requestedFeatures) {
      public Metadata(Text arg, int i, FeatureSet arg2) {
         this.description = arg;
         this.format = i;
         this.requestedFeatures = arg2;
      }

      public ResourcePackCompatibility getCompatibility(ResourceType type) {
         return ResourcePackCompatibility.from(this.format, type);
      }

      public Text description() {
         return this.description;
      }

      public int format() {
         return this.format;
      }

      public FeatureSet requestedFeatures() {
         return this.requestedFeatures;
      }
   }

   public static enum InsertionPosition {
      TOP,
      BOTTOM;

      public int insert(List items, Object item, Function profileGetter, boolean listInverted) {
         InsertionPosition lv = listInverted ? this.inverse() : this;
         int i;
         ResourcePackProfile lv2;
         if (lv == BOTTOM) {
            for(i = 0; i < items.size(); ++i) {
               lv2 = (ResourcePackProfile)profileGetter.apply(items.get(i));
               if (!lv2.isPinned() || lv2.getInitialPosition() != this) {
                  break;
               }
            }

            items.add(i, item);
            return i;
         } else {
            for(i = items.size() - 1; i >= 0; --i) {
               lv2 = (ResourcePackProfile)profileGetter.apply(items.get(i));
               if (!lv2.isPinned() || lv2.getInitialPosition() != this) {
                  break;
               }
            }

            items.add(i + 1, item);
            return i + 1;
         }
      }

      public InsertionPosition inverse() {
         return this == TOP ? BOTTOM : TOP;
      }

      // $FF: synthetic method
      private static InsertionPosition[] method_36583() {
         return new InsertionPosition[]{TOP, BOTTOM};
      }
   }
}
