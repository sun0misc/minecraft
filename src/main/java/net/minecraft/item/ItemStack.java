package net.minecraft.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.UnbreakingEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class ItemStack {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Registries.ITEM.getCodec().fieldOf("id").forGetter((stack) -> {
         return stack.item;
      }), Codec.INT.fieldOf("Count").forGetter((stack) -> {
         return stack.count;
      }), NbtCompound.CODEC.optionalFieldOf("tag").forGetter((stack) -> {
         return Optional.ofNullable(stack.nbt);
      })).apply(instance, ItemStack::new);
   });
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final ItemStack EMPTY = new ItemStack((Item)null);
   public static final DecimalFormat MODIFIER_FORMAT = (DecimalFormat)Util.make(new DecimalFormat("#.##"), (decimalFormat) -> {
      decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
   });
   public static final String ENCHANTMENTS_KEY = "Enchantments";
   public static final String DISPLAY_KEY = "display";
   public static final String NAME_KEY = "Name";
   public static final String LORE_KEY = "Lore";
   public static final String DAMAGE_KEY = "Damage";
   public static final String COLOR_KEY = "color";
   private static final String UNBREAKABLE_KEY = "Unbreakable";
   private static final String REPAIR_COST_KEY = "RepairCost";
   private static final String CAN_DESTROY_KEY = "CanDestroy";
   private static final String CAN_PLACE_ON_KEY = "CanPlaceOn";
   private static final String HIDE_FLAGS_KEY = "HideFlags";
   private static final Text DISABLED_TEXT;
   private static final int field_30903 = 0;
   private static final Style LORE_STYLE;
   private int count;
   private int bobbingAnimationTime;
   /** @deprecated */
   @Deprecated
   private final Item item;
   @Nullable
   private NbtCompound nbt;
   @Nullable
   private Entity holder;
   @Nullable
   private BlockPredicatesChecker destroyChecker;
   @Nullable
   private BlockPredicatesChecker placeChecker;

   public Optional getTooltipData() {
      return this.getItem().getTooltipData(this);
   }

   public ItemStack(ItemConvertible item) {
      this((ItemConvertible)item, 1);
   }

   public ItemStack(RegistryEntry entry) {
      this((ItemConvertible)((ItemConvertible)entry.value()), 1);
   }

   private ItemStack(ItemConvertible item, int count, Optional nbt) {
      this(item, count);
      nbt.ifPresent(this::setNbt);
   }

   public ItemStack(RegistryEntry itemEntry, int count) {
      this((ItemConvertible)itemEntry.value(), count);
   }

   public ItemStack(ItemConvertible item, int count) {
      this.item = item == null ? null : item.asItem();
      this.count = count;
      if (this.item != null && this.item.isDamageable()) {
         this.setDamage(this.getDamage());
      }

   }

   private ItemStack(NbtCompound nbt) {
      this.item = (Item)Registries.ITEM.get(new Identifier(nbt.getString("id")));
      this.count = nbt.getByte("Count");
      if (nbt.contains("tag", NbtElement.COMPOUND_TYPE)) {
         this.nbt = nbt.getCompound("tag");
         this.getItem().postProcessNbt(this.nbt);
      }

      if (this.getItem().isDamageable()) {
         this.setDamage(this.getDamage());
      }

   }

   public static ItemStack fromNbt(NbtCompound nbt) {
      try {
         return new ItemStack(nbt);
      } catch (RuntimeException var2) {
         LOGGER.debug("Tried to load invalid item: {}", nbt, var2);
         return EMPTY;
      }
   }

   public boolean isEmpty() {
      return this == EMPTY || this.item == Items.AIR || this.count <= 0;
   }

   public boolean isItemEnabled(FeatureSet enabledFeatures) {
      return this.isEmpty() || this.getItem().isEnabled(enabledFeatures);
   }

   public ItemStack split(int amount) {
      int j = Math.min(amount, this.getCount());
      ItemStack lv = this.copyWithCount(j);
      this.decrement(j);
      return lv;
   }

   public ItemStack copyAndEmpty() {
      if (this.isEmpty()) {
         return EMPTY;
      } else {
         ItemStack lv = this.copy();
         this.setCount(0);
         return lv;
      }
   }

   public Item getItem() {
      return this.isEmpty() ? Items.AIR : this.item;
   }

   public RegistryEntry getRegistryEntry() {
      return this.getItem().getRegistryEntry();
   }

   public boolean isIn(TagKey tag) {
      return this.getItem().getRegistryEntry().isIn(tag);
   }

   public boolean isOf(Item item) {
      return this.getItem() == item;
   }

   public boolean itemMatches(Predicate predicate) {
      return predicate.test(this.getItem().getRegistryEntry());
   }

   public boolean itemMatches(RegistryEntry itemEntry) {
      return this.getItem().getRegistryEntry() == itemEntry;
   }

   public Stream streamTags() {
      return this.getItem().getRegistryEntry().streamTags();
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      PlayerEntity lv = context.getPlayer();
      BlockPos lv2 = context.getBlockPos();
      CachedBlockPosition lv3 = new CachedBlockPosition(context.getWorld(), lv2, false);
      if (lv != null && !lv.getAbilities().allowModifyWorld && !this.canPlaceOn(context.getWorld().getRegistryManager().get(RegistryKeys.BLOCK), lv3)) {
         return ActionResult.PASS;
      } else {
         Item lv4 = this.getItem();
         ActionResult lv5 = lv4.useOnBlock(context);
         if (lv != null && lv5.shouldIncrementStat()) {
            lv.incrementStat(Stats.USED.getOrCreateStat(lv4));
         }

         return lv5;
      }
   }

   public float getMiningSpeedMultiplier(BlockState state) {
      return this.getItem().getMiningSpeedMultiplier(this, state);
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      return this.getItem().use(world, user, hand);
   }

   public ItemStack finishUsing(World world, LivingEntity user) {
      return this.getItem().finishUsing(this, world, user);
   }

   public NbtCompound writeNbt(NbtCompound nbt) {
      Identifier lv = Registries.ITEM.getId(this.getItem());
      nbt.putString("id", lv == null ? "minecraft:air" : lv.toString());
      nbt.putByte("Count", (byte)this.count);
      if (this.nbt != null) {
         nbt.put("tag", this.nbt.copy());
      }

      return nbt;
   }

   public int getMaxCount() {
      return this.getItem().getMaxCount();
   }

   public boolean isStackable() {
      return this.getMaxCount() > 1 && (!this.isDamageable() || !this.isDamaged());
   }

   public boolean isDamageable() {
      if (!this.isEmpty() && this.getItem().getMaxDamage() > 0) {
         NbtCompound lv = this.getNbt();
         return lv == null || !lv.getBoolean("Unbreakable");
      } else {
         return false;
      }
   }

   public boolean isDamaged() {
      return this.isDamageable() && this.getDamage() > 0;
   }

   public int getDamage() {
      return this.nbt == null ? 0 : this.nbt.getInt("Damage");
   }

   public void setDamage(int damage) {
      this.getOrCreateNbt().putInt("Damage", Math.max(0, damage));
   }

   public int getMaxDamage() {
      return this.getItem().getMaxDamage();
   }

   public boolean damage(int amount, Random random, @Nullable ServerPlayerEntity player) {
      if (!this.isDamageable()) {
         return false;
      } else {
         int j;
         if (amount > 0) {
            j = EnchantmentHelper.getLevel(Enchantments.UNBREAKING, this);
            int k = 0;

            for(int l = 0; j > 0 && l < amount; ++l) {
               if (UnbreakingEnchantment.shouldPreventDamage(this, j, random)) {
                  ++k;
               }
            }

            amount -= k;
            if (amount <= 0) {
               return false;
            }
         }

         if (player != null && amount != 0) {
            Criteria.ITEM_DURABILITY_CHANGED.trigger(player, this, this.getDamage() + amount);
         }

         j = this.getDamage() + amount;
         this.setDamage(j);
         return j >= this.getMaxDamage();
      }
   }

   public void damage(int amount, LivingEntity entity, Consumer breakCallback) {
      if (!entity.world.isClient && (!(entity instanceof PlayerEntity) || !((PlayerEntity)entity).getAbilities().creativeMode)) {
         if (this.isDamageable()) {
            if (this.damage(amount, entity.getRandom(), entity instanceof ServerPlayerEntity ? (ServerPlayerEntity)entity : null)) {
               breakCallback.accept(entity);
               Item lv = this.getItem();
               this.decrement(1);
               if (entity instanceof PlayerEntity) {
                  ((PlayerEntity)entity).incrementStat(Stats.BROKEN.getOrCreateStat(lv));
               }

               this.setDamage(0);
            }

         }
      }
   }

   public boolean isItemBarVisible() {
      return this.item.isItemBarVisible(this);
   }

   public int getItemBarStep() {
      return this.item.getItemBarStep(this);
   }

   public int getItemBarColor() {
      return this.item.getItemBarColor(this);
   }

   public boolean onStackClicked(Slot slot, ClickType clickType, PlayerEntity player) {
      return this.getItem().onStackClicked(this, slot, clickType, player);
   }

   public boolean onClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
      return this.getItem().onClicked(this, stack, slot, clickType, player, cursorStackReference);
   }

   public void postHit(LivingEntity target, PlayerEntity attacker) {
      Item lv = this.getItem();
      if (lv.postHit(this, target, attacker)) {
         attacker.incrementStat(Stats.USED.getOrCreateStat(lv));
      }

   }

   public void postMine(World world, BlockState state, BlockPos pos, PlayerEntity miner) {
      Item lv = this.getItem();
      if (lv.postMine(this, world, state, pos, miner)) {
         miner.incrementStat(Stats.USED.getOrCreateStat(lv));
      }

   }

   public boolean isSuitableFor(BlockState state) {
      return this.getItem().isSuitableFor(state);
   }

   public ActionResult useOnEntity(PlayerEntity user, LivingEntity entity, Hand hand) {
      return this.getItem().useOnEntity(this, user, entity, hand);
   }

   public ItemStack copy() {
      if (this.isEmpty()) {
         return EMPTY;
      } else {
         ItemStack lv = new ItemStack(this.getItem(), this.count);
         lv.setBobbingAnimationTime(this.getBobbingAnimationTime());
         if (this.nbt != null) {
            lv.nbt = this.nbt.copy();
         }

         return lv;
      }
   }

   public ItemStack copyWithCount(int count) {
      if (this.isEmpty()) {
         return EMPTY;
      } else {
         ItemStack lv = this.copy();
         lv.setCount(count);
         return lv;
      }
   }

   public static boolean areNbtEqual(ItemStack left, ItemStack right) {
      if (left.isEmpty() && right.isEmpty()) {
         return true;
      } else if (!left.isEmpty() && !right.isEmpty()) {
         if (left.nbt == null && right.nbt != null) {
            return false;
         } else {
            return left.nbt == null || left.nbt.equals(right.nbt);
         }
      } else {
         return false;
      }
   }

   public static boolean areEqual(ItemStack left, ItemStack right) {
      if (left.isEmpty() && right.isEmpty()) {
         return true;
      } else {
         return !left.isEmpty() && !right.isEmpty() ? left.isEqual(right) : false;
      }
   }

   private boolean isEqual(ItemStack stack) {
      if (this.getCount() != stack.getCount()) {
         return false;
      } else if (!this.isOf(stack.getItem())) {
         return false;
      } else if (this.nbt == null && stack.nbt != null) {
         return false;
      } else {
         return this.nbt == null || this.nbt.equals(stack.nbt);
      }
   }

   public static boolean areItemsEqual(ItemStack left, ItemStack right) {
      if (left == right) {
         return true;
      } else {
         return !left.isEmpty() && !right.isEmpty() ? left.isItemEqual(right) : false;
      }
   }

   public boolean isItemEqual(ItemStack stack) {
      return !stack.isEmpty() && this.isOf(stack.getItem());
   }

   public static boolean canCombine(ItemStack stack, ItemStack otherStack) {
      return stack.isOf(otherStack.getItem()) && areNbtEqual(stack, otherStack);
   }

   public String getTranslationKey() {
      return this.getItem().getTranslationKey(this);
   }

   public String toString() {
      int var10000 = this.getCount();
      return "" + var10000 + " " + this.getItem();
   }

   public void inventoryTick(World world, Entity entity, int slot, boolean selected) {
      if (this.bobbingAnimationTime > 0) {
         --this.bobbingAnimationTime;
      }

      if (this.getItem() != null) {
         this.getItem().inventoryTick(this, world, entity, slot, selected);
      }

   }

   public void onCraft(World world, PlayerEntity player, int amount) {
      player.increaseStat(Stats.CRAFTED.getOrCreateStat(this.getItem()), amount);
      this.getItem().onCraft(this, world, player);
   }

   public int getMaxUseTime() {
      return this.getItem().getMaxUseTime(this);
   }

   public UseAction getUseAction() {
      return this.getItem().getUseAction(this);
   }

   public void onStoppedUsing(World world, LivingEntity user, int remainingUseTicks) {
      this.getItem().onStoppedUsing(this, world, user, remainingUseTicks);
   }

   public boolean isUsedOnRelease() {
      return this.getItem().isUsedOnRelease(this);
   }

   public boolean hasNbt() {
      return !this.isEmpty() && this.nbt != null && !this.nbt.isEmpty();
   }

   @Nullable
   public NbtCompound getNbt() {
      return this.nbt;
   }

   public NbtCompound getOrCreateNbt() {
      if (this.nbt == null) {
         this.setNbt(new NbtCompound());
      }

      return this.nbt;
   }

   public NbtCompound getOrCreateSubNbt(String key) {
      if (this.nbt != null && this.nbt.contains(key, NbtElement.COMPOUND_TYPE)) {
         return this.nbt.getCompound(key);
      } else {
         NbtCompound lv = new NbtCompound();
         this.setSubNbt(key, lv);
         return lv;
      }
   }

   @Nullable
   public NbtCompound getSubNbt(String key) {
      return this.nbt != null && this.nbt.contains(key, NbtElement.COMPOUND_TYPE) ? this.nbt.getCompound(key) : null;
   }

   public void removeSubNbt(String key) {
      if (this.nbt != null && this.nbt.contains(key)) {
         this.nbt.remove(key);
         if (this.nbt.isEmpty()) {
            this.nbt = null;
         }
      }

   }

   public NbtList getEnchantments() {
      return this.nbt != null ? this.nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE) : new NbtList();
   }

   public void setNbt(@Nullable NbtCompound nbt) {
      this.nbt = nbt;
      if (this.getItem().isDamageable()) {
         this.setDamage(this.getDamage());
      }

      if (nbt != null) {
         this.getItem().postProcessNbt(nbt);
      }

   }

   public Text getName() {
      NbtCompound lv = this.getSubNbt("display");
      if (lv != null && lv.contains("Name", NbtElement.STRING_TYPE)) {
         try {
            Text lv2 = Text.Serializer.fromJson(lv.getString("Name"));
            if (lv2 != null) {
               return lv2;
            }

            lv.remove("Name");
         } catch (Exception var3) {
            lv.remove("Name");
         }
      }

      return this.getItem().getName(this);
   }

   public ItemStack setCustomName(@Nullable Text name) {
      NbtCompound lv = this.getOrCreateSubNbt("display");
      if (name != null) {
         lv.putString("Name", Text.Serializer.toJson(name));
      } else {
         lv.remove("Name");
      }

      return this;
   }

   public void removeCustomName() {
      NbtCompound lv = this.getSubNbt("display");
      if (lv != null) {
         lv.remove("Name");
         if (lv.isEmpty()) {
            this.removeSubNbt("display");
         }
      }

      if (this.nbt != null && this.nbt.isEmpty()) {
         this.nbt = null;
      }

   }

   public boolean hasCustomName() {
      NbtCompound lv = this.getSubNbt("display");
      return lv != null && lv.contains("Name", NbtElement.STRING_TYPE);
   }

   public List getTooltip(@Nullable PlayerEntity player, TooltipContext context) {
      List list = Lists.newArrayList();
      MutableText lv = Text.empty().append(this.getName()).formatted(this.getRarity().formatting);
      if (this.hasCustomName()) {
         lv.formatted(Formatting.ITALIC);
      }

      list.add(lv);
      if (!context.isAdvanced() && !this.hasCustomName() && this.isOf(Items.FILLED_MAP)) {
         Integer integer = FilledMapItem.getMapId(this);
         if (integer != null) {
            list.add(Text.literal("#" + integer).formatted(Formatting.GRAY));
         }
      }

      int i = this.getHideFlags();
      if (isSectionVisible(i, ItemStack.TooltipSection.ADDITIONAL)) {
         this.getItem().appendTooltip(this, player == null ? null : player.world, list, context);
      }

      int j;
      if (this.hasNbt()) {
         if (isSectionVisible(i, ItemStack.TooltipSection.UPGRADES) && player != null) {
            ArmorTrim.appendTooltip(this, player.world.getRegistryManager(), list);
         }

         if (isSectionVisible(i, ItemStack.TooltipSection.ENCHANTMENTS)) {
            appendEnchantments(list, this.getEnchantments());
         }

         if (this.nbt.contains("display", NbtElement.COMPOUND_TYPE)) {
            NbtCompound lv2 = this.nbt.getCompound("display");
            if (isSectionVisible(i, ItemStack.TooltipSection.DYE) && lv2.contains("color", NbtElement.NUMBER_TYPE)) {
               if (context.isAdvanced()) {
                  list.add(Text.translatable("item.color", String.format(Locale.ROOT, "#%06X", lv2.getInt("color"))).formatted(Formatting.GRAY));
               } else {
                  list.add(Text.translatable("item.dyed").formatted(Formatting.GRAY, Formatting.ITALIC));
               }
            }

            if (lv2.getType("Lore") == NbtElement.LIST_TYPE) {
               NbtList lv3 = lv2.getList("Lore", NbtElement.STRING_TYPE);

               for(j = 0; j < lv3.size(); ++j) {
                  String string = lv3.getString(j);

                  try {
                     MutableText lv4 = Text.Serializer.fromJson(string);
                     if (lv4 != null) {
                        list.add(Texts.setStyleIfAbsent(lv4, LORE_STYLE));
                     }
                  } catch (Exception var19) {
                     lv2.remove("Lore");
                  }
               }
            }
         }
      }

      int k;
      if (isSectionVisible(i, ItemStack.TooltipSection.MODIFIERS)) {
         EquipmentSlot[] var21 = EquipmentSlot.values();
         k = var21.length;

         for(j = 0; j < k; ++j) {
            EquipmentSlot lv5 = var21[j];
            Multimap multimap = this.getAttributeModifiers(lv5);
            if (!multimap.isEmpty()) {
               list.add(ScreenTexts.EMPTY);
               list.add(Text.translatable("item.modifiers." + lv5.getName()).formatted(Formatting.GRAY));
               Iterator var11 = multimap.entries().iterator();

               while(var11.hasNext()) {
                  Map.Entry entry = (Map.Entry)var11.next();
                  EntityAttributeModifier lv6 = (EntityAttributeModifier)entry.getValue();
                  double d = lv6.getValue();
                  boolean bl = false;
                  if (player != null) {
                     if (lv6.getId() == Item.ATTACK_DAMAGE_MODIFIER_ID) {
                        d += player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                        d += (double)EnchantmentHelper.getAttackDamage(this, EntityGroup.DEFAULT);
                        bl = true;
                     } else if (lv6.getId() == Item.ATTACK_SPEED_MODIFIER_ID) {
                        d += player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED);
                        bl = true;
                     }
                  }

                  double e;
                  if (lv6.getOperation() != EntityAttributeModifier.Operation.MULTIPLY_BASE && lv6.getOperation() != EntityAttributeModifier.Operation.MULTIPLY_TOTAL) {
                     if (((EntityAttribute)entry.getKey()).equals(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)) {
                        e = d * 10.0;
                     } else {
                        e = d;
                     }
                  } else {
                     e = d * 100.0;
                  }

                  if (bl) {
                     list.add(ScreenTexts.space().append((Text)Text.translatable("attribute.modifier.equals." + lv6.getOperation().getId(), MODIFIER_FORMAT.format(e), Text.translatable(((EntityAttribute)entry.getKey()).getTranslationKey()))).formatted(Formatting.DARK_GREEN));
                  } else if (d > 0.0) {
                     list.add(Text.translatable("attribute.modifier.plus." + lv6.getOperation().getId(), MODIFIER_FORMAT.format(e), Text.translatable(((EntityAttribute)entry.getKey()).getTranslationKey())).formatted(Formatting.BLUE));
                  } else if (d < 0.0) {
                     e *= -1.0;
                     list.add(Text.translatable("attribute.modifier.take." + lv6.getOperation().getId(), MODIFIER_FORMAT.format(e), Text.translatable(((EntityAttribute)entry.getKey()).getTranslationKey())).formatted(Formatting.RED));
                  }
               }
            }
         }
      }

      if (this.hasNbt()) {
         if (isSectionVisible(i, ItemStack.TooltipSection.UNBREAKABLE) && this.nbt.getBoolean("Unbreakable")) {
            list.add(Text.translatable("item.unbreakable").formatted(Formatting.BLUE));
         }

         NbtList lv7;
         if (isSectionVisible(i, ItemStack.TooltipSection.CAN_DESTROY) && this.nbt.contains("CanDestroy", NbtElement.LIST_TYPE)) {
            lv7 = this.nbt.getList("CanDestroy", NbtElement.STRING_TYPE);
            if (!lv7.isEmpty()) {
               list.add(ScreenTexts.EMPTY);
               list.add(Text.translatable("item.canBreak").formatted(Formatting.GRAY));

               for(k = 0; k < lv7.size(); ++k) {
                  list.addAll(parseBlockTag(lv7.getString(k)));
               }
            }
         }

         if (isSectionVisible(i, ItemStack.TooltipSection.CAN_PLACE) && this.nbt.contains("CanPlaceOn", NbtElement.LIST_TYPE)) {
            lv7 = this.nbt.getList("CanPlaceOn", NbtElement.STRING_TYPE);
            if (!lv7.isEmpty()) {
               list.add(ScreenTexts.EMPTY);
               list.add(Text.translatable("item.canPlace").formatted(Formatting.GRAY));

               for(k = 0; k < lv7.size(); ++k) {
                  list.addAll(parseBlockTag(lv7.getString(k)));
               }
            }
         }
      }

      if (context.isAdvanced()) {
         if (this.isDamaged()) {
            list.add(Text.translatable("item.durability", this.getMaxDamage() - this.getDamage(), this.getMaxDamage()));
         }

         list.add(Text.literal(Registries.ITEM.getId(this.getItem()).toString()).formatted(Formatting.DARK_GRAY));
         if (this.hasNbt()) {
            list.add(Text.translatable("item.nbt_tags", this.nbt.getKeys().size()).formatted(Formatting.DARK_GRAY));
         }
      }

      if (player != null && !this.getItem().isEnabled(player.getWorld().getEnabledFeatures())) {
         list.add(DISABLED_TEXT);
      }

      return list;
   }

   private static boolean isSectionVisible(int flags, TooltipSection tooltipSection) {
      return (flags & tooltipSection.getFlag()) == 0;
   }

   private int getHideFlags() {
      return this.hasNbt() && this.nbt.contains("HideFlags", NbtElement.NUMBER_TYPE) ? this.nbt.getInt("HideFlags") : 0;
   }

   public void addHideFlag(TooltipSection tooltipSection) {
      NbtCompound lv = this.getOrCreateNbt();
      lv.putInt("HideFlags", lv.getInt("HideFlags") | tooltipSection.getFlag());
   }

   public static void appendEnchantments(List tooltip, NbtList enchantments) {
      for(int i = 0; i < enchantments.size(); ++i) {
         NbtCompound lv = enchantments.getCompound(i);
         Registries.ENCHANTMENT.getOrEmpty(EnchantmentHelper.getIdFromNbt(lv)).ifPresent((e) -> {
            tooltip.add(e.getName(EnchantmentHelper.getLevelFromNbt(lv)));
         });
      }

   }

   private static Collection parseBlockTag(String tag) {
      try {
         return (Collection)BlockArgumentParser.blockOrTag(Registries.BLOCK.getReadOnlyWrapper(), (String)tag, true).map((arg) -> {
            return Lists.newArrayList(new Text[]{arg.blockState().getBlock().getName().formatted(Formatting.DARK_GRAY)});
         }, (arg) -> {
            return (List)arg.tag().stream().map((argx) -> {
               return ((Block)argx.value()).getName().formatted(Formatting.DARK_GRAY);
            }).collect(Collectors.toList());
         });
      } catch (CommandSyntaxException var2) {
         return Lists.newArrayList(new Text[]{Text.literal("missingno").formatted(Formatting.DARK_GRAY)});
      }
   }

   public boolean hasGlint() {
      return this.getItem().hasGlint(this);
   }

   public Rarity getRarity() {
      return this.getItem().getRarity(this);
   }

   public boolean isEnchantable() {
      if (!this.getItem().isEnchantable(this)) {
         return false;
      } else {
         return !this.hasEnchantments();
      }
   }

   public void addEnchantment(Enchantment enchantment, int level) {
      this.getOrCreateNbt();
      if (!this.nbt.contains("Enchantments", NbtElement.LIST_TYPE)) {
         this.nbt.put("Enchantments", new NbtList());
      }

      NbtList lv = this.nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
      lv.add(EnchantmentHelper.createNbt(EnchantmentHelper.getEnchantmentId(enchantment), (byte)level));
   }

   public boolean hasEnchantments() {
      if (this.nbt != null && this.nbt.contains("Enchantments", NbtElement.LIST_TYPE)) {
         return !this.nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE).isEmpty();
      } else {
         return false;
      }
   }

   public void setSubNbt(String key, NbtElement element) {
      this.getOrCreateNbt().put(key, element);
   }

   public boolean isInFrame() {
      return this.holder instanceof ItemFrameEntity;
   }

   public void setHolder(@Nullable Entity holder) {
      this.holder = holder;
   }

   @Nullable
   public ItemFrameEntity getFrame() {
      return this.holder instanceof ItemFrameEntity ? (ItemFrameEntity)this.getHolder() : null;
   }

   @Nullable
   public Entity getHolder() {
      return !this.isEmpty() ? this.holder : null;
   }

   public int getRepairCost() {
      return this.hasNbt() && this.nbt.contains("RepairCost", NbtElement.INT_TYPE) ? this.nbt.getInt("RepairCost") : 0;
   }

   public void setRepairCost(int repairCost) {
      this.getOrCreateNbt().putInt("RepairCost", repairCost);
   }

   public Multimap getAttributeModifiers(EquipmentSlot slot) {
      Object multimap;
      if (this.hasNbt() && this.nbt.contains("AttributeModifiers", NbtElement.LIST_TYPE)) {
         multimap = HashMultimap.create();
         NbtList lv = this.nbt.getList("AttributeModifiers", NbtElement.COMPOUND_TYPE);

         for(int i = 0; i < lv.size(); ++i) {
            NbtCompound lv2 = lv.getCompound(i);
            if (!lv2.contains("Slot", NbtElement.STRING_TYPE) || lv2.getString("Slot").equals(slot.getName())) {
               Optional optional = Registries.ATTRIBUTE.getOrEmpty(Identifier.tryParse(lv2.getString("AttributeName")));
               if (optional.isPresent()) {
                  EntityAttributeModifier lv3 = EntityAttributeModifier.fromNbt(lv2);
                  if (lv3 != null && lv3.getId().getLeastSignificantBits() != 0L && lv3.getId().getMostSignificantBits() != 0L) {
                     ((Multimap)multimap).put((EntityAttribute)optional.get(), lv3);
                  }
               }
            }
         }
      } else {
         multimap = this.getItem().getAttributeModifiers(slot);
      }

      return (Multimap)multimap;
   }

   public void addAttributeModifier(EntityAttribute attribute, EntityAttributeModifier modifier, @Nullable EquipmentSlot slot) {
      this.getOrCreateNbt();
      if (!this.nbt.contains("AttributeModifiers", NbtElement.LIST_TYPE)) {
         this.nbt.put("AttributeModifiers", new NbtList());
      }

      NbtList lv = this.nbt.getList("AttributeModifiers", NbtElement.COMPOUND_TYPE);
      NbtCompound lv2 = modifier.toNbt();
      lv2.putString("AttributeName", Registries.ATTRIBUTE.getId(attribute).toString());
      if (slot != null) {
         lv2.putString("Slot", slot.getName());
      }

      lv.add(lv2);
   }

   public Text toHoverableText() {
      MutableText lv = Text.empty().append(this.getName());
      if (this.hasCustomName()) {
         lv.formatted(Formatting.ITALIC);
      }

      MutableText lv2 = Texts.bracketed(lv);
      if (!this.isEmpty()) {
         lv2.formatted(this.getRarity().formatting).styled((style) -> {
            return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(this)));
         });
      }

      return lv2;
   }

   public boolean canPlaceOn(Registry blockRegistry, CachedBlockPosition pos) {
      if (this.placeChecker == null) {
         this.placeChecker = new BlockPredicatesChecker("CanPlaceOn");
      }

      return this.placeChecker.check(this, blockRegistry, pos);
   }

   public boolean canDestroy(Registry blockRegistry, CachedBlockPosition pos) {
      if (this.destroyChecker == null) {
         this.destroyChecker = new BlockPredicatesChecker("CanDestroy");
      }

      return this.destroyChecker.check(this, blockRegistry, pos);
   }

   public int getBobbingAnimationTime() {
      return this.bobbingAnimationTime;
   }

   public void setBobbingAnimationTime(int bobbingAnimationTime) {
      this.bobbingAnimationTime = bobbingAnimationTime;
   }

   public int getCount() {
      return this.isEmpty() ? 0 : this.count;
   }

   public void setCount(int count) {
      this.count = count;
   }

   public void increment(int amount) {
      this.setCount(this.getCount() + amount);
   }

   public void decrement(int amount) {
      this.increment(-amount);
   }

   public void usageTick(World world, LivingEntity user, int remainingUseTicks) {
      this.getItem().usageTick(world, user, this, remainingUseTicks);
   }

   public void onItemEntityDestroyed(ItemEntity entity) {
      this.getItem().onItemEntityDestroyed(entity);
   }

   public boolean isFood() {
      return this.getItem().isFood();
   }

   public SoundEvent getDrinkSound() {
      return this.getItem().getDrinkSound();
   }

   public SoundEvent getEatSound() {
      return this.getItem().getEatSound();
   }

   static {
      DISABLED_TEXT = Text.translatable("item.disabled").formatted(Formatting.RED);
      LORE_STYLE = Style.EMPTY.withColor(Formatting.DARK_PURPLE).withItalic(true);
   }

   public static enum TooltipSection {
      ENCHANTMENTS,
      MODIFIERS,
      UNBREAKABLE,
      CAN_DESTROY,
      CAN_PLACE,
      ADDITIONAL,
      DYE,
      UPGRADES;

      private final int flag = 1 << this.ordinal();

      public int getFlag() {
         return this.flag;
      }

      // $FF: synthetic method
      private static TooltipSection[] method_36678() {
         return new TooltipSection[]{ENCHANTMENTS, MODIFIERS, UNBREAKABLE, CAN_DESTROY, CAN_PLACE, ADDITIONAL, DYE, UPGRADES};
      }
   }
}
