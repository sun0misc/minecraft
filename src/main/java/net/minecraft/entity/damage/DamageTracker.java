package net.minecraft.entity.damage;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class DamageTracker {
   public static final int DAMAGE_COOLDOWN = 100;
   public static final int ATTACK_DAMAGE_COOLDOWN = 300;
   private static final Style INTENTIONAL_GAME_DESIGN_ISSUE_LINK_STYLE;
   private final List recentDamage = Lists.newArrayList();
   private final LivingEntity entity;
   private int ageOnLastDamage;
   private int ageOnLastAttacked;
   private int ageOnLastUpdate;
   private boolean recentlyAttacked;
   private boolean hasDamage;
   @Nullable
   private String fallDeathSuffix;

   public DamageTracker(LivingEntity entity) {
      this.entity = entity;
   }

   public void setFallDeathSuffix() {
      this.clearFallDeathSuffix();
      Optional optional = this.entity.getClimbingPos();
      if (optional.isPresent()) {
         BlockState lv = this.entity.world.getBlockState((BlockPos)optional.get());
         if (!lv.isOf(Blocks.LADDER) && !lv.isIn(BlockTags.TRAPDOORS)) {
            if (lv.isOf(Blocks.VINE)) {
               this.fallDeathSuffix = "vines";
            } else if (!lv.isOf(Blocks.WEEPING_VINES) && !lv.isOf(Blocks.WEEPING_VINES_PLANT)) {
               if (!lv.isOf(Blocks.TWISTING_VINES) && !lv.isOf(Blocks.TWISTING_VINES_PLANT)) {
                  if (lv.isOf(Blocks.SCAFFOLDING)) {
                     this.fallDeathSuffix = "scaffolding";
                  } else {
                     this.fallDeathSuffix = "other_climbable";
                  }
               } else {
                  this.fallDeathSuffix = "twisting_vines";
               }
            } else {
               this.fallDeathSuffix = "weeping_vines";
            }
         } else {
            this.fallDeathSuffix = "ladder";
         }
      } else if (this.entity.isTouchingWater()) {
         this.fallDeathSuffix = "water";
      }

   }

   public void onDamage(DamageSource damageSource, float originalHealth, float damage) {
      this.update();
      this.setFallDeathSuffix();
      DamageRecord lv = new DamageRecord(damageSource, this.entity.age, originalHealth, damage, this.fallDeathSuffix, this.entity.fallDistance);
      this.recentDamage.add(lv);
      this.ageOnLastDamage = this.entity.age;
      this.hasDamage = true;
      if (lv.isAttackerLiving() && !this.recentlyAttacked && this.entity.isAlive()) {
         this.recentlyAttacked = true;
         this.ageOnLastAttacked = this.entity.age;
         this.ageOnLastUpdate = this.ageOnLastAttacked;
         this.entity.enterCombat();
      }

   }

   public Text getDeathMessage() {
      if (this.recentDamage.isEmpty()) {
         return Text.translatable("death.attack.generic", this.entity.getDisplayName());
      } else {
         DamageRecord lv = this.getBiggestFall();
         DamageRecord lv2 = (DamageRecord)this.recentDamage.get(this.recentDamage.size() - 1);
         Text lv3 = lv2.getAttackerName();
         DamageSource lv4 = lv2.getDamageSource();
         Entity lv5 = lv4.getAttacker();
         DeathMessageType lv6 = lv4.getType().deathMessageType();
         Object lv9;
         if (lv != null && lv6 == DeathMessageType.FALL_VARIANTS) {
            Text lv7 = lv.getAttackerName();
            DamageSource lv8 = lv.getDamageSource();
            if (!lv8.isIn(DamageTypeTags.IS_FALL) && !lv8.isIn(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL)) {
               ItemStack var10000;
               if (lv7 != null && !lv7.equals(lv3)) {
                  Entity lv10 = lv8.getAttacker();
                  if (lv10 instanceof LivingEntity) {
                     LivingEntity lv11 = (LivingEntity)lv10;
                     var10000 = lv11.getMainHandStack();
                  } else {
                     var10000 = ItemStack.EMPTY;
                  }

                  ItemStack lv12 = var10000;
                  if (!lv12.isEmpty() && lv12.hasCustomName()) {
                     lv9 = Text.translatable("death.fell.assist.item", this.entity.getDisplayName(), lv7, lv12.toHoverableText());
                  } else {
                     lv9 = Text.translatable("death.fell.assist", this.entity.getDisplayName(), lv7);
                  }
               } else if (lv3 != null) {
                  if (lv5 instanceof LivingEntity) {
                     LivingEntity lv13 = (LivingEntity)lv5;
                     var10000 = lv13.getMainHandStack();
                  } else {
                     var10000 = ItemStack.EMPTY;
                  }

                  ItemStack lv14 = var10000;
                  if (!lv14.isEmpty() && lv14.hasCustomName()) {
                     lv9 = Text.translatable("death.fell.finish.item", this.entity.getDisplayName(), lv3, lv14.toHoverableText());
                  } else {
                     lv9 = Text.translatable("death.fell.finish", this.entity.getDisplayName(), lv3);
                  }
               } else {
                  lv9 = Text.translatable("death.fell.killer", this.entity.getDisplayName());
               }
            } else {
               lv9 = Text.translatable("death.fell.accident." + this.getFallDeathSuffix(lv), this.entity.getDisplayName());
            }
         } else {
            if (lv6 == DeathMessageType.INTENTIONAL_GAME_DESIGN) {
               String string = "death.attack." + lv4.getName();
               Text lv15 = Texts.bracketed(Text.translatable(string + ".link")).fillStyle(INTENTIONAL_GAME_DESIGN_ISSUE_LINK_STYLE);
               return Text.translatable(string + ".message", this.entity.getDisplayName(), lv15);
            }

            lv9 = lv4.getDeathMessage(this.entity);
         }

         return (Text)lv9;
      }
   }

   @Nullable
   public LivingEntity getBiggestAttacker() {
      LivingEntity lv = null;
      PlayerEntity lv2 = null;
      float f = 0.0F;
      float g = 0.0F;
      Iterator var5 = this.recentDamage.iterator();

      while(true) {
         DamageRecord lv3;
         LivingEntity lv5;
         do {
            Entity var8;
            do {
               if (!var5.hasNext()) {
                  if (lv2 != null && g >= f / 3.0F) {
                     return lv2;
                  }

                  return lv;
               }

               lv3 = (DamageRecord)var5.next();
               var8 = lv3.getDamageSource().getAttacker();
               if (var8 instanceof PlayerEntity lv4) {
                  if (lv2 == null || lv3.getDamage() > g) {
                     g = lv3.getDamage();
                     lv2 = lv4;
                  }
               }

               var8 = lv3.getDamageSource().getAttacker();
            } while(!(var8 instanceof LivingEntity));

            lv5 = (LivingEntity)var8;
         } while(lv != null && !(lv3.getDamage() > f));

         f = lv3.getDamage();
         lv = lv5;
      }
   }

   @Nullable
   private DamageRecord getBiggestFall() {
      DamageRecord lv = null;
      DamageRecord lv2 = null;
      float f = 0.0F;
      float g = 0.0F;

      for(int i = 0; i < this.recentDamage.size(); ++i) {
         DamageRecord lv3 = (DamageRecord)this.recentDamage.get(i);
         DamageRecord lv4 = i > 0 ? (DamageRecord)this.recentDamage.get(i - 1) : null;
         DamageSource lv5 = lv3.getDamageSource();
         boolean bl = lv5.isIn(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL);
         float h = bl ? Float.MAX_VALUE : lv3.getFallDistance();
         if ((lv5.isIn(DamageTypeTags.IS_FALL) || bl) && h > 0.0F && (lv == null || h > g)) {
            if (i > 0) {
               lv = lv4;
            } else {
               lv = lv3;
            }

            g = h;
         }

         if (lv3.getFallDeathSuffix() != null && (lv2 == null || lv3.getDamage() > f)) {
            lv2 = lv3;
            f = lv3.getDamage();
         }
      }

      if (g > 5.0F && lv != null) {
         return lv;
      } else if (f > 5.0F && lv2 != null) {
         return lv2;
      } else {
         return null;
      }
   }

   private String getFallDeathSuffix(DamageRecord damageRecord) {
      return damageRecord.getFallDeathSuffix() == null ? "generic" : damageRecord.getFallDeathSuffix();
   }

   public boolean hasDamage() {
      this.update();
      return this.hasDamage;
   }

   public boolean wasRecentlyAttacked() {
      this.update();
      return this.recentlyAttacked;
   }

   public int getTimeSinceLastAttack() {
      return this.recentlyAttacked ? this.entity.age - this.ageOnLastAttacked : this.ageOnLastUpdate - this.ageOnLastAttacked;
   }

   private void clearFallDeathSuffix() {
      this.fallDeathSuffix = null;
   }

   public void update() {
      int i = this.recentlyAttacked ? 300 : 100;
      if (this.hasDamage && (!this.entity.isAlive() || this.entity.age - this.ageOnLastDamage > i)) {
         boolean bl = this.recentlyAttacked;
         this.hasDamage = false;
         this.recentlyAttacked = false;
         this.ageOnLastUpdate = this.entity.age;
         if (bl) {
            this.entity.endCombat();
         }

         this.recentDamage.clear();
      }

   }

   public LivingEntity getEntity() {
      return this.entity;
   }

   @Nullable
   public DamageRecord getMostRecentDamage() {
      return this.recentDamage.isEmpty() ? null : (DamageRecord)this.recentDamage.get(this.recentDamage.size() - 1);
   }

   public int getBiggestAttackerId() {
      LivingEntity lv = this.getBiggestAttacker();
      return lv == null ? -1 : lv.getId();
   }

   static {
      INTENTIONAL_GAME_DESIGN_ISSUE_LINK_STYLE = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://bugs.mojang.com/browse/MCPE-28723")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("MCPE-28723")));
   }
}
