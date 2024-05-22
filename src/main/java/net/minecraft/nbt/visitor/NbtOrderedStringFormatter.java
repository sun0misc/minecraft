/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt.visitor;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
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
import net.minecraft.nbt.visitor.NbtElementVisitor;
import net.minecraft.util.Util;

public class NbtOrderedStringFormatter
implements NbtElementVisitor {
    private static final Map<String, List<String>> ENTRY_ORDER_OVERRIDES = Util.make(Maps.newHashMap(), map -> {
        map.put("{}", Lists.newArrayList("DataVersion", "author", "size", "data", "entities", "palette", "palettes"));
        map.put("{}.data.[].{}", Lists.newArrayList("pos", "state", "nbt"));
        map.put("{}.entities.[].{}", Lists.newArrayList("blockPos", "pos"));
    });
    private static final Set<String> IGNORED_PATHS = Sets.newHashSet("{}.size.[]", "{}.data.[].{}", "{}.palette.[].{}", "{}.entities.[].{}");
    private static final Pattern SIMPLE_NAME = Pattern.compile("[A-Za-z0-9._+-]+");
    private static final String KEY_VALUE_SEPARATOR = String.valueOf(':');
    private static final String ENTRY_SEPARATOR = String.valueOf(',');
    private static final String SQUARE_OPEN_BRACKET = "[";
    private static final String SQUARE_CLOSE_BRACKET = "]";
    private static final String SEMICOLON = ";";
    private static final String SPACE = " ";
    private static final String CURLY_OPEN_BRACKET = "{";
    private static final String CURLY_CLOSE_BRACKET = "}";
    private static final String NEW_LINE = "\n";
    private final String prefix;
    private final int indentationLevel;
    private final List<String> pathParts;
    private String result = "";

    public NbtOrderedStringFormatter() {
        this("    ", 0, Lists.newArrayList());
    }

    public NbtOrderedStringFormatter(String prefix, int indentationLevel, List<String> pathParts) {
        this.prefix = prefix;
        this.indentationLevel = indentationLevel;
        this.pathParts = pathParts;
    }

    public String apply(NbtElement element) {
        element.accept(this);
        return this.result;
    }

    @Override
    public void visitString(NbtString element) {
        this.result = NbtString.escape(element.asString());
    }

    @Override
    public void visitByte(NbtByte element) {
        this.result = String.valueOf(element.numberValue()) + "b";
    }

    @Override
    public void visitShort(NbtShort element) {
        this.result = String.valueOf(element.numberValue()) + "s";
    }

    @Override
    public void visitInt(NbtInt element) {
        this.result = String.valueOf(element.numberValue());
    }

    @Override
    public void visitLong(NbtLong element) {
        this.result = String.valueOf(element.numberValue()) + "L";
    }

    @Override
    public void visitFloat(NbtFloat element) {
        this.result = element.floatValue() + "f";
    }

    @Override
    public void visitDouble(NbtDouble element) {
        this.result = element.doubleValue() + "d";
    }

    @Override
    public void visitByteArray(NbtByteArray element) {
        StringBuilder stringBuilder = new StringBuilder(SQUARE_OPEN_BRACKET).append("B").append(SEMICOLON);
        byte[] bs = element.getByteArray();
        for (int i = 0; i < bs.length; ++i) {
            stringBuilder.append(SPACE).append(bs[i]).append("B");
            if (i == bs.length - 1) continue;
            stringBuilder.append(ENTRY_SEPARATOR);
        }
        stringBuilder.append(SQUARE_CLOSE_BRACKET);
        this.result = stringBuilder.toString();
    }

    @Override
    public void visitIntArray(NbtIntArray element) {
        StringBuilder stringBuilder = new StringBuilder(SQUARE_OPEN_BRACKET).append("I").append(SEMICOLON);
        int[] is = element.getIntArray();
        for (int i = 0; i < is.length; ++i) {
            stringBuilder.append(SPACE).append(is[i]);
            if (i == is.length - 1) continue;
            stringBuilder.append(ENTRY_SEPARATOR);
        }
        stringBuilder.append(SQUARE_CLOSE_BRACKET);
        this.result = stringBuilder.toString();
    }

    @Override
    public void visitLongArray(NbtLongArray element) {
        String string = "L";
        StringBuilder stringBuilder = new StringBuilder(SQUARE_OPEN_BRACKET).append("L").append(SEMICOLON);
        long[] ls = element.getLongArray();
        for (int i = 0; i < ls.length; ++i) {
            stringBuilder.append(SPACE).append(ls[i]).append("L");
            if (i == ls.length - 1) continue;
            stringBuilder.append(ENTRY_SEPARATOR);
        }
        stringBuilder.append(SQUARE_CLOSE_BRACKET);
        this.result = stringBuilder.toString();
    }

    @Override
    public void visitList(NbtList element) {
        String string;
        if (element.isEmpty()) {
            this.result = "[]";
            return;
        }
        StringBuilder stringBuilder = new StringBuilder(SQUARE_OPEN_BRACKET);
        this.pushPathPart("[]");
        String string2 = string = IGNORED_PATHS.contains(this.joinPath()) ? "" : this.prefix;
        if (!string.isEmpty()) {
            stringBuilder.append(NEW_LINE);
        }
        for (int i = 0; i < element.size(); ++i) {
            stringBuilder.append(Strings.repeat(string, this.indentationLevel + 1));
            stringBuilder.append(new NbtOrderedStringFormatter(string, this.indentationLevel + 1, this.pathParts).apply(element.get(i)));
            if (i == element.size() - 1) continue;
            stringBuilder.append(ENTRY_SEPARATOR).append(string.isEmpty() ? SPACE : NEW_LINE);
        }
        if (!string.isEmpty()) {
            stringBuilder.append(NEW_LINE).append(Strings.repeat(string, this.indentationLevel));
        }
        stringBuilder.append(SQUARE_CLOSE_BRACKET);
        this.result = stringBuilder.toString();
        this.popPathPart();
    }

    @Override
    public void visitCompound(NbtCompound compound) {
        String string;
        if (compound.isEmpty()) {
            this.result = "{}";
            return;
        }
        StringBuilder stringBuilder = new StringBuilder(CURLY_OPEN_BRACKET);
        this.pushPathPart("{}");
        String string2 = string = IGNORED_PATHS.contains(this.joinPath()) ? "" : this.prefix;
        if (!string.isEmpty()) {
            stringBuilder.append(NEW_LINE);
        }
        List<String> collection = this.getSortedNames(compound);
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            String string22 = (String)iterator.next();
            NbtElement lv = compound.get(string22);
            this.pushPathPart(string22);
            stringBuilder.append(Strings.repeat(string, this.indentationLevel + 1)).append(NbtOrderedStringFormatter.escapeName(string22)).append(KEY_VALUE_SEPARATOR).append(SPACE).append(new NbtOrderedStringFormatter(string, this.indentationLevel + 1, this.pathParts).apply(lv));
            this.popPathPart();
            if (!iterator.hasNext()) continue;
            stringBuilder.append(ENTRY_SEPARATOR).append(string.isEmpty() ? SPACE : NEW_LINE);
        }
        if (!string.isEmpty()) {
            stringBuilder.append(NEW_LINE).append(Strings.repeat(string, this.indentationLevel));
        }
        stringBuilder.append(CURLY_CLOSE_BRACKET);
        this.result = stringBuilder.toString();
        this.popPathPart();
    }

    private void popPathPart() {
        this.pathParts.remove(this.pathParts.size() - 1);
    }

    private void pushPathPart(String part) {
        this.pathParts.add(part);
    }

    protected List<String> getSortedNames(NbtCompound compound) {
        HashSet<String> set = Sets.newHashSet(compound.getKeys());
        ArrayList<String> list = Lists.newArrayList();
        List<String> list2 = ENTRY_ORDER_OVERRIDES.get(this.joinPath());
        if (list2 != null) {
            for (String string : list2) {
                if (!set.remove(string)) continue;
                list.add(string);
            }
            if (!set.isEmpty()) {
                set.stream().sorted().forEach(list::add);
            }
        } else {
            list.addAll(set);
            Collections.sort(list);
        }
        return list;
    }

    public String joinPath() {
        return String.join((CharSequence)".", this.pathParts);
    }

    protected static String escapeName(String name) {
        if (SIMPLE_NAME.matcher(name).matches()) {
            return name;
        }
        return NbtString.escape(name);
    }

    @Override
    public void visitEnd(NbtEnd element) {
    }
}

