package net.minecraft.entity.data;

import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import org.jetbrains.annotations.Nullable;

public class TrackedDataHandlerRegistry {
   private static final Int2ObjectBiMap DATA_HANDLERS = Int2ObjectBiMap.create(16);
   public static final TrackedDataHandler BYTE = TrackedDataHandler.of((buf, byte_) -> {
      buf.writeByte(byte_);
   }, PacketByteBuf::readByte);
   public static final TrackedDataHandler INTEGER = TrackedDataHandler.of(PacketByteBuf::writeVarInt, PacketByteBuf::readVarInt);
   public static final TrackedDataHandler LONG = TrackedDataHandler.of(PacketByteBuf::writeVarLong, PacketByteBuf::readVarLong);
   public static final TrackedDataHandler FLOAT = TrackedDataHandler.of(PacketByteBuf::writeFloat, PacketByteBuf::readFloat);
   public static final TrackedDataHandler STRING = TrackedDataHandler.of(PacketByteBuf::writeString, PacketByteBuf::readString);
   public static final TrackedDataHandler TEXT_COMPONENT = TrackedDataHandler.of(PacketByteBuf::writeText, PacketByteBuf::readText);
   public static final TrackedDataHandler OPTIONAL_TEXT_COMPONENT = TrackedDataHandler.ofOptional(PacketByteBuf::writeText, PacketByteBuf::readText);
   public static final TrackedDataHandler ITEM_STACK = new TrackedDataHandler() {
      public void write(PacketByteBuf arg, ItemStack arg2) {
         arg.writeItemStack(arg2);
      }

      public ItemStack read(PacketByteBuf arg) {
         return arg.readItemStack();
      }

      public ItemStack copy(ItemStack arg) {
         return arg.copy();
      }

      // $FF: synthetic method
      public Object read(PacketByteBuf buf) {
         return this.read(buf);
      }
   };
   public static final TrackedDataHandler BLOCK_STATE;
   public static final TrackedDataHandler OPTIONAL_BLOCK_STATE;
   public static final TrackedDataHandler BOOLEAN;
   public static final TrackedDataHandler PARTICLE;
   public static final TrackedDataHandler ROTATION;
   public static final TrackedDataHandler BLOCK_POS;
   public static final TrackedDataHandler OPTIONAL_BLOCK_POS;
   public static final TrackedDataHandler FACING;
   public static final TrackedDataHandler OPTIONAL_UUID;
   public static final TrackedDataHandler OPTIONAL_GLOBAL_POS;
   public static final TrackedDataHandler NBT_COMPOUND;
   public static final TrackedDataHandler VILLAGER_DATA;
   public static final TrackedDataHandler OPTIONAL_INT;
   public static final TrackedDataHandler ENTITY_POSE;
   public static final TrackedDataHandler CAT_VARIANT;
   public static final TrackedDataHandler FROG_VARIANT;
   public static final TrackedDataHandler PAINTING_VARIANT;
   public static final TrackedDataHandler SNIFFER_STATE;
   public static final TrackedDataHandler VECTOR3F;
   public static final TrackedDataHandler QUATERNIONF;

   public static void register(TrackedDataHandler handler) {
      DATA_HANDLERS.add(handler);
   }

   @Nullable
   public static TrackedDataHandler get(int id) {
      return (TrackedDataHandler)DATA_HANDLERS.get(id);
   }

   public static int getId(TrackedDataHandler handler) {
      return DATA_HANDLERS.getRawId(handler);
   }

   private TrackedDataHandlerRegistry() {
   }

