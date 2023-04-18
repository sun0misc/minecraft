package net.minecraft.resource;

import java.nio.file.Path;
import net.minecraft.SharedConstants;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.metadata.PackFeatureSetMetadata;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataMap;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;

public class VanillaDataPackProvider extends VanillaResourcePackProvider {
   private static final PackResourceMetadata METADATA;
   private static final PackFeatureSetMetadata FEATURE_FLAGS;
   private static final ResourceMetadataMap METADATA_MAP;
   private static final Text NAME;
   private static final Identifier ID;

   public VanillaDataPackProvider() {
      super(ResourceType.SERVER_DATA, createDefaultPack(), ID);
   }

   private static DefaultResourcePack createDefaultPack() {
      return (new DefaultResourcePackBuilder()).withMetadataMap(METADATA_MAP).withNamespaces("minecraft").runCallback().withDefaultPaths().build();
   }

   protected Text getProfileName(String id) {
      return Text.literal(id);
   }

   @Nullable
   protected ResourcePackProfile createDefault(ResourcePack pack) {
      return ResourcePackProfile.create("vanilla", NAME, false, (name) -> {
         return pack;
      }, ResourceType.SERVER_DATA, ResourcePackProfile.InsertionPosition.BOTTOM, ResourcePackSource.BUILTIN);
   }

   @Nullable
   protected ResourcePackProfile create(String name, ResourcePackProfile.PackFactory packFactory, Text displayName) {
      return ResourcePackProfile.create(name, displayName, false, packFactory, ResourceType.SERVER_DATA, ResourcePackProfile.InsertionPosition.TOP, ResourcePackSource.FEATURE);
   }

   public static ResourcePackManager createManager(Path dataPacksPath) {
      return new ResourcePackManager(new ResourcePackProvider[]{new VanillaDataPackProvider(), new FileResourcePackProvider(dataPacksPath, ResourceType.SERVER_DATA, ResourcePackSource.WORLD)});
   }

   public static ResourcePackManager createManager(LevelStorage.Session session) {
      return createManager(session.getDirectory(WorldSavePath.DATAPACKS));
   }

   static {
      METADATA = new PackResourceMetadata(Text.translatable("dataPack.vanilla.description"), SharedConstants.getGameVersion().getResourceVersion(ResourceType.SERVER_DATA));
      FEATURE_FLAGS = new PackFeatureSetMetadata(FeatureFlags.DEFAULT_ENABLED_FEATURES);
      METADATA_MAP = ResourceMetadataMap.of(PackResourceMetadata.SERIALIZER, METADATA, PackFeatureSetMetadata.SERIALIZER, FEATURE_FLAGS);
      NAME = Text.translatable("dataPack.vanilla.name");
      ID = new Identifier("minecraft", "datapacks");
   }
}
