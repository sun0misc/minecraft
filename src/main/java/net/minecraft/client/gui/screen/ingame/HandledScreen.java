package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.util.Iterator;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public abstract class HandledScreen extends Screen implements ScreenHandlerProvider {
   public static final Identifier BACKGROUND_TEXTURE = new Identifier("textures/gui/container/inventory.png");
   private static final float field_32318 = 100.0F;
   private static final int field_32319 = 500;
   public static final int field_32322 = 100;
   private static final int field_32321 = 200;
   protected int backgroundWidth = 176;
   protected int backgroundHeight = 166;
   protected int titleX;
   protected int titleY;
   protected int playerInventoryTitleX;
   protected int playerInventoryTitleY;
   protected final ScreenHandler handler;
   protected final Text playerInventoryTitle;
   @Nullable
   protected Slot focusedSlot;
   @Nullable
   private Slot touchDragSlotStart;
   @Nullable
   private Slot touchDropOriginSlot;
   @Nullable
   private Slot touchHoveredSlot;
   @Nullable
   private Slot lastClickedSlot;
   protected int x;
   protected int y;
   private boolean touchIsRightClickDrag;
   private ItemStack touchDragStack;
   private int touchDropX;
   private int touchDropY;
   private long touchDropTime;
   private ItemStack touchDropReturningStack;
   private long touchDropTimer;
   protected final Set cursorDragSlots;
   protected boolean cursorDragging;
   private int heldButtonType;
   private int heldButtonCode;
   private boolean cancelNextRelease;
   private int draggedStackRemainder;
   private long lastButtonClickTime;
   private int lastClickedButton;
   private boolean doubleClicking;
   private ItemStack quickMovingStack;

   public HandledScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
      super(title);
      this.touchDragStack = ItemStack.EMPTY;
      this.touchDropReturningStack = ItemStack.EMPTY;
      this.cursorDragSlots = Sets.newHashSet();
      this.quickMovingStack = ItemStack.EMPTY;
      this.handler = handler;
      this.playerInventoryTitle = inventory.getDisplayName();
      this.cancelNextRelease = true;
      this.titleX = 8;
      this.titleY = 6;
      this.playerInventoryTitleX = 8;
      this.playerInventoryTitleY = this.backgroundHeight - 94;
   }

   protected void init() {
      this.x = (this.width - this.backgroundWidth) / 2;
      this.y = (this.height - this.backgroundHeight) / 2;
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      int k = this.x;
      int l = this.y;
      this.drawBackground(matrices, delta, mouseX, mouseY);
      RenderSystem.disableDepthTest();
      super.render(matrices, mouseX, mouseY, delta);
      matrices.push();
      matrices.translate((float)k, (float)l, 0.0F);
      this.focusedSlot = null;

      int n;
      int o;
      for(int m = 0; m < this.handler.slots.size(); ++m) {
         Slot lv = (Slot)this.handler.slots.get(m);
         if (lv.isEnabled()) {
            this.drawSlot(matrices, lv);
         }

         if (this.isPointOverSlot(lv, (double)mouseX, (double)mouseY) && lv.isEnabled()) {
            this.focusedSlot = lv;
            n = lv.x;
            o = lv.y;
            drawSlotHighlight(matrices, n, o, 0);
         }
      }

      this.drawForeground(matrices, mouseX, mouseY);
      ItemStack lv2 = this.touchDragStack.isEmpty() ? this.handler.getCursorStack() : this.touchDragStack;
      if (!lv2.isEmpty()) {
         int p = true;
         n = this.touchDragStack.isEmpty() ? 8 : 16;
         String string = null;
         if (!this.touchDragStack.isEmpty() && this.touchIsRightClickDrag) {
            lv2 = lv2.copyWithCount(MathHelper.ceil((float)lv2.getCount() / 2.0F));
         } else if (this.cursorDragging && this.cursorDragSlots.size() > 1) {
            lv2 = lv2.copyWithCount(this.draggedStackRemainder);
            if (lv2.isEmpty()) {
               string = Formatting.YELLOW + "0";
            }
         }

         this.drawItem(matrices, lv2, mouseX - k - 8, mouseY - l - n, string);
      }

      if (!this.touchDropReturningStack.isEmpty()) {
         float g = (float)(Util.getMeasuringTimeMs() - this.touchDropTime) / 100.0F;
         if (g >= 1.0F) {
            g = 1.0F;
            this.touchDropReturningStack = ItemStack.EMPTY;
         }

         n = this.touchDropOriginSlot.x - this.touchDropX;
         o = this.touchDropOriginSlot.y - this.touchDropY;
         int q = this.touchDropX + (int)((float)n * g);
         int r = this.touchDropY + (int)((float)o * g);
         this.drawItem(matrices, this.touchDropReturningStack, q, r, (String)null);
      }

      matrices.pop();
      RenderSystem.enableDepthTest();
   }

   public static void drawSlotHighlight(MatrixStack matrices, int x, int y, int z) {
      RenderSystem.disableDepthTest();
      RenderSystem.colorMask(true, true, true, false);
      fillGradient(matrices, x, y, x + 16, y + 16, -2130706433, -2130706433, z);
      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.enableDepthTest();
   }

   protected void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {
      if (this.handler.getCursorStack().isEmpty() && this.focusedSlot != null && this.focusedSlot.hasStack()) {
         this.renderTooltip(matrices, this.focusedSlot.getStack(), x, y);
      }

   }

   private void drawItem(MatrixStack matrices, ItemStack stack, int x, int y, String amountText) {
      matrices.push();
      matrices.translate(0.0F, 0.0F, 232.0F);
      this.itemRenderer.renderInGuiWithOverrides(matrices, stack, x, y);
      this.itemRenderer.renderGuiItemOverlay(matrices, this.textRenderer, stack, x, y - (this.touchDragStack.isEmpty() ? 0 : 8), amountText);
      matrices.pop();
   }

   protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
      this.textRenderer.draw(matrices, this.title, (float)this.titleX, (float)this.titleY, 4210752);
      this.textRenderer.draw(matrices, this.playerInventoryTitle, (float)this.playerInventoryTitleX, (float)this.playerInventoryTitleY, 4210752);
   }

   protected abstract void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY);

   private void drawSlot(MatrixStack matrices, Slot slot) {
      int i = slot.x;
      int j = slot.y;
      ItemStack lv = slot.getStack();
      boolean bl = false;
      boolean bl2 = slot == this.touchDragSlotStart && !this.touchDragStack.isEmpty() && !this.touchIsRightClickDrag;
      ItemStack lv2 = this.handler.getCursorStack();
      String string = null;
      if (slot == this.touchDragSlotStart && !this.touchDragStack.isEmpty() && this.touchIsRightClickDrag && !lv.isEmpty()) {
         lv = lv.copyWithCount(lv.getCount() / 2);
      } else if (this.cursorDragging && this.cursorDragSlots.contains(slot) && !lv2.isEmpty()) {
         if (this.cursorDragSlots.size() == 1) {
            return;
         }

         if (ScreenHandler.canInsertItemIntoSlot(slot, lv2, true) && this.handler.canInsertIntoSlot(slot)) {
            bl = true;
            int k = Math.min(lv2.getMaxCount(), slot.getMaxItemCount(lv2));
            int l = slot.getStack().isEmpty() ? 0 : slot.getStack().getCount();
            int m = ScreenHandler.calculateStackSize(this.cursorDragSlots, this.heldButtonType, lv2) + l;
            if (m > k) {
               m = k;
               String var10000 = Formatting.YELLOW.toString();
               string = var10000 + k;
            }

            lv = lv2.copyWithCount(m);
         } else {
            this.cursorDragSlots.remove(slot);
            this.calculateOffset();
         }
      }

      matrices.push();
      matrices.translate(0.0F, 0.0F, 100.0F);
      if (lv.isEmpty() && slot.isEnabled()) {
         Pair pair = slot.getBackgroundSprite();
         if (pair != null) {
            Sprite lv3 = (Sprite)this.client.getSpriteAtlas((Identifier)pair.getFirst()).apply((Identifier)pair.getSecond());
            RenderSystem.setShaderTexture(0, lv3.getAtlasId());
            drawSprite(matrices, i, j, 0, 16, 16, lv3);
            bl2 = true;
         }
      }

      if (!bl2) {
         if (bl) {
            fill(matrices, i, j, i + 16, j + 16, -2130706433);
         }

         this.itemRenderer.renderInGuiWithOverrides(matrices, this.client.player, lv, i, j, slot.x + slot.y * this.backgroundWidth);
         this.itemRenderer.renderGuiItemOverlay(matrices, this.textRenderer, lv, i, j, string);
      }

      matrices.pop();
   }

   private void calculateOffset() {
      ItemStack lv = this.handler.getCursorStack();
      if (!lv.isEmpty() && this.cursorDragging) {
         if (this.heldButtonType == 2) {
            this.draggedStackRemainder = lv.getMaxCount();
         } else {
            this.draggedStackRemainder = lv.getCount();

            int i;
            int k;
            for(Iterator var2 = this.cursorDragSlots.iterator(); var2.hasNext(); this.draggedStackRemainder -= k - i) {
               Slot lv2 = (Slot)var2.next();
               ItemStack lv3 = lv2.getStack();
               i = lv3.isEmpty() ? 0 : lv3.getCount();
               int j = Math.min(lv.getMaxCount(), lv2.getMaxItemCount(lv));
               k = Math.min(ScreenHandler.calculateStackSize(this.cursorDragSlots, this.heldButtonType, lv) + i, j);
            }

         }
      }
   }

   @Nullable
   private Slot getSlotAt(double x, double y) {
      for(int i = 0; i < this.handler.slots.size(); ++i) {
         Slot lv = (Slot)this.handler.slots.get(i);
         if (this.isPointOverSlot(lv, x, y) && lv.isEnabled()) {
            return lv;
         }
      }

      return null;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else {
         boolean bl = this.client.options.pickItemKey.matchesMouse(button) && this.client.interactionManager.hasCreativeInventory();
         Slot lv = this.getSlotAt(mouseX, mouseY);
         long l = Util.getMeasuringTimeMs();
         this.doubleClicking = this.lastClickedSlot == lv && l - this.lastButtonClickTime < 250L && this.lastClickedButton == button;
         this.cancelNextRelease = false;
         if (button != 0 && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT && !bl) {
            this.onMouseClick(button);
         } else {
            int j = this.x;
            int k = this.y;
            boolean bl2 = this.isClickOutsideBounds(mouseX, mouseY, j, k, button);
            int m = -1;
            if (lv != null) {
               m = lv.id;
            }

            if (bl2) {
               m = -999;
            }

            if ((Boolean)this.client.options.getTouchscreen().getValue() && bl2 && this.handler.getCursorStack().isEmpty()) {
               this.close();
               return true;
            }

            if (m != -1) {
               if ((Boolean)this.client.options.getTouchscreen().getValue()) {
                  if (lv != null && lv.hasStack()) {
                     this.touchDragSlotStart = lv;
                     this.touchDragStack = ItemStack.EMPTY;
                     this.touchIsRightClickDrag = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
                  } else {
                     this.touchDragSlotStart = null;
                  }
               } else if (!this.cursorDragging) {
                  if (this.handler.getCursorStack().isEmpty()) {
                     if (bl) {
                        this.onMouseClick(lv, m, button, SlotActionType.CLONE);
                     } else {
                        boolean bl3 = m != -999 && (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT));
                        SlotActionType lv2 = SlotActionType.PICKUP;
                        if (bl3) {
                           this.quickMovingStack = lv != null && lv.hasStack() ? lv.getStack().copy() : ItemStack.EMPTY;
                           lv2 = SlotActionType.QUICK_MOVE;
                        } else if (m == -999) {
                           lv2 = SlotActionType.THROW;
                        }

                        this.onMouseClick(lv, m, button, lv2);
                     }

                     this.cancelNextRelease = true;
                  } else {
                     this.cursorDragging = true;
                     this.heldButtonCode = button;
                     this.cursorDragSlots.clear();
                     if (button == 0) {
                        this.heldButtonType = 0;
                     } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        this.heldButtonType = 1;
                     } else if (bl) {
                        this.heldButtonType = 2;
                     }
                  }
               }
            }
         }

         this.lastClickedSlot = lv;
         this.lastButtonClickTime = l;
         this.lastClickedButton = button;
         return true;
      }
   }

   private void onMouseClick(int button) {
      if (this.focusedSlot != null && this.handler.getCursorStack().isEmpty()) {
         if (this.client.options.swapHandsKey.matchesMouse(button)) {
            this.onMouseClick(this.focusedSlot, this.focusedSlot.id, 40, SlotActionType.SWAP);
            return;
         }

         for(int j = 0; j < 9; ++j) {
            if (this.client.options.hotbarKeys[j].matchesMouse(button)) {
               this.onMouseClick(this.focusedSlot, this.focusedSlot.id, j, SlotActionType.SWAP);
            }
         }
      }

   }

   protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
      return mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.backgroundWidth) || mouseY >= (double)(top + this.backgroundHeight);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      Slot lv = this.getSlotAt(mouseX, mouseY);
      ItemStack lv2 = this.handler.getCursorStack();
      if (this.touchDragSlotStart != null && (Boolean)this.client.options.getTouchscreen().getValue()) {
         if (button == 0 || button == 1) {
            if (this.touchDragStack.isEmpty()) {
               if (lv != this.touchDragSlotStart && !this.touchDragSlotStart.getStack().isEmpty()) {
                  this.touchDragStack = this.touchDragSlotStart.getStack().copy();
               }
            } else if (this.touchDragStack.getCount() > 1 && lv != null && ScreenHandler.canInsertItemIntoSlot(lv, this.touchDragStack, false)) {
               long l = Util.getMeasuringTimeMs();
               if (this.touchHoveredSlot == lv) {
                  if (l - this.touchDropTimer > 500L) {
                     this.onMouseClick(this.touchDragSlotStart, this.touchDragSlotStart.id, 0, SlotActionType.PICKUP);
                     this.onMouseClick(lv, lv.id, 1, SlotActionType.PICKUP);
                     this.onMouseClick(this.touchDragSlotStart, this.touchDragSlotStart.id, 0, SlotActionType.PICKUP);
                     this.touchDropTimer = l + 750L;
                     this.touchDragStack.decrement(1);
                  }
               } else {
                  this.touchHoveredSlot = lv;
                  this.touchDropTimer = l;
               }
            }
         }
      } else if (this.cursorDragging && lv != null && !lv2.isEmpty() && (lv2.getCount() > this.cursorDragSlots.size() || this.heldButtonType == 2) && ScreenHandler.canInsertItemIntoSlot(lv, lv2, true) && lv.canInsert(lv2) && this.handler.canInsertIntoSlot(lv)) {
         this.cursorDragSlots.add(lv);
         this.calculateOffset();
      }

      return true;
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      Slot lv = this.getSlotAt(mouseX, mouseY);
      int j = this.x;
      int k = this.y;
      boolean bl = this.isClickOutsideBounds(mouseX, mouseY, j, k, button);
      int l = GLFW.GLFW_KEY_UNKNOWN;
      if (lv != null) {
         l = lv.id;
      }

      if (bl) {
         l = -999;
      }

      Slot lv2;
      Iterator var13;
      if (this.doubleClicking && lv != null && button == 0 && this.handler.canInsertIntoSlot(ItemStack.EMPTY, lv)) {
         if (hasShiftDown()) {
            if (!this.quickMovingStack.isEmpty()) {
               var13 = this.handler.slots.iterator();

               while(var13.hasNext()) {
                  lv2 = (Slot)var13.next();
                  if (lv2 != null && lv2.canTakeItems(this.client.player) && lv2.hasStack() && lv2.inventory == lv.inventory && ScreenHandler.canInsertItemIntoSlot(lv2, this.quickMovingStack, true)) {
                     this.onMouseClick(lv2, lv2.id, button, SlotActionType.QUICK_MOVE);
                  }
               }
            }
         } else {
            this.onMouseClick(lv, l, button, SlotActionType.PICKUP_ALL);
         }

         this.doubleClicking = false;
         this.lastButtonClickTime = 0L;
      } else {
         if (this.cursorDragging && this.heldButtonCode != button) {
            this.cursorDragging = false;
            this.cursorDragSlots.clear();
            this.cancelNextRelease = true;
            return true;
         }

         if (this.cancelNextRelease) {
            this.cancelNextRelease = false;
            return true;
         }

         boolean bl2;
         if (this.touchDragSlotStart != null && (Boolean)this.client.options.getTouchscreen().getValue()) {
            if (button == 0 || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
               if (this.touchDragStack.isEmpty() && lv != this.touchDragSlotStart) {
                  this.touchDragStack = this.touchDragSlotStart.getStack();
               }

               bl2 = ScreenHandler.canInsertItemIntoSlot(lv, this.touchDragStack, false);
               if (l != GLFW.GLFW_KEY_UNKNOWN && !this.touchDragStack.isEmpty() && bl2) {
                  this.onMouseClick(this.touchDragSlotStart, this.touchDragSlotStart.id, button, SlotActionType.PICKUP);
                  this.onMouseClick(lv, l, 0, SlotActionType.PICKUP);
                  if (this.handler.getCursorStack().isEmpty()) {
                     this.touchDropReturningStack = ItemStack.EMPTY;
                  } else {
                     this.onMouseClick(this.touchDragSlotStart, this.touchDragSlotStart.id, button, SlotActionType.PICKUP);
                     this.touchDropX = MathHelper.floor(mouseX - (double)j);
                     this.touchDropY = MathHelper.floor(mouseY - (double)k);
                     this.touchDropOriginSlot = this.touchDragSlotStart;
                     this.touchDropReturningStack = this.touchDragStack;
                     this.touchDropTime = Util.getMeasuringTimeMs();
                  }
               } else if (!this.touchDragStack.isEmpty()) {
                  this.touchDropX = MathHelper.floor(mouseX - (double)j);
                  this.touchDropY = MathHelper.floor(mouseY - (double)k);
                  this.touchDropOriginSlot = this.touchDragSlotStart;
                  this.touchDropReturningStack = this.touchDragStack;
                  this.touchDropTime = Util.getMeasuringTimeMs();
               }

               this.endTouchDrag();
            }
         } else if (this.cursorDragging && !this.cursorDragSlots.isEmpty()) {
            this.onMouseClick((Slot)null, -999, ScreenHandler.packQuickCraftData(0, this.heldButtonType), SlotActionType.QUICK_CRAFT);
            var13 = this.cursorDragSlots.iterator();

            while(var13.hasNext()) {
               lv2 = (Slot)var13.next();
               this.onMouseClick(lv2, lv2.id, ScreenHandler.packQuickCraftData(1, this.heldButtonType), SlotActionType.QUICK_CRAFT);
            }

            this.onMouseClick((Slot)null, -999, ScreenHandler.packQuickCraftData(2, this.heldButtonType), SlotActionType.QUICK_CRAFT);
         } else if (!this.handler.getCursorStack().isEmpty()) {
            if (this.client.options.pickItemKey.matchesMouse(button)) {
               this.onMouseClick(lv, l, button, SlotActionType.CLONE);
            } else {
               bl2 = l != -999 && (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT));
               if (bl2) {
                  this.quickMovingStack = lv != null && lv.hasStack() ? lv.getStack().copy() : ItemStack.EMPTY;
               }

               this.onMouseClick(lv, l, button, bl2 ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP);
            }
         }
      }

      if (this.handler.getCursorStack().isEmpty()) {
         this.lastButtonClickTime = 0L;
      }

      this.cursorDragging = false;
      return true;
   }

   public void endTouchDrag() {
      this.touchDragStack = ItemStack.EMPTY;
      this.touchDragSlotStart = null;
   }

   private boolean isPointOverSlot(Slot slot, double pointX, double pointY) {
      return this.isPointWithinBounds(slot.x, slot.y, 16, 16, pointX, pointY);
   }

   protected boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
      int m = this.x;
      int n = this.y;
      pointX -= (double)m;
      pointY -= (double)n;
      return pointX >= (double)(x - 1) && pointX < (double)(x + width + 1) && pointY >= (double)(y - 1) && pointY < (double)(y + height + 1);
   }

   protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
      if (slot != null) {
         slotId = slot.id;
      }

      this.client.interactionManager.clickSlot(this.handler.syncId, slotId, button, actionType, this.client.player);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (super.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
         this.close();
         return true;
      } else {
         this.handleHotbarKeyPressed(keyCode, scanCode);
         if (this.focusedSlot != null && this.focusedSlot.hasStack()) {
            if (this.client.options.pickItemKey.matchesKey(keyCode, scanCode)) {
               this.onMouseClick(this.focusedSlot, this.focusedSlot.id, 0, SlotActionType.CLONE);
            } else if (this.client.options.dropKey.matchesKey(keyCode, scanCode)) {
               this.onMouseClick(this.focusedSlot, this.focusedSlot.id, hasControlDown() ? 1 : 0, SlotActionType.THROW);
            }
         }

         return true;
      }
   }

   protected boolean handleHotbarKeyPressed(int keyCode, int scanCode) {
      if (this.handler.getCursorStack().isEmpty() && this.focusedSlot != null) {
         if (this.client.options.swapHandsKey.matchesKey(keyCode, scanCode)) {
            this.onMouseClick(this.focusedSlot, this.focusedSlot.id, 40, SlotActionType.SWAP);
            return true;
         }

         for(int k = 0; k < 9; ++k) {
            if (this.client.options.hotbarKeys[k].matchesKey(keyCode, scanCode)) {
               this.onMouseClick(this.focusedSlot, this.focusedSlot.id, k, SlotActionType.SWAP);
               return true;
            }
         }
      }

      return false;
   }

   public void removed() {
      if (this.client.player != null) {
         this.handler.onClosed(this.client.player);
      }
   }

   public boolean shouldPause() {
      return false;
   }

   public final void tick() {
      super.tick();
      if (this.client.player.isAlive() && !this.client.player.isRemoved()) {
         this.handledScreenTick();
      } else {
         this.client.player.closeHandledScreen();
      }

   }

   protected void handledScreenTick() {
   }

   public ScreenHandler getScreenHandler() {
      return this.handler;
   }

   public void close() {
      this.client.player.closeHandledScreen();
      super.close();
   }
}
