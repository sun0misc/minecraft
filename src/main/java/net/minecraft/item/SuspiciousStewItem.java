package net.minecraft.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.PotionUtil;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SuspiciousStewItem extends Item {
   public static final String EFFECTS_KEY = "Effects";
   public static final String EFFECT_ID_KEY = "EffectId";
   public static final String EFFECT_DURATION_KEY = "EffectDuration";
   public static final int DEFAULT_DURATION = 160;

   public SuspiciousStewItem(Item.Settings arg) {
      super(arg);
   }

   public static void addEffectToStew(ItemStack stew, StatusEffect effect, int duration) {
      NbtCompound lv = stew.getOrCreateNbt();
      NbtList lv2 = lv.getList("Effects", NbtElement.LIST_TYPE);
      NbtCompound lv3 = new NbtCompound();
      lv3.putInt("EffectId", StatusEffect.getRawId(effect));
      lv3.putInt("EffectDuration", duration);
      lv2.add(lv3);
      lv.put("Effects", lv2);
   }

   private static void forEachEffect(ItemStack stew, Consumer effectConsumer) {
      NbtCompound lv = stew.getNbt();
      if (lv != null && lv.contains("Effects", NbtElement.LIST_TYPE)) {
         NbtList lv2 = lv.getList("Effects", NbtElement.COMPOUND_TYPE);

         for(int i = 0; i < lv2.size(); ++i) {
            NbtCompound lv3 = lv2.getCompound(i);
            int j;
            if (lv3.contains("EffectDuration", NbtElement.NUMBER_TYPE)) {
               j = lv3.getInt("EffectDuration");
            } else {
               j = 160;
            }

            StatusEffect lv4 = StatusEffect.byRawId(lv3.getInt("EffectId"));
            if (lv4 != null) {
               effectConsumer.accept(new StatusEffectInstance(lv4, j));
            }
         }
      }

   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      if (context.isCreative()) {
         List list2 = new ArrayList();
         Objects.requireNonNull(list2);
         forEachEffect(stack, list2::add);
         PotionUtil.buildTooltip((List)list2, tooltip, 1.0F);
      }

   }

   public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
      ItemStack lv = super.finishUsing(stack, world, user);
      Objects.requireNonNull(user);
      forEachEffect(lv, user::addStatusEffect);
      return user instanceof PlayerEntity && ((PlayerEntity)user).getAbilities().creativeMode ? lv : new ItemStack(Items.BOWL);
   }
}
