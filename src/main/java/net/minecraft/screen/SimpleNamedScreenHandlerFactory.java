package net.minecraft.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public final class SimpleNamedScreenHandlerFactory implements NamedScreenHandlerFactory {
   private final Text name;
   private final ScreenHandlerFactory baseFactory;

   public SimpleNamedScreenHandlerFactory(ScreenHandlerFactory baseFactory, Text name) {
      this.baseFactory = baseFactory;
      this.name = name;
   }

   public Text getDisplayName() {
      return this.name;
   }

   public ScreenHandler createMenu(int i, PlayerInventory arg, PlayerEntity arg2) {
      return this.baseFactory.createMenu(i, arg, arg2);
   }
}
