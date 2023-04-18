package net.minecraft.client.sound;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BiomeEffectSoundPlayer implements ClientPlayerTickable {
   private static final int MAX_STRENGTH = 40;
   private static final float field_32995 = 0.001F;
   private final ClientPlayerEntity player;
   private final SoundManager soundManager;
   private final BiomeAccess biomeAccess;
   private final Random random;
   private final Object2ObjectArrayMap soundLoops = new Object2ObjectArrayMap();
   private Optional moodSound = Optional.empty();
   private Optional additionsSound = Optional.empty();
   private float moodPercentage;
   @Nullable
   private Biome activeBiome;

   public BiomeEffectSoundPlayer(ClientPlayerEntity player, SoundManager soundManager, BiomeAccess biomeAccess) {
      this.random = player.world.getRandom();
      this.player = player;
      this.soundManager = soundManager;
      this.biomeAccess = biomeAccess;
   }

   public float getMoodPercentage() {
      return this.moodPercentage;
   }

   public void tick() {
      this.soundLoops.values().removeIf(MovingSoundInstance::isDone);
      Biome lv = (Biome)this.biomeAccess.getBiomeForNoiseGen(this.player.getX(), this.player.getY(), this.player.getZ()).value();
      if (lv != this.activeBiome) {
         this.activeBiome = lv;
         this.moodSound = lv.getMoodSound();
         this.additionsSound = lv.getAdditionsSound();
         this.soundLoops.values().forEach(MusicLoop::fadeOut);
         lv.getLoopSound().ifPresent((sound) -> {
            this.soundLoops.compute(lv, (soundx, loop) -> {
               if (loop == null) {
                  loop = new MusicLoop((SoundEvent)sound.value());
                  this.soundManager.play(loop);
               }

               loop.fadeIn();
               return loop;
            });
         });
      }

      this.additionsSound.ifPresent((sound) -> {
         if (this.random.nextDouble() < sound.getChance()) {
            this.soundManager.play(PositionedSoundInstance.ambient((SoundEvent)sound.getSound().value()));
         }

      });
      this.moodSound.ifPresent((sound) -> {
         World lv = this.player.world;
         int i = sound.getSpawnRange() * 2 + 1;
         BlockPos lv2 = BlockPos.ofFloored(this.player.getX() + (double)this.random.nextInt(i) - (double)sound.getSpawnRange(), this.player.getEyeY() + (double)this.random.nextInt(i) - (double)sound.getSpawnRange(), this.player.getZ() + (double)this.random.nextInt(i) - (double)sound.getSpawnRange());
         int j = lv.getLightLevel(LightType.SKY, lv2);
         if (j > 0) {
            this.moodPercentage -= (float)j / (float)lv.getMaxLightLevel() * 0.001F;
         } else {
            this.moodPercentage -= (float)(lv.getLightLevel(LightType.BLOCK, lv2) - 1) / (float)sound.getCultivationTicks();
         }

         if (this.moodPercentage >= 1.0F) {
            double d = (double)lv2.getX() + 0.5;
            double e = (double)lv2.getY() + 0.5;
            double f = (double)lv2.getZ() + 0.5;
            double g = d - this.player.getX();
            double h = e - this.player.getEyeY();
            double k = f - this.player.getZ();
            double l = Math.sqrt(g * g + h * h + k * k);
            double m = l + sound.getExtraDistance();
            PositionedSoundInstance lv3 = PositionedSoundInstance.ambient((SoundEvent)sound.getSound().value(), this.random, this.player.getX() + g / l * m, this.player.getEyeY() + h / l * m, this.player.getZ() + k / l * m);
            this.soundManager.play(lv3);
            this.moodPercentage = 0.0F;
         } else {
            this.moodPercentage = Math.max(this.moodPercentage, 0.0F);
         }

      });
   }

   @Environment(EnvType.CLIENT)
   public static class MusicLoop extends MovingSoundInstance {
      private int delta;
      private int strength;

      public MusicLoop(SoundEvent sound) {
         super(sound, SoundCategory.AMBIENT, SoundInstance.createRandom());
         this.repeat = true;
         this.repeatDelay = 0;
         this.volume = 1.0F;
         this.relative = true;
      }

      public void tick() {
         if (this.strength < 0) {
            this.setDone();
         }

         this.strength += this.delta;
         this.volume = MathHelper.clamp((float)this.strength / 40.0F, 0.0F, 1.0F);
      }

      public void fadeOut() {
         this.strength = Math.min(this.strength, 40);
         this.delta = -1;
      }

      public void fadeIn() {
         this.strength = Math.max(0, this.strength);
         this.delta = 1;
      }
   }
}
