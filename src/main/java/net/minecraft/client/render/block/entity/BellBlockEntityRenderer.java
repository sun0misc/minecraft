/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class BellBlockEntityRenderer
implements BlockEntityRenderer<BellBlockEntity> {
    public static final SpriteIdentifier BELL_BODY_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.method_60656("entity/bell/bell_body"));
    private static final String BELL_BODY = "bell_body";
    private final ModelPart bellBody;

    public BellBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        ModelPart lv = ctx.getLayerModelPart(EntityModelLayers.BELL);
        this.bellBody = lv.getChild(BELL_BODY);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(BELL_BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-3.0f, -6.0f, -3.0f, 6.0f, 7.0f, 6.0f), ModelTransform.pivot(8.0f, 12.0f, 8.0f));
        lv3.addChild("bell_base", ModelPartBuilder.create().uv(0, 13).cuboid(4.0f, 4.0f, 4.0f, 8.0f, 2.0f, 8.0f), ModelTransform.pivot(-8.0f, -12.0f, -8.0f));
        return TexturedModelData.of(lv, 32, 32);
    }

    @Override
    public void render(BellBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
        float g = (float)arg.ringTicks + f;
        float h = 0.0f;
        float k = 0.0f;
        if (arg.ringing) {
            float l = MathHelper.sin(g / (float)Math.PI) / (4.0f + g / 3.0f);
            if (arg.lastSideHit == Direction.NORTH) {
                h = -l;
            } else if (arg.lastSideHit == Direction.SOUTH) {
                h = l;
            } else if (arg.lastSideHit == Direction.EAST) {
                k = -l;
            } else if (arg.lastSideHit == Direction.WEST) {
                k = l;
            }
        }
        this.bellBody.pitch = h;
        this.bellBody.roll = k;
        VertexConsumer lv = BELL_BODY_TEXTURE.getVertexConsumer(arg3, RenderLayer::getEntitySolid);
        this.bellBody.render(arg2, lv, i, j);
    }
}

