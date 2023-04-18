package net.minecraft.client.gl;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class GlBlendState {
   @Nullable
   private static GlBlendState activeBlendState;
   private final int srcRgb;
   private final int srcAlpha;
   private final int dstRgb;
   private final int dstAlpha;
   private final int mode;
   private final boolean separateBlend;
   private final boolean blendDisabled;

   private GlBlendState(boolean separateBlend, boolean blendDisabled, int srcRgb, int dstRgb, int srcAlpha, int dstAlpha, int mode) {
      this.separateBlend = separateBlend;
      this.srcRgb = srcRgb;
      this.dstRgb = dstRgb;
      this.srcAlpha = srcAlpha;
      this.dstAlpha = dstAlpha;
      this.blendDisabled = blendDisabled;
      this.mode = mode;
   }

   public GlBlendState() {
      this(false, true, 1, 0, 1, 0, GlConst.GL_FUNC_ADD);
   }

   public GlBlendState(int srcRgb, int dstRgb, int func) {
      this(false, false, srcRgb, dstRgb, srcRgb, dstRgb, func);
   }

   public GlBlendState(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha, int func) {
      this(true, false, srcRgb, dstRgb, srcAlpha, dstAlpha, func);
   }

   public void enable() {
      if (!this.equals(activeBlendState)) {
         if (activeBlendState == null || this.blendDisabled != activeBlendState.isBlendDisabled()) {
            activeBlendState = this;
            if (this.blendDisabled) {
               RenderSystem.disableBlend();
               return;
            }

            RenderSystem.enableBlend();
         }

         RenderSystem.blendEquation(this.mode);
         if (this.separateBlend) {
            RenderSystem.blendFuncSeparate(this.srcRgb, this.dstRgb, this.srcAlpha, this.dstAlpha);
         } else {
            RenderSystem.blendFunc(this.srcRgb, this.dstRgb);
         }

      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof GlBlendState)) {
         return false;
      } else {
         GlBlendState lv = (GlBlendState)o;
         if (this.mode != lv.mode) {
            return false;
         } else if (this.dstAlpha != lv.dstAlpha) {
            return false;
         } else if (this.dstRgb != lv.dstRgb) {
            return false;
         } else if (this.blendDisabled != lv.blendDisabled) {
            return false;
         } else if (this.separateBlend != lv.separateBlend) {
            return false;
         } else if (this.srcAlpha != lv.srcAlpha) {
            return false;
         } else {
            return this.srcRgb == lv.srcRgb;
         }
      }
   }

   public int hashCode() {
      int i = this.srcRgb;
      i = 31 * i + this.srcAlpha;
      i = 31 * i + this.dstRgb;
      i = 31 * i + this.dstAlpha;
      i = 31 * i + this.mode;
      i = 31 * i + (this.separateBlend ? 1 : 0);
      i = 31 * i + (this.blendDisabled ? 1 : 0);
      return i;
   }

   public boolean isBlendDisabled() {
      return this.blendDisabled;
   }

   public static int getModeFromString(String name) {
      String string2 = name.trim().toLowerCase(Locale.ROOT);
      if ("add".equals(string2)) {
         return GlConst.GL_FUNC_ADD;
      } else if ("subtract".equals(string2)) {
         return GlConst.GL_FUNC_SUBTRACT;
      } else if ("reversesubtract".equals(string2)) {
         return GlConst.GL_FUNC_REVERSE_SUBTRACT;
      } else if ("reverse_subtract".equals(string2)) {
         return GlConst.GL_FUNC_REVERSE_SUBTRACT;
      } else if ("min".equals(string2)) {
         return GlConst.GL_MIN;
      } else {
         return "max".equals(string2) ? GlConst.GL_MAX : GlConst.GL_FUNC_ADD;
      }
   }

   public static int getFactorFromString(String expression) {
      String string2 = expression.trim().toLowerCase(Locale.ROOT);
      string2 = string2.replaceAll("_", "");
      string2 = string2.replaceAll("one", "1");
      string2 = string2.replaceAll("zero", "0");
      string2 = string2.replaceAll("minus", "-");
      if ("0".equals(string2)) {
         return 0;
      } else if ("1".equals(string2)) {
         return 1;
      } else if ("srccolor".equals(string2)) {
         return GlConst.GL_SRC_COLOR;
      } else if ("1-srccolor".equals(string2)) {
         return GlConst.GL_ONE_MINUS_SRC_COLOR;
      } else if ("dstcolor".equals(string2)) {
         return GlConst.GL_DST_COLOR;
      } else if ("1-dstcolor".equals(string2)) {
         return GlConst.GL_ONE_MINUS_DST_COLOR;
      } else if ("srcalpha".equals(string2)) {
         return GlConst.GL_SRC_ALPHA;
      } else if ("1-srcalpha".equals(string2)) {
         return GlConst.GL_ONE_MINUS_SRC_ALPHA;
      } else if ("dstalpha".equals(string2)) {
         return GlConst.GL_DST_ALPHA;
      } else {
         return "1-dstalpha".equals(string2) ? GlConst.GL_ONE_MINUS_DST_ALPHA : -1;
      }
   }
}
