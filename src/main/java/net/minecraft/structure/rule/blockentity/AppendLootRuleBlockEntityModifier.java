package net.minecraft.structure.rule.blockentity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class AppendLootRuleBlockEntityModifier implements RuleBlockEntityModifier {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Identifier.CODEC.fieldOf("loot_table").forGetter((modifier) -> {
         return modifier.lootTable;
      })).apply(instance, AppendLootRuleBlockEntityModifier::new);
   });
   private final Identifier lootTable;

   public AppendLootRuleBlockEntityModifier(Identifier lootTable) {
      this.lootTable = lootTable;
   }

   public NbtCompound modifyBlockEntityNbt(Random random, @Nullable NbtCompound nbt) {
      NbtCompound lv = nbt == null ? new NbtCompound() : nbt.copy();
      DataResult var10000 = Identifier.CODEC.encodeStart(NbtOps.INSTANCE, this.lootTable);
      Logger var10001 = LOGGER;
      Objects.requireNonNull(var10001);
      var10000.resultOrPartial(var10001::error).ifPresent((nbtx) -> {
         lv.put("LootTable", nbtx);
      });
      lv.putLong("LootTableSeed", random.nextLong());
      return lv;
   }

   public RuleBlockEntityModifierType getType() {
      return RuleBlockEntityModifierType.APPEND_LOOT;
   }
}
