package net.minecraft.entity.ai.brain;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.GlobalPos;

public class MemoryModuleType {
   public static final MemoryModuleType DUMMY = register("dummy");
   public static final MemoryModuleType HOME;
   public static final MemoryModuleType JOB_SITE;
   public static final MemoryModuleType POTENTIAL_JOB_SITE;
   public static final MemoryModuleType MEETING_POINT;
   public static final MemoryModuleType SECONDARY_JOB_SITE;
   public static final MemoryModuleType MOBS;
   public static final MemoryModuleType VISIBLE_MOBS;
   public static final MemoryModuleType VISIBLE_VILLAGER_BABIES;
   public static final MemoryModuleType NEAREST_PLAYERS;
   public static final MemoryModuleType NEAREST_VISIBLE_PLAYER;
   public static final MemoryModuleType NEAREST_VISIBLE_TARGETABLE_PLAYER;
   public static final MemoryModuleType WALK_TARGET;
   public static final MemoryModuleType LOOK_TARGET;
   public static final MemoryModuleType ATTACK_TARGET;
   public static final MemoryModuleType ATTACK_COOLING_DOWN;
   public static final MemoryModuleType INTERACTION_TARGET;
   public static final MemoryModuleType BREED_TARGET;
   public static final MemoryModuleType RIDE_TARGET;
   public static final MemoryModuleType PATH;
   public static final MemoryModuleType INTERACTABLE_DOORS;
   public static final MemoryModuleType DOORS_TO_CLOSE;
   public static final MemoryModuleType NEAREST_BED;
   public static final MemoryModuleType HURT_BY;
   public static final MemoryModuleType HURT_BY_ENTITY;
   public static final MemoryModuleType AVOID_TARGET;
   public static final MemoryModuleType NEAREST_HOSTILE;
   public static final MemoryModuleType NEAREST_ATTACKABLE;
   public static final MemoryModuleType HIDING_PLACE;
   public static final MemoryModuleType HEARD_BELL_TIME;
   public static final MemoryModuleType CANT_REACH_WALK_TARGET_SINCE;
   public static final MemoryModuleType GOLEM_DETECTED_RECENTLY;
   public static final MemoryModuleType LAST_SLEPT;
   public static final MemoryModuleType LAST_WOKEN;
   public static final MemoryModuleType LAST_WORKED_AT_POI;
   public static final MemoryModuleType NEAREST_VISIBLE_ADULT;
   public static final MemoryModuleType NEAREST_VISIBLE_WANTED_ITEM;
   public static final MemoryModuleType NEAREST_VISIBLE_NEMESIS;
   public static final MemoryModuleType PLAY_DEAD_TICKS;
   public static final MemoryModuleType TEMPTING_PLAYER;
   public static final MemoryModuleType TEMPTATION_COOLDOWN_TICKS;
   public static final MemoryModuleType GAZE_COOLDOWN_TICKS;
   public static final MemoryModuleType IS_TEMPTED;
   public static final MemoryModuleType LONG_JUMP_COOLING_DOWN;
   public static final MemoryModuleType LONG_JUMP_MID_JUMP;
   public static final MemoryModuleType HAS_HUNTING_COOLDOWN;
   public static final MemoryModuleType RAM_COOLDOWN_TICKS;
   public static final MemoryModuleType RAM_TARGET;
   public static final MemoryModuleType IS_IN_WATER;
   public static final MemoryModuleType IS_PREGNANT;
   public static final MemoryModuleType IS_PANICKING;
   public static final MemoryModuleType UNREACHABLE_TONGUE_TARGETS;
   public static final MemoryModuleType ANGRY_AT;
   public static final MemoryModuleType UNIVERSAL_ANGER;
   public static final MemoryModuleType ADMIRING_ITEM;
   public static final MemoryModuleType TIME_TRYING_TO_REACH_ADMIRE_ITEM;
   public static final MemoryModuleType DISABLE_WALK_TO_ADMIRE_ITEM;
   public static final MemoryModuleType ADMIRING_DISABLED;
   public static final MemoryModuleType HUNTED_RECENTLY;
   public static final MemoryModuleType CELEBRATE_LOCATION;
   public static final MemoryModuleType DANCING;
   public static final MemoryModuleType NEAREST_VISIBLE_HUNTABLE_HOGLIN;
   public static final MemoryModuleType NEAREST_VISIBLE_BABY_HOGLIN;
   public static final MemoryModuleType NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD;
   public static final MemoryModuleType NEARBY_ADULT_PIGLINS;
   public static final MemoryModuleType NEAREST_VISIBLE_ADULT_PIGLINS;
   public static final MemoryModuleType NEAREST_VISIBLE_ADULT_HOGLINS;
   public static final MemoryModuleType NEAREST_VISIBLE_ADULT_PIGLIN;
   public static final MemoryModuleType NEAREST_VISIBLE_ZOMBIFIED;
   public static final MemoryModuleType VISIBLE_ADULT_PIGLIN_COUNT;
   public static final MemoryModuleType VISIBLE_ADULT_HOGLIN_COUNT;
   public static final MemoryModuleType NEAREST_PLAYER_HOLDING_WANTED_ITEM;
   public static final MemoryModuleType ATE_RECENTLY;
   public static final MemoryModuleType NEAREST_REPELLENT;
   public static final MemoryModuleType PACIFIED;
   public static final MemoryModuleType ROAR_TARGET;
   public static final MemoryModuleType DISTURBANCE_LOCATION;
   public static final MemoryModuleType RECENT_PROJECTILE;
   public static final MemoryModuleType IS_SNIFFING;
   public static final MemoryModuleType IS_EMERGING;
   public static final MemoryModuleType ROAR_SOUND_DELAY;
   public static final MemoryModuleType DIG_COOLDOWN;
   public static final MemoryModuleType ROAR_SOUND_COOLDOWN;
   public static final MemoryModuleType SNIFF_COOLDOWN;
   public static final MemoryModuleType TOUCH_COOLDOWN;
   public static final MemoryModuleType VIBRATION_COOLDOWN;
   public static final MemoryModuleType SONIC_BOOM_COOLDOWN;
   public static final MemoryModuleType SONIC_BOOM_SOUND_COOLDOWN;
   public static final MemoryModuleType SONIC_BOOM_SOUND_DELAY;
   public static final MemoryModuleType LIKED_PLAYER;
   public static final MemoryModuleType LIKED_NOTEBLOCK;
   public static final MemoryModuleType LIKED_NOTEBLOCK_COOLDOWN_TICKS;
   public static final MemoryModuleType ITEM_PICKUP_COOLDOWN_TICKS;
   public static final MemoryModuleType SNIFFER_EXPLORED_POSITIONS;
   public static final MemoryModuleType SNIFFER_SNIFFING_TARGET;
   public static final MemoryModuleType SNIFFER_DIGGING;
   public static final MemoryModuleType SNIFFER_HAPPY;
   private final Optional codec;

