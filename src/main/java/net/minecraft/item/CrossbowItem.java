package net.minecraft.item;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CrossbowItem extends RangedWeaponItem implements Vanishable {
   private static final String CHARGED_KEY = "Charged";
   private static final String CHARGED_PROJECTILES_KEY = "ChargedProjectiles";
   private static final int DEFAULT_PULL_TIME = 25;
   public static final int RANGE = 8;
   private boolean charged = false;
   private boolean loaded = false;
   private static final float field_30867 = 0.2F;
   private static final float field_30868 = 0.5F;
   private static final float DEFAULT_SPEED = 3.15F;
   private static final float FIREWORK_ROCKET_SPEED = 1.6F;

   public CrossbowItem(Item.Settings arg) {
      super(arg);
   }

   public Predicate getHeldProjectiles() {
      return CROSSBOW_HELD_PROJECTILES;
   }

   public Predicate getProjectiles() {
      return BOW_PROJECTILES;
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      ItemStack lv = user.getStackInHand(hand);
      if (isCharged(lv)) {
         shootAll(world, user, hand, lv, getSpeed(lv), 1.0F);
         setCharged(lv, false);
         return TypedActionResult.consume(lv);
      } else if (!user.getProjectileType(lv).isEmpty()) {
         if (!isCharged(lv)) {
            this.charged = false;
            this.loaded = false;
            user.setCurrentHand(hand);
         }

         return TypedActionResult.consume(lv);
      } else {
         return TypedActionResult.fail(lv);
      }
   }

   private static float getSpeed(ItemStack stack) {
      return hasProjectile(stack, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
   }

   public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
      int j = this.getMaxUseTime(stack) - remainingUseTicks;
      float f = getPullProgress(j, stack);
      if (f >= 1.0F && !isCharged(stack) && loadProjectiles(user, stack)) {
         setCharged(stack, true);
         SoundCategory lv = user instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE;
         world.playSound((PlayerEntity)null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_CROSSBOW_LOADING_END, lv, 1.0F, 1.0F / (world.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
      }

   }

   private static boolean loadProjectiles(LivingEntity shooter, ItemStack projectile) {
      int i = EnchantmentHelper.getLevel(Enchantments.MULTISHOT, projectile);
      int j = i == 0 ? 1 : 3;
      boolean bl = shooter instanceof PlayerEntity && ((PlayerEntity)shooter).getAbilities().creativeMode;
      ItemStack lv = shooter.getProjectileType(projectile);
      ItemStack lv2 = lv.copy();

      for(int k = 0; k < j; ++k) {
         if (k > 0) {
            lv = lv2.copy();
         }

         if (lv.isEmpty() && bl) {
            lv = new ItemStack(Items.ARROW);
            lv2 = lv.copy();
         }

         if (!loadProjectile(shooter, projectile, lv, k > 0, bl)) {
            return false;
         }
      }

      return true;
   }

   private static boolean loadProjectile(LivingEntity shooter, ItemStack crossbow, ItemStack projectile, boolean simulated, boolean creative) {
      if (projectile.isEmpty()) {
         return false;
      } else {
         boolean bl3 = creative && projectile.getItem() instanceof ArrowItem;
         ItemStack lv;
         if (!bl3 && !creative && !simulated) {
            lv = projectile.split(1);
            if (projectile.isEmpty() && shooter instanceof PlayerEntity) {
               ((PlayerEntity)shooter).getInventory().removeOne(projectile);
            }
         } else {
            lv = projectile.copy();
         }

         putProjectile(crossbow, lv);
         return true;
      }
   }

   public static boolean isCharged(ItemStack stack) {
      NbtCompound lv = stack.getNbt();
      return lv != null && lv.getBoolean("Charged");
   }

   public static void setCharged(ItemStack stack, boolean charged) {
      NbtCompound lv = stack.getOrCreateNbt();
      lv.putBoolean("Charged", charged);
   }

   private static void putProjectile(ItemStack crossbow, ItemStack projectile) {
      NbtCompound lv = crossbow.getOrCreateNbt();
      NbtList lv2;
      if (lv.contains("ChargedProjectiles", NbtElement.LIST_TYPE)) {
         lv2 = lv.getList("ChargedProjectiles", NbtElement.COMPOUND_TYPE);
      } else {
         lv2 = new NbtList();
      }

      NbtCompound lv3 = new NbtCompound();
      projectile.writeNbt(lv3);
      lv2.add(lv3);
      lv.put("ChargedProjectiles", lv2);
   }

   private static List getProjectiles(ItemStack crossbow) {
      List list = Lists.newArrayList();
      NbtCompound lv = crossbow.getNbt();
      if (lv != null && lv.contains("ChargedProjectiles", NbtElement.LIST_TYPE)) {
         NbtList lv2 = lv.getList("ChargedProjectiles", NbtElement.COMPOUND_TYPE);
         if (lv2 != null) {
            for(int i = 0; i < lv2.size(); ++i) {
               NbtCompound lv3 = lv2.getCompound(i);
               list.add(ItemStack.fromNbt(lv3));
            }
         }
      }

      return list;
   }

   private static void clearProjectiles(ItemStack crossbow) {
      NbtCompound lv = crossbow.getNbt();
      if (lv != null) {
         NbtList lv2 = lv.getList("ChargedProjectiles", NbtElement.LIST_TYPE);
         lv2.clear();
         lv.put("ChargedProjectiles", lv2);
      }

   }

   public static boolean hasProjectile(ItemStack crossbow, Item projectile) {
      return getProjectiles(crossbow).stream().anyMatch((s) -> {
         return s.isOf(projectile);
      });
   }

   private static void shoot(World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated) {
      if (!world.isClient) {
         boolean bl2 = projectile.isOf(Items.FIREWORK_ROCKET);
         Object lv;
         if (bl2) {
            lv = new FireworkRocketEntity(world, projectile, shooter, shooter.getX(), shooter.getEyeY() - 0.15000000596046448, shooter.getZ(), true);
         } else {
            lv = createArrow(world, shooter, crossbow, projectile);
            if (creative || simulated != 0.0F) {
               ((PersistentProjectileEntity)lv).pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            }
         }

         if (shooter instanceof CrossbowUser) {
            CrossbowUser lv2 = (CrossbowUser)shooter;
            lv2.shoot(lv2.getTarget(), crossbow, (ProjectileEntity)lv, simulated);
         } else {
            Vec3d lv3 = shooter.getOppositeRotationVector(1.0F);
            Quaternionf quaternionf = (new Quaternionf()).setAngleAxis((double)(simulated * 0.017453292F), lv3.x, lv3.y, lv3.z);
            Vec3d lv4 = shooter.getRotationVec(1.0F);
            Vector3f vector3f = lv4.toVector3f().rotate(quaternionf);
            ((ProjectileEntity)lv).setVelocity((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z(), speed, divergence);
         }

         crossbow.damage(bl2 ? 3 : 1, shooter, (e) -> {
            e.sendToolBreakStatus(hand);
         });
         world.spawnEntity((Entity)lv);
         world.playSound((PlayerEntity)null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0F, soundPitch);
      }
   }

   private static PersistentProjectileEntity createArrow(World world, LivingEntity entity, ItemStack crossbow, ItemStack arrow) {
      ArrowItem lv = (ArrowItem)(arrow.getItem() instanceof ArrowItem ? arrow.getItem() : Items.ARROW);
      PersistentProjectileEntity lv2 = lv.createArrow(world, arrow, entity);
      if (entity instanceof PlayerEntity) {
         lv2.setCritical(true);
      }

      lv2.setSound(SoundEvents.ITEM_CROSSBOW_HIT);
      lv2.setShotFromCrossbow(true);
      int i = EnchantmentHelper.getLevel(Enchantments.PIERCING, crossbow);
      if (i > 0) {
         lv2.setPierceLevel((byte)i);
      }

      return lv2;
   }

   public static void shootAll(World world, LivingEntity entity, Hand hand, ItemStack stack, float speed, float divergence) {
      List list = getProjectiles(stack);
      float[] fs = getSoundPitches(entity.getRandom());

      for(int i = 0; i < list.size(); ++i) {
         ItemStack lv = (ItemStack)list.get(i);
         boolean bl = entity instanceof PlayerEntity && ((PlayerEntity)entity).getAbilities().creativeMode;
         if (!lv.isEmpty()) {
            if (i == 0) {
               shoot(world, entity, hand, stack, lv, fs[i], bl, speed, divergence, 0.0F);
            } else if (i == 1) {
               shoot(world, entity, hand, stack, lv, fs[i], bl, speed, divergence, -10.0F);
            } else if (i == 2) {
               shoot(world, entity, hand, stack, lv, fs[i], bl, speed, divergence, 10.0F);
            }
         }
      }

      postShoot(world, entity, stack);
   }

   private static float[] getSoundPitches(Random random) {
      boolean bl = random.nextBoolean();
      return new float[]{1.0F, getSoundPitch(bl, random), getSoundPitch(!bl, random)};
   }

   private static float getSoundPitch(boolean flag, Random random) {
      float f = flag ? 0.63F : 0.43F;
      return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + f;
   }

   private static void postShoot(World world, LivingEntity entity, ItemStack stack) {
      if (entity instanceof ServerPlayerEntity lv) {
         if (!world.isClient) {
            Criteria.SHOT_CROSSBOW.trigger(lv, stack);
         }

         lv.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
      }

      clearProjectiles(stack);
   }

   public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
      if (!world.isClient) {
         int j = EnchantmentHelper.getLevel(Enchantments.QUICK_CHARGE, stack);
         SoundEvent lv = this.getQuickChargeSound(j);
         SoundEvent lv2 = j == 0 ? SoundEvents.ITEM_CROSSBOW_LOADING_MIDDLE : null;
         float f = (float)(stack.getMaxUseTime() - remainingUseTicks) / (float)getPullTime(stack);
         if (f < 0.2F) {
            this.charged = false;
            this.loaded = false;
         }

         if (f >= 0.2F && !this.charged) {
            this.charged = true;
            world.playSound((PlayerEntity)null, user.getX(), user.getY(), user.getZ(), lv, SoundCategory.PLAYERS, 0.5F, 1.0F);
         }

         if (f >= 0.5F && lv2 != null && !this.loaded) {
            this.loaded = true;
            world.playSound((PlayerEntity)null, user.getX(), user.getY(), user.getZ(), lv2, SoundCategory.PLAYERS, 0.5F, 1.0F);
         }
      }

   }

   public int getMaxUseTime(ItemStack stack) {
      return getPullTime(stack) + 3;
   }

   public static int getPullTime(ItemStack stack) {
      int i = EnchantmentHelper.getLevel(Enchantments.QUICK_CHARGE, stack);
      return i == 0 ? 25 : 25 - 5 * i;
   }

   public UseAction getUseAction(ItemStack stack) {
      return UseAction.CROSSBOW;
   }

   private SoundEvent getQuickChargeSound(int stage) {
      switch (stage) {
         case 1:
            return SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_1;
         case 2:
            return SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_2;
         case 3:
            return SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_3;
         default:
            return SoundEvents.ITEM_CROSSBOW_LOADING_START;
      }
   }

   private static float getPullProgress(int useTicks, ItemStack stack) {
      float f = (float)useTicks / (float)getPullTime(stack);
      if (f > 1.0F) {
         f = 1.0F;
      }

      return f;
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      List list2 = getProjectiles(stack);
      if (isCharged(stack) && !list2.isEmpty()) {
         ItemStack lv = (ItemStack)list2.get(0);
         tooltip.add(Text.translatable("item.minecraft.crossbow.projectile").append(ScreenTexts.SPACE).append(lv.toHoverableText()));
         if (context.isAdvanced() && lv.isOf(Items.FIREWORK_ROCKET)) {
            List list3 = Lists.newArrayList();
            Items.FIREWORK_ROCKET.appendTooltip(lv, world, list3, context);
            if (!list3.isEmpty()) {
               for(int i = 0; i < list3.size(); ++i) {
                  list3.set(i, Text.literal("  ").append((Text)list3.get(i)).formatted(Formatting.GRAY));
               }

               tooltip.addAll(list3);
            }
         }

      }
   }

   public boolean isUsedOnRelease(ItemStack stack) {
      return stack.isOf(this);
   }

   public int getRange() {
      return 8;
   }
}
