package net.minecraft.entity.decoration.painting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.PaintingVariantTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PaintingEntity extends AbstractDecorationEntity implements VariantHolder {
   private static final TrackedData VARIANT;
   private static final RegistryKey DEFAULT_VARIANT;
   public static final String VARIANT_NBT_KEY = "variant";

   private static RegistryEntry getDefaultVariant() {
      return Registries.PAINTING_VARIANT.entryOf(DEFAULT_VARIANT);
   }

   public PaintingEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   protected void initDataTracker() {
      this.dataTracker.startTracking(VARIANT, getDefaultVariant());
   }

   public void onTrackedDataSet(TrackedData data) {
      if (VARIANT.equals(data)) {
         this.updateAttachmentPosition();
      }

   }

   public void setVariant(RegistryEntry variant) {
      this.dataTracker.set(VARIANT, variant);
   }

   public RegistryEntry getVariant() {
      return (RegistryEntry)this.dataTracker.get(VARIANT);
   }

   public static Optional placePainting(World world, BlockPos pos, Direction facing) {
      PaintingEntity lv = new PaintingEntity(world, pos);
      List list = new ArrayList();
      Iterable var10000 = Registries.PAINTING_VARIANT.iterateEntries(PaintingVariantTags.PLACEABLE);
      Objects.requireNonNull(list);
      var10000.forEach(list::add);
      if (list.isEmpty()) {
         return Optional.empty();
      } else {
         lv.setFacing(facing);
         list.removeIf((variant) -> {
            lv.setVariant(variant);
            return !lv.canStayAttached();
         });
         if (list.isEmpty()) {
            return Optional.empty();
         } else {
            int i = list.stream().mapToInt(PaintingEntity::getSize).max().orElse(0);
            list.removeIf((variant) -> {
               return getSize(variant) < i;
            });
            Optional optional = Util.getRandomOrEmpty(list, lv.random);
            if (optional.isEmpty()) {
               return Optional.empty();
            } else {
               lv.setVariant((RegistryEntry)optional.get());
               lv.setFacing(facing);
               return Optional.of(lv);
            }
         }
      }
   }

   private static int getSize(RegistryEntry variant) {
      return ((PaintingVariant)variant.value()).getWidth() * ((PaintingVariant)variant.value()).getHeight();
   }

   private PaintingEntity(World world, BlockPos pos) {
      super(EntityType.PAINTING, world, pos);
   }

   public PaintingEntity(World world, BlockPos pos, Direction direction, RegistryEntry variant) {
      this(world, pos);
      this.setVariant(variant);
      this.setFacing(direction);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      writeVariantToNbt(nbt, this.getVariant());
      nbt.putByte("facing", (byte)this.facing.getHorizontal());
      super.writeCustomDataToNbt(nbt);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      RegistryEntry lv = (RegistryEntry)readVariantFromNbt(nbt).orElseGet(PaintingEntity::getDefaultVariant);
      this.setVariant(lv);
      this.facing = Direction.fromHorizontal(nbt.getByte("facing"));
      super.readCustomDataFromNbt(nbt);
      this.setFacing(this.facing);
   }

   public static void writeVariantToNbt(NbtCompound nbt, RegistryEntry variant) {
      nbt.putString("variant", ((RegistryKey)variant.getKey().orElse(DEFAULT_VARIANT)).getValue().toString());
   }

   public static Optional readVariantFromNbt(NbtCompound nbt) {
      Optional var10000 = Optional.ofNullable(Identifier.tryParse(nbt.getString("variant"))).map((id) -> {
         return RegistryKey.of(RegistryKeys.PAINTING_VARIANT, id);
      });
      DefaultedRegistry var10001 = Registries.PAINTING_VARIANT;
      Objects.requireNonNull(var10001);
      return var10000.flatMap(var10001::getEntry);
   }

   public int getWidthPixels() {
      return ((PaintingVariant)this.getVariant().value()).getWidth();
   }

   public int getHeightPixels() {
      return ((PaintingVariant)this.getVariant().value()).getHeight();
   }

   public void onBreak(@Nullable Entity entity) {
      if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
         this.playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0F, 1.0F);
         if (entity instanceof PlayerEntity) {
            PlayerEntity lv = (PlayerEntity)entity;
            if (lv.getAbilities().creativeMode) {
               return;
            }
         }

         this.dropItem(Items.PAINTING);
      }
   }

   public void onPlace() {
      this.playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0F, 1.0F);
   }

   public void refreshPositionAndAngles(double x, double y, double z, float yaw, float pitch) {
      this.setPosition(x, y, z);
   }

   public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
      this.setPosition(x, y, z);
   }

   public Vec3d getSyncedPos() {
      return Vec3d.of(this.attachmentPos);
   }

   public Packet createSpawnPacket() {
      return new EntitySpawnS2CPacket(this, this.facing.getId(), this.getDecorationBlockPos());
   }

   public void onSpawnPacket(EntitySpawnS2CPacket packet) {
      super.onSpawnPacket(packet);
      this.setFacing(Direction.byId(packet.getEntityData()));
   }

   public ItemStack getPickBlockStack() {
      return new ItemStack(Items.PAINTING);
   }

   // $FF: synthetic method
   public Object getVariant() {
      return this.getVariant();
   }

   static {
      VARIANT = DataTracker.registerData(PaintingEntity.class, TrackedDataHandlerRegistry.PAINTING_VARIANT);
      DEFAULT_VARIANT = PaintingVariants.KEBAB;
   }
}
