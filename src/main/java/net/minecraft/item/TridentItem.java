package net.minecraft.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.function.Consumer;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TridentItem extends Item implements Vanishable {
   public static final int field_30926 = 10;
   public static final float ATTACK_DAMAGE = 8.0F;
   public static final float field_30928 = 2.5F;
   private final Multimap attributeModifiers;

   public TridentItem(Item.Settings arg) {
      super(arg);
      ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
      builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Tool modifier", 8.0, EntityAttributeModifier.Operation.ADDITION));
      builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Tool modifier", -2.9000000953674316, EntityAttributeModifier.Operation.ADDITION));
      this.attributeModifiers = builder.build();
   }

   public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
      return !miner.isCreative();
   }

   public UseAction getUseAction(ItemStack stack) {
      return UseAction.SPEAR;
   }

   public int getMaxUseTime(ItemStack stack) {
      return 72000;
   }

   public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
      if (user instanceof PlayerEntity lv) {
         int j = this.getMaxUseTime(stack) - remainingUseTicks;
         if (j >= 10) {
            int k = EnchantmentHelper.getRiptide(stack);
            if (k <= 0 || lv.isTouchingWaterOrRain()) {
               if (!world.isClient) {
                  stack.damage(1, (LivingEntity)lv, (Consumer)((px) -> {
                     px.sendToolBreakStatus(user.getActiveHand());
                  }));
                  if (k == 0) {
                     TridentEntity lv2 = new TridentEntity(world, lv, stack);
                     lv2.setVelocity(lv, lv.getPitch(), lv.getYaw(), 0.0F, 2.5F + (float)k * 0.5F, 1.0F);
                     if (lv.getAbilities().creativeMode) {
                        lv2.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                     }

                     world.spawnEntity(lv2);
                     world.playSoundFromEntity((PlayerEntity)null, lv2, SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
                     if (!lv.getAbilities().creativeMode) {
                        lv.getInventory().removeOne(stack);
                     }
                  }
               }

               lv.incrementStat(Stats.USED.getOrCreateStat(this));
               if (k > 0) {
                  float f = lv.getYaw();
                  float g = lv.getPitch();
                  float h = -MathHelper.sin(f * 0.017453292F) * MathHelper.cos(g * 0.017453292F);
                  float l = -MathHelper.sin(g * 0.017453292F);
                  float m = MathHelper.cos(f * 0.017453292F) * MathHelper.cos(g * 0.017453292F);
                  float n = MathHelper.sqrt(h * h + l * l + m * m);
                  float o = 3.0F * ((1.0F + (float)k) / 4.0F);
                  h *= o / n;
                  l *= o / n;
                  m *= o / n;
                  lv.addVelocity((double)h, (double)l, (double)m);
                  lv.useRiptide(20);
                  if (lv.isOnGround()) {
                     float p = 1.1999999F;
                     lv.move(MovementType.SELF, new Vec3d(0.0, 1.1999999284744263, 0.0));
                  }

                  SoundEvent lv3;
                  if (k >= 3) {
                     lv3 = SoundEvents.ITEM_TRIDENT_RIPTIDE_3;
                  } else if (k == 2) {
                     lv3 = SoundEvents.ITEM_TRIDENT_RIPTIDE_2;
                  } else {
                     lv3 = SoundEvents.ITEM_TRIDENT_RIPTIDE_1;
                  }

                  world.playSoundFromEntity((PlayerEntity)null, lv, lv3, SoundCategory.PLAYERS, 1.0F, 1.0F);
               }

            }
         }
      }
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      ItemStack lv = user.getStackInHand(hand);
      if (lv.getDamage() >= lv.getMaxDamage() - 1) {
         return TypedActionResult.fail(lv);
      } else if (EnchantmentHelper.getRiptide(lv) > 0 && !user.isTouchingWaterOrRain()) {
         return TypedActionResult.fail(lv);
      } else {
         user.setCurrentHand(hand);
         return TypedActionResult.consume(lv);
      }
   }

   public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      stack.damage(1, (LivingEntity)attacker, (Consumer)((e) -> {
         e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
      }));
      return true;
   }

   public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
      if ((double)state.getHardness(world, pos) != 0.0) {
         stack.damage(2, (LivingEntity)miner, (Consumer)((e) -> {
            e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
         }));
      }

      return true;
   }

   public Multimap getAttributeModifiers(EquipmentSlot slot) {
      return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot);
   }

   public int getEnchantability() {
      return 1;
   }
}
