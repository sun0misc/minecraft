package net.minecraft.village;

import com.google.common.collect.ImmutableSet;
import java.util.function.Predicate;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.PointOfInterestTypeTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.Nullable;

public record VillagerProfession(String id, Predicate heldWorkstation, Predicate acquirableWorkstation, ImmutableSet gatherableItems, ImmutableSet secondaryJobSites, @Nullable SoundEvent workSound) {
   public static final Predicate IS_ACQUIRABLE_JOB_SITE = (poiType) -> {
      return poiType.isIn(PointOfInterestTypeTags.ACQUIRABLE_JOB_SITE);
   };
   public static final VillagerProfession NONE;
   public static final VillagerProfession ARMORER;
   public static final VillagerProfession BUTCHER;
   public static final VillagerProfession CARTOGRAPHER;
   public static final VillagerProfession CLERIC;
   public static final VillagerProfession FARMER;
   public static final VillagerProfession FISHERMAN;
   public static final VillagerProfession FLETCHER;
   public static final VillagerProfession LEATHERWORKER;
   public static final VillagerProfession LIBRARIAN;
   public static final VillagerProfession MASON;
   public static final VillagerProfession NITWIT;
   public static final VillagerProfession SHEPHERD;
   public static final VillagerProfession TOOLSMITH;
   public static final VillagerProfession WEAPONSMITH;

   public VillagerProfession(String id, Predicate predicate, Predicate predicate2, ImmutableSet immutableSet, ImmutableSet immutableSet2, @Nullable SoundEvent arg) {
      this.id = id;
      this.heldWorkstation = predicate;
      this.acquirableWorkstation = predicate2;
      this.gatherableItems = immutableSet;
      this.secondaryJobSites = immutableSet2;
      this.workSound = arg;
   }

   public String toString() {
      return this.id;
   }

   private static VillagerProfession register(String id, RegistryKey heldWorkstation, @Nullable SoundEvent workSound) {
      return register(id, (entry) -> {
         return entry.matchesKey(heldWorkstation);
      }, (entry) -> {
         return entry.matchesKey(heldWorkstation);
      }, workSound);
   }

   private static VillagerProfession register(String id, Predicate heldWorkstation, Predicate acquirableWorkstation, @Nullable SoundEvent workSound) {
      return register(id, heldWorkstation, acquirableWorkstation, ImmutableSet.of(), ImmutableSet.of(), workSound);
   }

   private static VillagerProfession register(String id, RegistryKey heldWorkstation, ImmutableSet gatherableItems, ImmutableSet secondaryJobSites, @Nullable SoundEvent workSound) {
      return register(id, (entry) -> {
         return entry.matchesKey(heldWorkstation);
      }, (entry) -> {
         return entry.matchesKey(heldWorkstation);
      }, gatherableItems, secondaryJobSites, workSound);
   }

   private static VillagerProfession register(String id, Predicate heldWorkstation, Predicate acquirableWorkstation, ImmutableSet gatherableItems, ImmutableSet secondaryJobSites, @Nullable SoundEvent workSound) {
      return (VillagerProfession)Registry.register(Registries.VILLAGER_PROFESSION, (Identifier)(new Identifier(id)), new VillagerProfession(id, heldWorkstation, acquirableWorkstation, gatherableItems, secondaryJobSites, workSound));
   }

   public String id() {
      return this.id;
   }

   public Predicate heldWorkstation() {
      return this.heldWorkstation;
   }

   public Predicate acquirableWorkstation() {
      return this.acquirableWorkstation;
   }

   public ImmutableSet gatherableItems() {
      return this.gatherableItems;
   }

   public ImmutableSet secondaryJobSites() {
      return this.secondaryJobSites;
   }

   @Nullable
   public SoundEvent workSound() {
      return this.workSound;
   }

   static {
      NONE = register("none", PointOfInterestType.NONE, IS_ACQUIRABLE_JOB_SITE, (SoundEvent)null);
      ARMORER = register("armorer", PointOfInterestTypes.ARMORER, SoundEvents.ENTITY_VILLAGER_WORK_ARMORER);
      BUTCHER = register("butcher", PointOfInterestTypes.BUTCHER, SoundEvents.ENTITY_VILLAGER_WORK_BUTCHER);
      CARTOGRAPHER = register("cartographer", PointOfInterestTypes.CARTOGRAPHER, SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER);
      CLERIC = register("cleric", PointOfInterestTypes.CLERIC, SoundEvents.ENTITY_VILLAGER_WORK_CLERIC);
      FARMER = register("farmer", PointOfInterestTypes.FARMER, ImmutableSet.of(Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.BONE_MEAL), ImmutableSet.of(Blocks.FARMLAND), SoundEvents.ENTITY_VILLAGER_WORK_FARMER);
      FISHERMAN = register("fisherman", PointOfInterestTypes.FISHERMAN, SoundEvents.ENTITY_VILLAGER_WORK_FISHERMAN);
      FLETCHER = register("fletcher", PointOfInterestTypes.FLETCHER, SoundEvents.ENTITY_VILLAGER_WORK_FLETCHER);
      LEATHERWORKER = register("leatherworker", PointOfInterestTypes.LEATHERWORKER, SoundEvents.ENTITY_VILLAGER_WORK_LEATHERWORKER);
      LIBRARIAN = register("librarian", PointOfInterestTypes.LIBRARIAN, SoundEvents.ENTITY_VILLAGER_WORK_LIBRARIAN);
      MASON = register("mason", PointOfInterestTypes.MASON, SoundEvents.ENTITY_VILLAGER_WORK_MASON);
      NITWIT = register("nitwit", PointOfInterestType.NONE, PointOfInterestType.NONE, (SoundEvent)null);
      SHEPHERD = register("shepherd", PointOfInterestTypes.SHEPHERD, SoundEvents.ENTITY_VILLAGER_WORK_SHEPHERD);
      TOOLSMITH = register("toolsmith", PointOfInterestTypes.TOOLSMITH, SoundEvents.ENTITY_VILLAGER_WORK_TOOLSMITH);
      WEAPONSMITH = register("weaponsmith", PointOfInterestTypes.WEAPONSMITH, SoundEvents.ENTITY_VILLAGER_WORK_WEAPONSMITH);
   }
}
