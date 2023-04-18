package net.minecraft.structure.rule.blockentity;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class ClearRuleBlockEntityModifier implements RuleBlockEntityModifier {
   private static final ClearRuleBlockEntityModifier INSTANCE = new ClearRuleBlockEntityModifier();
   public static final Codec CODEC;

   public NbtCompound modifyBlockEntityNbt(Random random, @Nullable NbtCompound nbt) {
      return new NbtCompound();
   }

   public RuleBlockEntityModifierType getType() {
      return RuleBlockEntityModifierType.CLEAR;
   }

   static {
      CODEC = Codec.unit(INSTANCE);
   }
}
