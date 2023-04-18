package net.minecraft.world;

import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Unmodifiable;

@Unmodifiable
public class LocalDifficulty {
   private static final float field_29953 = -72000.0F;
   private static final float field_29954 = 1440000.0F;
   private static final float field_29955 = 3600000.0F;
   private final Difficulty globalDifficulty;
   private final float localDifficulty;

   public LocalDifficulty(Difficulty difficulty, long timeOfDay, long inhabitedTime, float moonSize) {
      this.globalDifficulty = difficulty;
      this.localDifficulty = this.setLocalDifficulty(difficulty, timeOfDay, inhabitedTime, moonSize);
   }

   public Difficulty getGlobalDifficulty() {
      return this.globalDifficulty;
   }

   public float getLocalDifficulty() {
      return this.localDifficulty;
   }

   public boolean isAtLeastHard() {
      return this.localDifficulty >= (float)Difficulty.HARD.ordinal();
   }

   public boolean isHarderThan(float difficulty) {
      return this.localDifficulty > difficulty;
   }

   public float getClampedLocalDifficulty() {
      if (this.localDifficulty < 2.0F) {
         return 0.0F;
      } else {
         return this.localDifficulty > 4.0F ? 1.0F : (this.localDifficulty - 2.0F) / 2.0F;
      }
   }

   private float setLocalDifficulty(Difficulty difficulty, long timeOfDay, long inhabitedTime, float moonSize) {
      if (difficulty == Difficulty.PEACEFUL) {
         return 0.0F;
      } else {
         boolean bl = difficulty == Difficulty.HARD;
         float g = 0.75F;
         float h = MathHelper.clamp(((float)timeOfDay + -72000.0F) / 1440000.0F, 0.0F, 1.0F) * 0.25F;
         g += h;
         float i = 0.0F;
         i += MathHelper.clamp((float)inhabitedTime / 3600000.0F, 0.0F, 1.0F) * (bl ? 1.0F : 0.75F);
         i += MathHelper.clamp(moonSize * 0.25F, 0.0F, h);
         if (difficulty == Difficulty.EASY) {
            i *= 0.5F;
         }

         g += i;
         return (float)difficulty.getId() * g;
      }
   }
}
