package net.minecraft.loot.context;

import net.minecraft.util.Identifier;

public class LootContextParameters {
   public static final LootContextParameter THIS_ENTITY = register("this_entity");
   public static final LootContextParameter LAST_DAMAGE_PLAYER = register("last_damage_player");
   public static final LootContextParameter DAMAGE_SOURCE = register("damage_source");
   public static final LootContextParameter KILLER_ENTITY = register("killer_entity");
   public static final LootContextParameter DIRECT_KILLER_ENTITY = register("direct_killer_entity");
   public static final LootContextParameter ORIGIN = register("origin");
   public static final LootContextParameter BLOCK_STATE = register("block_state");
   public static final LootContextParameter BLOCK_ENTITY = register("block_entity");
   public static final LootContextParameter TOOL = register("tool");
   public static final LootContextParameter EXPLOSION_RADIUS = register("explosion_radius");

   private static LootContextParameter register(String name) {
      return new LootContextParameter(new Identifier(name));
   }
}
