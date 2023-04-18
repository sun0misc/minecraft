package net.minecraft.entity.passive;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EatGrassGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class SheepEntity extends AnimalEntity implements Shearable {
   private static final int MAX_GRASS_TIMER = 40;
   private static final TrackedData COLOR;
   private static final Map DROPS;
   private static final Map COLORS;
   private int eatGrassTimer;
   private EatGrassGoal eatGrassGoal;

   private static float[] getDyedColor(DyeColor color) {
      if (color == DyeColor.WHITE) {
         return new float[]{0.9019608F, 0.9019608F, 0.9019608F};
      } else {
         float[] fs = color.getColorComponents();
         float f = 0.75F;
         return new float[]{fs[0] * 0.75F, fs[1] * 0.75F, fs[2] * 0.75F};
      }
   }

   public static float[] getRgbColor(DyeColor dyeColor) {
      return (float[])COLORS.get(dyeColor);
   }

   public SheepEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   protected void initGoals() {
      this.eatGrassGoal = new EatGrassGoal(this);
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(1, new EscapeDangerGoal(this, 1.25));
      this.goalSelector.add(2, new AnimalMateGoal(this, 1.0));
      this.goalSelector.add(3, new TemptGoal(this, 1.1, Ingredient.ofItems(Items.WHEAT), false));
      this.goalSelector.add(4, new FollowParentGoal(this, 1.1));
      this.goalSelector.add(5, this.eatGrassGoal);
      this.goalSelector.add(6, new WanderAroundFarGoal(this, 1.0));
      this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.add(8, new LookAroundGoal(this));
   }

   protected void mobTick() {
      this.eatGrassTimer = this.eatGrassGoal.getTimer();
      super.mobTick();
   }

   public void tickMovement() {
      if (this.world.isClient) {
         this.eatGrassTimer = Math.max(0, this.eatGrassTimer - 1);
      }

      super.tickMovement();
   }

   public static DefaultAttributeContainer.Builder createSheepAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23000000417232513);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(COLOR, (byte)0);
   }

   public Identifier getLootTableId() {
      if (this.isSheared()) {
         return this.getType().getLootTableId();
      } else {
         Identifier var10000;
         switch (this.getColor()) {
            case WHITE:
               var10000 = LootTables.WHITE_SHEEP_ENTITY;
               break;
            case ORANGE:
               var10000 = LootTables.ORANGE_SHEEP_ENTITY;
               break;
            case MAGENTA:
               var10000 = LootTables.MAGENTA_SHEEP_ENTITY;
               break;
            case LIGHT_BLUE:
               var10000 = LootTables.LIGHT_BLUE_SHEEP_ENTITY;
               break;
            case YELLOW:
               var10000 = LootTables.YELLOW_SHEEP_ENTITY;
               break;
            case LIME:
               var10000 = LootTables.LIME_SHEEP_ENTITY;
               break;
            case PINK:
               var10000 = LootTables.PINK_SHEEP_ENTITY;
               break;
            case GRAY:
               var10000 = LootTables.GRAY_SHEEP_ENTITY;
               break;
            case LIGHT_GRAY:
               var10000 = LootTables.LIGHT_GRAY_SHEEP_ENTITY;
               break;
            case CYAN:
               var10000 = LootTables.CYAN_SHEEP_ENTITY;
               break;
            case PURPLE:
               var10000 = LootTables.PURPLE_SHEEP_ENTITY;
               break;
            case BLUE:
               var10000 = LootTables.BLUE_SHEEP_ENTITY;
               break;
            case BROWN:
               var10000 = LootTables.BROWN_SHEEP_ENTITY;
               break;
            case GREEN:
               var10000 = LootTables.GREEN_SHEEP_ENTITY;
               break;
            case RED:
               var10000 = LootTables.RED_SHEEP_ENTITY;
               break;
            case BLACK:
               var10000 = LootTables.BLACK_SHEEP_ENTITY;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }
   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.SET_SHEEP_EAT_GRASS_TIMER_OR_PRIME_TNT_MINECART) {
         this.eatGrassTimer = 40;
      } else {
         super.handleStatus(status);
      }

   }

   public float getNeckAngle(float delta) {
      if (this.eatGrassTimer <= 0) {
         return 0.0F;
      } else if (this.eatGrassTimer >= 4 && this.eatGrassTimer <= 36) {
         return 1.0F;
      } else {
         return this.eatGrassTimer < 4 ? ((float)this.eatGrassTimer - delta) / 4.0F : -((float)(this.eatGrassTimer - 40) - delta) / 4.0F;
      }
   }

   public float getHeadAngle(float delta) {
      if (this.eatGrassTimer > 4 && this.eatGrassTimer <= 36) {
         float g = ((float)(this.eatGrassTimer - 4) - delta) / 32.0F;
         return 0.62831855F + 0.21991149F * MathHelper.sin(g * 28.7F);
      } else {
         return this.eatGrassTimer > 0 ? 0.62831855F : this.getPitch() * 0.017453292F;
      }
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      if (lv.isOf(Items.SHEARS)) {
         if (!this.world.isClient && this.isShearable()) {
            this.sheared(SoundCategory.PLAYERS);
            this.emitGameEvent(GameEvent.SHEAR, player);
            lv.damage(1, (LivingEntity)player, (Consumer)((playerx) -> {
               playerx.sendToolBreakStatus(hand);
            }));
            return ActionResult.SUCCESS;
         } else {
            return ActionResult.CONSUME;
         }
      } else {
         return super.interactMob(player, hand);
      }
   }

   public void sheared(SoundCategory shearedSoundCategory) {
      this.world.playSoundFromEntity((PlayerEntity)null, this, SoundEvents.ENTITY_SHEEP_SHEAR, shearedSoundCategory, 1.0F, 1.0F);
      this.setSheared(true);
      int i = 1 + this.random.nextInt(3);

      for(int j = 0; j < i; ++j) {
         ItemEntity lv = this.dropItem((ItemConvertible)DROPS.get(this.getColor()), 1);
         if (lv != null) {
            lv.setVelocity(lv.getVelocity().add((double)((this.random.nextFloat() - this.random.nextFloat()) * 0.1F), (double)(this.random.nextFloat() * 0.05F), (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.1F)));
         }
      }

   }

   public boolean isShearable() {
      return this.isAlive() && !this.isSheared() && !this.isBaby();
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("Sheared", this.isSheared());
      nbt.putByte("Color", (byte)this.getColor().getId());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.setSheared(nbt.getBoolean("Sheared"));
      this.setColor(DyeColor.byId(nbt.getByte("Color")));
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_SHEEP_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_SHEEP_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_SHEEP_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_SHEEP_STEP, 0.15F, 1.0F);
   }

   public DyeColor getColor() {
      return DyeColor.byId((Byte)this.dataTracker.get(COLOR) & 15);
   }

   public void setColor(DyeColor color) {
      byte b = (Byte)this.dataTracker.get(COLOR);
      this.dataTracker.set(COLOR, (byte)(b & 240 | color.getId() & 15));
   }

   public boolean isSheared() {
      return ((Byte)this.dataTracker.get(COLOR) & 16) != 0;
   }

   public void setSheared(boolean sheared) {
      byte b = (Byte)this.dataTracker.get(COLOR);
      if (sheared) {
         this.dataTracker.set(COLOR, (byte)(b | 16));
      } else {
         this.dataTracker.set(COLOR, (byte)(b & -17));
      }

   }

   public static DyeColor generateDefaultColor(Random random) {
      int i = random.nextInt(100);
      if (i < 5) {
         return DyeColor.BLACK;
      } else if (i < 10) {
         return DyeColor.GRAY;
      } else if (i < 15) {
         return DyeColor.LIGHT_GRAY;
      } else if (i < 18) {
         return DyeColor.BROWN;
      } else {
         return random.nextInt(500) == 0 ? DyeColor.PINK : DyeColor.WHITE;
      }
   }

   @Nullable
   public SheepEntity createChild(ServerWorld arg, PassiveEntity arg2) {
      SheepEntity lv = (SheepEntity)EntityType.SHEEP.create(arg);
      if (lv != null) {
         lv.setColor(this.getChildColor(this, (SheepEntity)arg2));
      }

      return lv;
   }

   public void onEatingGrass() {
      super.onEatingGrass();
      this.setSheared(false);
      if (this.isBaby()) {
         this.growUp(60);
      }

   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      this.setColor(generateDefaultColor(world.getRandom()));
      return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
   }

   private DyeColor getChildColor(AnimalEntity firstParent, AnimalEntity secondParent) {
      DyeColor lv = ((SheepEntity)firstParent).getColor();
      DyeColor lv2 = ((SheepEntity)secondParent).getColor();
      CraftingInventory lv3 = createDyeMixingCraftingInventory(lv, lv2);
      Optional var10000 = this.world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, lv3, this.world).map((recipe) -> {
         return recipe.craft(lv3, this.world.getRegistryManager());
      }).map(ItemStack::getItem);
      Objects.requireNonNull(DyeItem.class);
      var10000 = var10000.filter(DyeItem.class::isInstance);
      Objects.requireNonNull(DyeItem.class);
      return (DyeColor)var10000.map(DyeItem.class::cast).map(DyeItem::getColor).orElseGet(() -> {
         return this.world.random.nextBoolean() ? lv : lv2;
      });
   }

   private static CraftingInventory createDyeMixingCraftingInventory(DyeColor firstColor, DyeColor secondColor) {
      CraftingInventory lv = new CraftingInventory(new ScreenHandler((ScreenHandlerType)null, -1) {
         public ItemStack quickMove(PlayerEntity player, int slot) {
            return ItemStack.EMPTY;
         }

         public boolean canUse(PlayerEntity player) {
            return false;
         }
      }, 2, 1);
      lv.setStack(0, new ItemStack(DyeItem.byColor(firstColor)));
      lv.setStack(1, new ItemStack(DyeItem.byColor(secondColor)));
      return lv;
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 0.95F * dimensions.height;
   }

   // $FF: synthetic method
   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return this.createChild(world, entity);
   }

   static {
      COLOR = DataTracker.registerData(SheepEntity.class, TrackedDataHandlerRegistry.BYTE);
      DROPS = (Map)Util.make(Maps.newEnumMap(DyeColor.class), (map) -> {
         map.put(DyeColor.WHITE, Blocks.WHITE_WOOL);
         map.put(DyeColor.ORANGE, Blocks.ORANGE_WOOL);
         map.put(DyeColor.MAGENTA, Blocks.MAGENTA_WOOL);
         map.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
         map.put(DyeColor.YELLOW, Blocks.YELLOW_WOOL);
         map.put(DyeColor.LIME, Blocks.LIME_WOOL);
         map.put(DyeColor.PINK, Blocks.PINK_WOOL);
         map.put(DyeColor.GRAY, Blocks.GRAY_WOOL);
         map.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
         map.put(DyeColor.CYAN, Blocks.CYAN_WOOL);
         map.put(DyeColor.PURPLE, Blocks.PURPLE_WOOL);
         map.put(DyeColor.BLUE, Blocks.BLUE_WOOL);
         map.put(DyeColor.BROWN, Blocks.BROWN_WOOL);
         map.put(DyeColor.GREEN, Blocks.GREEN_WOOL);
         map.put(DyeColor.RED, Blocks.RED_WOOL);
         map.put(DyeColor.BLACK, Blocks.BLACK_WOOL);
      });
      COLORS = Maps.newEnumMap((Map)Arrays.stream(DyeColor.values()).collect(Collectors.toMap((color) -> {
         return color;
      }, SheepEntity::getDyedColor)));
   }
}
