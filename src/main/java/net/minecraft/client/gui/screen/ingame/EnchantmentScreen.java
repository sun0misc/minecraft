package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class EnchantmentScreen extends HandledScreen {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/enchanting_table.png");
   private static final Identifier BOOK_TEXTURE = new Identifier("textures/entity/enchanting_table_book.png");
   private final Random random = Random.create();
   private BookModel BOOK_MODEL;
   public int ticks;
   public float nextPageAngle;
   public float pageAngle;
   public float approximatePageAngle;
   public float pageRotationSpeed;
   public float nextPageTurningSpeed;
   public float pageTurningSpeed;
   private ItemStack stack;

   public EnchantmentScreen(EnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      this.stack = ItemStack.EMPTY;
   }

   protected void init() {
      super.init();
      this.BOOK_MODEL = new BookModel(this.client.getEntityModelLoader().getModelPart(EntityModelLayers.BOOK));
   }

   public void handledScreenTick() {
      super.handledScreenTick();
      this.doTick();
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int j = (this.width - this.backgroundWidth) / 2;
      int k = (this.height - this.backgroundHeight) / 2;

      for(int l = 0; l < 3; ++l) {
         double f = mouseX - (double)(j + 60);
         double g = mouseY - (double)(k + 14 + 19 * l);
         if (f >= 0.0 && g >= 0.0 && f < 108.0 && g < 19.0 && ((EnchantmentScreenHandler)this.handler).onButtonClick(this.client.player, l)) {
            this.client.interactionManager.clickButton(((EnchantmentScreenHandler)this.handler).syncId, l);
            return true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
      DiffuseLighting.disableGuiDepthLighting();
      RenderSystem.setShaderTexture(0, TEXTURE);
      int k = (this.width - this.backgroundWidth) / 2;
      int l = (this.height - this.backgroundHeight) / 2;
      drawTexture(matrices, k, l, 0, 0, this.backgroundWidth, this.backgroundHeight);
      int m = (int)this.client.getWindow().getScaleFactor();
      RenderSystem.viewport((this.width - 320) / 2 * m, (this.height - 240) / 2 * m, 320 * m, 240 * m);
      Matrix4f matrix4f = (new Matrix4f()).translation(-0.34F, 0.23F, 0.0F).perspective(1.5707964F, 1.3333334F, 9.0F, 80.0F);
      RenderSystem.backupProjectionMatrix();
      RenderSystem.setProjectionMatrix(matrix4f, VertexSorter.BY_DISTANCE);
      matrices.push();
      matrices.loadIdentity();
      matrices.translate(0.0F, 3.3F, 1984.0F);
      float g = 5.0F;
      matrices.scale(5.0F, 5.0F, 5.0F);
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(20.0F));
      float h = MathHelper.lerp(delta, this.pageTurningSpeed, this.nextPageTurningSpeed);
      matrices.translate((1.0F - h) * 0.2F, (1.0F - h) * 0.1F, (1.0F - h) * 0.25F);
      float n = -(1.0F - h) * 90.0F - 90.0F;
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(n));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
      float o = MathHelper.lerp(delta, this.pageAngle, this.nextPageAngle) + 0.25F;
      float p = MathHelper.lerp(delta, this.pageAngle, this.nextPageAngle) + 0.75F;
      o = (o - (float)MathHelper.floor(o)) * 1.6F - 0.3F;
      p = (p - (float)MathHelper.floor(p)) * 1.6F - 0.3F;
      if (o < 0.0F) {
         o = 0.0F;
      }

      if (p < 0.0F) {
         p = 0.0F;
      }

      if (o > 1.0F) {
         o = 1.0F;
      }

      if (p > 1.0F) {
         p = 1.0F;
      }

      this.BOOK_MODEL.setPageAngles(0.0F, o, p, h);
      VertexConsumerProvider.Immediate lv = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
      VertexConsumer lv2 = lv.getBuffer(this.BOOK_MODEL.getLayer(BOOK_TEXTURE));
      this.BOOK_MODEL.render(matrices, lv2, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
      lv.draw();
      matrices.pop();
      RenderSystem.viewport(0, 0, this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight());
      RenderSystem.restoreProjectionMatrix();
      DiffuseLighting.enableGuiDepthLighting();
      EnchantingPhrases.getInstance().setSeed((long)((EnchantmentScreenHandler)this.handler).getSeed());
      int q = ((EnchantmentScreenHandler)this.handler).getLapisCount();

      for(int r = 0; r < 3; ++r) {
         int s = k + 60;
         int t = s + 20;
         RenderSystem.setShaderTexture(0, TEXTURE);
         int u = ((EnchantmentScreenHandler)this.handler).enchantmentPower[r];
         if (u == 0) {
            drawTexture(matrices, s, l + 14 + 19 * r, 0, 185, 108, 19);
         } else {
            String string = "" + u;
            int v = 86 - this.textRenderer.getWidth(string);
            StringVisitable lv3 = EnchantingPhrases.getInstance().generatePhrase(this.textRenderer, v);
            int w = 6839882;
            if ((q < r + 1 || this.client.player.experienceLevel < u) && !this.client.player.getAbilities().creativeMode) {
               drawTexture(matrices, s, l + 14 + 19 * r, 0, 185, 108, 19);
               drawTexture(matrices, s + 1, l + 15 + 19 * r, 16 * r, 239, 16, 16);
               this.textRenderer.drawTrimmed(matrices, lv3, t, l + 16 + 19 * r, v, (w & 16711422) >> 1);
               w = 4226832;
            } else {
               int x = mouseX - (k + 60);
               int y = mouseY - (l + 14 + 19 * r);
               if (x >= 0 && y >= 0 && x < 108 && y < 19) {
                  drawTexture(matrices, s, l + 14 + 19 * r, 0, 204, 108, 19);
                  w = 16777088;
               } else {
                  drawTexture(matrices, s, l + 14 + 19 * r, 0, 166, 108, 19);
               }

               drawTexture(matrices, s + 1, l + 15 + 19 * r, 16 * r, 223, 16, 16);
               this.textRenderer.drawTrimmed(matrices, lv3, t, l + 16 + 19 * r, v, w);
               w = 8453920;
            }

            this.textRenderer.drawWithShadow(matrices, string, (float)(t + 86 - this.textRenderer.getWidth(string)), (float)(l + 16 + 19 * r + 7), w);
         }
      }

   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      delta = this.client.getTickDelta();
      this.renderBackground(matrices);
      super.render(matrices, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(matrices, mouseX, mouseY);
      boolean bl = this.client.player.getAbilities().creativeMode;
      int k = ((EnchantmentScreenHandler)this.handler).getLapisCount();

      for(int l = 0; l < 3; ++l) {
         int m = ((EnchantmentScreenHandler)this.handler).enchantmentPower[l];
         Enchantment lv = Enchantment.byRawId(((EnchantmentScreenHandler)this.handler).enchantmentId[l]);
         int n = ((EnchantmentScreenHandler)this.handler).enchantmentLevel[l];
         int o = l + 1;
         if (this.isPointWithinBounds(60, 14 + 19 * l, 108, 17, (double)mouseX, (double)mouseY) && m > 0 && n >= 0 && lv != null) {
            List list = Lists.newArrayList();
            list.add(Text.translatable("container.enchant.clue", lv.getName(n)).formatted(Formatting.WHITE));
            if (!bl) {
               list.add(ScreenTexts.EMPTY);
               if (this.client.player.experienceLevel < m) {
                  list.add(Text.translatable("container.enchant.level.requirement", ((EnchantmentScreenHandler)this.handler).enchantmentPower[l]).formatted(Formatting.RED));
               } else {
                  MutableText lv2;
                  if (o == 1) {
                     lv2 = Text.translatable("container.enchant.lapis.one");
                  } else {
                     lv2 = Text.translatable("container.enchant.lapis.many", o);
                  }

                  list.add(lv2.formatted(k >= o ? Formatting.GRAY : Formatting.RED));
                  MutableText lv3;
                  if (o == 1) {
                     lv3 = Text.translatable("container.enchant.level.one");
                  } else {
                     lv3 = Text.translatable("container.enchant.level.many", o);
                  }

                  list.add(lv3.formatted(Formatting.GRAY));
               }
            }

            this.renderTooltip(matrices, list, mouseX, mouseY);
            break;
         }
      }

   }

   public void doTick() {
      ItemStack lv = ((EnchantmentScreenHandler)this.handler).getSlot(0).getStack();
      if (!ItemStack.areEqual(lv, this.stack)) {
         this.stack = lv;

         do {
            this.approximatePageAngle += (float)(this.random.nextInt(4) - this.random.nextInt(4));
         } while(this.nextPageAngle <= this.approximatePageAngle + 1.0F && this.nextPageAngle >= this.approximatePageAngle - 1.0F);
      }

      ++this.ticks;
      this.pageAngle = this.nextPageAngle;
      this.pageTurningSpeed = this.nextPageTurningSpeed;
      boolean bl = false;

      for(int i = 0; i < 3; ++i) {
         if (((EnchantmentScreenHandler)this.handler).enchantmentPower[i] != 0) {
            bl = true;
         }
      }

      if (bl) {
         this.nextPageTurningSpeed += 0.2F;
      } else {
         this.nextPageTurningSpeed -= 0.2F;
      }

      this.nextPageTurningSpeed = MathHelper.clamp(this.nextPageTurningSpeed, 0.0F, 1.0F);
      float f = (this.approximatePageAngle - this.nextPageAngle) * 0.4F;
      float g = 0.2F;
      f = MathHelper.clamp(f, -0.2F, 0.2F);
      this.pageRotationSpeed += (f - this.pageRotationSpeed) * 0.9F;
      this.nextPageAngle += this.pageRotationSpeed;
   }
}
