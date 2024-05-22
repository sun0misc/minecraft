/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;

public class Schema100
extends Schema {
    public Schema100(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    protected static TypeTemplate targetItems(Schema schema) {
        return DSL.optionalFields("ArmorItems", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "HandItems", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "body_armor_item", TypeReferences.ITEM_STACK.in(schema));
    }

    protected static void targetEntityItems(Schema schema, Map<String, Supplier<TypeTemplate>> map, String entityId) {
        schema.register(map, entityId, () -> Schema100.targetItems(schema));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
        Schema100.targetEntityItems(schema, map, "ArmorStand");
        Schema100.targetEntityItems(schema, map, "Creeper");
        Schema100.targetEntityItems(schema, map, "Skeleton");
        Schema100.targetEntityItems(schema, map, "Spider");
        Schema100.targetEntityItems(schema, map, "Giant");
        Schema100.targetEntityItems(schema, map, "Zombie");
        Schema100.targetEntityItems(schema, map, "Slime");
        Schema100.targetEntityItems(schema, map, "Ghast");
        Schema100.targetEntityItems(schema, map, "PigZombie");
        schema.register(map, "Enderman", (String name) -> DSL.optionalFields("carried", TypeReferences.BLOCK_NAME.in(schema), Schema100.targetItems(schema)));
        Schema100.targetEntityItems(schema, map, "CaveSpider");
        Schema100.targetEntityItems(schema, map, "Silverfish");
        Schema100.targetEntityItems(schema, map, "Blaze");
        Schema100.targetEntityItems(schema, map, "LavaSlime");
        Schema100.targetEntityItems(schema, map, "EnderDragon");
        Schema100.targetEntityItems(schema, map, "WitherBoss");
        Schema100.targetEntityItems(schema, map, "Bat");
        Schema100.targetEntityItems(schema, map, "Witch");
        Schema100.targetEntityItems(schema, map, "Endermite");
        Schema100.targetEntityItems(schema, map, "Guardian");
        Schema100.targetEntityItems(schema, map, "Pig");
        Schema100.targetEntityItems(schema, map, "Sheep");
        Schema100.targetEntityItems(schema, map, "Cow");
        Schema100.targetEntityItems(schema, map, "Chicken");
        Schema100.targetEntityItems(schema, map, "Squid");
        Schema100.targetEntityItems(schema, map, "Wolf");
        Schema100.targetEntityItems(schema, map, "MushroomCow");
        Schema100.targetEntityItems(schema, map, "SnowMan");
        Schema100.targetEntityItems(schema, map, "Ozelot");
        Schema100.targetEntityItems(schema, map, "VillagerGolem");
        schema.register(map, "EntityHorse", (String name) -> DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "ArmorItem", TypeReferences.ITEM_STACK.in(schema), "SaddleItem", TypeReferences.ITEM_STACK.in(schema), Schema100.targetItems(schema)));
        Schema100.targetEntityItems(schema, map, "Rabbit");
        schema.register(map, "Villager", (String name) -> DSL.optionalFields("Inventory", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "Offers", DSL.optionalFields("Recipes", DSL.list(TypeReferences.VILLAGER_TRADE.in(schema))), Schema100.targetItems(schema)));
        Schema100.targetEntityItems(schema, map, "Shulker");
        schema.register(map, "AreaEffectCloud", (String string) -> DSL.optionalFields("Particle", TypeReferences.PARTICLE.in(schema)));
        schema.registerSimple(map, "ShulkerBullet");
        return map;
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);
        schema.registerType(false, TypeReferences.STRUCTURE, () -> DSL.optionalFields("entities", DSL.list(DSL.optionalFields("nbt", TypeReferences.ENTITY_TREE.in(schema))), "blocks", DSL.list(DSL.optionalFields("nbt", TypeReferences.BLOCK_ENTITY.in(schema))), "palette", DSL.list(TypeReferences.BLOCK_STATE.in(schema))));
        schema.registerType(false, TypeReferences.BLOCK_STATE, DSL::remainder);
        schema.registerType(false, TypeReferences.FLAT_BLOCK_STATE, DSL::remainder);
    }
}

