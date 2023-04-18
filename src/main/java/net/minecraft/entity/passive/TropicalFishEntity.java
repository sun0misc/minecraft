package net.minecraft.entity.passive;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class TropicalFishEntity extends SchoolingFishEntity implements VariantHolder {
   public static final String BUCKET_VARIANT_TAG_KEY = "BucketVariantTag";
   private static final TrackedData VARIANT;
   public static final List COMMON_VARIANTS;
   private boolean commonSpawn = true;

   public TropicalFishEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public static String getToolTipForVariant(int variant) {
      return "entity.minecraft.tropical_fish.predefined." + variant;
   }

   static int getVariantId(Variety variety, DyeColor baseColor, DyeColor patternColor) {
      return variety.getId() & '\uffff' | (baseColor.getId() & 255) << 16 | (patternColor.getId() & 255) << 24;
   }

   public static DyeColor getBaseDyeColor(int variant) {
      return DyeColor.byId(variant >> 16 & 255);
   }

   public static DyeColor getPatternDyeColor(int variant) {
      return DyeColor.byId(variant >> 24 & 255);
   }

   public static Variety getVariety(int variant) {
      return TropicalFishEntity.Variety.fromId(variant & '\uffff');
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(VARIANT, 0);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("Variant", this.getTropicalFishVariant());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.setTropicalFishVariant(nbt.getInt("Variant"));
   }

   private void setTropicalFishVariant(int variant) {
      this.dataTracker.set(VARIANT, variant);
   }

   public boolean spawnsTooManyForEachTry(int count) {
      return !this.commonSpawn;
   }

   private int getTropicalFishVariant() {
      return (Integer)this.dataTracker.get(VARIANT);
   }

   public DyeColor getBaseColorComponents() {
      return getBaseDyeColor(this.getTropicalFishVariant());
   }

   public DyeColor getPatternColorComponents() {
      return getPatternDyeColor(this.getTropicalFishVariant());
   }

   public Variety getVariant() {
      return getVariety(this.getTropicalFishVariant());
   }

   public void setVariant(Variety arg) {
      int i = this.getTropicalFishVariant();
      DyeColor lv = getBaseDyeColor(i);
      DyeColor lv2 = getPatternDyeColor(i);
      this.setTropicalFishVariant(getVariantId(arg, lv, lv2));
   }

   public void copyDataToStack(ItemStack stack) {
      super.copyDataToStack(stack);
      NbtCompound lv = stack.getOrCreateNbt();
      lv.putInt("BucketVariantTag", this.getTropicalFishVariant());
   }

   public ItemStack getBucketItem() {
      return new ItemStack(Items.TROPICAL_FISH_BUCKET);
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_TROPICAL_FISH_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_TROPICAL_FISH_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_TROPICAL_FISH_HURT;
   }

   protected SoundEvent getFlopSound() {
      return SoundEvents.ENTITY_TROPICAL_FISH_FLOP;
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      EntityData entityData = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
      if (spawnReason == SpawnReason.BUCKET && entityNbt != null && entityNbt.contains("BucketVariantTag", NbtElement.INT_TYPE)) {
         this.setTropicalFishVariant(entityNbt.getInt("BucketVariantTag"));
         return (EntityData)entityData;
      } else {
         Random lv = world.getRandom();
         Variant lv3;
         if (entityData instanceof TropicalFishData) {
            TropicalFishData lv2 = (TropicalFishData)entityData;
            lv3 = lv2.variant;
         } else if ((double)lv.nextFloat() < 0.9) {
            lv3 = (Variant)Util.getRandom(COMMON_VARIANTS, lv);
            entityData = new TropicalFishData(this, lv3);
         } else {
            this.commonSpawn = false;
            Variety[] lvs = TropicalFishEntity.Variety.values();
            DyeColor[] lvs2 = DyeColor.values();
            Variety lv4 = (Variety)Util.getRandom((Object[])lvs, lv);
            DyeColor lv5 = (DyeColor)Util.getRandom((Object[])lvs2, lv);
            DyeColor lv6 = (DyeColor)Util.getRandom((Object[])lvs2, lv);
            lv3 = new Variant(lv4, lv5, lv6);
         }

         this.setTropicalFishVariant(lv3.getId());
         return (EntityData)entityData;
      }
   }

   public static boolean canTropicalFishSpawn(EntityType type, WorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
      return world.getFluidState(pos.down()).isIn(FluidTags.WATER) && world.getBlockState(pos.up()).isOf(Blocks.WATER) && (world.getBiome(pos).isIn(BiomeTags.ALLOWS_TROPICAL_FISH_SPAWNS_AT_ANY_HEIGHT) || WaterCreatureEntity.canSpawn(type, world, reason, pos, random));
   }

   // $FF: synthetic method
   public Object getVariant() {
      return this.getVariant();
   }

   static {
      VARIANT = DataTracker.registerData(TropicalFishEntity.class, TrackedDataHandlerRegistry.INTEGER);
      COMMON_VARIANTS = List.of(new Variant(TropicalFishEntity.Variety.STRIPEY, DyeColor.ORANGE, DyeColor.GRAY), new Variant(TropicalFishEntity.Variety.FLOPPER, DyeColor.GRAY, DyeColor.GRAY), new Variant(TropicalFishEntity.Variety.FLOPPER, DyeColor.GRAY, DyeColor.BLUE), new Variant(TropicalFishEntity.Variety.CLAYFISH, DyeColor.WHITE, DyeColor.GRAY), new Variant(TropicalFishEntity.Variety.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY), new Variant(TropicalFishEntity.Variety.KOB, DyeColor.ORANGE, DyeColor.WHITE), new Variant(TropicalFishEntity.Variety.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE), new Variant(TropicalFishEntity.Variety.BLOCKFISH, DyeColor.PURPLE, DyeColor.YELLOW), new Variant(TropicalFishEntity.Variety.CLAYFISH, DyeColor.WHITE, DyeColor.RED), new Variant(TropicalFishEntity.Variety.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW), new Variant(TropicalFishEntity.Variety.GLITTER, DyeColor.WHITE, DyeColor.GRAY), new Variant(TropicalFishEntity.Variety.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE), new Variant(TropicalFishEntity.Variety.DASHER, DyeColor.CYAN, DyeColor.PINK), new Variant(TropicalFishEntity.Variety.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE), new Variant(TropicalFishEntity.Variety.BETTY, DyeColor.RED, DyeColor.WHITE), new Variant(TropicalFishEntity.Variety.SNOOPER, DyeColor.GRAY, DyeColor.RED), new Variant(TropicalFishEntity.Variety.BLOCKFISH, DyeColor.RED, DyeColor.WHITE), new Variant(TropicalFishEntity.Variety.FLOPPER, DyeColor.WHITE, DyeColor.YELLOW), new Variant(TropicalFishEntity.Variety.KOB, DyeColor.RED, DyeColor.WHITE), new Variant(TropicalFishEntity.Variety.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE), new Variant(TropicalFishEntity.Variety.DASHER, DyeColor.CYAN, DyeColor.YELLOW), new Variant(TropicalFishEntity.Variety.FLOPPER, DyeColor.YELLOW, DyeColor.YELLOW));
   }

   public static enum Variety implements StringIdentifiable {
      KOB("kob", TropicalFishEntity.Size.SMALL, 0),
      SUNSTREAK("sunstreak", TropicalFishEntity.Size.SMALL, 1),
      SNOOPER("snooper", TropicalFishEntity.Size.SMALL, 2),
      DASHER("dasher", TropicalFishEntity.Size.SMALL, 3),
      BRINELY("brinely", TropicalFishEntity.Size.SMALL, 4),
      SPOTTY("spotty", TropicalFishEntity.Size.SMALL, 5),
      FLOPPER("flopper", TropicalFishEntity.Size.LARGE, 0),
      STRIPEY("stripey", TropicalFishEntity.Size.LARGE, 1),
      GLITTER("glitter", TropicalFishEntity.Size.LARGE, 2),
      BLOCKFISH("blockfish", TropicalFishEntity.Size.LARGE, 3),
      BETTY("betty", TropicalFishEntity.Size.LARGE, 4),
      CLAYFISH("clayfish", TropicalFishEntity.Size.LARGE, 5);

      public static final Codec CODEC = StringIdentifiable.createCodec(Variety::values);
      private static final IntFunction BY_ID = ValueLists.createIdToValueFunction(Variety::getId, values(), (Object)KOB);
      private final String name;
      private final Text text;
      private final Size size;
      private final int id;

      private Variety(String name, Size size, int id) {
         this.name = name;
         this.size = size;
         this.id = size.id | id << 8;
         this.text = Text.translatable("entity.minecraft.tropical_fish.type." + this.name);
      }

      public static Variety fromId(int id) {
         return (Variety)BY_ID.apply(id);
      }

      public Size getSize() {
         return this.size;
      }

      public int getId() {
         return this.id;
      }

      public String asString() {
         return this.name;
      }

      public Text getText() {
         return this.text;
      }

      // $FF: synthetic method
      private static Variety[] method_36643() {
         return new Variety[]{KOB, SUNSTREAK, SNOOPER, DASHER, BRINELY, SPOTTY, FLOPPER, STRIPEY, GLITTER, BLOCKFISH, BETTY, CLAYFISH};
      }
   }

   private static class TropicalFishData extends SchoolingFishEntity.FishData {
      final Variant variant;

      TropicalFishData(TropicalFishEntity leader, Variant variant) {
         super(leader);
         this.variant = variant;
      }
   }

   public static record Variant(Variety variety, DyeColor baseColor, DyeColor patternColor) {
      public Variant(Variety arg, DyeColor arg2, DyeColor arg3) {
         this.variety = arg;
         this.baseColor = arg2;
         this.patternColor = arg3;
      }

      public int getId() {
         return TropicalFishEntity.getVariantId(this.variety, this.baseColor, this.patternColor);
      }

      public Variety variety() {
         return this.variety;
      }

      public DyeColor baseColor() {
         return this.baseColor;
      }

      public DyeColor patternColor() {
         return this.patternColor;
      }
   }

   public static enum Size {
      SMALL(0),
      LARGE(1);

      final int id;

      private Size(int id) {
         this.id = id;
      }

      // $FF: synthetic method
      private static Size[] method_47866() {
         return new Size[]{SMALL, LARGE};
      }
   }
}
