/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtType;
import net.minecraft.text.Text;

public class StringNbtReader {
    public static final SimpleCommandExceptionType TRAILING = new SimpleCommandExceptionType(Text.translatable("argument.nbt.trailing"));
    public static final SimpleCommandExceptionType EXPECTED_KEY = new SimpleCommandExceptionType(Text.translatable("argument.nbt.expected.key"));
    public static final SimpleCommandExceptionType EXPECTED_VALUE = new SimpleCommandExceptionType(Text.translatable("argument.nbt.expected.value"));
    public static final Dynamic2CommandExceptionType LIST_MIXED = new Dynamic2CommandExceptionType((receivedType, expectedType) -> Text.stringifiedTranslatable("argument.nbt.list.mixed", receivedType, expectedType));
    public static final Dynamic2CommandExceptionType ARRAY_MIXED = new Dynamic2CommandExceptionType((receivedType, expectedType) -> Text.stringifiedTranslatable("argument.nbt.array.mixed", receivedType, expectedType));
    public static final DynamicCommandExceptionType ARRAY_INVALID = new DynamicCommandExceptionType(type -> Text.stringifiedTranslatable("argument.nbt.array.invalid", type));
    public static final char COMMA = ',';
    public static final char COLON = ':';
    private static final char SQUARE_OPEN_BRACKET = '[';
    private static final char SQUARE_CLOSE_BRACKET = ']';
    private static final char RIGHT_CURLY_BRACKET = '}';
    private static final char LEFT_CURLY_BRACKET = '{';
    private static final Pattern DOUBLE_PATTERN_IMPLICIT = Pattern.compile("[-+]?(?:[0-9]+[.]|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?", 2);
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d", 2);
    private static final Pattern FLOAT_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?f", 2);
    private static final Pattern BYTE_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)b", 2);
    private static final Pattern LONG_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)l", 2);
    private static final Pattern SHORT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)s", 2);
    private static final Pattern INT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)");
    public static final Codec<NbtCompound> STRINGIFIED_CODEC = Codec.STRING.comapFlatMap(nbt -> {
        try {
            return DataResult.success(new StringNbtReader(new StringReader((String)nbt)).readCompound(), Lifecycle.stable());
        } catch (CommandSyntaxException commandSyntaxException) {
            return DataResult.error(commandSyntaxException::getMessage);
        }
    }, NbtCompound::toString);
    public static final Codec<NbtCompound> NBT_COMPOUND_CODEC = Codec.withAlternative(STRINGIFIED_CODEC, NbtCompound.CODEC);
    private final StringReader reader;

    public static NbtCompound parse(String string) throws CommandSyntaxException {
        return new StringNbtReader(new StringReader(string)).readCompound();
    }

    @VisibleForTesting
    NbtCompound readCompound() throws CommandSyntaxException {
        NbtCompound lv = this.parseCompound();
        this.reader.skipWhitespace();
        if (this.reader.canRead()) {
            throw TRAILING.createWithContext(this.reader);
        }
        return lv;
    }

    public StringNbtReader(StringReader reader) {
        this.reader = reader;
    }

    protected String readString() throws CommandSyntaxException {
        this.reader.skipWhitespace();
        if (!this.reader.canRead()) {
            throw EXPECTED_KEY.createWithContext(this.reader);
        }
        return this.reader.readString();
    }

    protected NbtElement parseElementPrimitive() throws CommandSyntaxException {
        this.reader.skipWhitespace();
        int i = this.reader.getCursor();
        if (StringReader.isQuotedStringStart(this.reader.peek())) {
            return NbtString.of(this.reader.readQuotedString());
        }
        String string = this.reader.readUnquotedString();
        if (string.isEmpty()) {
            this.reader.setCursor(i);
            throw EXPECTED_VALUE.createWithContext(this.reader);
        }
        return this.parsePrimitive(string);
    }

    private NbtElement parsePrimitive(String input) {
        try {
            if (FLOAT_PATTERN.matcher(input).matches()) {
                return NbtFloat.of(Float.parseFloat(input.substring(0, input.length() - 1)));
            }
            if (BYTE_PATTERN.matcher(input).matches()) {
                return NbtByte.of(Byte.parseByte(input.substring(0, input.length() - 1)));
            }
            if (LONG_PATTERN.matcher(input).matches()) {
                return NbtLong.of(Long.parseLong(input.substring(0, input.length() - 1)));
            }
            if (SHORT_PATTERN.matcher(input).matches()) {
                return NbtShort.of(Short.parseShort(input.substring(0, input.length() - 1)));
            }
            if (INT_PATTERN.matcher(input).matches()) {
                return NbtInt.of(Integer.parseInt(input));
            }
            if (DOUBLE_PATTERN.matcher(input).matches()) {
                return NbtDouble.of(Double.parseDouble(input.substring(0, input.length() - 1)));
            }
            if (DOUBLE_PATTERN_IMPLICIT.matcher(input).matches()) {
                return NbtDouble.of(Double.parseDouble(input));
            }
            if ("true".equalsIgnoreCase(input)) {
                return NbtByte.ONE;
            }
            if ("false".equalsIgnoreCase(input)) {
                return NbtByte.ZERO;
            }
        } catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        return NbtString.of(input);
    }

    public NbtElement parseElement() throws CommandSyntaxException {
        this.reader.skipWhitespace();
        if (!this.reader.canRead()) {
            throw EXPECTED_VALUE.createWithContext(this.reader);
        }
        char c = this.reader.peek();
        if (c == '{') {
            return this.parseCompound();
        }
        if (c == '[') {
            return this.parseArray();
        }
        return this.parseElementPrimitive();
    }

    protected NbtElement parseArray() throws CommandSyntaxException {
        if (this.reader.canRead(3) && !StringReader.isQuotedStringStart(this.reader.peek(1)) && this.reader.peek(2) == ';') {
            return this.parseElementPrimitiveArray();
        }
        return this.parseList();
    }

    public NbtCompound parseCompound() throws CommandSyntaxException {
        this.expect('{');
        NbtCompound lv = new NbtCompound();
        this.reader.skipWhitespace();
        while (this.reader.canRead() && this.reader.peek() != '}') {
            int i = this.reader.getCursor();
            String string = this.readString();
            if (string.isEmpty()) {
                this.reader.setCursor(i);
                throw EXPECTED_KEY.createWithContext(this.reader);
            }
            this.expect(':');
            lv.put(string, this.parseElement());
            if (!this.readComma()) break;
            if (this.reader.canRead()) continue;
            throw EXPECTED_KEY.createWithContext(this.reader);
        }
        this.expect('}');
        return lv;
    }

    private NbtElement parseList() throws CommandSyntaxException {
        this.expect('[');
        this.reader.skipWhitespace();
        if (!this.reader.canRead()) {
            throw EXPECTED_VALUE.createWithContext(this.reader);
        }
        NbtList lv = new NbtList();
        NbtType<?> lv2 = null;
        while (this.reader.peek() != ']') {
            int i = this.reader.getCursor();
            NbtElement lv3 = this.parseElement();
            NbtType<?> lv4 = lv3.getNbtType();
            if (lv2 == null) {
                lv2 = lv4;
            } else if (lv4 != lv2) {
                this.reader.setCursor(i);
                throw LIST_MIXED.createWithContext(this.reader, lv4.getCommandFeedbackName(), lv2.getCommandFeedbackName());
            }
            lv.add(lv3);
            if (!this.readComma()) break;
            if (this.reader.canRead()) continue;
            throw EXPECTED_VALUE.createWithContext(this.reader);
        }
        this.expect(']');
        return lv;
    }

    private NbtElement parseElementPrimitiveArray() throws CommandSyntaxException {
        this.expect('[');
        int i = this.reader.getCursor();
        char c = this.reader.read();
        this.reader.read();
        this.reader.skipWhitespace();
        if (!this.reader.canRead()) {
            throw EXPECTED_VALUE.createWithContext(this.reader);
        }
        if (c == 'B') {
            return new NbtByteArray(this.readArray(NbtByteArray.TYPE, NbtByte.TYPE));
        }
        if (c == 'L') {
            return new NbtLongArray(this.readArray(NbtLongArray.TYPE, NbtLong.TYPE));
        }
        if (c == 'I') {
            return new NbtIntArray(this.readArray(NbtIntArray.TYPE, NbtInt.TYPE));
        }
        this.reader.setCursor(i);
        throw ARRAY_INVALID.createWithContext(this.reader, String.valueOf(c));
    }

    private <T extends Number> List<T> readArray(NbtType<?> arrayTypeReader, NbtType<?> typeReader) throws CommandSyntaxException {
        ArrayList<Number> list = Lists.newArrayList();
        while (this.reader.peek() != ']') {
            int i = this.reader.getCursor();
            NbtElement lv = this.parseElement();
            NbtType<?> lv2 = lv.getNbtType();
            if (lv2 != typeReader) {
                this.reader.setCursor(i);
                throw ARRAY_MIXED.createWithContext(this.reader, lv2.getCommandFeedbackName(), arrayTypeReader.getCommandFeedbackName());
            }
            if (typeReader == NbtByte.TYPE) {
                list.add(((AbstractNbtNumber)lv).byteValue());
            } else if (typeReader == NbtLong.TYPE) {
                list.add(((AbstractNbtNumber)lv).longValue());
            } else {
                list.add(((AbstractNbtNumber)lv).intValue());
            }
            if (!this.readComma()) break;
            if (this.reader.canRead()) continue;
            throw EXPECTED_VALUE.createWithContext(this.reader);
        }
        this.expect(']');
        return list;
    }

    private boolean readComma() {
        this.reader.skipWhitespace();
        if (this.reader.canRead() && this.reader.peek() == ',') {
            this.reader.skip();
            this.reader.skipWhitespace();
            return true;
        }
        return false;
    }

    private void expect(char c) throws CommandSyntaxException {
        this.reader.skipWhitespace();
        this.reader.expect(c);
    }
}

