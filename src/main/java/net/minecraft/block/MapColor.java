/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.google.common.base.Preconditions;

public class MapColor {
    private static final MapColor[] COLORS = new MapColor[64];
    public static final MapColor CLEAR = new MapColor(0, 0);
    public static final MapColor PALE_GREEN = new MapColor(1, 8368696);
    public static final MapColor PALE_YELLOW = new MapColor(2, 16247203);
    public static final MapColor WHITE_GRAY = new MapColor(3, 0xC7C7C7);
    public static final MapColor BRIGHT_RED = new MapColor(4, 0xFF0000);
    public static final MapColor PALE_PURPLE = new MapColor(5, 0xA0A0FF);
    public static final MapColor IRON_GRAY = new MapColor(6, 0xA7A7A7);
    public static final MapColor DARK_GREEN = new MapColor(7, 31744);
    public static final MapColor WHITE = new MapColor(8, 0xFFFFFF);
    public static final MapColor LIGHT_BLUE_GRAY = new MapColor(9, 10791096);
    public static final MapColor DIRT_BROWN = new MapColor(10, 9923917);
    public static final MapColor STONE_GRAY = new MapColor(11, 0x707070);
    public static final MapColor WATER_BLUE = new MapColor(12, 0x4040FF);
    public static final MapColor OAK_TAN = new MapColor(13, 9402184);
    public static final MapColor OFF_WHITE = new MapColor(14, 0xFFFCF5);
    public static final MapColor ORANGE = new MapColor(15, 14188339);
    public static final MapColor MAGENTA = new MapColor(16, 11685080);
    public static final MapColor LIGHT_BLUE = new MapColor(17, 6724056);
    public static final MapColor YELLOW = new MapColor(18, 0xE5E533);
    public static final MapColor LIME = new MapColor(19, 8375321);
    public static final MapColor PINK = new MapColor(20, 15892389);
    public static final MapColor GRAY = new MapColor(21, 0x4C4C4C);
    public static final MapColor LIGHT_GRAY = new MapColor(22, 0x999999);
    public static final MapColor CYAN = new MapColor(23, 5013401);
    public static final MapColor PURPLE = new MapColor(24, 8339378);
    public static final MapColor BLUE = new MapColor(25, 3361970);
    public static final MapColor BROWN = new MapColor(26, 6704179);
    public static final MapColor GREEN = new MapColor(27, 6717235);
    public static final MapColor RED = new MapColor(28, 0x993333);
    public static final MapColor BLACK = new MapColor(29, 0x191919);
    public static final MapColor GOLD = new MapColor(30, 16445005);
    public static final MapColor DIAMOND_BLUE = new MapColor(31, 6085589);
    public static final MapColor LAPIS_BLUE = new MapColor(32, 4882687);
    public static final MapColor EMERALD_GREEN = new MapColor(33, 55610);
    public static final MapColor SPRUCE_BROWN = new MapColor(34, 8476209);
    public static final MapColor DARK_RED = new MapColor(35, 0x700200);
    public static final MapColor TERRACOTTA_WHITE = new MapColor(36, 13742497);
    public static final MapColor TERRACOTTA_ORANGE = new MapColor(37, 10441252);
    public static final MapColor TERRACOTTA_MAGENTA = new MapColor(38, 9787244);
    public static final MapColor TERRACOTTA_LIGHT_BLUE = new MapColor(39, 7367818);
    public static final MapColor TERRACOTTA_YELLOW = new MapColor(40, 12223780);
    public static final MapColor TERRACOTTA_LIME = new MapColor(41, 6780213);
    public static final MapColor TERRACOTTA_PINK = new MapColor(42, 10505550);
    public static final MapColor TERRACOTTA_GRAY = new MapColor(43, 0x392923);
    public static final MapColor TERRACOTTA_LIGHT_GRAY = new MapColor(44, 8874850);
    public static final MapColor TERRACOTTA_CYAN = new MapColor(45, 0x575C5C);
    public static final MapColor TERRACOTTA_PURPLE = new MapColor(46, 8014168);
    public static final MapColor TERRACOTTA_BLUE = new MapColor(47, 4996700);
    public static final MapColor TERRACOTTA_BROWN = new MapColor(48, 4993571);
    public static final MapColor TERRACOTTA_GREEN = new MapColor(49, 5001770);
    public static final MapColor TERRACOTTA_RED = new MapColor(50, 9321518);
    public static final MapColor TERRACOTTA_BLACK = new MapColor(51, 2430480);
    public static final MapColor DULL_RED = new MapColor(52, 12398641);
    public static final MapColor DULL_PINK = new MapColor(53, 9715553);
    public static final MapColor DARK_CRIMSON = new MapColor(54, 6035741);
    public static final MapColor TEAL = new MapColor(55, 1474182);
    public static final MapColor DARK_AQUA = new MapColor(56, 3837580);
    public static final MapColor DARK_DULL_PINK = new MapColor(57, 5647422);
    public static final MapColor BRIGHT_TEAL = new MapColor(58, 1356933);
    public static final MapColor DEEPSLATE_GRAY = new MapColor(59, 0x646464);
    public static final MapColor RAW_IRON_PINK = new MapColor(60, 14200723);
    public static final MapColor LICHEN_GREEN = new MapColor(61, 8365974);
    public final int color;
    public final int id;

    private MapColor(int id, int color) {
        if (id < 0 || id > 63) {
            throw new IndexOutOfBoundsException("Map colour ID must be between 0 and 63 (inclusive)");
        }
        this.id = id;
        this.color = color;
        MapColor.COLORS[id] = this;
    }

    public int getRenderColor(Brightness brightness) {
        if (this == CLEAR) {
            return 0;
        }
        int i = brightness.brightness;
        int j = (this.color >> 16 & 0xFF) * i / 255;
        int k = (this.color >> 8 & 0xFF) * i / 255;
        int l = (this.color & 0xFF) * i / 255;
        return 0xFF000000 | l << 16 | k << 8 | j;
    }

    public static MapColor get(int id) {
        Preconditions.checkPositionIndex(id, COLORS.length, "material id");
        return MapColor.getUnchecked(id);
    }

    private static MapColor getUnchecked(int id) {
        MapColor lv = COLORS[id];
        return lv != null ? lv : CLEAR;
    }

    public static int getRenderColor(int colorByte) {
        int j = colorByte & 0xFF;
        return MapColor.getUnchecked(j >> 2).getRenderColor(Brightness.get(j & 3));
    }

    public byte getRenderColorByte(Brightness brightness) {
        return (byte)(this.id << 2 | brightness.id & 3);
    }

    public static enum Brightness {
        LOW(0, 180),
        NORMAL(1, 220),
        HIGH(2, 255),
        LOWEST(3, 135);

        private static final Brightness[] VALUES;
        public final int id;
        public final int brightness;

        private Brightness(int id, int brightness) {
            this.id = id;
            this.brightness = brightness;
        }

        public static Brightness validateAndGet(int id) {
            Preconditions.checkPositionIndex(id, VALUES.length, "brightness id");
            return Brightness.get(id);
        }

        static Brightness get(int id) {
            return VALUES[id];
        }

        static {
            VALUES = new Brightness[]{LOW, NORMAL, HIGH, LOWEST};
        }
    }
}

