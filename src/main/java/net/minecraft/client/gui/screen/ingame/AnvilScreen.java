package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class AnvilScreen extends ForgingScreen {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/anvil.png");
   private static final Text TOO_EXPENSIVE_TEXT = Text.translatable("container.repair.expensive");
   private TextFieldWidget nameField;
   private final PlayerEntity player;

   public AnvilScreen(AnvilScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title, TEXTURE);
      this.player = inventory.player;
      this.titleX = 60;
   }

   public void handledScreenTick() {
      super.handledScreenTick();
      this.nameField.tick();
   }

   protected void setup() {
      int i = (this.width - this.backgroundWidth) / 2;
      int j = (this.height - this.backgroundHeight) / 2;
      this.nameField = new TextFieldWidget(this.textRenderer, i + 62, j + 24, 103, 12, Text.translatable("container.repair"));
      this.nameField.setFocusUnlocked(false);
      this.nameField.setEditableColor(-1);
      this.nameField.setUneditableColor(-1);
      this.nameField.setDrawsBackground(false);
      this.nameField.setMaxLength(50);
      this.nameField.setChangedListener(this::onRenamed);
      this.nameField.setText("");
      this.addSelectableChild(this.nameField);
      this.setInitialFocus(this.nameField);
      this.nameField.setEditable(false);
   }

   public void resize(MinecraftClient client, int width, int height) {
      String string = this.nameField.getText();
      this.init(client, width, height);
      this.nameField.setText(string);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.client.player.closeHandledScreen();
      }

      return !this.nameField.keyPressed(keyCode, scanCode, modifiers) && !this.nameField.isActive() ? super.keyPressed(keyCode, scanCode, modifiers) : true;
   }

   private void onRenamed(String name) {
      if (!name.isEmpty()) {
         String string2 = name;
         Slot lv = ((AnvilScreenHandler)this.handler).getSlot(0);
         if (lv != null && lv.hasStack() && !lv.getStack().hasCustomName() && name.equals(lv.getStack().getName().getString())) {
            string2 = "";
         }

         ((AnvilScreenHandler)this.handler).setNewItemName(string2);
         this.client.player.networkHandler.sendPacket(new RenameItemC2SPacket(string2));
      }
   }

   protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
      super.drawForeground(matrices, mouseX, mouseY);
      int k = ((AnvilScreenHandler)this.handler).getLevelCost();
      if (k > 0) {
         int l = 8453920;
         Object lv;
         if (k >= 40 && !this.client.player.getAbilities().creativeMode) {
            lv = TOO_EXPENSIVE_TEXT;
            l = 16736352;
         } else if (!((AnvilScreenHandler)this.handler).getSlot(2).hasStack()) {
            lv = null;
         } else {
            lv = Text.translatable("container.repair.cost", k);
            if (!((AnvilScreenHandler)this.handler).getSlot(2).canTakeItems(this.player)) {
               l = 16736352;
            }
         }

         if (lv != null) {
            int m = this.backgroundWidth - 8 - this.textRenderer.getWidth((StringVisitable)lv) - 2;
            int n = true;
            fill(matrices, m - 2, 67, this.backgroundWidth - 8, 79, 1325400064);
            this.textRenderer.drawWithShadow(matrices, (Text)lv, (float)m, 69.0F, l);
         }
      }

   }

   protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
      super.drawBackground(matrices, delta, mouseX, mouseY);
      drawTexture(matrices, this.x + 59, this.y + 20, 0, this.backgroundHeight + (((AnvilScreenHandler)this.handler).getSlot(0).hasStack() ? 0 : 16), 110, 16);
   }

   public void renderForeground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.nameField.render(matrices, mouseX, mouseY, delta);
   }

   protected void drawInvalidRecipeArrow(MatrixStack matrices, int x, int y) {
      if ((((AnvilScreenHandler)this.handler).getSlot(0).hasStack() || ((AnvilScreenHandler)this.handler).getSlot(1).hasStack()) && !((AnvilScreenHandler)this.handler).getSlot(((AnvilScreenHandler)this.handler).getResultSlotIndex()).hasStack()) {
         drawTexture(matrices, x + 99, y + 45, this.backgroundWidth, 0, 28, 21);
      }

   }

   public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
      if (slotId == 0) {
         this.nameField.setText(stack.isEmpty() ? "" : stack.getName().getString());
         this.nameField.setEditable(!stack.isEmpty());
         this.setFocused(this.nameField);
      }

   }
}