   static {
      BLOCK_STATE = TrackedDataHandler.of(Block.STATE_IDS);
      OPTIONAL_BLOCK_STATE = new TrackedDataHandler.ImmutableHandler() {
         public void write(PacketByteBuf arg, Optional optional) {
            if (optional.isPresent()) {
               arg.writeVarInt(Block.getRawIdFromState((BlockState)optional.get()));
            } else {
               arg.writeVarInt(0);
            }

         }

         public Optional read(PacketByteBuf arg) {
            int i = arg.readVarInt();
            return i == 0 ? Optional.empty() : Optional.of(Block.getStateFromRawId(i));
         }

         // $FF: synthetic method
         public Object read(PacketByteBuf buf) {
            return this.read(buf);
         }
      };
      BOOLEAN = TrackedDataHandler.of(PacketByteBuf::writeBoolean, PacketByteBuf::readBoolean);
      PARTICLE = new TrackedDataHandler.ImmutableHandler() {
         public void write(PacketByteBuf arg, ParticleEffect arg2) {
            arg.writeRegistryValue(Registries.PARTICLE_TYPE, arg2.getType());
            arg2.write(arg);
         }

         public ParticleEffect read(PacketByteBuf arg) {
            return this.read(arg, (ParticleType)arg.readRegistryValue(Registries.PARTICLE_TYPE));
         }

         private ParticleEffect read(PacketByteBuf buf, ParticleType type) {
            return type.getParametersFactory().read(type, buf);
         }

         // $FF: synthetic method
         public Object read(PacketByteBuf buf) {
            return this.read(buf);
         }
      };
      ROTATION = new TrackedDataHandler.ImmutableHandler() {
         public void write(PacketByteBuf arg, EulerAngle arg2) {
            arg.writeFloat(arg2.getPitch());
            arg.writeFloat(arg2.getYaw());
            arg.writeFloat(arg2.getRoll());
         }

         public EulerAngle read(PacketByteBuf arg) {
            return new EulerAngle(arg.readFloat(), arg.readFloat(), arg.readFloat());
         }

         // $FF: synthetic method
         public Object read(PacketByteBuf buf) {
            return this.read(buf);
         }
      };
      BLOCK_POS = TrackedDataHandler.of(PacketByteBuf::writeBlockPos, PacketByteBuf::readBlockPos);
      OPTIONAL_BLOCK_POS = TrackedDataHandler.ofOptional(PacketByteBuf::writeBlockPos, PacketByteBuf::readBlockPos);
      FACING = TrackedDataHandler.ofEnum(Direction.class);
      OPTIONAL_UUID = TrackedDataHandler.ofOptional(PacketByteBuf::writeUuid, PacketByteBuf::readUuid);
      OPTIONAL_GLOBAL_POS = TrackedDataHandler.ofOptional(PacketByteBuf::writeGlobalPos, PacketByteBuf::readGlobalPos);
      NBT_COMPOUND = new TrackedDataHandler() {
         public void write(PacketByteBuf arg, NbtCompound arg2) {
            arg.writeNbt(arg2);
         }

         public NbtCompound read(PacketByteBuf arg) {
            return arg.readNbt();
         }

         public NbtCompound copy(NbtCompound arg) {
            return arg.copy();
         }

         // $FF: synthetic method
         public Object read(PacketByteBuf buf) {
            return this.read(buf);
         }
      };
      VILLAGER_DATA = new TrackedDataHandler.ImmutableHandler() {
         public void write(PacketByteBuf arg, VillagerData arg2) {
            arg.writeRegistryValue(Registries.VILLAGER_TYPE, arg2.getType());
            arg.writeRegistryValue(Registries.VILLAGER_PROFESSION, arg2.getProfession());
            arg.writeVarInt(arg2.getLevel());
         }

         public VillagerData read(PacketByteBuf arg) {
            return new VillagerData((VillagerType)arg.readRegistryValue(Registries.VILLAGER_TYPE), (VillagerProfession)arg.readRegistryValue(Registries.VILLAGER_PROFESSION), arg.readVarInt());
         }

         // $FF: synthetic method
         public Object read(PacketByteBuf buf) {
            return this.read(buf);
         }
      };
      OPTIONAL_INT = new TrackedDataHandler.ImmutableHandler() {
         public void write(PacketByteBuf arg, OptionalInt optionalInt) {
            arg.writeVarInt(optionalInt.orElse(-1) + 1);
         }

         public OptionalInt read(PacketByteBuf arg) {
            int i = arg.readVarInt();
            return i == 0 ? OptionalInt.empty() : OptionalInt.of(i - 1);
         }

         // $FF: synthetic method
         public Object read(PacketByteBuf buf) {
            return this.read(buf);
         }
      };
      ENTITY_POSE = TrackedDataHandler.ofEnum(EntityPose.class);
      CAT_VARIANT = TrackedDataHandler.of(Registries.CAT_VARIANT);
      FROG_VARIANT = TrackedDataHandler.of(Registries.FROG_VARIANT);
      PAINTING_VARIANT = TrackedDataHandler.of(Registries.PAINTING_VARIANT.getIndexedEntries());
      SNIFFER_STATE = TrackedDataHandler.ofEnum(SnifferEntity.State.class);
      VECTOR3F = TrackedDataHandler.of(PacketByteBuf::writeVector3f, PacketByteBuf::readVector3f);
      QUATERNIONF = TrackedDataHandler.of(PacketByteBuf::writeQuaternionf, PacketByteBuf::readQuaternionf);
      register(BYTE);
      register(INTEGER);
      register(LONG);
      register(FLOAT);
      register(STRING);
      register(TEXT_COMPONENT);
      register(OPTIONAL_TEXT_COMPONENT);
      register(ITEM_STACK);
      register(BOOLEAN);
      register(ROTATION);
      register(BLOCK_POS);
      register(OPTIONAL_BLOCK_POS);
      register(FACING);
      register(OPTIONAL_UUID);
      register(BLOCK_STATE);
      register(OPTIONAL_BLOCK_STATE);
      register(NBT_COMPOUND);
      register(PARTICLE);
      register(VILLAGER_DATA);
      register(OPTIONAL_INT);
      register(ENTITY_POSE);
      register(CAT_VARIANT);
      register(FROG_VARIANT);
      register(OPTIONAL_GLOBAL_POS);
      register(PAINTING_VARIANT);
      register(SNIFFER_STATE);
      register(VECTOR3F);
      register(QUATERNIONF);
   }
}
