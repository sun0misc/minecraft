/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt;

import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtEnd;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtType;

public class NbtTypes {
    private static final NbtType<?>[] VALUES = new NbtType[]{NbtEnd.TYPE, NbtByte.TYPE, NbtShort.TYPE, NbtInt.TYPE, NbtLong.TYPE, NbtFloat.TYPE, NbtDouble.TYPE, NbtByteArray.TYPE, NbtString.TYPE, NbtList.TYPE, NbtCompound.TYPE, NbtIntArray.TYPE, NbtLongArray.TYPE};

    public static NbtType<?> byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return NbtType.createInvalid(id);
        }
        return VALUES[id];
    }
}

