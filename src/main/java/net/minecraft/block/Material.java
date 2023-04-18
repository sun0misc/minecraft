package net.minecraft.block;

public final class Material {
   public static final Material PLANT;
   public static final Material AGGREGATE;
   public static final Material WOOD;
   public static final Material STONE;
   public static final Material GLASS;
   public static final Material ALLOWS_MOVEMENT_LIGHT_PASSES_THROUGH_NOT_SOLID_REPLACEABLE;
   public static final Material ALLOWS_MOVEMENT_LIGHT_PASSES_THROUGH_NOT_SOLID;
   public static final Material ALLOWS_MOVEMENT;
   public static final Material LIGHT_PASSES_THROUGH;
   public static final Material COBWEB;
   public static final Material NOT_SOLID_ALLOWS_MOVEMENT;
   public static final Material GENERIC;
   private final MapColor color;
   private final boolean blocksMovement;
   private final boolean blocksLight;
   private final boolean replaceable;
   private final boolean solid;

   public Material(MapColor color, boolean solid, boolean blocksMovement, boolean blocksLight, boolean replaceable) {
      this.color = color;
      this.solid = solid;
      this.blocksMovement = blocksMovement;
      this.blocksLight = blocksLight;
      this.replaceable = replaceable;
   }

   public boolean isSolid() {
      return this.solid;
   }

   public boolean blocksMovement() {
      return this.blocksMovement;
   }

   public boolean isReplaceable() {
      return this.replaceable;
   }

   public boolean blocksLight() {
      return this.blocksLight;
   }

   public MapColor getColor() {
      return this.color;
   }

   static {
      PLANT = (new Builder(MapColor.DARK_GREEN)).allowsMovement().lightPassesThrough().notSolid().build();
      AGGREGATE = (new Builder(MapColor.PALE_YELLOW)).build();
      WOOD = (new Builder(MapColor.OAK_TAN)).build();
      STONE = (new Builder(MapColor.STONE_GRAY)).build();
      GLASS = (new Builder(MapColor.CLEAR)).lightPassesThrough().build();
      ALLOWS_MOVEMENT_LIGHT_PASSES_THROUGH_NOT_SOLID_REPLACEABLE = (new Builder(MapColor.CLEAR)).allowsMovement().lightPassesThrough().notSolid().replaceable().build();
      ALLOWS_MOVEMENT_LIGHT_PASSES_THROUGH_NOT_SOLID = (new Builder(MapColor.CLEAR)).allowsMovement().lightPassesThrough().notSolid().build();
      ALLOWS_MOVEMENT = (new Builder(MapColor.CLEAR)).allowsMovement().build();
      LIGHT_PASSES_THROUGH = (new Builder(MapColor.CLEAR)).lightPassesThrough().build();
      COBWEB = (new Builder(MapColor.CLEAR)).allowsMovement().lightPassesThrough().build();
      NOT_SOLID_ALLOWS_MOVEMENT = (new Builder(MapColor.CLEAR)).notSolid().allowsMovement().build();
      GENERIC = (new Builder(MapColor.CLEAR)).build();
   }

   public static class Builder {
      private boolean blocksMovement = true;
      private boolean replaceable;
      private boolean solid = true;
      private final MapColor color;
      private boolean blocksLight = true;

      public Builder(MapColor color) {
         this.color = color;
      }

      public Builder notSolid() {
         this.solid = false;
         return this;
      }

      public Builder allowsMovement() {
         this.blocksMovement = false;
         return this;
      }

      Builder lightPassesThrough() {
         this.blocksLight = false;
         return this;
      }

      public Builder replaceable() {
         this.replaceable = true;
         return this;
      }

      public Material build() {
         return new Material(this.color, this.solid, this.blocksMovement, this.blocksLight, this.replaceable);
      }
   }
}
