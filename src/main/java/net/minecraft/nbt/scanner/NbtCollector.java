/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.nbt.scanner;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
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
import net.minecraft.nbt.scanner.NbtScanner;
import org.jetbrains.annotations.Nullable;

public class NbtCollector
implements NbtScanner {
    private String currentKey = "";
    @Nullable
    private NbtElement root;
    private final Deque<Consumer<NbtElement>> stack = new ArrayDeque<Consumer<NbtElement>>();

    @Nullable
    public NbtElement getRoot() {
        return this.root;
    }

    protected int getDepth() {
        return this.stack.size();
    }

    private void append(NbtElement nbt) {
        this.stack.getLast().accept(nbt);
    }

    @Override
    public NbtScanner.Result visitEnd() {
        this.append(NbtEnd.INSTANCE);
        return NbtScanner.Result.CONTINUE;
    }

    @Override
    public NbtScanner.Result visitString(String value) {
        this.append(NbtString.of(value));
        return NbtScanner.Result.CONTINUE;
    }

    @Override
    public NbtScanner.Result visitByte(byte value) {
        this.append(NbtByte.of(value));
        return NbtScanner.Result.CONTINUE;
    }

    @Override
    public NbtScanner.Result visitShort(short value) {
        this.append(NbtShort.of(value));
        return NbtScanner.Result.CONTINUE;
    }

    @Override
    public NbtScanner.Result visitInt(int value) {
        this.append(NbtInt.of(value));
        return NbtScanner.Result.CONTINUE;
    }

    @Override
    public NbtScanner.Result visitLong(long value) {
        this.append(NbtLong.of(value));
        return NbtScanner.Result.CONTINUE;
    }

    @Override
    public NbtScanner.Result visitFloat(float value) {
        this.append(NbtFloat.of(value));
        return NbtScanner.Result.CONTINUE;
    }

    @Override
    public NbtScanner.Result visitDouble(double value) {
        this.append(NbtDouble.of(value));
        return NbtScanner.Result.CONTINUE;
    }

    @Override
    public NbtScanner.Result visitByteArray(byte[] value) {
        this.append(new NbtByteArray(value));
        return NbtScanner.Result.CONTINUE;
    }

    @Override
    public NbtScanner.Result visitIntArray(int[] value) {
        this.append(new NbtIntArray(value));
        return NbtScanner.Result.CONTINUE;
    }

    @Override
    public NbtScanner.Result visitLongArray(long[] value) {
        this.append(new NbtLongArray(value));
        return NbtScanner.Result.CONTINUE;
    }

    @Override
    public NbtScanner.Result visitListMeta(NbtType<?> entryType, int length) {
        return NbtScanner.Result.CONTINUE;
    }

    @Override
    public NbtScanner.NestedResult startListItem(NbtType<?> type, int index) {
        this.pushStack(type);
        return NbtScanner.NestedResult.ENTER;
    }

    @Override
    public NbtScanner.NestedResult visitSubNbtType(NbtType<?> type) {
        return NbtScanner.NestedResult.ENTER;
    }

    @Override
    public NbtScanner.NestedResult startSubNbt(NbtType<?> type, String key) {
        this.currentKey = key;
        this.pushStack(type);
        return NbtScanner.NestedResult.ENTER;
    }

    private void pushStack(NbtType<?> type) {
        if (type == NbtList.TYPE) {
            NbtList lv = new NbtList();
            this.append(lv);
            this.stack.addLast(lv::add);
        } else if (type == NbtCompound.TYPE) {
            NbtCompound lv2 = new NbtCompound();
            this.append(lv2);
            this.stack.addLast(nbt -> lv2.put(this.currentKey, (NbtElement)nbt));
        }
    }

    @Override
    public NbtScanner.Result endNested() {
        this.stack.removeLast();
        return NbtScanner.Result.CONTINUE;
    }

    @Override
    public NbtScanner.Result start(NbtType<?> rootType) {
        if (rootType == NbtList.TYPE) {
            NbtList lv = new NbtList();
            this.root = lv;
            this.stack.addLast(lv::add);
        } else if (rootType == NbtCompound.TYPE) {
            NbtCompound lv2 = new NbtCompound();
            this.root = lv2;
            this.stack.addLast(nbt -> lv2.put(this.currentKey, (NbtElement)nbt));
        } else {
            this.stack.addLast(nbt -> {
                this.root = nbt;
            });
        }
        return NbtScanner.Result.CONTINUE;
    }
}

