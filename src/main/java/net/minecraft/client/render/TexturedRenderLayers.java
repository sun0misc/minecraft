package net.minecraft.client.render;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.DecoratedPotPatterns;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TexturedRenderLayers {
   public static final Identifier SHULKER_BOXES_ATLAS_TEXTURE = new Identifier("textures/atlas/shulker_boxes.png");
   public static final Identifier BEDS_ATLAS_TEXTURE = new Identifier("textures/atlas/beds.png");
   public static final Identifier BANNER_PATTERNS_ATLAS_TEXTURE = new Identifier("textures/atlas/banner_patterns.png");
   public static final Identifier SHIELD_PATTERNS_ATLAS_TEXTURE = new Identifier("textures/atlas/shield_patterns.png");
   public static final Identifier SIGNS_ATLAS_TEXTURE = new Identifier("textures/atlas/signs.png");
   public static final Identifier CHEST_ATLAS_TEXTURE = new Identifier("textures/atlas/chest.png");
   public static final Identifier ARMOR_TRIMS_ATLAS_TEXTURE = new Identifier("textures/atlas/armor_trims.png");
   public static final Identifier DECORATED_POT_ATLAS_TEXTURE = new Identifier("textures/atlas/decorated_pot.png");
   private static final RenderLayer SHULKER_BOXES_RENDER_LAYER;
   private static final RenderLayer BEDS_RENDER_LAYER;
   private static final RenderLayer BANNER_PATTERNS_RENDER_LAYER;
   private static final RenderLayer SHIELD_PATTERNS_RENDER_LAYER;
   private static final RenderLayer SIGN_RENDER_LAYER;
   private static final RenderLayer CHEST_RENDER_LAYER;
   private static final RenderLayer ARMOR_TRIMS_RENDER_LAYER;
   private static final RenderLayer ENTITY_SOLID;
   private static final RenderLayer ENTITY_CUTOUT;
   private static final RenderLayer ITEM_ENTITY_TRANSLUCENT_CULL;
   private static final RenderLayer ENTITY_TRANSLUCENT_CULL;
   public static final SpriteIdentifier SHULKER_TEXTURE_ID;
   public static final List COLORED_SHULKER_BOXES_TEXTURES;
   public static final Map SIGN_TYPE_TEXTURES;
   public static final Map HANGING_SIGN_TYPE_TEXTURES;
   public static final Map BANNER_PATTERN_TEXTURES;
   public static final Map SHIELD_PATTERN_TEXTURES;
   public static final Map DECORATED_POT_PATTERN_TEXTURES;
   public static final SpriteIdentifier[] BED_TEXTURES;
   public static final SpriteIdentifier TRAPPED;
   public static final SpriteIdentifier TRAPPED_LEFT;
   public static final SpriteIdentifier TRAPPED_RIGHT;
   public static final SpriteIdentifier CHRISTMAS;
   public static final SpriteIdentifier CHRISTMAS_LEFT;
   public static final SpriteIdentifier CHRISTMAS_RIGHT;
   public static final SpriteIdentifier NORMAL;
   public static final SpriteIdentifier NORMAL_LEFT;
   public static final SpriteIdentifier NORMAL_RIGHT;
   public static final SpriteIdentifier ENDER;

   public static RenderLayer getBannerPatterns() {
      return BANNER_PATTERNS_RENDER_LAYER;
   }

   public static RenderLayer getShieldPatterns() {
      return SHIELD_PATTERNS_RENDER_LAYER;
   }

   public static RenderLayer getBeds() {
      return BEDS_RENDER_LAYER;
   }

   public static RenderLayer getShulkerBoxes() {
      return SHULKER_BOXES_RENDER_LAYER;
   }

   public static RenderLayer getSign() {
      return SIGN_RENDER_LAYER;
   }

   public static RenderLayer getHangingSign() {
      return SIGN_RENDER_LAYER;
   }

   public static RenderLayer getChest() {
      return CHEST_RENDER_LAYER;
   }

   public static RenderLayer getArmorTrims() {
      return ARMOR_TRIMS_RENDER_LAYER;
   }

   public static RenderLayer getEntitySolid() {
      return ENTITY_SOLID;
   }

   public static RenderLayer getEntityCutout() {
      return ENTITY_CUTOUT;
   }

   public static RenderLayer getItemEntityTranslucentCull() {
      return ITEM_ENTITY_TRANSLUCENT_CULL;
   }

   public static RenderLayer getEntityTranslucentCull() {
      return ENTITY_TRANSLUCENT_CULL;
   }

   public static void addDefaultTextures(Consumer adder) {
      adder.accept(SHULKER_TEXTURE_ID);
      COLORED_SHULKER_BOXES_TEXTURES.forEach(adder);
      BANNER_PATTERN_TEXTURES.values().forEach(adder);
      SHIELD_PATTERN_TEXTURES.values().forEach(adder);
      SIGN_TYPE_TEXTURES.values().forEach(adder);
      HANGING_SIGN_TYPE_TEXTURES.values().forEach(adder);
      SpriteIdentifier[] var1 = BED_TEXTURES;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         SpriteIdentifier lv = var1[var3];
         adder.accept(lv);
      }

      adder.accept(TRAPPED);
      adder.accept(TRAPPED_LEFT);
      adder.accept(TRAPPED_RIGHT);
      adder.accept(CHRISTMAS);
      adder.accept(CHRISTMAS_LEFT);
      adder.accept(CHRISTMAS_RIGHT);
      adder.accept(NORMAL);
      adder.accept(NORMAL_LEFT);
      adder.accept(NORMAL_RIGHT);
      adder.accept(ENDER);
   }

   private static SpriteIdentifier createSignTextureId(WoodType type) {
      return new SpriteIdentifier(SIGNS_ATLAS_TEXTURE, new Identifier("entity/signs/" + type.name()));
   }

   private static SpriteIdentifier createHangingSignTextureId(WoodType type) {
      return new SpriteIdentifier(SIGNS_ATLAS_TEXTURE, new Identifier("entity/signs/hanging/" + type.name()));
   }

   public static SpriteIdentifier getSignTextureId(WoodType signType) {
      return (SpriteIdentifier)SIGN_TYPE_TEXTURES.get(signType);
   }

   public static SpriteIdentifier getHangingSignTextureId(WoodType signType) {
      return (SpriteIdentifier)HANGING_SIGN_TYPE_TEXTURES.get(signType);
   }

   private static SpriteIdentifier createBannerPatternTextureId(RegistryKey bannerPattern) {
      return new SpriteIdentifier(BANNER_PATTERNS_ATLAS_TEXTURE, BannerPattern.getSpriteId(bannerPattern, true));
   }

   public static SpriteIdentifier getBannerPatternTextureId(RegistryKey bannerPattern) {
      return (SpriteIdentifier)BANNER_PATTERN_TEXTURES.get(bannerPattern);
   }

   private static SpriteIdentifier createShieldPatternTextureId(RegistryKey bannerPattern) {
      return new SpriteIdentifier(SHIELD_PATTERNS_ATLAS_TEXTURE, BannerPattern.getSpriteId(bannerPattern, false));
   }

   public static SpriteIdentifier getShieldPatternTextureId(RegistryKey bannerPattern) {
      return (SpriteIdentifier)SHIELD_PATTERN_TEXTURES.get(bannerPattern);
   }

   private static SpriteIdentifier createChestTextureId(String variant) {
      return new SpriteIdentifier(CHEST_ATLAS_TEXTURE, new Identifier("entity/chest/" + variant));
   }

   private static SpriteIdentifier createDecoratedPotPatternTextureId(RegistryKey potPatternKey) {
      return new SpriteIdentifier(DECORATED_POT_ATLAS_TEXTURE, DecoratedPotPatterns.getTextureId(potPatternKey));
   }

   @Nullable
   public static SpriteIdentifier getDecoratedPotPatternTextureId(@Nullable RegistryKey potPatternKey) {
      return potPatternKey == null ? null : (SpriteIdentifier)DECORATED_POT_PATTERN_TEXTURES.get(potPatternKey);
   }

   public static SpriteIdentifier getChestTextureId(BlockEntity blockEntity, ChestType type, boolean christmas) {
      if (blockEntity instanceof EnderChestBlockEntity) {
         return ENDER;
      } else if (christmas) {
         return getChestTextureId(type, CHRISTMAS, CHRISTMAS_LEFT, CHRISTMAS_RIGHT);
      } else {
         return blockEntity instanceof TrappedChestBlockEntity ? getChestTextureId(type, TRAPPED, TRAPPED_LEFT, TRAPPED_RIGHT) : getChestTextureId(type, NORMAL, NORMAL_LEFT, NORMAL_RIGHT);
      }
   }

   private static SpriteIdentifier getChestTextureId(ChestType type, SpriteIdentifier single, SpriteIdentifier left, SpriteIdentifier right) {
      switch (type) {
         case LEFT:
            return left;
         case RIGHT:
            return right;
         case SINGLE:
         default:
            return single;
      }
   }

   static {
      SHULKER_BOXES_RENDER_LAYER = RenderLayer.getEntityCutoutNoCull(SHULKER_BOXES_ATLAS_TEXTURE);
      BEDS_RENDER_LAYER = RenderLayer.getEntitySolid(BEDS_ATLAS_TEXTURE);
      BANNER_PATTERNS_RENDER_LAYER = RenderLayer.getEntityNoOutline(BANNER_PATTERNS_ATLAS_TEXTURE);
      SHIELD_PATTERNS_RENDER_LAYER = RenderLayer.getEntityNoOutline(SHIELD_PATTERNS_ATLAS_TEXTURE);
      SIGN_RENDER_LAYER = RenderLayer.getEntityCutoutNoCull(SIGNS_ATLAS_TEXTURE);
      CHEST_RENDER_LAYER = RenderLayer.getEntityCutout(CHEST_ATLAS_TEXTURE);
      ARMOR_TRIMS_RENDER_LAYER = RenderLayer.getArmorCutoutNoCull(ARMOR_TRIMS_ATLAS_TEXTURE);
      ENTITY_SOLID = RenderLayer.getEntitySolid(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
      ENTITY_CUTOUT = RenderLayer.getEntityCutout(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
      ITEM_ENTITY_TRANSLUCENT_CULL = RenderLayer.getItemEntityTranslucentCull(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
      ENTITY_TRANSLUCENT_CULL = RenderLayer.getEntityTranslucentCull(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
      SHULKER_TEXTURE_ID = new SpriteIdentifier(SHULKER_BOXES_ATLAS_TEXTURE, new Identifier("entity/shulker/shulker"));
      COLORED_SHULKER_BOXES_TEXTURES = (List)Stream.of("white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black").map((colorName) -> {
         return new SpriteIdentifier(SHULKER_BOXES_ATLAS_TEXTURE, new Identifier("entity/shulker/shulker_" + colorName));
      }).collect(ImmutableList.toImmutableList());
      SIGN_TYPE_TEXTURES = (Map)WoodType.stream().collect(Collectors.toMap(Function.identity(), TexturedRenderLayers::createSignTextureId));
      HANGING_SIGN_TYPE_TEXTURES = (Map)WoodType.stream().collect(Collectors.toMap(Function.identity(), TexturedRenderLayers::createHangingSignTextureId));
      BANNER_PATTERN_TEXTURES = (Map)Registries.BANNER_PATTERN.getKeys().stream().collect(Collectors.toMap(Function.identity(), TexturedRenderLayers::createBannerPatternTextureId));
      SHIELD_PATTERN_TEXTURES = (Map)Registries.BANNER_PATTERN.getKeys().stream().collect(Collectors.toMap(Function.identity(), TexturedRenderLayers::createShieldPatternTextureId));
      DECORATED_POT_PATTERN_TEXTURES = (Map)Registries.DECORATED_POT_PATTERNS.getKeys().stream().collect(Collectors.toMap(Function.identity(), TexturedRenderLayers::createDecoratedPotPatternTextureId));
      BED_TEXTURES = (SpriteIdentifier[])Arrays.stream(DyeColor.values()).sorted(Comparator.comparingInt(DyeColor::getId)).map((color) -> {
         return new SpriteIdentifier(BEDS_ATLAS_TEXTURE, new Identifier("entity/bed/" + color.getName()));
      }).toArray((i) -> {
         return new SpriteIdentifier[i];
      });
      TRAPPED = createChestTextureId("trapped");
      TRAPPED_LEFT = createChestTextureId("trapped_left");
      TRAPPED_RIGHT = createChestTextureId("trapped_right");
      CHRISTMAS = createChestTextureId("christmas");
      CHRISTMAS_LEFT = createChestTextureId("christmas_left");
      CHRISTMAS_RIGHT = createChestTextureId("christmas_right");
      NORMAL = createChestTextureId("normal");
      NORMAL_LEFT = createChestTextureId("normal_left");
      NORMAL_RIGHT = createChestTextureId("normal_right");
      ENDER = createChestTextureId("ender");
   }
}