   @VisibleForTesting
   public MemoryModuleType(Optional codec) {
      this.codec = codec.map(Memory::createCodec);
   }

   public String toString() {
      return Registries.MEMORY_MODULE_TYPE.getId(this).toString();
   }

   public Optional getCodec() {
      return this.codec;
   }

   private static MemoryModuleType register(String id, Codec codec) {
      return (MemoryModuleType)Registry.register(Registries.MEMORY_MODULE_TYPE, (Identifier)(new Identifier(id)), new MemoryModuleType(Optional.of(codec)));
   }

   private static MemoryModuleType register(String id) {
      return (MemoryModuleType)Registry.register(Registries.MEMORY_MODULE_TYPE, (Identifier)(new Identifier(id)), new MemoryModuleType(Optional.empty()));
   }

   static {
      HOME = register("home", GlobalPos.CODEC);
      JOB_SITE = register("job_site", GlobalPos.CODEC);
      POTENTIAL_JOB_SITE = register("potential_job_site", GlobalPos.CODEC);
      MEETING_POINT = register("meeting_point", GlobalPos.CODEC);
      SECONDARY_JOB_SITE = register("secondary_job_site");
      MOBS = register("mobs");
      VISIBLE_MOBS = register("visible_mobs");
      VISIBLE_VILLAGER_BABIES = register("visible_villager_babies");
      NEAREST_PLAYERS = register("nearest_players");
      NEAREST_VISIBLE_PLAYER = register("nearest_visible_player");
      NEAREST_VISIBLE_TARGETABLE_PLAYER = register("nearest_visible_targetable_player");
      WALK_TARGET = register("walk_target");
      LOOK_TARGET = register("look_target");
      ATTACK_TARGET = register("attack_target");
      ATTACK_COOLING_DOWN = register("attack_cooling_down");
      INTERACTION_TARGET = register("interaction_target");
      BREED_TARGET = register("breed_target");
      RIDE_TARGET = register("ride_target");
      PATH = register("path");
      INTERACTABLE_DOORS = register("interactable_doors");
      DOORS_TO_CLOSE = register("doors_to_close");
      NEAREST_BED = register("nearest_bed");
      HURT_BY = register("hurt_by");
      HURT_BY_ENTITY = register("hurt_by_entity");
      AVOID_TARGET = register("avoid_target");
      NEAREST_HOSTILE = register("nearest_hostile");
      NEAREST_ATTACKABLE = register("nearest_attackable");
      HIDING_PLACE = register("hiding_place");
      HEARD_BELL_TIME = register("heard_bell_time");
      CANT_REACH_WALK_TARGET_SINCE = register("cant_reach_walk_target_since");
      GOLEM_DETECTED_RECENTLY = register("golem_detected_recently", Codec.BOOL);
      LAST_SLEPT = register("last_slept", Codec.LONG);
      LAST_WOKEN = register("last_woken", Codec.LONG);
      LAST_WORKED_AT_POI = register("last_worked_at_poi", Codec.LONG);
      NEAREST_VISIBLE_ADULT = register("nearest_visible_adult");
      NEAREST_VISIBLE_WANTED_ITEM = register("nearest_visible_wanted_item");
      NEAREST_VISIBLE_NEMESIS = register("nearest_visible_nemesis");
      PLAY_DEAD_TICKS = register("play_dead_ticks", Codec.INT);
      TEMPTING_PLAYER = register("tempting_player");
      TEMPTATION_COOLDOWN_TICKS = register("temptation_cooldown_ticks", Codec.INT);
      GAZE_COOLDOWN_TICKS = register("gaze_cooldown_ticks", Codec.INT);
      IS_TEMPTED = register("is_tempted", Codec.BOOL);
      LONG_JUMP_COOLING_DOWN = register("long_jump_cooling_down", Codec.INT);
      LONG_JUMP_MID_JUMP = register("long_jump_mid_jump");
      HAS_HUNTING_COOLDOWN = register("has_hunting_cooldown", Codec.BOOL);
      RAM_COOLDOWN_TICKS = register("ram_cooldown_ticks", Codec.INT);
      RAM_TARGET = register("ram_target");
      IS_IN_WATER = register("is_in_water", Codec.unit(Unit.INSTANCE));
      IS_PREGNANT = register("is_pregnant", Codec.unit(Unit.INSTANCE));
      IS_PANICKING = register("is_panicking", Codec.BOOL);
      UNREACHABLE_TONGUE_TARGETS = register("unreachable_tongue_targets");
      ANGRY_AT = register("angry_at", Uuids.INT_STREAM_CODEC);
      UNIVERSAL_ANGER = register("universal_anger", Codec.BOOL);
      ADMIRING_ITEM = register("admiring_item", Codec.BOOL);
      TIME_TRYING_TO_REACH_ADMIRE_ITEM = register("time_trying_to_reach_admire_item");
      DISABLE_WALK_TO_ADMIRE_ITEM = register("disable_walk_to_admire_item");
      ADMIRING_DISABLED = register("admiring_disabled", Codec.BOOL);
      HUNTED_RECENTLY = register("hunted_recently", Codec.BOOL);
      CELEBRATE_LOCATION = register("celebrate_location");
      DANCING = register("dancing");
      NEAREST_VISIBLE_HUNTABLE_HOGLIN = register("nearest_visible_huntable_hoglin");
      NEAREST_VISIBLE_BABY_HOGLIN = register("nearest_visible_baby_hoglin");
      NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD = register("nearest_targetable_player_not_wearing_gold");
      NEARBY_ADULT_PIGLINS = register("nearby_adult_piglins");
      NEAREST_VISIBLE_ADULT_PIGLINS = register("nearest_visible_adult_piglins");
      NEAREST_VISIBLE_ADULT_HOGLINS = register("nearest_visible_adult_hoglins");
      NEAREST_VISIBLE_ADULT_PIGLIN = register("nearest_visible_adult_piglin");
      NEAREST_VISIBLE_ZOMBIFIED = register("nearest_visible_zombified");
      VISIBLE_ADULT_PIGLIN_COUNT = register("visible_adult_piglin_count");
      VISIBLE_ADULT_HOGLIN_COUNT = register("visible_adult_hoglin_count");
      NEAREST_PLAYER_HOLDING_WANTED_ITEM = register("nearest_player_holding_wanted_item");
      ATE_RECENTLY = register("ate_recently");
      NEAREST_REPELLENT = register("nearest_repellent");
      PACIFIED = register("pacified");
      ROAR_TARGET = register("roar_target");
      DISTURBANCE_LOCATION = register("disturbance_location");
      RECENT_PROJECTILE = register("recent_projectile", Codec.unit(Unit.INSTANCE));
      IS_SNIFFING = register("is_sniffing", Codec.unit(Unit.INSTANCE));
      IS_EMERGING = register("is_emerging", Codec.unit(Unit.INSTANCE));
      ROAR_SOUND_DELAY = register("roar_sound_delay", Codec.unit(Unit.INSTANCE));
      DIG_COOLDOWN = register("dig_cooldown", Codec.unit(Unit.INSTANCE));
      ROAR_SOUND_COOLDOWN = register("roar_sound_cooldown", Codec.unit(Unit.INSTANCE));
      SNIFF_COOLDOWN = register("sniff_cooldown", Codec.unit(Unit.INSTANCE));
      TOUCH_COOLDOWN = register("touch_cooldown", Codec.unit(Unit.INSTANCE));
      VIBRATION_COOLDOWN = register("vibration_cooldown", Codec.unit(Unit.INSTANCE));
      SONIC_BOOM_COOLDOWN = register("sonic_boom_cooldown", Codec.unit(Unit.INSTANCE));
      SONIC_BOOM_SOUND_COOLDOWN = register("sonic_boom_sound_cooldown", Codec.unit(Unit.INSTANCE));
      SONIC_BOOM_SOUND_DELAY = register("sonic_boom_sound_delay", Codec.unit(Unit.INSTANCE));
      LIKED_PLAYER = register("liked_player", Uuids.INT_STREAM_CODEC);
      LIKED_NOTEBLOCK = register("liked_noteblock", GlobalPos.CODEC);
      LIKED_NOTEBLOCK_COOLDOWN_TICKS = register("liked_noteblock_cooldown_ticks", Codec.INT);
      ITEM_PICKUP_COOLDOWN_TICKS = register("item_pickup_cooldown_ticks", Codec.INT);
      SNIFFER_EXPLORED_POSITIONS = register("sniffer_explored_positions", Codec.list(GlobalPos.CODEC));
      SNIFFER_SNIFFING_TARGET = register("sniffer_sniffing_target");
      SNIFFER_DIGGING = register("sniffer_digging");
      SNIFFER_HAPPY = register("sniffer_happy");
   }
}
