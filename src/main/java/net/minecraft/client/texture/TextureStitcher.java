package net.minecraft.client.texture;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TextureStitcher {
   private static final Comparator COMPARATOR = Comparator.comparing((holder) -> {
      return -holder.height;
   }).thenComparing((holder) -> {
      return -holder.width;
   }).thenComparing((holder) -> {
      return holder.sprite.getId();
   });
   private final int mipLevel;
   private final List holders = new ArrayList();
   private final List slots = new ArrayList();
   private int width;
   private int height;
   private final int maxWidth;
   private final int maxHeight;

   public TextureStitcher(int maxWidth, int maxHeight, int mipLevel) {
      this.mipLevel = mipLevel;
      this.maxWidth = maxWidth;
      this.maxHeight = maxHeight;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public void add(Stitchable info) {
      Holder lv = new Holder(info, this.mipLevel);
      this.holders.add(lv);
   }

   public void stitch() {
      List list = new ArrayList(this.holders);
      list.sort(COMPARATOR);
      Iterator var2 = list.iterator();

      Holder lv;
      do {
         if (!var2.hasNext()) {
            return;
         }

         lv = (Holder)var2.next();
      } while(this.fit(lv));

      throw new TextureStitcherCannotFitException(lv.sprite, (Collection)list.stream().map((holder) -> {
         return holder.sprite;
      }).collect(ImmutableList.toImmutableList()));
   }

   public void getStitchedSprites(SpriteConsumer consumer) {
      Iterator var2 = this.slots.iterator();

      while(var2.hasNext()) {
         Slot lv = (Slot)var2.next();
         lv.addAllFilledSlots(consumer);
      }

   }

   static int applyMipLevel(int size, int mipLevel) {
      return (size >> mipLevel) + ((size & (1 << mipLevel) - 1) == 0 ? 0 : 1) << mipLevel;
   }

   private boolean fit(Holder holder) {
      Iterator var2 = this.slots.iterator();

      Slot lv;
      do {
         if (!var2.hasNext()) {
            return this.growAndFit(holder);
         }

         lv = (Slot)var2.next();
      } while(!lv.fit(holder));

      return true;
   }

   private boolean growAndFit(Holder arg) {
      int i = MathHelper.smallestEncompassingPowerOfTwo(this.width);
      int j = MathHelper.smallestEncompassingPowerOfTwo(this.height);
      int k = MathHelper.smallestEncompassingPowerOfTwo(this.width + arg.width);
      int l = MathHelper.smallestEncompassingPowerOfTwo(this.height + arg.height);
      boolean bl = k <= this.maxWidth;
      boolean bl2 = l <= this.maxHeight;
      if (!bl && !bl2) {
         return false;
      } else {
         boolean bl3 = bl && i != k;
         boolean bl4 = bl2 && j != l;
         boolean bl5;
         if (bl3 ^ bl4) {
            bl5 = bl3;
         } else {
            bl5 = bl && i <= j;
         }

         Slot lv;
         if (bl5) {
            if (this.height == 0) {
               this.height = l;
            }

            lv = new Slot(this.width, 0, k - this.width, this.height);
            this.width = k;
         } else {
            lv = new Slot(0, this.height, this.width, l - this.height);
            this.height = l;
         }

         lv.fit(arg);
         this.slots.add(lv);
         return true;
      }
   }

   @Environment(EnvType.CLIENT)
   private static record Holder(Stitchable sprite, int width, int height) {
      final Stitchable sprite;
      final int width;
      final int height;

      public Holder(Stitchable sprite, int mipLevel) {
         this(sprite, TextureStitcher.applyMipLevel(sprite.getWidth(), mipLevel), TextureStitcher.applyMipLevel(sprite.getHeight(), mipLevel));
      }

      private Holder(Stitchable arg, int i, int j) {
         this.sprite = arg;
         this.width = i;
         this.height = j;
      }

      public Stitchable sprite() {
         return this.sprite;
      }

      public int width() {
         return this.width;
      }

      public int height() {
         return this.height;
      }
   }

   @Environment(EnvType.CLIENT)
   public interface Stitchable {
      int getWidth();

      int getHeight();

      Identifier getId();
   }

   @Environment(EnvType.CLIENT)
   public static class Slot {
      private final int x;
      private final int y;
      private final int width;
      private final int height;
      @Nullable
      private List subSlots;
      @Nullable
      private Holder texture;

      public Slot(int x, int y, int width, int height) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
      }

      public int getX() {
         return this.x;
      }

      public int getY() {
         return this.y;
      }

      public boolean fit(Holder holder) {
         if (this.texture != null) {
            return false;
         } else {
            int i = holder.width;
            int j = holder.height;
            if (i <= this.width && j <= this.height) {
               if (i == this.width && j == this.height) {
                  this.texture = holder;
                  return true;
               } else {
                  if (this.subSlots == null) {
                     this.subSlots = new ArrayList(1);
                     this.subSlots.add(new Slot(this.x, this.y, i, j));
                     int k = this.width - i;
                     int l = this.height - j;
                     if (l > 0 && k > 0) {
                        int m = Math.max(this.height, k);
                        int n = Math.max(this.width, l);
                        if (m >= n) {
                           this.subSlots.add(new Slot(this.x, this.y + j, i, l));
                           this.subSlots.add(new Slot(this.x + i, this.y, k, this.height));
                        } else {
                           this.subSlots.add(new Slot(this.x + i, this.y, k, j));
                           this.subSlots.add(new Slot(this.x, this.y + j, this.width, l));
                        }
                     } else if (k == 0) {
                        this.subSlots.add(new Slot(this.x, this.y + j, i, l));
                     } else if (l == 0) {
                        this.subSlots.add(new Slot(this.x + i, this.y, k, j));
                     }
                  }

                  Iterator var8 = this.subSlots.iterator();

                  Slot lv;
                  do {
                     if (!var8.hasNext()) {
                        return false;
                     }

                     lv = (Slot)var8.next();
                  } while(!lv.fit(holder));

                  return true;
               }
            } else {
               return false;
            }
         }
      }

      public void addAllFilledSlots(SpriteConsumer consumer) {
         if (this.texture != null) {
            consumer.load(this.texture.sprite, this.getX(), this.getY());
         } else if (this.subSlots != null) {
            Iterator var2 = this.subSlots.iterator();

            while(var2.hasNext()) {
               Slot lv = (Slot)var2.next();
               lv.addAllFilledSlots(consumer);
            }
         }

      }

      public String toString() {
         return "Slot{originX=" + this.x + ", originY=" + this.y + ", width=" + this.width + ", height=" + this.height + ", texture=" + this.texture + ", subSlots=" + this.subSlots + "}";
      }
   }

   @Environment(EnvType.CLIENT)
   public interface SpriteConsumer {
      void load(Stitchable info, int x, int y);
   }
}
