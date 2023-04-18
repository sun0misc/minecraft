package net.minecraft.client.render.entity.feature;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHat;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;

@Environment(EnvType.CLIENT)
public class VillagerClothingFeatureRenderer extends FeatureRenderer {
   private static final Int2ObjectMap LEVEL_TO_ID = (Int2ObjectMap)Util.make(new Int2ObjectOpenHashMap(), (levelToId) -> {
      levelToId.put(1, new Identifier("stone"));
      levelToId.put(2, new Identifier("iron"));
      levelToId.put(3, new Identifier("gold"));
      levelToId.put(4, new Identifier("emerald"));
      levelToId.put(5, new Identifier("diamond"));
   });
   private final Object2ObjectMap villagerTypeToHat = new Object2ObjectOpenHashMap();
   private final Object2ObjectMap professionToHat = new Object2ObjectOpenHashMap();
   private final ResourceManager resourceManager;
   private final String entityType;

   public VillagerClothingFeatureRenderer(FeatureRendererContext context, ResourceManager resourceManager, String entityType) {
      super(context);
      this.resourceManager = resourceManager;
      this.entityType = entityType;
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, LivingEntity arg3, float f, float g, float h, float j, float k, float l) {
      if (!arg3.isInvisible()) {
         VillagerData lv = ((VillagerDataContainer)arg3).getVillagerData();
         VillagerType lv2 = lv.getType();
         VillagerProfession lv3 = lv.getProfession();
         VillagerResourceMetadata.HatType lv4 = this.getHatType(this.villagerTypeToHat, "type", Registries.VILLAGER_TYPE, lv2);
         VillagerResourceMetadata.HatType lv5 = this.getHatType(this.professionToHat, "profession", Registries.VILLAGER_PROFESSION, lv3);
         EntityModel lv6 = this.getContextModel();
         ((ModelWithHat)lv6).setHatVisible(lv5 == VillagerResourceMetadata.HatType.NONE || lv5 == VillagerResourceMetadata.HatType.PARTIAL && lv4 != VillagerResourceMetadata.HatType.FULL);
         Identifier lv7 = this.findTexture("type", Registries.VILLAGER_TYPE.getId(lv2));
         renderModel(lv6, lv7, arg, arg2, i, arg3, 1.0F, 1.0F, 1.0F);
         ((ModelWithHat)lv6).setHatVisible(true);
         if (lv3 != VillagerProfession.NONE && !arg3.isBaby()) {
            Identifier lv8 = this.findTexture("profession", Registries.VILLAGER_PROFESSION.getId(lv3));
            renderModel(lv6, lv8, arg, arg2, i, arg3, 1.0F, 1.0F, 1.0F);
            if (lv3 != VillagerProfession.NITWIT) {
               Identifier lv9 = this.findTexture("profession_level", (Identifier)LEVEL_TO_ID.get(MathHelper.clamp(lv.getLevel(), 1, LEVEL_TO_ID.size())));
               renderModel(lv6, lv9, arg, arg2, i, arg3, 1.0F, 1.0F, 1.0F);
            }
         }

      }
   }

   private Identifier findTexture(String keyType, Identifier keyId) {
      return keyId.withPath((path) -> {
         return "textures/entity/" + this.entityType + "/" + keyType + "/" + path + ".png";
      });
   }

   public VillagerResourceMetadata.HatType getHatType(Object2ObjectMap hatLookUp, String keyType, DefaultedRegistry registry, Object key) {
      return (VillagerResourceMetadata.HatType)hatLookUp.computeIfAbsent(key, (k) -> {
         return (VillagerResourceMetadata.HatType)this.resourceManager.getResource(this.findTexture(keyType, registry.getId(key))).flatMap((resource) -> {
            try {
               return resource.getMetadata().decode(VillagerResourceMetadata.READER).map(VillagerResourceMetadata::getHatType);
            } catch (IOException var2) {
               return Optional.empty();
            }
         }).orElse(VillagerResourceMetadata.HatType.NONE);
      });
   }
}
