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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

@Environment(value=EnvType.CLIENT)
public class StructureBlockBlockEntityRenderer
implements BlockEntityRenderer<StructureBlockBlockEntity> {
    public StructureBlockBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(StructureBlockBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
        double o;
        double n;
        double m;
        double k;
        if (!MinecraftClient.getInstance().player.isCreativeLevelTwoOp() && !MinecraftClient.getInstance().player.isSpectator()) {
            return;
        }
        BlockPos lv = arg.getOffset();
        Vec3i lv2 = arg.getSize();
        if (lv2.getX() < 1 || lv2.getY() < 1 || lv2.getZ() < 1) {
            return;
        }
        if (arg.getMode() != StructureBlockMode.SAVE && arg.getMode() != StructureBlockMode.LOAD) {
            return;
        }
        double d = lv.getX();
        double e = lv.getZ();
        double g = lv.getY();
        double h = g + (double)lv2.getY();
        double l = switch (arg.getMirror()) {
            case BlockMirror.LEFT_RIGHT -> {
                k = lv2.getX();
                yield -lv2.getZ();
            }
            case BlockMirror.FRONT_BACK -> {
                k = -lv2.getX();
                yield lv2.getZ();
            }
            default -> {
                k = lv2.getX();
                yield lv2.getZ();
            }
        };
        double p = switch (arg.getRotation()) {
            case BlockRotation.CLOCKWISE_90 -> {
                m = l < 0.0 ? d : d + 1.0;
                n = k < 0.0 ? e + 1.0 : e;
                o = m - l;
                yield n + k;
            }
            case BlockRotation.CLOCKWISE_180 -> {
                m = k < 0.0 ? d : d + 1.0;
                n = l < 0.0 ? e : e + 1.0;
                o = m - k;
                yield n - l;
            }
            case BlockRotation.COUNTERCLOCKWISE_90 -> {
                m = l < 0.0 ? d + 1.0 : d;
                n = k < 0.0 ? e : e + 1.0;
                o = m + l;
                yield n - k;
            }
            default -> {
                m = k < 0.0 ? d + 1.0 : d;
                n = l < 0.0 ? e + 1.0 : e;
                o = m + k;
                yield n + l;
            }
        };
        float q = 1.0f;
        float r = 0.9f;
        float s = 0.5f;
        VertexConsumer lv3 = arg3.getBuffer(RenderLayer.getLines());
        if (arg.getMode() == StructureBlockMode.SAVE || arg.shouldShowBoundingBox()) {
            WorldRenderer.drawBox(arg2, lv3, m, g, n, o, h, p, 0.9f, 0.9f, 0.9f, 1.0f, 0.5f, 0.5f, 0.5f);
        }
        if (arg.getMode() == StructureBlockMode.SAVE && arg.shouldShowAir()) {
            this.renderInvisibleBlocks(arg, lv3, lv, arg2);
        }
    }

    private void renderInvisibleBlocks(StructureBlockBlockEntity entity, VertexConsumer vertices, BlockPos pos, MatrixStack matrices) {
        World lv = entity.getWorld();
        BlockPos lv2 = entity.getPos();
        BlockPos lv3 = lv2.add(pos);
        for (BlockPos lv4 : BlockPos.iterate(lv3, lv3.add(entity.getSize()).add(-1, -1, -1))) {
            boolean bl5;
            BlockState lv5 = lv.getBlockState(lv4);
            boolean bl = lv5.isAir();
            boolean bl2 = lv5.isOf(Blocks.STRUCTURE_VOID);
            boolean bl3 = lv5.isOf(Blocks.BARRIER);
            boolean bl4 = lv5.isOf(Blocks.LIGHT);
            boolean bl6 = bl5 = bl2 || bl3 || bl4;
            if (!bl && !bl5) continue;
            float f = bl ? 0.05f : 0.0f;
            double d = (float)(lv4.getX() - lv2.getX()) + 0.45f - f;
            double e = (float)(lv4.getY() - lv2.getY()) + 0.45f - f;
            double g = (float)(lv4.getZ() - lv2.getZ()) + 0.45f - f;
            double h = (float)(lv4.getX() - lv2.getX()) + 0.55f + f;
            double i = (float)(lv4.getY() - lv2.getY()) + 0.55f + f;
            double j = (float)(lv4.getZ() - lv2.getZ()) + 0.55f + f;
            if (bl) {
                WorldRenderer.drawBox(matrices, vertices, d, e, g, h, i, j, 0.5f, 0.5f, 1.0f, 1.0f, 0.5f, 0.5f, 1.0f);
                continue;
            }
            if (bl2) {
                WorldRenderer.drawBox(matrices, vertices, d, e, g, h, i, j, 1.0f, 0.75f, 0.75f, 1.0f, 1.0f, 0.75f, 0.75f);
                continue;
            }
            if (bl3) {
                WorldRenderer.drawBox(matrices, vertices, d, e, g, h, i, j, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f);
                continue;
            }
            if (!bl4) continue;
            WorldRenderer.drawBox(matrices, vertices, d, e, g, h, i, j, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f);
        }
    }

    @Override
    public boolean rendersOutsideBoundingBox(StructureBlockBlockEntity arg) {
        return true;
    }

    @Override
    public int getRenderDistance() {
        return 96;
    }
}

