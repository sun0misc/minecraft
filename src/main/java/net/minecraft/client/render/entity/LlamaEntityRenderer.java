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
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.LlamaDecorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.LlamaEntityModel;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class LlamaEntityRenderer
extends MobEntityRenderer<LlamaEntity, LlamaEntityModel<LlamaEntity>> {
    private static final Identifier CREAMY_TEXTURE = Identifier.method_60656("textures/entity/llama/creamy.png");
    private static final Identifier WHITE_TEXTURE = Identifier.method_60656("textures/entity/llama/white.png");
    private static final Identifier BROWN_TEXTURE = Identifier.method_60656("textures/entity/llama/brown.png");
    private static final Identifier GRAY_TEXTURE = Identifier.method_60656("textures/entity/llama/gray.png");

    public LlamaEntityRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer) {
        super(ctx, new LlamaEntityModel(ctx.getPart(layer)), 0.7f);
        this.addFeature(new LlamaDecorFeatureRenderer(this, ctx.getModelLoader()));
    }

    @Override
    public Identifier getTexture(LlamaEntity arg) {
        return switch (arg.getVariant()) {
            default -> throw new MatchException(null, null);
            case LlamaEntity.Variant.CREAMY -> CREAMY_TEXTURE;
            case LlamaEntity.Variant.WHITE -> WHITE_TEXTURE;
            case LlamaEntity.Variant.BROWN -> BROWN_TEXTURE;
            case LlamaEntity.Variant.GRAY -> GRAY_TEXTURE;
        };
    }
}

