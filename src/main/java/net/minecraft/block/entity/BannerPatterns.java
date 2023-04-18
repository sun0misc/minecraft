package net.minecraft.block.entity;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class BannerPatterns {
   public static final RegistryKey BASE = of("base");
   public static final RegistryKey SQUARE_BOTTOM_LEFT = of("square_bottom_left");
   public static final RegistryKey SQUARE_BOTTOM_RIGHT = of("square_bottom_right");
   public static final RegistryKey SQUARE_TOP_LEFT = of("square_top_left");
   public static final RegistryKey SQUARE_TOP_RIGHT = of("square_top_right");
   public static final RegistryKey STRIPE_BOTTOM = of("stripe_bottom");
   public static final RegistryKey STRIPE_TOP = of("stripe_top");
   public static final RegistryKey STRIPE_LEFT = of("stripe_left");
   public static final RegistryKey STRIPE_RIGHT = of("stripe_right");
   public static final RegistryKey STRIPE_CENTER = of("stripe_center");
   public static final RegistryKey STRIPE_MIDDLE = of("stripe_middle");
   public static final RegistryKey STRIPE_DOWNRIGHT = of("stripe_downright");
   public static final RegistryKey STRIPE_DOWNLEFT = of("stripe_downleft");
   public static final RegistryKey SMALL_STRIPES = of("small_stripes");
   public static final RegistryKey CROSS = of("cross");
   public static final RegistryKey STRAIGHT_CROSS = of("straight_cross");
   public static final RegistryKey TRIANGLE_BOTTOM = of("triangle_bottom");
   public static final RegistryKey TRIANGLE_TOP = of("triangle_top");
   public static final RegistryKey TRIANGLES_BOTTOM = of("triangles_bottom");
   public static final RegistryKey TRIANGLES_TOP = of("triangles_top");
   public static final RegistryKey DIAGONAL_LEFT = of("diagonal_left");
   public static final RegistryKey DIAGONAL_UP_RIGHT = of("diagonal_up_right");
   public static final RegistryKey DIAGONAL_UP_LEFT = of("diagonal_up_left");
   public static final RegistryKey DIAGONAL_RIGHT = of("diagonal_right");
   public static final RegistryKey CIRCLE = of("circle");
   public static final RegistryKey RHOMBUS = of("rhombus");
   public static final RegistryKey HALF_VERTICAL = of("half_vertical");
   public static final RegistryKey HALF_HORIZONTAL = of("half_horizontal");
   public static final RegistryKey HALF_VERTICAL_RIGHT = of("half_vertical_right");
   public static final RegistryKey HALF_HORIZONTAL_BOTTOM = of("half_horizontal_bottom");
   public static final RegistryKey BORDER = of("border");
   public static final RegistryKey CURLY_BORDER = of("curly_border");
   public static final RegistryKey GRADIENT = of("gradient");
   public static final RegistryKey GRADIENT_UP = of("gradient_up");
   public static final RegistryKey BRICKS = of("bricks");
   public static final RegistryKey GLOBE = of("globe");
   public static final RegistryKey CREEPER = of("creeper");
   public static final RegistryKey SKULL = of("skull");
   public static final RegistryKey FLOWER = of("flower");
   public static final RegistryKey MOJANG = of("mojang");
   public static final RegistryKey PIGLIN = of("piglin");

   private static RegistryKey of(String id) {
      return RegistryKey.of(RegistryKeys.BANNER_PATTERN, new Identifier(id));
   }

   public static BannerPattern registerAndGetDefault(Registry registry) {
      Registry.register(registry, (RegistryKey)BASE, new BannerPattern("b"));
      Registry.register(registry, (RegistryKey)SQUARE_BOTTOM_LEFT, new BannerPattern("bl"));
      Registry.register(registry, (RegistryKey)SQUARE_BOTTOM_RIGHT, new BannerPattern("br"));
      Registry.register(registry, (RegistryKey)SQUARE_TOP_LEFT, new BannerPattern("tl"));
      Registry.register(registry, (RegistryKey)SQUARE_TOP_RIGHT, new BannerPattern("tr"));
      Registry.register(registry, (RegistryKey)STRIPE_BOTTOM, new BannerPattern("bs"));
      Registry.register(registry, (RegistryKey)STRIPE_TOP, new BannerPattern("ts"));
      Registry.register(registry, (RegistryKey)STRIPE_LEFT, new BannerPattern("ls"));
      Registry.register(registry, (RegistryKey)STRIPE_RIGHT, new BannerPattern("rs"));
      Registry.register(registry, (RegistryKey)STRIPE_CENTER, new BannerPattern("cs"));
      Registry.register(registry, (RegistryKey)STRIPE_MIDDLE, new BannerPattern("ms"));
      Registry.register(registry, (RegistryKey)STRIPE_DOWNRIGHT, new BannerPattern("drs"));
      Registry.register(registry, (RegistryKey)STRIPE_DOWNLEFT, new BannerPattern("dls"));
      Registry.register(registry, (RegistryKey)SMALL_STRIPES, new BannerPattern("ss"));
      Registry.register(registry, (RegistryKey)CROSS, new BannerPattern("cr"));
      Registry.register(registry, (RegistryKey)STRAIGHT_CROSS, new BannerPattern("sc"));
      Registry.register(registry, (RegistryKey)TRIANGLE_BOTTOM, new BannerPattern("bt"));
      Registry.register(registry, (RegistryKey)TRIANGLE_TOP, new BannerPattern("tt"));
      Registry.register(registry, (RegistryKey)TRIANGLES_BOTTOM, new BannerPattern("bts"));
      Registry.register(registry, (RegistryKey)TRIANGLES_TOP, new BannerPattern("tts"));
      Registry.register(registry, (RegistryKey)DIAGONAL_LEFT, new BannerPattern("ld"));
      Registry.register(registry, (RegistryKey)DIAGONAL_UP_RIGHT, new BannerPattern("rd"));
      Registry.register(registry, (RegistryKey)DIAGONAL_UP_LEFT, new BannerPattern("lud"));
      Registry.register(registry, (RegistryKey)DIAGONAL_RIGHT, new BannerPattern("rud"));
      Registry.register(registry, (RegistryKey)CIRCLE, new BannerPattern("mc"));
      Registry.register(registry, (RegistryKey)RHOMBUS, new BannerPattern("mr"));
      Registry.register(registry, (RegistryKey)HALF_VERTICAL, new BannerPattern("vh"));
      Registry.register(registry, (RegistryKey)HALF_HORIZONTAL, new BannerPattern("hh"));
      Registry.register(registry, (RegistryKey)HALF_VERTICAL_RIGHT, new BannerPattern("vhr"));
      Registry.register(registry, (RegistryKey)HALF_HORIZONTAL_BOTTOM, new BannerPattern("hhb"));
      Registry.register(registry, (RegistryKey)BORDER, new BannerPattern("bo"));
      Registry.register(registry, (RegistryKey)CURLY_BORDER, new BannerPattern("cbo"));
      Registry.register(registry, (RegistryKey)GRADIENT, new BannerPattern("gra"));
      Registry.register(registry, (RegistryKey)GRADIENT_UP, new BannerPattern("gru"));
      Registry.register(registry, (RegistryKey)BRICKS, new BannerPattern("bri"));
      Registry.register(registry, (RegistryKey)GLOBE, new BannerPattern("glb"));
      Registry.register(registry, (RegistryKey)CREEPER, new BannerPattern("cre"));
      Registry.register(registry, (RegistryKey)SKULL, new BannerPattern("sku"));
      Registry.register(registry, (RegistryKey)FLOWER, new BannerPattern("flo"));
      Registry.register(registry, (RegistryKey)MOJANG, new BannerPattern("moj"));
      return (BannerPattern)Registry.register(registry, (RegistryKey)PIGLIN, new BannerPattern("pig"));
   }
}
