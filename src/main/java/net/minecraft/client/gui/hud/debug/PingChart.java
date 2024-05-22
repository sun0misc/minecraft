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
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.debug.DebugChart;
import net.minecraft.util.profiler.log.MultiValueDebugSampleLog;

@Environment(value=EnvType.CLIENT)
public class PingChart
extends DebugChart {
    private static final int field_45931 = -65536;
    private static final int field_45932 = -256;
    private static final int field_45933 = -16711936;
    private static final int field_45934 = 500;

    public PingChart(TextRenderer arg, MultiValueDebugSampleLog arg2) {
        super(arg, arg2);
    }

    @Override
    protected void renderThresholds(DrawContext context, int x, int width, int height) {
        this.drawBorderedText(context, "500 ms", x + 1, height - 60 + 1);
    }

    @Override
    protected String format(double value) {
        return String.format(Locale.ROOT, "%d ms", (int)Math.round(value));
    }

    @Override
    protected int getHeight(double value) {
        return (int)Math.round(value * 60.0 / 500.0);
    }

    @Override
    protected int getColor(long value) {
        return this.getColor(value, 0.0, -16711936, 250.0, -256, 500.0, -65536);
    }
}

