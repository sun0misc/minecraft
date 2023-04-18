package net.minecraft.client.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class LoomScreen extends HandledScreen {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/loom.png");
   private static final int PATTERN_LIST_COLUMNS = 4;
   private static final int PATTERN_LIST_ROWS = 4;
   private static final int SCROLLBAR_WIDTH = 12;
   private static final int SCROLLBAR_HEIGHT = 15;
   private static final int PATTERN_ENTRY_SIZE = 14;
   private static final int SCROLLBAR_AREA_HEIGHT = 56;
   private static final int PATTERN_LIST_OFFSET_X = 60;
   private static final int PATTERN_LIST_OFFSET_Y = 13;
   private ModelPart bannerField;
   @Nullable
   private List bannerPatterns;
   private ItemStack banner;
   private ItemStack dye;
   private ItemStack pattern;
   private boolean canApplyDyePattern;
   private boolean hasTooManyPatterns;
   private float scrollPosition;
   private boolean scrollbarClicked;
   private int visibleTopRow;

   public LoomScreen(LoomScreenHandler screenHandler, PlayerInventory inventory, Text title) {
      super(screenHandler, inventory, title);
      this.banner = ItemStack.EMPTY;
      this.dye = ItemStack.EMPTY;
      this.pattern = ItemStack.EMPTY;
      screenHandler.setInventoryChangeListener(this::onInventoryChanged);
      this.titleY -= 2;
   }

   protected void init() {
      super.init();
      this.bannerField = this.client.getEntityModelLoader().getModelPart(EntityModelLayers.BANNER).getChild("flag");
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      super.render(matrices, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(matrices, mouseX, mouseY);
   }

   private int getRows() {
      return MathHelper.ceilDiv(((LoomScreenHandler)this.handler).getBannerPatterns().size(), 4);
   }

   protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
      this.renderBackground(matrices);
      RenderSystem.setShaderTexture(0, TEXTURE);
      int k = this.x;
      int l = this.y;
      drawTexture(matrices, k, l, 0, 0, this.backgroundWidth, this.backgroundHeight);
      Slot lv = ((LoomScreenHandler)this.handler).getBannerSlot();
      Slot lv2 = ((LoomScreenHandler)this.handler).getDyeSlot();
      Slot lv3 = ((LoomScreenHandler)this.handler).getPatternSlot();
      Slot lv4 = ((LoomScreenHandler)this.handler).getOutputSlot();
      if (!lv.hasStack()) {
         drawTexture(matrices, k + lv.x, l + lv.y, this.backgroundWidth, 0, 16, 16);
      }

      if (!lv2.hasStack()) {
         drawTexture(matrices, k + lv2.x, l + lv2.y, this.backgroundWidth + 16, 0, 16, 16);
      }

      if (!lv3.hasStack()) {
         drawTexture(matrices, k + lv3.x, l + lv3.y, this.backgroundWidth + 32, 0, 16, 16);
      }

      int m = (int)(41.0F * this.scrollPosition);
      drawTexture(matrices, k + 119, l + 13 + m, 232 + (this.canApplyDyePattern ? 0 : 12), 0, 12, 15);
      DiffuseLighting.disableGuiDepthLighting();
      if (this.bannerPatterns != null && !this.hasTooManyPatterns) {
         VertexConsumerProvider.Immediate lv5 = this.client.getBufferBuilders().getEntityVertexConsumers();
         matrices.push();
         matrices.translate((float)(k + 139), (float)(l + 52), 0.0F);
         matrices.scale(24.0F, -24.0F, 1.0F);
         matrices.translate(0.5F, 0.5F, 0.5F);
         float g = 0.6666667F;
         matrices.scale(0.6666667F, -0.6666667F, -0.6666667F);
         this.bannerField.pitch = 0.0F;
         this.bannerField.pivotY = -32.0F;
         BannerBlockEntityRenderer.renderCanvas(matrices, lv5, 15728880, OverlayTexture.DEFAULT_UV, this.bannerField, ModelLoader.BANNER_BASE, true, this.bannerPatterns);
         matrices.pop();
         lv5.draw();
      } else if (this.hasTooManyPatterns) {
         drawTexture(matrices, k + lv4.x - 2, l + lv4.y - 2, this.backgroundWidth, 17, 17, 16);
      }

      if (this.canApplyDyePattern) {
         int n = k + 60;
         int o = l + 13;
         List list = ((LoomScreenHandler)this.handler).getBannerPatterns();

         label63:
         for(int p = 0; p < 4; ++p) {
            for(int q = 0; q < 4; ++q) {
               int r = p + this.visibleTopRow;
               int s = r * 4 + q;
               if (s >= list.size()) {
                  break label63;
               }

               RenderSystem.setShaderTexture(0, TEXTURE);
               int t = n + q * 14;
               int u = o + p * 14;
               boolean bl = mouseX >= t && mouseY >= u && mouseX < t + 14 && mouseY < u + 14;
               int v;
               if (s == ((LoomScreenHandler)this.handler).getSelectedPattern()) {
                  v = this.backgroundHeight + 14;
               } else if (bl) {
                  v = this.backgroundHeight + 28;
               } else {
                  v = this.backgroundHeight;
               }

               drawTexture(matrices, t, u, 0, v, 14, 14);
               this.drawBanner((RegistryEntry)list.get(s), t, u);
            }
         }
      }

      DiffuseLighting.enableGuiDepthLighting();
   }

   private void drawBanner(RegistryEntry bannerPattern, int x, int y) {
      NbtCompound lv = new NbtCompound();
      NbtList lv2 = (new BannerPattern.Patterns()).add(BannerPatterns.BASE, DyeColor.GRAY).add(bannerPattern, DyeColor.WHITE).toNbt();
      lv.put("Patterns", lv2);
      ItemStack lv3 = new ItemStack(Items.GRAY_BANNER);
      BlockItem.setBlockEntityNbt(lv3, BlockEntityType.BANNER, lv);
      MatrixStack lv4 = new MatrixStack();
      lv4.push();
      lv4.translate((float)x + 0.5F, (float)(y + 16), 0.0F);
      lv4.scale(6.0F, -6.0F, 1.0F);
      lv4.translate(0.5F, 0.5F, 0.0F);
      lv4.translate(0.5F, 0.5F, 0.5F);
      float f = 0.6666667F;
      lv4.scale(0.6666667F, -0.6666667F, -0.6666667F);
      VertexConsumerProvider.Immediate lv5 = this.client.getBufferBuilders().getEntityVertexConsumers();
      this.bannerField.pitch = 0.0F;
      this.bannerField.pivotY = -32.0F;
      List list = BannerBlockEntity.getPatternsFromNbt(DyeColor.GRAY, BannerBlockEntity.getPatternListNbt(lv3));
      BannerBlockEntityRenderer.renderCanvas(lv4, lv5, 15728880, OverlayTexture.DEFAULT_UV, this.bannerField, ModelLoader.BANNER_BASE, true, list);
      lv4.pop();
      lv5.draw();
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.scrollbarClicked = false;
      if (this.canApplyDyePattern) {
         int j = this.x + 60;
         int k = this.y + 13;

         for(int l = 0; l < 4; ++l) {
            for(int m = 0; m < 4; ++m) {
               double f = mouseX - (double)(j + m * 14);
               double g = mouseY - (double)(k + l * 14);
               int n = l + this.visibleTopRow;
               int o = n * 4 + m;
               if (f >= 0.0 && g >= 0.0 && f < 14.0 && g < 14.0 && ((LoomScreenHandler)this.handler).onButtonClick(this.client.player, o)) {
                  MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_LOOM_SELECT_PATTERN, 1.0F));
                  this.client.interactionManager.clickButton(((LoomScreenHandler)this.handler).syncId, o);
                  return true;
               }
            }
         }

         j = this.x + 119;
         k = this.y + 9;
         if (mouseX >= (double)j && mouseX < (double)(j + 12) && mouseY >= (double)k && mouseY < (double)(k + 56)) {
            this.scrollbarClicked = true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      int j = this.getRows() - 4;
      if (this.scrollbarClicked && this.canApplyDyePattern && j > 0) {
         int k = this.y + 13;
         int l = k + 56;
         this.scrollPosition = ((float)mouseY - (float)k - 7.5F) / ((float)(l - k) - 15.0F);
         this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
         this.visibleTopRow = Math.max((int)((double)(this.scrollPosition * (float)j) + 0.5), 0);
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      int i = this.getRows() - 4;
      if (this.canApplyDyePattern && i > 0) {
         float g = (float)amount / (float)i;
         this.scrollPosition = MathHelper.clamp(this.scrollPosition - g, 0.0F, 1.0F);
         this.visibleTopRow = Math.max((int)(this.scrollPosition * (float)i + 0.5F), 0);
      }

      return true;
   }

   protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
      return mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.backgroundWidth) || mouseY >= (double)(top + this.backgroundHeight);
   }

   private void onInventoryChanged() {
      ItemStack lv = ((LoomScreenHandler)this.handler).getOutputSlot().getStack();
      if (lv.isEmpty()) {
         this.bannerPatterns = null;
      } else {
         this.bannerPatterns = BannerBlockEntity.getPatternsFromNbt(((BannerItem)lv.getItem()).getColor(), BannerBlockEntity.getPatternListNbt(lv));
      }

      ItemStack lv2 = ((LoomScreenHandler)this.handler).getBannerSlot().getStack();
      ItemStack lv3 = ((LoomScreenHandler)this.handler).getDyeSlot().getStack();
      ItemStack lv4 = ((LoomScreenHandler)this.handler).getPatternSlot().getStack();
      NbtCompound lv5 = BlockItem.getBlockEntityNbt(lv2);
      this.hasTooManyPatterns = lv5 != null && lv5.contains("Patterns", NbtElement.LIST_TYPE) && !lv2.isEmpty() && lv5.getList("Patterns", NbtElement.COMPOUND_TYPE).size() >= 6;
      if (this.hasTooManyPatterns) {
         this.bannerPatterns = null;
      }

      if (!ItemStack.areEqual(lv2, this.banner) || !ItemStack.areEqual(lv3, this.dye) || !ItemStack.areEqual(lv4, this.pattern)) {
         this.canApplyDyePattern = !lv2.isEmpty() && !lv3.isEmpty() && !this.hasTooManyPatterns && !((LoomScreenHandler)this.handler).getBannerPatterns().isEmpty();
      }

      if (this.visibleTopRow >= this.getRows()) {
         this.visibleTopRow = 0;
         this.scrollPosition = 0.0F;
      }

      this.banner = lv2.copy();
      this.dye = lv3.copy();
      this.pattern = lv4.copy();
   }
}
