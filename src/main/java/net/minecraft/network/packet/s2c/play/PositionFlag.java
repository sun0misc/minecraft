/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import java.util.EnumSet;
import java.util.Set;

public enum PositionFlag {
    X(0),
    Y(1),
    Z(2),
    Y_ROT(3),
    X_ROT(4);

    public static final Set<PositionFlag> VALUES;
    public static final Set<PositionFlag> ROT;
    private final int shift;

    private PositionFlag(int shift) {
        this.shift = shift;
    }

    private int getMask() {
        return 1 << this.shift;
    }

    private boolean isSet(int mask) {
        return (mask & this.getMask()) == this.getMask();
    }

    public static Set<PositionFlag> getFlags(int mask) {
        EnumSet<PositionFlag> set = EnumSet.noneOf(PositionFlag.class);
        for (PositionFlag lv : PositionFlag.values()) {
            if (!lv.isSet(mask)) continue;
            set.add(lv);
        }
        return set;
    }

    public static int getBitfield(Set<PositionFlag> flags) {
        int i = 0;
        for (PositionFlag lv : flags) {
            i |= lv.getMask();
        }
        return i;
    }

    static {
        VALUES = Set.of(PositionFlag.values());
        ROT = Set.of(X_ROT, Y_ROT);
    }
}

