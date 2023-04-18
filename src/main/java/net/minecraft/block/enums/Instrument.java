package net.minecraft.block.enums;

import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.StringIdentifiable;

public enum Instrument implements StringIdentifiable {
   HARP("harp", SoundEvents.BLOCK_NOTE_BLOCK_HARP, Instrument.Type.BASE_BLOCK),
   BASEDRUM("basedrum", SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM, Instrument.Type.BASE_BLOCK),
   SNARE("snare", SoundEvents.BLOCK_NOTE_BLOCK_SNARE, Instrument.Type.BASE_BLOCK),
   HAT("hat", SoundEvents.BLOCK_NOTE_BLOCK_HAT, Instrument.Type.BASE_BLOCK),
   BASS("bass", SoundEvents.BLOCK_NOTE_BLOCK_BASS, Instrument.Type.BASE_BLOCK),
   FLUTE("flute", SoundEvents.BLOCK_NOTE_BLOCK_FLUTE, Instrument.Type.BASE_BLOCK),
   BELL("bell", SoundEvents.BLOCK_NOTE_BLOCK_BELL, Instrument.Type.BASE_BLOCK),
   GUITAR("guitar", SoundEvents.BLOCK_NOTE_BLOCK_GUITAR, Instrument.Type.BASE_BLOCK),
   CHIME("chime", SoundEvents.BLOCK_NOTE_BLOCK_CHIME, Instrument.Type.BASE_BLOCK),
   XYLOPHONE("xylophone", SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE, Instrument.Type.BASE_BLOCK),
   IRON_XYLOPHONE("iron_xylophone", SoundEvents.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, Instrument.Type.BASE_BLOCK),
   COW_BELL("cow_bell", SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL, Instrument.Type.BASE_BLOCK),
   DIDGERIDOO("didgeridoo", SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, Instrument.Type.BASE_BLOCK),
   BIT("bit", SoundEvents.BLOCK_NOTE_BLOCK_BIT, Instrument.Type.BASE_BLOCK),
   BANJO("banjo", SoundEvents.BLOCK_NOTE_BLOCK_BANJO, Instrument.Type.BASE_BLOCK),
   PLING("pling", SoundEvents.BLOCK_NOTE_BLOCK_PLING, Instrument.Type.BASE_BLOCK),
   ZOMBIE("zombie", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_ZOMBIE, Instrument.Type.MOB_HEAD),
   SKELETON("skeleton", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_SKELETON, Instrument.Type.MOB_HEAD),
   CREEPER("creeper", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_CREEPER, Instrument.Type.MOB_HEAD),
   DRAGON("dragon", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_ENDER_DRAGON, Instrument.Type.MOB_HEAD),
   WITHER_SKELETON("wither_skeleton", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_WITHER_SKELETON, Instrument.Type.MOB_HEAD),
   PIGLIN("piglin", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_PIGLIN, Instrument.Type.MOB_HEAD),
   CUSTOM_HEAD("custom_head", SoundEvents.UI_BUTTON_CLICK, Instrument.Type.CUSTOM);

   private final String name;
   private final RegistryEntry sound;
   private final Type type;

   private Instrument(String name, RegistryEntry sound, Type type) {
      this.name = name;
      this.sound = sound;
      this.type = type;
   }

   public String asString() {
      return this.name;
   }

   public RegistryEntry getSound() {
      return this.sound;
   }

   public boolean shouldSpawnNoteParticles() {
      return this.type == Instrument.Type.BASE_BLOCK;
   }

   public boolean hasCustomSound() {
      return this.type == Instrument.Type.CUSTOM;
   }

   public boolean shouldRequireAirAbove() {
      return this.type == Instrument.Type.BASE_BLOCK;
   }

   public static Optional fromAboveState(BlockState state) {
      if (state.isOf(Blocks.ZOMBIE_HEAD)) {
         return Optional.of(ZOMBIE);
      } else if (state.isOf(Blocks.SKELETON_SKULL)) {
         return Optional.of(SKELETON);
      } else if (state.isOf(Blocks.CREEPER_HEAD)) {
         return Optional.of(CREEPER);
      } else if (state.isOf(Blocks.DRAGON_HEAD)) {
         return Optional.of(DRAGON);
      } else if (state.isOf(Blocks.WITHER_SKELETON_SKULL)) {
         return Optional.of(WITHER_SKELETON);
      } else if (state.isOf(Blocks.PIGLIN_HEAD)) {
         return Optional.of(PIGLIN);
      } else {
         return state.isOf(Blocks.PLAYER_HEAD) ? Optional.of(CUSTOM_HEAD) : Optional.empty();
      }
   }

   public static Instrument fromBelowState(BlockState state) {
      if (state.isOf(Blocks.CLAY)) {
         return FLUTE;
      } else if (state.isOf(Blocks.GOLD_BLOCK)) {
         return BELL;
      } else if (state.isIn(BlockTags.WOOL)) {
         return GUITAR;
      } else if (state.isOf(Blocks.PACKED_ICE)) {
         return CHIME;
      } else if (state.isOf(Blocks.BONE_BLOCK)) {
         return XYLOPHONE;
      } else if (state.isOf(Blocks.IRON_BLOCK)) {
         return IRON_XYLOPHONE;
      } else if (state.isOf(Blocks.SOUL_SAND)) {
         return COW_BELL;
      } else if (state.isOf(Blocks.PUMPKIN)) {
         return DIDGERIDOO;
      } else if (state.isOf(Blocks.EMERALD_BLOCK)) {
         return BIT;
      } else if (state.isOf(Blocks.HAY_BLOCK)) {
         return BANJO;
      } else if (state.isOf(Blocks.GLOWSTONE)) {
         return PLING;
      } else {
         Material lv = state.getMaterial();
         if (lv == Material.STONE) {
            return BASEDRUM;
         } else if (lv == Material.AGGREGATE) {
            return SNARE;
         } else if (lv == Material.GLASS) {
            return HAT;
         } else {
            return lv == Material.WOOD ? BASS : HARP;
         }
      }
   }

   // $FF: synthetic method
   private static Instrument[] method_36730() {
      return new Instrument[]{HARP, BASEDRUM, SNARE, HAT, BASS, FLUTE, BELL, GUITAR, CHIME, XYLOPHONE, IRON_XYLOPHONE, COW_BELL, DIDGERIDOO, BIT, BANJO, PLING, ZOMBIE, SKELETON, CREEPER, DRAGON, WITHER_SKELETON, PIGLIN, CUSTOM_HEAD};
   }

   private static enum Type {
      BASE_BLOCK,
      MOB_HEAD,
      CUSTOM;

      // $FF: synthetic method
      private static Type[] method_47892() {
         return new Type[]{BASE_BLOCK, MOB_HEAD, CUSTOM};
      }
   }
}
