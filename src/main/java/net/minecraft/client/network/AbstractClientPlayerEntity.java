package net.minecraft.client.network;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class AbstractClientPlayerEntity extends PlayerEntity {
   private static final String SKIN_URL = "http://skins.minecraft.net/MinecraftSkins/%s.png";
   @Nullable
   private PlayerListEntry playerListEntry;
   protected Vec3d lastVelocity;
   public float elytraPitch;
   public float elytraYaw;
   public float elytraRoll;
   public final ClientWorld clientWorld;

   public AbstractClientPlayerEntity(ClientWorld world, GameProfile profile) {
      super(world, world.getSpawnPos(), world.getSpawnAngle(), profile);
      this.lastVelocity = Vec3d.ZERO;
      this.clientWorld = world;
   }

   public boolean isSpectator() {
      PlayerListEntry lv = this.getPlayerListEntry();
      return lv != null && lv.getGameMode() == GameMode.SPECTATOR;
   }

   public boolean isCreative() {
      PlayerListEntry lv = this.getPlayerListEntry();
      return lv != null && lv.getGameMode() == GameMode.CREATIVE;
   }

   public boolean canRenderCapeTexture() {
      return this.getPlayerListEntry() != null;
   }

   @Nullable
   protected PlayerListEntry getPlayerListEntry() {
      if (this.playerListEntry == null) {
         this.playerListEntry = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(this.getUuid());
      }

      return this.playerListEntry;
   }

   public void tick() {
      this.lastVelocity = this.getVelocity();
      super.tick();
   }

   public Vec3d lerpVelocity(float tickDelta) {
      return this.lastVelocity.lerp(this.getVelocity(), (double)tickDelta);
   }

   public boolean hasSkinTexture() {
      PlayerListEntry lv = this.getPlayerListEntry();
      return lv != null && lv.hasSkinTexture();
   }

   public Identifier getSkinTexture() {
      PlayerListEntry lv = this.getPlayerListEntry();
      return lv == null ? DefaultSkinHelper.getTexture(this.getUuid()) : lv.getSkinTexture();
   }

   @Nullable
   public Identifier getCapeTexture() {
      PlayerListEntry lv = this.getPlayerListEntry();
      return lv == null ? null : lv.getCapeTexture();
   }

   public boolean canRenderElytraTexture() {
      return this.getPlayerListEntry() != null;
   }

   @Nullable
   public Identifier getElytraTexture() {
      PlayerListEntry lv = this.getPlayerListEntry();
      return lv == null ? null : lv.getElytraTexture();
   }

   public static void loadSkin(Identifier id, String playerName) {
      TextureManager lv = MinecraftClient.getInstance().getTextureManager();
      AbstractTexture lv2 = lv.getOrDefault(id, MissingSprite.getMissingSpriteTexture());
      if (lv2 == MissingSprite.getMissingSpriteTexture()) {
         AbstractTexture lv2 = new PlayerSkinTexture((File)null, String.format(Locale.ROOT, "http://skins.minecraft.net/MinecraftSkins/%s.png", StringHelper.stripTextFormat(playerName)), DefaultSkinHelper.getTexture(Uuids.getOfflinePlayerUuid(playerName)), true, (Runnable)null);
         lv.registerTexture(id, lv2);
      }

   }

   public static Identifier getSkinId(String playerName) {
      return new Identifier("skins/" + Hashing.sha1().hashUnencodedChars(StringHelper.stripTextFormat(playerName)));
   }

   public String getModel() {
      PlayerListEntry lv = this.getPlayerListEntry();
      return lv == null ? DefaultSkinHelper.getModel(this.getUuid()) : lv.getModel();
   }

   public float getFovMultiplier() {
      float f = 1.0F;
      if (this.getAbilities().flying) {
         f *= 1.1F;
      }

      f *= ((float)this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) / this.getAbilities().getWalkSpeed() + 1.0F) / 2.0F;
      if (this.getAbilities().getWalkSpeed() == 0.0F || Float.isNaN(f) || Float.isInfinite(f)) {
         f = 1.0F;
      }

      ItemStack lv = this.getActiveItem();
      if (this.isUsingItem()) {
         if (lv.isOf(Items.BOW)) {
            int i = this.getItemUseTime();
            float g = (float)i / 20.0F;
            if (g > 1.0F) {
               g = 1.0F;
            } else {
               g *= g;
            }

            f *= 1.0F - g * 0.15F;
         } else if (MinecraftClient.getInstance().options.getPerspective().isFirstPerson() && this.isUsingSpyglass()) {
            return 0.1F;
         }
      }

      return MathHelper.lerp(((Double)MinecraftClient.getInstance().options.getFovEffectScale().getValue()).floatValue(), 1.0F, f);
   }
}
