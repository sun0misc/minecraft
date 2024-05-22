/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.StuckObjectsFeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class StuckArrowsFeatureRenderer<T extends LivingEntity, M extends PlayerEntityModel<T>>
extends StuckObjectsFeatureRenderer<T, M> {
    private final EntityRenderDispatcher dispatcher;

    public StuckArrowsFeatureRenderer(EntityRendererFactory.Context context, LivingEntityRenderer<T, M> entityRenderer) {
        super(entityRenderer);
        this.dispatcher = context.getRenderDispatcher();
    }

    @Override
    protected int getObjectCount(T entity) {
        return ((LivingEntity)entity).getStuckArrowCount();
    }

    @Override
    protected void renderObject(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Entity entity, float directionX, float directionY, float directionZ, float tickDelta) {
        float k = MathHelper.sqrt(directionX * directionX + directionZ * directionZ);
        ArrowEntity lv = new ArrowEntity(entity.getWorld(), entity.getX(), entity.getY(), entity.getZ(), ItemStack.EMPTY, null);
        lv.setYaw((float)(Math.atan2(directionX, directionZ) * 57.2957763671875));
        lv.setPitch((float)(Math.atan2(directionY, k) * 57.2957763671875));
        lv.prevYaw = lv.getYaw();
        lv.prevPitch = lv.getPitch();
        this.dispatcher.render(lv, 0.0, 0.0, 0.0, 0.0f, tickDelta, matrices, vertexConsumers, light);
    }
}

