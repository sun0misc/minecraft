package net.minecraft.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.function.Consumer;
import net.fabricmc.yarn.constants.MiningLevels;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MiningToolItem extends ToolItem implements Vanishable {
   private final TagKey effectiveBlocks;
   protected final float miningSpeed;
   private final float attackDamage;
   private final Multimap attributeModifiers;

   protected MiningToolItem(float attackDamage, float attackSpeed, ToolMaterial material, TagKey effectiveBlocks, Item.Settings settings) {
      super(material, settings);
      this.effectiveBlocks = effectiveBlocks;
      this.miningSpeed = material.getMiningSpeedMultiplier();
      this.attackDamage = attackDamage + material.getAttackDamage();
      ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
      builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Tool modifier", (double)this.attackDamage, EntityAttributeModifier.Operation.ADDITION));
      builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Tool modifier", (double)attackSpeed, EntityAttributeModifier.Operation.ADDITION));
      this.attributeModifiers = builder.build();
   }

   public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
      return state.isIn(this.effectiveBlocks) ? this.miningSpeed : 1.0F;
   }

   public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      stack.damage(2, (LivingEntity)attacker, (Consumer)((e) -> {
         e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
      }));
      return true;
   }

   public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
      if (!world.isClient && state.getHardness(world, pos) != 0.0F) {
         stack.damage(1, (LivingEntity)miner, (Consumer)((e) -> {
            e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
         }));
      }

      return true;
   }

   public Multimap getAttributeModifiers(EquipmentSlot slot) {
      return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot);
   }

   public float getAttackDamage() {
      return this.attackDamage;
   }

   public boolean isSuitableFor(BlockState state) {
      int i = this.getMaterial().getMiningLevel();
      if (i < MiningLevels.DIAMOND && state.isIn(BlockTags.NEEDS_DIAMOND_TOOL)) {
         return false;
      } else if (i < MiningLevels.IRON && state.isIn(BlockTags.NEEDS_IRON_TOOL)) {
         return false;
      } else {
         return i < MiningLevels.STONE && state.isIn(BlockTags.NEEDS_STONE_TOOL) ? false : state.isIn(this.effectiveBlocks);
      }
   }
}
