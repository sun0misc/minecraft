/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceManager;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface EntityRendererFactory<T extends Entity> {
    public EntityRenderer<T> create(Context var1);

    @Environment(value=EnvType.CLIENT)
    public static class Context {
        private final EntityRenderDispatcher renderDispatcher;
        private final ItemRenderer itemRenderer;
        private final BlockRenderManager blockRenderManager;
        private final HeldItemRenderer heldItemRenderer;
        private final ResourceManager resourceManager;
        private final EntityModelLoader modelLoader;
        private final TextRenderer textRenderer;

        public Context(EntityRenderDispatcher renderDispatcher, ItemRenderer itemRenderer, BlockRenderManager blockRenderManager, HeldItemRenderer heldItemRenderer, ResourceManager resourceManager, EntityModelLoader modelLoader, TextRenderer textRenderer) {
            this.renderDispatcher = renderDispatcher;
            this.itemRenderer = itemRenderer;
            this.blockRenderManager = blockRenderManager;
            this.heldItemRenderer = heldItemRenderer;
            this.resourceManager = resourceManager;
            this.modelLoader = modelLoader;
            this.textRenderer = textRenderer;
        }

        public EntityRenderDispatcher getRenderDispatcher() {
            return this.renderDispatcher;
        }

        public ItemRenderer getItemRenderer() {
            return this.itemRenderer;
        }

        public BlockRenderManager getBlockRenderManager() {
            return this.blockRenderManager;
        }

        public HeldItemRenderer getHeldItemRenderer() {
            return this.heldItemRenderer;
        }

        public ResourceManager getResourceManager() {
            return this.resourceManager;
        }

        public EntityModelLoader getModelLoader() {
            return this.modelLoader;
        }

        public BakedModelManager getModelManager() {
            return this.blockRenderManager.getModels().getModelManager();
        }

        public ModelPart getPart(EntityModelLayer layer) {
            return this.modelLoader.getModelPart(layer);
        }

        public TextRenderer getTextRenderer() {
            return this.textRenderer;
        }
    }
}

