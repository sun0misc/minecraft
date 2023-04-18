package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerClothingFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieVillagerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ZombieVillagerEntityRenderer extends BipedEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/zombie_villager/zombie_villager.png");

   public ZombieVillagerEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new ZombieVillagerEntityModel(arg.getPart(EntityModelLayers.ZOMBIE_VILLAGER)), 0.5F);
      this.addFeature(new ArmorFeatureRenderer(this, new ZombieVillagerEntityModel(arg.getPart(EntityModelLayers.ZOMBIE_VILLAGER_INNER_ARMOR)), new ZombieVillagerEntityModel(arg.getPart(EntityModelLayers.ZOMBIE_VILLAGER_OUTER_ARMOR)), arg.getModelManager()));
      this.addFeature(new VillagerClothingFeatureRenderer(this, arg.getResourceManager(), "zombie_villager"));
   }

   public Identifier getTexture(ZombieVillagerEntity arg) {
      return TEXTURE;
   }

   protected boolean isShaking(ZombieVillagerEntity arg) {
      return super.isShaking(arg) || arg.isConverting();
   }

   // $FF: synthetic method
   protected boolean isShaking(LivingEntity entity) {
      return this.isShaking((ZombieVillagerEntity)entity);
   }
}
