package net.minecraft.potion;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class PotionUtil {
   public static final String CUSTOM_POTION_EFFECTS_KEY = "CustomPotionEffects";
   public static final String CUSTOM_POTION_COLOR_KEY = "CustomPotionColor";
   public static final String POTION_KEY = "Potion";
   private static final int DEFAULT_COLOR = 16253176;
   private static final Text NONE_TEXT;

   public static List getPotionEffects(ItemStack stack) {
      return getPotionEffects(stack.getNbt());
   }

   public static List getPotionEffects(Potion potion, Collection custom) {
      List list = Lists.newArrayList();
      list.addAll(potion.getEffects());
      list.addAll(custom);
      return list;
   }

   public static List getPotionEffects(@Nullable NbtCompound nbt) {
      List list = Lists.newArrayList();
      list.addAll(getPotion(nbt).getEffects());
      getCustomPotionEffects(nbt, list);
      return list;
   }

   public static List getCustomPotionEffects(ItemStack stack) {
      return getCustomPotionEffects(stack.getNbt());
   }

   public static List getCustomPotionEffects(@Nullable NbtCompound nbt) {
      List list = Lists.newArrayList();
      getCustomPotionEffects(nbt, list);
      return list;
   }

   public static void getCustomPotionEffects(@Nullable NbtCompound nbt, List list) {
      if (nbt != null && nbt.contains("CustomPotionEffects", NbtElement.LIST_TYPE)) {
         NbtList lv = nbt.getList("CustomPotionEffects", NbtElement.COMPOUND_TYPE);

         for(int i = 0; i < lv.size(); ++i) {
            NbtCompound lv2 = lv.getCompound(i);
            StatusEffectInstance lv3 = StatusEffectInstance.fromNbt(lv2);
            if (lv3 != null) {
               list.add(lv3);
            }
         }
      }

   }

   public static int getColor(ItemStack stack) {
      NbtCompound lv = stack.getNbt();
      if (lv != null && lv.contains("CustomPotionColor", NbtElement.NUMBER_TYPE)) {
         return lv.getInt("CustomPotionColor");
      } else {
         return getPotion(stack) == Potions.EMPTY ? 16253176 : getColor((Collection)getPotionEffects(stack));
      }
   }

   public static int getColor(Potion potion) {
      return potion == Potions.EMPTY ? 16253176 : getColor((Collection)potion.getEffects());
   }

   public static int getColor(Collection effects) {
      int i = 3694022;
      if (effects.isEmpty()) {
         return 3694022;
      } else {
         float f = 0.0F;
         float g = 0.0F;
         float h = 0.0F;
         int j = 0;
         Iterator var6 = effects.iterator();

         while(var6.hasNext()) {
            StatusEffectInstance lv = (StatusEffectInstance)var6.next();
            if (lv.shouldShowParticles()) {
               int k = lv.getEffectType().getColor();
               int l = lv.getAmplifier() + 1;
               f += (float)(l * (k >> 16 & 255)) / 255.0F;
               g += (float)(l * (k >> 8 & 255)) / 255.0F;
               h += (float)(l * (k >> 0 & 255)) / 255.0F;
               j += l;
            }
         }

         if (j == 0) {
            return 0;
         } else {
            f = f / (float)j * 255.0F;
            g = g / (float)j * 255.0F;
            h = h / (float)j * 255.0F;
            return (int)f << 16 | (int)g << 8 | (int)h;
         }
      }
   }

   public static Potion getPotion(ItemStack stack) {
      return getPotion(stack.getNbt());
   }

   public static Potion getPotion(@Nullable NbtCompound compound) {
      return compound == null ? Potions.EMPTY : Potion.byId(compound.getString("Potion"));
   }

   public static ItemStack setPotion(ItemStack stack, Potion potion) {
      Identifier lv = Registries.POTION.getId(potion);
      if (potion == Potions.EMPTY) {
         stack.removeSubNbt("Potion");
      } else {
         stack.getOrCreateNbt().putString("Potion", lv.toString());
      }

      return stack;
   }

   public static ItemStack setCustomPotionEffects(ItemStack stack, Collection effects) {
      if (effects.isEmpty()) {
         return stack;
      } else {
         NbtCompound lv = stack.getOrCreateNbt();
         NbtList lv2 = lv.getList("CustomPotionEffects", NbtElement.LIST_TYPE);
         Iterator var4 = effects.iterator();

         while(var4.hasNext()) {
            StatusEffectInstance lv3 = (StatusEffectInstance)var4.next();
            lv2.add(lv3.writeNbt(new NbtCompound()));
         }

         lv.put("CustomPotionEffects", lv2);
         return stack;
      }
   }

   public static void buildTooltip(ItemStack stack, List list, float durationMultiplier) {
      buildTooltip(getPotionEffects(stack), list, durationMultiplier);
   }

   public static void buildTooltip(List statusEffects, List list, float durationMultiplier) {
      List list3 = Lists.newArrayList();
      Iterator var4;
      MutableText lv2;
      StatusEffect lv3;
      if (statusEffects.isEmpty()) {
         list.add(NONE_TEXT);
      } else {
         for(var4 = statusEffects.iterator(); var4.hasNext(); list.add(lv2.formatted(lv3.getCategory().getFormatting()))) {
            StatusEffectInstance lv = (StatusEffectInstance)var4.next();
            lv2 = Text.translatable(lv.getTranslationKey());
            lv3 = lv.getEffectType();
            Map map = lv3.getAttributeModifiers();
            if (!map.isEmpty()) {
               Iterator var9 = map.entrySet().iterator();

               while(var9.hasNext()) {
                  Map.Entry entry = (Map.Entry)var9.next();
                  EntityAttributeModifier lv4 = (EntityAttributeModifier)entry.getValue();
                  EntityAttributeModifier lv5 = new EntityAttributeModifier(lv4.getName(), lv3.adjustModifierAmount(lv.getAmplifier(), lv4), lv4.getOperation());
                  list3.add(new Pair((EntityAttribute)entry.getKey(), lv5));
               }
            }

            if (lv.getAmplifier() > 0) {
               lv2 = Text.translatable("potion.withAmplifier", lv2, Text.translatable("potion.potency." + lv.getAmplifier()));
            }

            if (!lv.isDurationBelow(20)) {
               lv2 = Text.translatable("potion.withDuration", lv2, StatusEffectUtil.durationToString(lv, durationMultiplier));
            }
         }
      }

      if (!list3.isEmpty()) {
         list.add(ScreenTexts.EMPTY);
         list.add(Text.translatable("potion.whenDrank").formatted(Formatting.DARK_PURPLE));
         var4 = list3.iterator();

         while(var4.hasNext()) {
            Pair pair = (Pair)var4.next();
            EntityAttributeModifier lv6 = (EntityAttributeModifier)pair.getSecond();
            double d = lv6.getValue();
            double e;
            if (lv6.getOperation() != EntityAttributeModifier.Operation.MULTIPLY_BASE && lv6.getOperation() != EntityAttributeModifier.Operation.MULTIPLY_TOTAL) {
               e = lv6.getValue();
            } else {
               e = lv6.getValue() * 100.0;
            }

            if (d > 0.0) {
               list.add(Text.translatable("attribute.modifier.plus." + lv6.getOperation().getId(), ItemStack.MODIFIER_FORMAT.format(e), Text.translatable(((EntityAttribute)pair.getFirst()).getTranslationKey())).formatted(Formatting.BLUE));
            } else if (d < 0.0) {
               e *= -1.0;
               list.add(Text.translatable("attribute.modifier.take." + lv6.getOperation().getId(), ItemStack.MODIFIER_FORMAT.format(e), Text.translatable(((EntityAttribute)pair.getFirst()).getTranslationKey())).formatted(Formatting.RED));
            }
         }
      }

   }

   static {
      NONE_TEXT = Text.translatable("effect.none").formatted(Formatting.GRAY);
   }
}
