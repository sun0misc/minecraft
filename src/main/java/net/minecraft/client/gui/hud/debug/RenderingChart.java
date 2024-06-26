/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.debug;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.debug.DebugChart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Colors;
import net.minecraft.util.profiler.log.MultiValueDebugSampleLog;

@Environment(value=EnvType.CLIENT)
public class RenderingChart
extends DebugChart {
    private static final int field_45926 = -65536;
    private static final int field_45927 = -256;
    private static final int field_45928 = -16711936;
    private static final int field_45929 = 30;
    private static final double field_45930 = 33.333333333333336;

    public RenderingChart(TextRenderer arg, MultiValueDebugSampleLog arg2) {
        super(arg, arg2);
    }

    @Override
    protected void renderThresholds(DrawContext context, int x, int width, int height) {
        this.drawBorderedText(context, "30 FPS", x + 1, height - 60 + 1);
        this.drawBorderedText(context, "60 FPS", x + 1, height - 30 + 1);
        context.drawHorizontalLine(RenderLayer.getGuiOverlay(), x, x + width - 1, height - 30, Colors.WHITE);
        int l = MinecraftClient.getInstance().options.getMaxFps().getValue();
        if (l > 0 && l <= 250) {
            context.drawHorizontalLine(RenderLayer.getGuiOverlay(), x, x + width - 1, height - this.getHeight(1.0E9 / (double)l) - 1, -16711681);
        }
    }

    @Override
    protected String format(double value) {
        return String.format(Locale.ROOT, "%d ms", (int)Math.round(RenderingChart.toMillisecondsPerFrame(value)));
    }

    @Override
    protected int getHeight(double value) {
        return (int)Math.round(RenderingChart.toMillisecondsPerFrame(value) * 60.0 / 33.333333333333336);
    }

    @Override
    protected int getColor(long value) {
        return this.getColor(RenderingChart.toMillisecondsPerFrame(value), 0.0, -16711936, 28.0, -256, 56.0, -65536);
    }

    private static double toMillisecondsPerFrame(double nanosecondsPerFrame) {
        return nanosecondsPerFrame / 1000000.0;
    }
}

