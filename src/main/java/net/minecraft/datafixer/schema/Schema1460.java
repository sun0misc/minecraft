package net.minecraft.datafixer.schema;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;

public class Schema1460 extends IdentifierNormalizingSchema {
   public Schema1460(int i, Schema schema) {
      super(i, schema);
   }

   protected static void targetEntityItems(Schema schema, Map map, String entityId) {
      schema.register(map, entityId, () -> {
         return Schema100.targetItems(schema);
      });
   }

   protected static void method_5273(Schema schema, Map map, String name) {
      schema.register(map, name, () -> {
         return DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)));
      });
   }

   public Map registerEntities(Schema schema) {
      Map map = Maps.newHashMap();
      schema.registerSimple(map, "minecraft:area_effect_cloud");
      targetEntityItems(schema, map, "minecraft:armor_stand");
      schema.register(map, "minecraft:arrow", (name) -> {
         return DSL.optionalFields("inBlockState", TypeReferences.BLOCK_STATE.in(schema));
      });
      targetEntityItems(schema, map, "minecraft:bat");
      targetEntityItems(schema, map, "minecraft:blaze");
      schema.registerSimple(map, "minecraft:boat");
      targetEntityItems(schema, map, "minecraft:cave_spider");
      schema.register(map, "minecraft:chest_minecart", (name) -> {
         return DSL.optionalFields("DisplayState", TypeReferences.BLOCK_STATE.in(schema), "Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)));
      });
      targetEntityItems(schema, map, "minecraft:chicken");
      schema.register(map, "minecraft:commandblock_minecart", (name) -> {
         return DSL.optionalFields("DisplayState", TypeReferences.BLOCK_STATE.in(schema));
      });
      targetEntityItems(schema, map, "minecraft:cow");
      targetEntityItems(schema, map, "minecraft:creeper");
      schema.register(map, "minecraft:donkey", (name) -> {
         return DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "SaddleItem", TypeReferences.ITEM_STACK.in(schema), Schema100.targetItems(schema));
      });
      schema.registerSimple(map, "minecraft:dragon_fireball");
      schema.registerSimple(map, "minecraft:egg");
      targetEntityItems(schema, map, "minecraft:elder_guardian");
      schema.registerSimple(map, "minecraft:ender_crystal");
      targetEntityItems(schema, map, "minecraft:ender_dragon");
      schema.register(map, "minecraft:enderman", (name) -> {
         return DSL.optionalFields("carriedBlockState", TypeReferences.BLOCK_STATE.in(schema), Schema100.targetItems(schema));
      });
      targetEntityItems(schema, map, "minecraft:endermite");
      schema.registerSimple(map, "minecraft:ender_pearl");
      schema.registerSimple(map, "minecraft:evocation_fangs");
      targetEntityItems(schema, map, "minecraft:evocation_illager");
      schema.registerSimple(map, "minecraft:eye_of_ender_signal");
      schema.register(map, "minecraft:falling_block", (name) -> {
         return DSL.optionalFields("BlockState", TypeReferences.BLOCK_STATE.in(schema), "TileEntityData", TypeReferences.BLOCK_ENTITY.in(schema));
      });
      schema.registerSimple(map, "minecraft:fireball");
      schema.register(map, "minecraft:fireworks_rocket", (name) -> {
         return DSL.optionalFields("FireworksItem", TypeReferences.ITEM_STACK.in(schema));
      });
      schema.register(map, "minecraft:furnace_minecart", (name) -> {
         return DSL.optionalFields("DisplayState", TypeReferences.BLOCK_STATE.in(schema));
      });
      targetEntityItems(schema, map, "minecraft:ghast");
      targetEntityItems(schema, map, "minecraft:giant");
      targetEntityItems(schema, map, "minecraft:guardian");
      schema.register(map, "minecraft:hopper_minecart", (name) -> {
         return DSL.optionalFields("DisplayState", TypeReferences.BLOCK_STATE.in(schema), "Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)));
      });
      schema.register(map, "minecraft:horse", (name) -> {
         return DSL.optionalFields("ArmorItem", TypeReferences.ITEM_STACK.in(schema), "SaddleItem", TypeReferences.ITEM_STACK.in(schema), Schema100.targetItems(schema));
      });
      targetEntityItems(schema, map, "minecraft:husk");
      schema.registerSimple(map, "minecraft:illusion_illager");
      schema.register(map, "minecraft:item", (name) -> {
         return DSL.optionalFields("Item", TypeReferences.ITEM_STACK.in(schema));
      });
      schema.register(map, "minecraft:item_frame", (name) -> {
         return DSL.optionalFields("Item", TypeReferences.ITEM_STACK.in(schema));
      });
      schema.registerSimple(map, "minecraft:leash_knot");
      schema.register(map, "minecraft:llama", (name) -> {
         return DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "SaddleItem", TypeReferences.ITEM_STACK.in(schema), "DecorItem", TypeReferences.ITEM_STACK.in(schema), Schema100.targetItems(schema));
      });
      schema.registerSimple(map, "minecraft:llama_spit");
      targetEntityItems(schema, map, "minecraft:magma_cube");
      schema.register(map, "minecraft:minecart", (name) -> {
         return DSL.optionalFields("DisplayState", TypeReferences.BLOCK_STATE.in(schema));
      });
      targetEntityItems(schema, map, "minecraft:mooshroom");
      schema.register(map, "minecraft:mule", (name) -> {
         return DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "SaddleItem", TypeReferences.ITEM_STACK.in(schema), Schema100.targetItems(schema));
      });
      targetEntityItems(schema, map, "minecraft:ocelot");
      schema.registerSimple(map, "minecraft:painting");
      schema.registerSimple(map, "minecraft:parrot");
      targetEntityItems(schema, map, "minecraft:pig");
      targetEntityItems(schema, map, "minecraft:polar_bear");
      schema.register(map, "minecraft:potion", (name) -> {
         return DSL.optionalFields("Potion", TypeReferences.ITEM_STACK.in(schema));
      });
      targetEntityItems(schema, map, "minecraft:rabbit");
      targetEntityItems(schema, map, "minecraft:sheep");
      targetEntityItems(schema, map, "minecraft:shulker");
      schema.registerSimple(map, "minecraft:shulker_bullet");
      targetEntityItems(schema, map, "minecraft:silverfish");
      targetEntityItems(schema, map, "minecraft:skeleton");
      schema.register(map, "minecraft:skeleton_horse", (name) -> {
         return DSL.optionalFields("SaddleItem", TypeReferences.ITEM_STACK.in(schema), Schema100.targetItems(schema));
      });
      targetEntityItems(schema, map, "minecraft:slime");
      schema.registerSimple(map, "minecraft:small_fireball");
      schema.registerSimple(map, "minecraft:snowball");
      targetEntityItems(schema, map, "minecraft:snowman");
      schema.register(map, "minecraft:spawner_minecart", (name) -> {
         return DSL.optionalFields("DisplayState", TypeReferences.BLOCK_STATE.in(schema), TypeReferences.UNTAGGED_SPAWNER.in(schema));
      });
      schema.register(map, "minecraft:spectral_arrow", (name) -> {
         return DSL.optionalFields("inBlockState", TypeReferences.BLOCK_STATE.in(schema));
      });
      targetEntityItems(schema, map, "minecraft:spider");
      targetEntityItems(schema, map, "minecraft:squid");
      targetEntityItems(schema, map, "minecraft:stray");
      schema.registerSimple(map, "minecraft:tnt");
      schema.register(map, "minecraft:tnt_minecart", (name) -> {
         return DSL.optionalFields("DisplayState", TypeReferences.BLOCK_STATE.in(schema));
      });
      targetEntityItems(schema, map, "minecraft:vex");
      schema.register(map, "minecraft:villager", (name) -> {
         return DSL.optionalFields("Inventory", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "Offers", DSL.optionalFields("Recipes", DSL.list(DSL.optionalFields("buy", TypeReferences.ITEM_STACK.in(schema), "buyB", TypeReferences.ITEM_STACK.in(schema), "sell", TypeReferences.ITEM_STACK.in(schema)))), Schema100.targetItems(schema));
      });
      targetEntityItems(schema, map, "minecraft:villager_golem");
      targetEntityItems(schema, map, "minecraft:vindication_illager");
      targetEntityItems(schema, map, "minecraft:witch");
      targetEntityItems(schema, map, "minecraft:wither");
      targetEntityItems(schema, map, "minecraft:wither_skeleton");
      schema.registerSimple(map, "minecraft:wither_skull");
      targetEntityItems(schema, map, "minecraft:wolf");
      schema.registerSimple(map, "minecraft:xp_bottle");
      schema.registerSimple(map, "minecraft:xp_orb");
      targetEntityItems(schema, map, "minecraft:zombie");
      schema.register(map, "minecraft:zombie_horse", (name) -> {
         return DSL.optionalFields("SaddleItem", TypeReferences.ITEM_STACK.in(schema), Schema100.targetItems(schema));
      });
      targetEntityItems(schema, map, "minecraft:zombie_pigman");
      targetEntityItems(schema, map, "minecraft:zombie_villager");
      return map;
   }

   public Map registerBlockEntities(Schema schema) {
      Map map = Maps.newHashMap();
      method_5273(schema, map, "minecraft:furnace");
      method_5273(schema, map, "minecraft:chest");
      method_5273(schema, map, "minecraft:trapped_chest");
      schema.registerSimple(map, "minecraft:ender_chest");
      schema.register(map, "minecraft:jukebox", (name) -> {
         return DSL.optionalFields("RecordItem", TypeReferences.ITEM_STACK.in(schema));
      });
      method_5273(schema, map, "minecraft:dispenser");
      method_5273(schema, map, "minecraft:dropper");
      schema.registerSimple(map, "minecraft:sign");
      schema.register(map, "minecraft:mob_spawner", (name) -> {
         return TypeReferences.UNTAGGED_SPAWNER.in(schema);
      });
      schema.register(map, "minecraft:piston", (name) -> {
         return DSL.optionalFields("blockState", TypeReferences.BLOCK_STATE.in(schema));
      });
      method_5273(schema, map, "minecraft:brewing_stand");
      schema.registerSimple(map, "minecraft:enchanting_table");
      schema.registerSimple(map, "minecraft:end_portal");
      schema.registerSimple(map, "minecraft:beacon");
      schema.registerSimple(map, "minecraft:skull");
      schema.registerSimple(map, "minecraft:daylight_detector");
      method_5273(schema, map, "minecraft:hopper");
      schema.registerSimple(map, "minecraft:comparator");
      schema.registerSimple(map, "minecraft:banner");
      schema.registerSimple(map, "minecraft:structure_block");
      schema.registerSimple(map, "minecraft:end_gateway");
      schema.registerSimple(map, "minecraft:command_block");
      method_5273(schema, map, "minecraft:shulker_box");
      schema.registerSimple(map, "minecraft:bed");
      return map;
   }

   public void registerTypes(Schema schema, Map entityTypes, Map blockEntityTypes) {
      schema.registerType(false, TypeReferences.LEVEL, DSL::remainder);
      schema.registerType(false, TypeReferences.RECIPE, () -> {
         return DSL.constType(getIdentifierType());
      });
      schema.registerType(false, TypeReferences.PLAYER, () -> {
         return DSL.optionalFields("RootVehicle", DSL.optionalFields("Entity", TypeReferences.ENTITY_TREE.in(schema)), "Inventory", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "EnderItems", DSL.list(TypeReferences.ITEM_STACK.in(schema)), DSL.optionalFields("ShoulderEntityLeft", TypeReferences.ENTITY_TREE.in(schema), "ShoulderEntityRight", TypeReferences.ENTITY_TREE.in(schema), "recipeBook", DSL.optionalFields("recipes", DSL.list(TypeReferences.RECIPE.in(schema)), "toBeDisplayed", DSL.list(TypeReferences.RECIPE.in(schema)))));
      });
      schema.registerType(false, TypeReferences.CHUNK, () -> {
         return DSL.fields("Level", DSL.optionalFields("Entities", DSL.list(TypeReferences.ENTITY_TREE.in(schema)), "TileEntities", DSL.list(DSL.or(TypeReferences.BLOCK_ENTITY.in(schema), DSL.remainder())), "TileTicks", DSL.list(DSL.fields("i", TypeReferences.BLOCK_NAME.in(schema))), "Sections", DSL.list(DSL.optionalFields("Palette", DSL.list(TypeReferences.BLOCK_STATE.in(schema))))));
      });
      schema.registerType(true, TypeReferences.BLOCK_ENTITY, () -> {
         return DSL.taggedChoiceLazy("id", getIdentifierType(), blockEntityTypes);
      });
      schema.registerType(true, TypeReferences.ENTITY_TREE, () -> {
         return DSL.optionalFields("Passengers", DSL.list(TypeReferences.ENTITY_TREE.in(schema)), TypeReferences.ENTITY.in(schema));
      });
      schema.registerType(true, TypeReferences.ENTITY, () -> {
         return DSL.taggedChoiceLazy("id", getIdentifierType(), entityTypes);
      });
      schema.registerType(true, TypeReferences.ITEM_STACK, () -> {
         return DSL.hook(DSL.optionalFields("id", TypeReferences.ITEM_NAME.in(schema), "tag", DSL.optionalFields("EntityTag", TypeReferences.ENTITY_TREE.in(schema), "BlockEntityTag", TypeReferences.BLOCK_ENTITY.in(schema), "CanDestroy", DSL.list(TypeReferences.BLOCK_NAME.in(schema)), "CanPlaceOn", DSL.list(TypeReferences.BLOCK_NAME.in(schema)), "Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)))), Schema705.field_5746, HookFunction.IDENTITY);
      });
      schema.registerType(false, TypeReferences.HOTBAR, () -> {
         return DSL.compoundList(DSL.list(TypeReferences.ITEM_STACK.in(schema)));
      });
      schema.registerType(false, TypeReferences.OPTIONS, DSL::remainder);
      schema.registerType(false, TypeReferences.STRUCTURE, () -> {
         return DSL.optionalFields("entities", DSL.list(DSL.optionalFields("nbt", TypeReferences.ENTITY_TREE.in(schema))), "blocks", DSL.list(DSL.optionalFields("nbt", TypeReferences.BLOCK_ENTITY.in(schema))), "palette", DSL.list(TypeReferences.BLOCK_STATE.in(schema)));
      });
      schema.registerType(false, TypeReferences.BLOCK_NAME, () -> {
         return DSL.constType(getIdentifierType());
      });
      schema.registerType(false, TypeReferences.ITEM_NAME, () -> {
         return DSL.constType(getIdentifierType());
      });
      schema.registerType(false, TypeReferences.BLOCK_STATE, DSL::remainder);
      Supplier supplier = () -> {
         return DSL.compoundList(TypeReferences.ITEM_NAME.in(schema), DSL.constType(DSL.intType()));
      };
      schema.registerType(false, TypeReferences.STATS, () -> {
         return DSL.optionalFields("stats", DSL.optionalFields("minecraft:mined", DSL.compoundList(TypeReferences.BLOCK_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:crafted", (TypeTemplate)supplier.get(), "minecraft:used", (TypeTemplate)supplier.get(), "minecraft:broken", (TypeTemplate)supplier.get(), "minecraft:picked_up", (TypeTemplate)supplier.get(), DSL.optionalFields("minecraft:dropped", (TypeTemplate)supplier.get(), "minecraft:killed", DSL.compoundList(TypeReferences.ENTITY_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:killed_by", DSL.compoundList(TypeReferences.ENTITY_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:custom", DSL.compoundList(DSL.constType(getIdentifierType()), DSL.constType(DSL.intType())))));
      });
      schema.registerType(false, TypeReferences.SAVED_DATA, () -> {
         return DSL.optionalFields("data", DSL.optionalFields("Features", DSL.compoundList(TypeReferences.STRUCTURE_FEATURE.in(schema)), "Objectives", DSL.list(TypeReferences.OBJECTIVE.in(schema)), "Teams", DSL.list(TypeReferences.TEAM.in(schema))));
      });
      schema.registerType(false, TypeReferences.STRUCTURE_FEATURE, DSL::remainder);
      Map map3 = Schema1451v6.method_37389(schema);
      schema.registerType(false, TypeReferences.OBJECTIVE, () -> {
         return DSL.hook(DSL.optionalFields("CriteriaType", DSL.taggedChoiceLazy("type", DSL.string(), map3)), Schema1451v6.field_34014, Schema1451v6.field_34015);
      });
      schema.registerType(false, TypeReferences.TEAM, DSL::remainder);
      schema.registerType(true, TypeReferences.UNTAGGED_SPAWNER, () -> {
         return DSL.optionalFields("SpawnPotentials", DSL.list(DSL.fields("Entity", TypeReferences.ENTITY_TREE.in(schema))), "SpawnData", TypeReferences.ENTITY_TREE.in(schema));
      });
      schema.registerType(false, TypeReferences.ADVANCEMENTS, () -> {
         return DSL.optionalFields("minecraft:adventure/adventuring_time", DSL.optionalFields("criteria", DSL.compoundList(TypeReferences.BIOME.in(schema), DSL.constType(DSL.string()))), "minecraft:adventure/kill_a_mob", DSL.optionalFields("criteria", DSL.compoundList(TypeReferences.ENTITY_NAME.in(schema), DSL.constType(DSL.string()))), "minecraft:adventure/kill_all_mobs", DSL.optionalFields("criteria", DSL.compoundList(TypeReferences.ENTITY_NAME.in(schema), DSL.constType(DSL.string()))), "minecraft:husbandry/bred_all_animals", DSL.optionalFields("criteria", DSL.compoundList(TypeReferences.ENTITY_NAME.in(schema), DSL.constType(DSL.string()))));
      });
      schema.registerType(false, TypeReferences.BIOME, () -> {
         return DSL.constType(getIdentifierType());
      });
      schema.registerType(false, TypeReferences.ENTITY_NAME, () -> {
         return DSL.constType(getIdentifierType());
      });
      schema.registerType(false, TypeReferences.POI_CHUNK, DSL::remainder);
      schema.registerType(false, TypeReferences.WORLD_GEN_SETTINGS, DSL::remainder);
      schema.registerType(false, TypeReferences.ENTITY_CHUNK, () -> {
         return DSL.optionalFields("Entities", DSL.list(TypeReferences.ENTITY_TREE.in(schema)));
      });
   }
}
