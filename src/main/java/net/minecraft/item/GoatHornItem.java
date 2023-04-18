package net.minecraft.item;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class GoatHornItem extends Item {
   private static final String INSTRUMENT_KEY = "instrument";
   private final TagKey instrumentTag;

   public GoatHornItem(Item.Settings settings, TagKey instrumentTag) {
      super(settings);
      this.instrumentTag = instrumentTag;
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      Optional optional = this.getInstrument(stack).flatMap(RegistryEntry::getKey);
      if (optional.isPresent()) {
         MutableText lv = Text.translatable(Util.createTranslationKey("instrument", ((RegistryKey)optional.get()).getValue()));
         tooltip.add(lv.formatted(Formatting.GRAY));
      }

   }

   public static ItemStack getStackForInstrument(Item item, RegistryEntry instrument) {
      ItemStack lv = new ItemStack(item);
      setInstrument(lv, instrument);
      return lv;
   }

   public static void setRandomInstrumentFromTag(ItemStack stack, TagKey instrumentTag, Random random) {
      Optional optional = Registries.INSTRUMENT.getEntryList(instrumentTag).flatMap((entryList) -> {
         return entryList.getRandom(random);
      });
      optional.ifPresent((instrument) -> {
         setInstrument(stack, instrument);
      });
   }

   private static void setInstrument(ItemStack stack, RegistryEntry instrument) {
      NbtCompound lv = stack.getOrCreateNbt();
      lv.putString("instrument", ((RegistryKey)instrument.getKey().orElseThrow(() -> {
         return new IllegalStateException("Invalid instrument");
      })).getValue().toString());
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      ItemStack lv = user.getStackInHand(hand);
      Optional optional = this.getInstrument(lv);
      if (optional.isPresent()) {
         Instrument lv2 = (Instrument)((RegistryEntry)optional.get()).value();
         user.setCurrentHand(hand);
         playSound(world, user, lv2);
         user.getItemCooldownManager().set(this, lv2.useDuration());
         user.incrementStat(Stats.USED.getOrCreateStat(this));
         return TypedActionResult.consume(lv);
      } else {
         return TypedActionResult.fail(lv);
      }
   }

   public int getMaxUseTime(ItemStack stack) {
      Optional optional = this.getInstrument(stack);
      return (Integer)optional.map((instrument) -> {
         return ((Instrument)instrument.value()).useDuration();
      }).orElse(0);
   }

   private Optional getInstrument(ItemStack stack) {
      NbtCompound lv = stack.getNbt();
      if (lv != null && lv.contains("instrument", NbtElement.STRING_TYPE)) {
         Identifier lv2 = Identifier.tryParse(lv.getString("instrument"));
         if (lv2 != null) {
            return Registries.INSTRUMENT.getEntry(RegistryKey.of(RegistryKeys.INSTRUMENT, lv2));
         }
      }

      Iterator iterator = Registries.INSTRUMENT.iterateEntries(this.instrumentTag).iterator();
      return iterator.hasNext() ? Optional.of((RegistryEntry)iterator.next()) : Optional.empty();
   }

   public UseAction getUseAction(ItemStack stack) {
      return UseAction.TOOT_HORN;
   }

   private static void playSound(World world, PlayerEntity player, Instrument instrument) {
      SoundEvent lv = (SoundEvent)instrument.soundEvent().value();
      float f = instrument.range() / 16.0F;
      world.playSoundFromEntity(player, player, lv, SoundCategory.RECORDS, f, 1.0F);
      world.emitGameEvent(GameEvent.INSTRUMENT_PLAY, player.getPos(), GameEvent.Emitter.of((Entity)player));
   }
}
