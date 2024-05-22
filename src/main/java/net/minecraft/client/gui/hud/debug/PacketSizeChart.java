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
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.log.MultiValueDebugSampleLog;

@Environment(value=EnvType.CLIENT)
public class PacketSizeChart
extends DebugChart {
    private static final int field_45920 = -16711681;
    private static final int field_45921 = -6250241;
    private static final int field_45922 = -65536;
    private static final int field_45923 = 1024;
    private static final int field_45924 = 0x100000;
    private static final int field_45925 = 0x100000;

    public PacketSizeChart(TextRenderer arg, MultiValueDebugSampleLog arg2) {
        super(arg, arg2);
    }

    @Override
    protected void renderThresholds(DrawContext context, int x, int width, int height) {
        this.drawSizeBar(context, x, width, height, 64);
        this.drawSizeBar(context, x, width, height, 1024);
        this.drawSizeBar(context, x, width, height, 16384);
        this.drawBorderedText(context, PacketSizeChart.formatBytesPerSecond(1048576.0), x + 1, height - PacketSizeChart.calculateHeight(1048576.0) + 1);
    }

    private void drawSizeBar(DrawContext context, int x, int width, int height, int bytes) {
        this.drawSizeBar(context, x, width, height - PacketSizeChart.calculateHeight(bytes), PacketSizeChart.formatBytesPerSecond(bytes));
    }

    private void drawSizeBar(DrawContext context, int x, int width, int y, String label) {
        this.drawBorderedText(context, label, x + 1, y + 1);
        context.drawHorizontalLine(RenderLayer.getGuiOverlay(), x, x + width - 1, y, Colors.WHITE);
    }

    @Override
    protected String format(double value) {
        return PacketSizeChart.formatBytesPerSecond(PacketSizeChart.toBytesPerSecond(value));
    }

    private static String formatBytesPerSecond(double value) {
        if (value >= 1048576.0) {
            return String.format(Locale.ROOT, "%.1f MiB/s", value / 1048576.0);
        }
        if (value >= 1024.0) {
            return String.format(Locale.ROOT, "%.1f KiB/s", value / 1024.0);
        }
        return String.format(Locale.ROOT, "%d B/s", MathHelper.floor(value));
    }

    @Override
    protected int getHeight(double value) {
        return PacketSizeChart.calculateHeight(PacketSizeChart.toBytesPerSecond(value));
    }

    private static int calculateHeight(double value) {
        return (int)Math.round(Math.log(value + 1.0) * 60.0 / Math.log(1048576.0));
    }

    @Override
    protected int getColor(long value) {
        return this.getColor(PacketSizeChart.toBytesPerSecond(value), 0.0, -16711681, 8192.0, -6250241, 1.048576E7, -65536);
    }

    private static double toBytesPerSecond(double bytesPerTick) {
        return bytesPerTick * 20.0;
    }
}

