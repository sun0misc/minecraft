package net.minecraft.network;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtTagSizeTracker;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PacketByteBuf extends ByteBuf {
   private static final int MAX_VAR_INT_LENGTH = 5;
   private static final int MAX_VAR_LONG_LENGTH = 10;
   public static final int MAX_READ_NBT_SIZE = 2097152;
   private final ByteBuf parent;
   public static final short DEFAULT_MAX_STRING_LENGTH = Short.MAX_VALUE;
   public static final int MAX_TEXT_LENGTH = 262144;
   private static final int field_39381 = 256;
   private static final int field_39382 = 256;
   private static final int field_39383 = 512;
   private static final Gson GSON = new Gson();

   public PacketByteBuf(ByteBuf parent) {
      this.parent = parent;
   }

   public static int getVarIntLength(int value) {
      for(int j = 1; j < MAX_VAR_INT_LENGTH; ++j) {
         if ((value & -1 << j * 7) == 0) {
            return j;
         }
      }

      return MAX_VAR_INT_LENGTH;
   }

   public static int getVarLongLength(long value) {
      for(int i = 1; i < MAX_VAR_LONG_LENGTH; ++i) {
         if ((value & -1L << i * 7) == 0L) {
            return i;
         }
      }

      return MAX_VAR_LONG_LENGTH;
   }

   /** @deprecated */
   @Deprecated
   public Object decode(DynamicOps ops, Codec codec) {
      NbtCompound lv = this.readUnlimitedNbt();
      return Util.getResult(codec.parse(ops, lv), (error) -> {
         return new DecoderException("Failed to decode: " + error + " " + lv);
      });
   }

   /** @deprecated */
   @Deprecated
   public void encode(DynamicOps ops, Codec codec, Object value) {
      NbtElement lv = (NbtElement)Util.getResult(codec.encodeStart(ops, value), (error) -> {
         return new EncoderException("Failed to encode: " + error + " " + value);
      });
      this.writeNbt((NbtCompound)lv);
   }

   public Object decodeAsJson(Codec codec) {
      JsonElement jsonElement = (JsonElement)JsonHelper.deserialize(GSON, this.readString(), JsonElement.class);
      DataResult dataResult = codec.parse(JsonOps.INSTANCE, jsonElement);
      return Util.getResult(dataResult, (error) -> {
         return new DecoderException("Failed to decode json: " + error);
      });
   }

   public void encodeAsJson(Codec codec, Object value) {
      DataResult dataResult = codec.encodeStart(JsonOps.INSTANCE, value);
      this.writeString(GSON.toJson((JsonElement)Util.getResult(dataResult, (error) -> {
         return new EncoderException("Failed to encode: " + error + " " + value);
      })));
   }

   public void writeRegistryValue(IndexedIterable registry, Object value) {
      int i = registry.getRawId(value);
      if (i == -1) {
         throw new IllegalArgumentException("Can't find id for '" + value + "' in map " + registry);
      } else {
         this.writeVarInt(i);
      }
   }

   public void writeRegistryEntry(IndexedIterable registryEntries, RegistryEntry entry, PacketWriter writer) {
      switch (entry.getType()) {
         case REFERENCE:
            int i = registryEntries.getRawId(entry);
            if (i == -1) {
               Object var10002 = entry.value();
               throw new IllegalArgumentException("Can't find id for '" + var10002 + "' in map " + registryEntries);
            }

            this.writeVarInt(i + 1);
            break;
         case DIRECT:
            this.writeVarInt(0);
            writer.accept(this, entry.value());
      }

   }

   @Nullable
   public Object readRegistryValue(IndexedIterable registry) {
      int i = this.readVarInt();
      return registry.get(i);
   }

   public RegistryEntry readRegistryEntry(IndexedIterable registryEntries, PacketReader reader) {
      int i = this.readVarInt();
      if (i == 0) {
         return RegistryEntry.of(reader.apply(this));
      } else {
         RegistryEntry lv = (RegistryEntry)registryEntries.get(i - 1);
         if (lv == null) {
            throw new IllegalArgumentException("Can't find element with id " + i);
         } else {
            return lv;
         }
      }
   }

   public static IntFunction getMaxValidator(IntFunction applier, int max) {
      return (value) -> {
         if (value > max) {
            throw new DecoderException("Value " + value + " is larger than limit " + max);
         } else {
            return applier.apply(value);
         }
      };
   }

   public Collection readCollection(IntFunction collectionFactory, PacketReader reader) {
      int i = this.readVarInt();
      Collection collection = (Collection)collectionFactory.apply(i);

      for(int j = 0; j < i; ++j) {
         collection.add(reader.apply(this));
      }

      return collection;
   }

   public void writeCollection(Collection collection, PacketWriter writer) {
      this.writeVarInt(collection.size());
      Iterator var3 = collection.iterator();

      while(var3.hasNext()) {
         Object object = var3.next();
         writer.accept(this, object);
      }

   }

   public List readList(PacketReader reader) {
      return (List)this.readCollection(Lists::newArrayListWithCapacity, reader);
   }

   public IntList readIntList() {
      int i = this.readVarInt();
      IntList intList = new IntArrayList();

      for(int j = 0; j < i; ++j) {
         intList.add(this.readVarInt());
      }

      return intList;
   }

   public void writeIntList(IntList list) {
      this.writeVarInt(list.size());
      list.forEach(this::writeVarInt);
   }

   public Map readMap(IntFunction mapFactory, PacketReader keyReader, PacketReader valueReader) {
      int i = this.readVarInt();
      Map map = (Map)mapFactory.apply(i);

      for(int j = 0; j < i; ++j) {
         Object object = keyReader.apply(this);
         Object object2 = valueReader.apply(this);
         map.put(object, object2);
      }

      return map;
   }

   public Map readMap(PacketReader keyReader, PacketReader valueReader) {
      return this.readMap(Maps::newHashMapWithExpectedSize, keyReader, valueReader);
   }

   public void writeMap(Map map, PacketWriter keyWriter, PacketWriter valueWriter) {
      this.writeVarInt(map.size());
      map.forEach((key, value) -> {
         keyWriter.accept(this, key);
         valueWriter.accept(this, value);
      });
   }

   public void forEachInCollection(Consumer consumer) {
      int i = this.readVarInt();

      for(int j = 0; j < i; ++j) {
         consumer.accept(this);
      }

   }

   public void writeEnumSet(EnumSet enumSet, Class type) {
      Enum[] enums = (Enum[])type.getEnumConstants();
      BitSet bitSet = new BitSet(enums.length);

      for(int i = 0; i < enums.length; ++i) {
         bitSet.set(i, enumSet.contains(enums[i]));
      }

      this.writeBitSet(bitSet, enums.length);
   }

   public EnumSet readEnumSet(Class type) {
      Enum[] enums = (Enum[])type.getEnumConstants();
      BitSet bitSet = this.readBitSet(enums.length);
      EnumSet enumSet = EnumSet.noneOf(type);

      for(int i = 0; i < enums.length; ++i) {
         if (bitSet.get(i)) {
            enumSet.add(enums[i]);
         }
      }

      return enumSet;
   }

   public void writeOptional(Optional value, PacketWriter writer) {
      if (value.isPresent()) {
         this.writeBoolean(true);
         writer.accept(this, value.get());
      } else {
         this.writeBoolean(false);
      }

   }

   public Optional readOptional(PacketReader reader) {
      return this.readBoolean() ? Optional.of(reader.apply(this)) : Optional.empty();
   }

   @Nullable
   public Object readNullable(PacketReader reader) {
      return this.readBoolean() ? reader.apply(this) : null;
   }

   public void writeNullable(@Nullable Object value, PacketWriter writer) {
      if (value != null) {
         this.writeBoolean(true);
         writer.accept(this, value);
      } else {
         this.writeBoolean(false);
      }

   }

   public void writeEither(Either either, PacketWriter leftWriter, PacketWriter rightWriter) {
      either.ifLeft((object) -> {
         this.writeBoolean(true);
         leftWriter.accept(this, object);
      }).ifRight((object) -> {
         this.writeBoolean(false);
         rightWriter.accept(this, object);
      });
   }

   public Either readEither(PacketReader leftReader, PacketReader rightReader) {
      return this.readBoolean() ? Either.left(leftReader.apply(this)) : Either.right(rightReader.apply(this));
   }

   public byte[] readByteArray() {
      return this.readByteArray(this.readableBytes());
   }

   public PacketByteBuf writeByteArray(byte[] array) {
      this.writeVarInt(array.length);
      this.writeBytes(array);
      return this;
   }

   public byte[] readByteArray(int maxSize) {
      int j = this.readVarInt();
      if (j > maxSize) {
         throw new DecoderException("ByteArray with size " + j + " is bigger than allowed " + maxSize);
      } else {
         byte[] bs = new byte[j];
         this.readBytes(bs);
         return bs;
      }
   }

   public PacketByteBuf writeIntArray(int[] array) {
      this.writeVarInt(array.length);
      int[] var2 = array;
      int var3 = array.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         int i = var2[var4];
         this.writeVarInt(i);
      }

      return this;
   }

   public int[] readIntArray() {
      return this.readIntArray(this.readableBytes());
   }

   public int[] readIntArray(int maxSize) {
      int j = this.readVarInt();
      if (j > maxSize) {
         throw new DecoderException("VarIntArray with size " + j + " is bigger than allowed " + maxSize);
      } else {
         int[] is = new int[j];

         for(int k = 0; k < is.length; ++k) {
            is[k] = this.readVarInt();
         }

         return is;
      }
   }

   public PacketByteBuf writeLongArray(long[] array) {
      this.writeVarInt(array.length);
      long[] var2 = array;
      int var3 = array.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         long l = var2[var4];
         this.writeLong(l);
      }

      return this;
   }

   public long[] readLongArray() {
      return this.readLongArray((long[])null);
   }

   public long[] readLongArray(@Nullable long[] toArray) {
      return this.readLongArray(toArray, this.readableBytes() / 8);
   }

   public long[] readLongArray(@Nullable long[] toArray, int maxSize) {
      int j = this.readVarInt();
      if (toArray == null || toArray.length != j) {
         if (j > maxSize) {
            throw new DecoderException("LongArray with size " + j + " is bigger than allowed " + maxSize);
         }

         toArray = new long[j];
      }

      for(int k = 0; k < toArray.length; ++k) {
         toArray[k] = this.readLong();
      }

      return toArray;
   }

   @VisibleForTesting
   public byte[] getWrittenBytes() {
      int i = this.writerIndex();
      byte[] bs = new byte[i];
      this.getBytes(0, (byte[])bs);
      return bs;
   }

   public BlockPos readBlockPos() {
      return BlockPos.fromLong(this.readLong());
   }

   public PacketByteBuf writeBlockPos(BlockPos pos) {
      this.writeLong(pos.asLong());
      return this;
   }

   public ChunkPos readChunkPos() {
      return new ChunkPos(this.readLong());
   }

   public PacketByteBuf writeChunkPos(ChunkPos pos) {
      this.writeLong(pos.toLong());
      return this;
   }

   public ChunkSectionPos readChunkSectionPos() {
      return ChunkSectionPos.from(this.readLong());
   }

   public PacketByteBuf writeChunkSectionPos(ChunkSectionPos pos) {
      this.writeLong(pos.asLong());
      return this;
   }

   public GlobalPos readGlobalPos() {
      RegistryKey lv = this.readRegistryKey(RegistryKeys.WORLD);
      BlockPos lv2 = this.readBlockPos();
      return GlobalPos.create(lv, lv2);
   }

   public void writeGlobalPos(GlobalPos pos) {
      this.writeRegistryKey(pos.getDimension());
      this.writeBlockPos(pos.getPos());
   }

   public Vector3f readVector3f() {
      return new Vector3f(this.readFloat(), this.readFloat(), this.readFloat());
   }

   public void writeVector3f(Vector3f vector3f) {
      this.writeFloat(vector3f.x());
      this.writeFloat(vector3f.y());
      this.writeFloat(vector3f.z());
   }

   public Quaternionf readQuaternionf() {
      return new Quaternionf(this.readFloat(), this.readFloat(), this.readFloat(), this.readFloat());
   }

   public void writeQuaternionf(Quaternionf quaternionf) {
      this.writeFloat(quaternionf.x);
      this.writeFloat(quaternionf.y);
      this.writeFloat(quaternionf.z);
      this.writeFloat(quaternionf.w);
   }

   public Text readText() {
      Text lv = Text.Serializer.fromJson(this.readString(MAX_TEXT_LENGTH));
      if (lv == null) {
         throw new DecoderException("Received unexpected null component");
      } else {
         return lv;
      }
   }

   public PacketByteBuf writeText(Text text) {
      return this.writeString(Text.Serializer.toJson(text), MAX_TEXT_LENGTH);
   }

   public Enum readEnumConstant(Class enumClass) {
      return ((Enum[])enumClass.getEnumConstants())[this.readVarInt()];
   }

   public PacketByteBuf writeEnumConstant(Enum instance) {
      return this.writeVarInt(instance.ordinal());
   }

   public int readVarInt() {
      int i = 0;
      int j = 0;

      byte b;
      do {
         b = this.readByte();
         i |= (b & 127) << j++ * 7;
         if (j > 5) {
            throw new RuntimeException("VarInt too big");
         }
      } while((b & 128) == 128);

      return i;
   }

   public long readVarLong() {
      long l = 0L;
      int i = 0;

      byte b;
      do {
         b = this.readByte();
         l |= (long)(b & 127) << i++ * 7;
         if (i > 10) {
            throw new RuntimeException("VarLong too big");
         }
      } while((b & 128) == 128);

      return l;
   }

   public PacketByteBuf writeUuid(UUID uuid) {
      this.writeLong(uuid.getMostSignificantBits());
      this.writeLong(uuid.getLeastSignificantBits());
      return this;
   }

   public UUID readUuid() {
      return new UUID(this.readLong(), this.readLong());
   }

   public PacketByteBuf writeVarInt(int value) {
      while((value & -128) != 0) {
         this.writeByte(value & 127 | 128);
         value >>>= 7;
      }

      this.writeByte(value);
      return this;
   }

   public PacketByteBuf writeVarLong(long value) {
      while((value & -128L) != 0L) {
         this.writeByte((int)(value & 127L) | 128);
         value >>>= 7;
      }

      this.writeByte((int)value);
      return this;
   }

   public PacketByteBuf writeNbt(@Nullable NbtCompound compound) {
      if (compound == null) {
         this.writeByte(0);
      } else {
         try {
            NbtIo.write((NbtCompound)compound, (DataOutput)(new ByteBufOutputStream(this)));
         } catch (IOException var3) {
            throw new EncoderException(var3);
         }
      }

      return this;
   }

   @Nullable
   public NbtCompound readNbt() {
      return this.readNbt(new NbtTagSizeTracker(2097152L));
   }

   @Nullable
   public NbtCompound readUnlimitedNbt() {
      return this.readNbt(NbtTagSizeTracker.EMPTY);
   }

   @Nullable
   public NbtCompound readNbt(NbtTagSizeTracker sizeTracker) {
      int i = this.readerIndex();
      byte b = this.readByte();
      if (b == 0) {
         return null;
      } else {
         this.readerIndex(i);

         try {
            return NbtIo.read(new ByteBufInputStream(this), sizeTracker);
         } catch (IOException var5) {
            throw new EncoderException(var5);
         }
      }
   }

   public PacketByteBuf writeItemStack(ItemStack stack) {
      if (stack.isEmpty()) {
         this.writeBoolean(false);
      } else {
         this.writeBoolean(true);
         Item lv = stack.getItem();
         this.writeRegistryValue(Registries.ITEM, lv);
         this.writeByte(stack.getCount());
         NbtCompound lv2 = null;
         if (lv.isDamageable() || lv.isNbtSynced()) {
            lv2 = stack.getNbt();
         }

         this.writeNbt(lv2);
      }

      return this;
   }

   public ItemStack readItemStack() {
      if (!this.readBoolean()) {
         return ItemStack.EMPTY;
      } else {
         Item lv = (Item)this.readRegistryValue(Registries.ITEM);
         int i = this.readByte();
         ItemStack lv2 = new ItemStack(lv, i);
         lv2.setNbt(this.readNbt());
         return lv2;
      }
   }

   public String readString() {
      return this.readString(DEFAULT_MAX_STRING_LENGTH);
   }

   public String readString(int maxLength) {
      int j = toEncodedStringLength(maxLength);
      int k = this.readVarInt();
      if (k > j) {
         throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + k + " > " + j + ")");
      } else if (k < 0) {
         throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
      } else {
         String string = this.toString(this.readerIndex(), k, StandardCharsets.UTF_8);
         this.readerIndex(this.readerIndex() + k);
         if (string.length() > maxLength) {
            int var10002 = string.length();
            throw new DecoderException("The received string length is longer than maximum allowed (" + var10002 + " > " + maxLength + ")");
         } else {
            return string;
         }
      }
   }

   public PacketByteBuf writeString(String string) {
      return this.writeString(string, DEFAULT_MAX_STRING_LENGTH);
   }

   public PacketByteBuf writeString(String string, int maxLength) {
      if (string.length() > maxLength) {
         int var10002 = string.length();
         throw new EncoderException("String too big (was " + var10002 + " characters, max " + maxLength + ")");
      } else {
         byte[] bs = string.getBytes(StandardCharsets.UTF_8);
         int j = toEncodedStringLength(maxLength);
         if (bs.length > j) {
            throw new EncoderException("String too big (was " + bs.length + " bytes encoded, max " + j + ")");
         } else {
            this.writeVarInt(bs.length);
            this.writeBytes(bs);
            return this;
         }
      }
   }

   private static int toEncodedStringLength(int decodedLength) {
      return decodedLength * 3;
   }

   public Identifier readIdentifier() {
      return new Identifier(this.readString(DEFAULT_MAX_STRING_LENGTH));
   }

   public PacketByteBuf writeIdentifier(Identifier id) {
      this.writeString(id.toString());
      return this;
   }

   public RegistryKey readRegistryKey(RegistryKey registryRef) {
      Identifier lv = this.readIdentifier();
      return RegistryKey.of(registryRef, lv);
   }

   public void writeRegistryKey(RegistryKey key) {
      this.writeIdentifier(key.getValue());
   }

   public Date readDate() {
      return new Date(this.readLong());
   }

   public PacketByteBuf writeDate(Date date) {
      this.writeLong(date.getTime());
      return this;
   }

   public Instant readInstant() {
      return Instant.ofEpochMilli(this.readLong());
   }

   public void writeInstant(Instant instant) {
      this.writeLong(instant.toEpochMilli());
   }

   public PublicKey readPublicKey() {
      try {
         return NetworkEncryptionUtils.decodeEncodedRsaPublicKey(this.readByteArray(512));
      } catch (NetworkEncryptionException var2) {
         throw new DecoderException("Malformed public key bytes", var2);
      }
   }

   public PacketByteBuf writePublicKey(PublicKey publicKey) {
      this.writeByteArray(publicKey.getEncoded());
      return this;
   }

   public BlockHitResult readBlockHitResult() {
      BlockPos lv = this.readBlockPos();
      Direction lv2 = (Direction)this.readEnumConstant(Direction.class);
      float f = this.readFloat();
      float g = this.readFloat();
      float h = this.readFloat();
      boolean bl = this.readBoolean();
      return new BlockHitResult(new Vec3d((double)lv.getX() + (double)f, (double)lv.getY() + (double)g, (double)lv.getZ() + (double)h), lv2, lv, bl);
   }

   public void writeBlockHitResult(BlockHitResult hitResult) {
      BlockPos lv = hitResult.getBlockPos();
      this.writeBlockPos(lv);
      this.writeEnumConstant(hitResult.getSide());
      Vec3d lv2 = hitResult.getPos();
      this.writeFloat((float)(lv2.x - (double)lv.getX()));
      this.writeFloat((float)(lv2.y - (double)lv.getY()));
      this.writeFloat((float)(lv2.z - (double)lv.getZ()));
      this.writeBoolean(hitResult.isInsideBlock());
   }

   public BitSet readBitSet() {
      return BitSet.valueOf(this.readLongArray());
   }

   public void writeBitSet(BitSet bitSet) {
      this.writeLongArray(bitSet.toLongArray());
   }

   public BitSet readBitSet(int size) {
      byte[] bs = new byte[MathHelper.ceilDiv(size, 8)];
      this.readBytes(bs);
      return BitSet.valueOf(bs);
   }

   public void writeBitSet(BitSet bitSet, int size) {
      if (bitSet.length() > size) {
         int var10002 = bitSet.length();
         throw new EncoderException("BitSet is larger than expected size (" + var10002 + ">" + size + ")");
      } else {
         byte[] bs = bitSet.toByteArray();
         this.writeBytes(Arrays.copyOf(bs, MathHelper.ceilDiv(size, 8)));
      }
   }

   public GameProfile readGameProfile() {
      UUID uUID = this.readUuid();
      String string = this.readString(16);
      GameProfile gameProfile = new GameProfile(uUID, string);
      gameProfile.getProperties().putAll(this.readPropertyMap());
      return gameProfile;
   }

   public void writeGameProfile(GameProfile gameProfile) {
      this.writeUuid(gameProfile.getId());
      this.writeString(gameProfile.getName());
      this.writePropertyMap(gameProfile.getProperties());
   }

   public PropertyMap readPropertyMap() {
      PropertyMap propertyMap = new PropertyMap();
      this.forEachInCollection((buf) -> {
         Property property = this.readProperty();
         propertyMap.put(property.getName(), property);
      });
      return propertyMap;
   }

   public void writePropertyMap(PropertyMap propertyMap) {
      this.writeCollection(propertyMap.values(), PacketByteBuf::writeProperty);
   }

   public Property readProperty() {
      String string = this.readString();
      String string2 = this.readString();
      if (this.readBoolean()) {
         String string3 = this.readString();
         return new Property(string, string2, string3);
      } else {
         return new Property(string, string2);
      }
   }

   public void writeProperty(Property property) {
      this.writeString(property.getName());
      this.writeString(property.getValue());
      if (property.hasSignature()) {
         this.writeBoolean(true);
         this.writeString(property.getSignature());
      } else {
         this.writeBoolean(false);
      }

   }

   public int capacity() {
      return this.parent.capacity();
   }

   public ByteBuf capacity(int capacity) {
      return this.parent.capacity(capacity);
   }

   public int maxCapacity() {
      return this.parent.maxCapacity();
   }

   public ByteBufAllocator alloc() {
      return this.parent.alloc();
   }

   public ByteOrder order() {
      return this.parent.order();
   }

   public ByteBuf order(ByteOrder byteOrder) {
      return this.parent.order(byteOrder);
   }

   public ByteBuf unwrap() {
      return this.parent.unwrap();
   }

   public boolean isDirect() {
      return this.parent.isDirect();
   }

   public boolean isReadOnly() {
      return this.parent.isReadOnly();
   }

   public ByteBuf asReadOnly() {
      return this.parent.asReadOnly();
   }

   public int readerIndex() {
      return this.parent.readerIndex();
   }

   public ByteBuf readerIndex(int index) {
      return this.parent.readerIndex(index);
   }

   public int writerIndex() {
      return this.parent.writerIndex();
   }

   public ByteBuf writerIndex(int index) {
      return this.parent.writerIndex(index);
   }

   public ByteBuf setIndex(int readerIndex, int writerIndex) {
      return this.parent.setIndex(readerIndex, writerIndex);
   }

   public int readableBytes() {
      return this.parent.readableBytes();
   }

   public int writableBytes() {
      return this.parent.writableBytes();
   }

   public int maxWritableBytes() {
      return this.parent.maxWritableBytes();
   }

   public boolean isReadable() {
      return this.parent.isReadable();
   }

   public boolean isReadable(int size) {
      return this.parent.isReadable(size);
   }

   public boolean isWritable() {
      return this.parent.isWritable();
   }

   public boolean isWritable(int size) {
      return this.parent.isWritable(size);
   }

   public ByteBuf clear() {
      return this.parent.clear();
   }

   public ByteBuf markReaderIndex() {
      return this.parent.markReaderIndex();
   }

   public ByteBuf resetReaderIndex() {
      return this.parent.resetReaderIndex();
   }

   public ByteBuf markWriterIndex() {
      return this.parent.markWriterIndex();
   }

   public ByteBuf resetWriterIndex() {
      return this.parent.resetWriterIndex();
   }

   public ByteBuf discardReadBytes() {
      return this.parent.discardReadBytes();
   }

   public ByteBuf discardSomeReadBytes() {
      return this.parent.discardSomeReadBytes();
   }

   public ByteBuf ensureWritable(int minBytes) {
      return this.parent.ensureWritable(minBytes);
   }

   public int ensureWritable(int minBytes, boolean force) {
      return this.parent.ensureWritable(minBytes, force);
   }

   public boolean getBoolean(int index) {
      return this.parent.getBoolean(index);
   }

   public byte getByte(int index) {
      return this.parent.getByte(index);
   }

   public short getUnsignedByte(int index) {
      return this.parent.getUnsignedByte(index);
   }

   public short getShort(int index) {
      return this.parent.getShort(index);
   }

   public short getShortLE(int index) {
      return this.parent.getShortLE(index);
   }

   public int getUnsignedShort(int index) {
      return this.parent.getUnsignedShort(index);
   }

   public int getUnsignedShortLE(int index) {
      return this.parent.getUnsignedShortLE(index);
   }

   public int getMedium(int index) {
      return this.parent.getMedium(index);
   }

   public int getMediumLE(int index) {
      return this.parent.getMediumLE(index);
   }

   public int getUnsignedMedium(int index) {
      return this.parent.getUnsignedMedium(index);
   }

   public int getUnsignedMediumLE(int index) {
      return this.parent.getUnsignedMediumLE(index);
   }

   public int getInt(int index) {
      return this.parent.getInt(index);
   }

   public int getIntLE(int index) {
      return this.parent.getIntLE(index);
   }

   public long getUnsignedInt(int index) {
      return this.parent.getUnsignedInt(index);
   }

   public long getUnsignedIntLE(int index) {
      return this.parent.getUnsignedIntLE(index);
   }

   public long getLong(int index) {
      return this.parent.getLong(index);
   }

   public long getLongLE(int index) {
      return this.parent.getLongLE(index);
   }

   public char getChar(int index) {
      return this.parent.getChar(index);
   }

   public float getFloat(int index) {
      return this.parent.getFloat(index);
   }

   public double getDouble(int index) {
      return this.parent.getDouble(index);
   }

   public ByteBuf getBytes(int index, ByteBuf buf) {
      return this.parent.getBytes(index, buf);
   }

   public ByteBuf getBytes(int index, ByteBuf buf, int length) {
      return this.parent.getBytes(index, buf, length);
   }

   public ByteBuf getBytes(int index, ByteBuf buf, int outputIndex, int length) {
      return this.parent.getBytes(index, buf, outputIndex, length);
   }

   public ByteBuf getBytes(int index, byte[] bytes) {
      return this.parent.getBytes(index, bytes);
   }

   public ByteBuf getBytes(int index, byte[] bytes, int outputIndex, int length) {
      return this.parent.getBytes(index, bytes, outputIndex, length);
   }

   public ByteBuf getBytes(int index, ByteBuffer buf) {
      return this.parent.getBytes(index, buf);
   }

   public ByteBuf getBytes(int index, OutputStream stream, int length) throws IOException {
      return this.parent.getBytes(index, stream, length);
   }

   public int getBytes(int index, GatheringByteChannel channel, int length) throws IOException {
      return this.parent.getBytes(index, channel, length);
   }

   public int getBytes(int index, FileChannel channel, long pos, int length) throws IOException {
      return this.parent.getBytes(index, channel, pos, length);
   }

   public CharSequence getCharSequence(int index, int length, Charset charset) {
      return this.parent.getCharSequence(index, length, charset);
   }

   public ByteBuf setBoolean(int index, boolean value) {
      return this.parent.setBoolean(index, value);
   }

   public ByteBuf setByte(int index, int value) {
      return this.parent.setByte(index, value);
   }

   public ByteBuf setShort(int index, int value) {
      return this.parent.setShort(index, value);
   }

   public ByteBuf setShortLE(int index, int value) {
      return this.parent.setShortLE(index, value);
   }

   public ByteBuf setMedium(int index, int value) {
      return this.parent.setMedium(index, value);
   }

   public ByteBuf setMediumLE(int index, int value) {
      return this.parent.setMediumLE(index, value);
   }

   public ByteBuf setInt(int index, int value) {
      return this.parent.setInt(index, value);
   }

   public ByteBuf setIntLE(int index, int value) {
      return this.parent.setIntLE(index, value);
   }

   public ByteBuf setLong(int index, long value) {
      return this.parent.setLong(index, value);
   }

   public ByteBuf setLongLE(int index, long value) {
      return this.parent.setLongLE(index, value);
   }

   public ByteBuf setChar(int index, int value) {
      return this.parent.setChar(index, value);
   }

   public ByteBuf setFloat(int index, float value) {
      return this.parent.setFloat(index, value);
   }

   public ByteBuf setDouble(int index, double value) {
      return this.parent.setDouble(index, value);
   }

   public ByteBuf setBytes(int index, ByteBuf buf) {
      return this.parent.setBytes(index, buf);
   }

   public ByteBuf setBytes(int index, ByteBuf buf, int length) {
      return this.parent.setBytes(index, buf, length);
   }

   public ByteBuf setBytes(int index, ByteBuf buf, int sourceIndex, int length) {
      return this.parent.setBytes(index, buf, sourceIndex, length);
   }

   public ByteBuf setBytes(int index, byte[] bytes) {
      return this.parent.setBytes(index, bytes);
   }

   public ByteBuf setBytes(int index, byte[] bytes, int sourceIndex, int length) {
      return this.parent.setBytes(index, bytes, sourceIndex, length);
   }

   public ByteBuf setBytes(int index, ByteBuffer buf) {
      return this.parent.setBytes(index, buf);
   }

   public int setBytes(int index, InputStream stream, int length) throws IOException {
      return this.parent.setBytes(index, stream, length);
   }

   public int setBytes(int index, ScatteringByteChannel channel, int length) throws IOException {
      return this.parent.setBytes(index, channel, length);
   }

   public int setBytes(int index, FileChannel channel, long pos, int length) throws IOException {
      return this.parent.setBytes(index, channel, pos, length);
   }

   public ByteBuf setZero(int index, int length) {
      return this.parent.setZero(index, length);
   }

   public int setCharSequence(int index, CharSequence sequence, Charset charset) {
      return this.parent.setCharSequence(index, sequence, charset);
   }

   public boolean readBoolean() {
      return this.parent.readBoolean();
   }

   public byte readByte() {
      return this.parent.readByte();
   }

   public short readUnsignedByte() {
      return this.parent.readUnsignedByte();
   }

   public short readShort() {
      return this.parent.readShort();
   }

   public short readShortLE() {
      return this.parent.readShortLE();
   }

   public int readUnsignedShort() {
      return this.parent.readUnsignedShort();
   }

   public int readUnsignedShortLE() {
      return this.parent.readUnsignedShortLE();
   }

   public int readMedium() {
      return this.parent.readMedium();
   }

   public int readMediumLE() {
      return this.parent.readMediumLE();
   }

   public int readUnsignedMedium() {
      return this.parent.readUnsignedMedium();
   }

   public int readUnsignedMediumLE() {
      return this.parent.readUnsignedMediumLE();
   }

   public int readInt() {
      return this.parent.readInt();
   }

   public int readIntLE() {
      return this.parent.readIntLE();
   }

   public long readUnsignedInt() {
      return this.parent.readUnsignedInt();
   }

   public long readUnsignedIntLE() {
      return this.parent.readUnsignedIntLE();
   }

   public long readLong() {
      return this.parent.readLong();
   }

   public long readLongLE() {
      return this.parent.readLongLE();
   }

   public char readChar() {
      return this.parent.readChar();
   }

   public float readFloat() {
      return this.parent.readFloat();
   }

   public double readDouble() {
      return this.parent.readDouble();
   }

   public ByteBuf readBytes(int length) {
      return this.parent.readBytes(length);
   }

   public ByteBuf readSlice(int length) {
      return this.parent.readSlice(length);
   }

   public ByteBuf readRetainedSlice(int length) {
      return this.parent.readRetainedSlice(length);
   }

   public ByteBuf readBytes(ByteBuf buf) {
      return this.parent.readBytes(buf);
   }

   public ByteBuf readBytes(ByteBuf buf, int length) {
      return this.parent.readBytes(buf, length);
   }

   public ByteBuf readBytes(ByteBuf buf, int outputIndex, int length) {
      return this.parent.readBytes(buf, outputIndex, length);
   }

   public ByteBuf readBytes(byte[] bytes) {
      return this.parent.readBytes(bytes);
   }

   public ByteBuf readBytes(byte[] bytes, int outputIndex, int length) {
      return this.parent.readBytes(bytes, outputIndex, length);
   }

   public ByteBuf readBytes(ByteBuffer buf) {
      return this.parent.readBytes(buf);
   }

   public ByteBuf readBytes(OutputStream stream, int length) throws IOException {
      return this.parent.readBytes(stream, length);
   }

   public int readBytes(GatheringByteChannel channel, int length) throws IOException {
      return this.parent.readBytes(channel, length);
   }

   public CharSequence readCharSequence(int length, Charset charset) {
      return this.parent.readCharSequence(length, charset);
   }

   public int readBytes(FileChannel channel, long pos, int length) throws IOException {
      return this.parent.readBytes(channel, pos, length);
   }

   public ByteBuf skipBytes(int length) {
      return this.parent.skipBytes(length);
   }

   public ByteBuf writeBoolean(boolean value) {
      return this.parent.writeBoolean(value);
   }

   public ByteBuf writeByte(int value) {
      return this.parent.writeByte(value);
   }

   public ByteBuf writeShort(int value) {
      return this.parent.writeShort(value);
   }

   public ByteBuf writeShortLE(int value) {
      return this.parent.writeShortLE(value);
   }

   public ByteBuf writeMedium(int value) {
      return this.parent.writeMedium(value);
   }

   public ByteBuf writeMediumLE(int value) {
      return this.parent.writeMediumLE(value);
   }

   public ByteBuf writeInt(int value) {
      return this.parent.writeInt(value);
   }

   public ByteBuf writeIntLE(int value) {
      return this.parent.writeIntLE(value);
   }

   public ByteBuf writeLong(long value) {
      return this.parent.writeLong(value);
   }

   public ByteBuf writeLongLE(long value) {
      return this.parent.writeLongLE(value);
   }

   public ByteBuf writeChar(int value) {
      return this.parent.writeChar(value);
   }

   public ByteBuf writeFloat(float value) {
      return this.parent.writeFloat(value);
   }

   public ByteBuf writeDouble(double value) {
      return this.parent.writeDouble(value);
   }

   public ByteBuf writeBytes(ByteBuf buf) {
      return this.parent.writeBytes(buf);
   }

   public ByteBuf writeBytes(ByteBuf buf, int length) {
      return this.parent.writeBytes(buf, length);
   }

   public ByteBuf writeBytes(ByteBuf buf, int sourceIndex, int length) {
      return this.parent.writeBytes(buf, sourceIndex, length);
   }

   public ByteBuf writeBytes(byte[] bytes) {
      return this.parent.writeBytes(bytes);
   }

   public ByteBuf writeBytes(byte[] bytes, int sourceIndex, int length) {
      return this.parent.writeBytes(bytes, sourceIndex, length);
   }

   public ByteBuf writeBytes(ByteBuffer buf) {
      return this.parent.writeBytes(buf);
   }

   public int writeBytes(InputStream stream, int length) throws IOException {
      return this.parent.writeBytes(stream, length);
   }

   public int writeBytes(ScatteringByteChannel channel, int length) throws IOException {
      return this.parent.writeBytes(channel, length);
   }

   public int writeBytes(FileChannel channel, long pos, int length) throws IOException {
      return this.parent.writeBytes(channel, pos, length);
   }

   public ByteBuf writeZero(int length) {
      return this.parent.writeZero(length);
   }

   public int writeCharSequence(CharSequence sequence, Charset charset) {
      return this.parent.writeCharSequence(sequence, charset);
   }

   public int indexOf(int from, int to, byte value) {
      return this.parent.indexOf(from, to, value);
   }

   public int bytesBefore(byte value) {
      return this.parent.bytesBefore(value);
   }

   public int bytesBefore(int length, byte value) {
      return this.parent.bytesBefore(length, value);
   }

   public int bytesBefore(int index, int length, byte value) {
      return this.parent.bytesBefore(index, length, value);
   }

   public int forEachByte(ByteProcessor byteProcessor) {
      return this.parent.forEachByte(byteProcessor);
   }

   public int forEachByte(int index, int length, ByteProcessor byteProcessor) {
      return this.parent.forEachByte(index, length, byteProcessor);
   }

   public int forEachByteDesc(ByteProcessor byteProcessor) {
      return this.parent.forEachByteDesc(byteProcessor);
   }

   public int forEachByteDesc(int index, int length, ByteProcessor byteProcessor) {
      return this.parent.forEachByteDesc(index, length, byteProcessor);
   }

   public ByteBuf copy() {
      return this.parent.copy();
   }

   public ByteBuf copy(int index, int length) {
      return this.parent.copy(index, length);
   }

   public ByteBuf slice() {
      return this.parent.slice();
   }

   public ByteBuf retainedSlice() {
      return this.parent.retainedSlice();
   }

   public ByteBuf slice(int index, int length) {
      return this.parent.slice(index, length);
   }

   public ByteBuf retainedSlice(int index, int length) {
      return this.parent.retainedSlice(index, length);
   }

   public ByteBuf duplicate() {
      return this.parent.duplicate();
   }

   public ByteBuf retainedDuplicate() {
      return this.parent.retainedDuplicate();
   }

   public int nioBufferCount() {
      return this.parent.nioBufferCount();
   }

   public ByteBuffer nioBuffer() {
      return this.parent.nioBuffer();
   }

   public ByteBuffer nioBuffer(int index, int length) {
      return this.parent.nioBuffer(index, length);
   }

   public ByteBuffer internalNioBuffer(int index, int length) {
      return this.parent.internalNioBuffer(index, length);
   }

   public ByteBuffer[] nioBuffers() {
      return this.parent.nioBuffers();
   }

   public ByteBuffer[] nioBuffers(int index, int length) {
      return this.parent.nioBuffers(index, length);
   }

   public boolean hasArray() {
      return this.parent.hasArray();
   }

   public byte[] array() {
      return this.parent.array();
   }

   public int arrayOffset() {
      return this.parent.arrayOffset();
   }

   public boolean hasMemoryAddress() {
      return this.parent.hasMemoryAddress();
   }

   public long memoryAddress() {
      return this.parent.memoryAddress();
   }

   public String toString(Charset charset) {
      return this.parent.toString(charset);
   }

   public String toString(int index, int length, Charset charset) {
      return this.parent.toString(index, length, charset);
   }

   public int hashCode() {
      return this.parent.hashCode();
   }

   public boolean equals(Object o) {
      return this.parent.equals(o);
   }

   public int compareTo(ByteBuf byteBuf) {
      return this.parent.compareTo(byteBuf);
   }

   public String toString() {
      return this.parent.toString();
   }

   public ByteBuf retain(int i) {
      return this.parent.retain(i);
   }

   public ByteBuf retain() {
      return this.parent.retain();
   }

   public ByteBuf touch() {
      return this.parent.touch();
   }

   public ByteBuf touch(Object object) {
      return this.parent.touch(object);
   }

   public int refCnt() {
      return this.parent.refCnt();
   }

   public boolean release() {
      return this.parent.release();
   }

   public boolean release(int decrement) {
      return this.parent.release(decrement);
   }

   @FunctionalInterface
   public interface PacketWriter extends BiConsumer {
      default PacketWriter asOptional() {
         return (buf, value) -> {
            buf.writeOptional(value, this);
         };
      }
   }

   @FunctionalInterface
   public interface PacketReader extends Function {
      default PacketReader asOptional() {
         return (buf) -> {
            return buf.readOptional(this);
         };
      }
   }
}
