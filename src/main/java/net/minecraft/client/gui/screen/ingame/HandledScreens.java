package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class HandledScreens {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map PROVIDERS = Maps.newHashMap();

   public static void open(@Nullable ScreenHandlerType type, MinecraftClient client, int id, Text title) {
      if (type == null) {
         LOGGER.warn("Trying to open invalid screen with name: {}", title.getString());
      } else {
         Provider lv = getProvider(type);
         if (lv == null) {
            LOGGER.warn("Failed to create screen for menu type: {}", Registries.SCREEN_HANDLER.getId(type));
         } else {
            lv.open(title, type, client, id);
         }
      }
   }

   @Nullable
   private static Provider getProvider(ScreenHandlerType type) {
      return (Provider)PROVIDERS.get(type);
   }

   private static void register(ScreenHandlerType type, Provider provider) {
      Provider lv = (Provider)PROVIDERS.put(type, provider);
      if (lv != null) {
         throw new IllegalStateException("Duplicate registration for " + Registries.SCREEN_HANDLER.getId(type));
      }
   }

   public static boolean isMissingScreens() {
      boolean bl = false;
      Iterator var1 = Registries.SCREEN_HANDLER.iterator();

      while(var1.hasNext()) {
         ScreenHandlerType lv = (ScreenHandlerType)var1.next();
         if (!PROVIDERS.containsKey(lv)) {
            LOGGER.debug("Menu {} has no matching screen", Registries.SCREEN_HANDLER.getId(lv));
            bl = true;
         }
      }

      return bl;
   }

   static {
      register(ScreenHandlerType.GENERIC_9X1, GenericContainerScreen::new);
      register(ScreenHandlerType.GENERIC_9X2, GenericContainerScreen::new);
      register(ScreenHandlerType.GENERIC_9X3, GenericContainerScreen::new);
      register(ScreenHandlerType.GENERIC_9X4, GenericContainerScreen::new);
      register(ScreenHandlerType.GENERIC_9X5, GenericContainerScreen::new);
      register(ScreenHandlerType.GENERIC_9X6, GenericContainerScreen::new);
      register(ScreenHandlerType.GENERIC_3X3, Generic3x3ContainerScreen::new);
      register(ScreenHandlerType.ANVIL, AnvilScreen::new);
      register(ScreenHandlerType.BEACON, BeaconScreen::new);
      register(ScreenHandlerType.BLAST_FURNACE, BlastFurnaceScreen::new);
      register(ScreenHandlerType.BREWING_STAND, BrewingStandScreen::new);
      register(ScreenHandlerType.CRAFTING, CraftingScreen::new);
      register(ScreenHandlerType.ENCHANTMENT, EnchantmentScreen::new);
      register(ScreenHandlerType.FURNACE, FurnaceScreen::new);
      register(ScreenHandlerType.GRINDSTONE, GrindstoneScreen::new);
      register(ScreenHandlerType.HOPPER, HopperScreen::new);
      register(ScreenHandlerType.LECTERN, LecternScreen::new);
      register(ScreenHandlerType.LOOM, LoomScreen::new);
      register(ScreenHandlerType.MERCHANT, MerchantScreen::new);
      register(ScreenHandlerType.SHULKER_BOX, ShulkerBoxScreen::new);
      register(ScreenHandlerType.SMITHING, SmithingScreen::new);
      register(ScreenHandlerType.SMOKER, SmokerScreen::new);
      register(ScreenHandlerType.CARTOGRAPHY_TABLE, CartographyTableScreen::new);
      register(ScreenHandlerType.STONECUTTER, StonecutterScreen::new);
   }

   @Environment(EnvType.CLIENT)
   interface Provider {
      default void open(Text name, ScreenHandlerType type, MinecraftClient client, int id) {
         Screen lv = this.create(type.create(id, client.player.getInventory()), client.player.getInventory(), name);
         client.player.currentScreenHandler = ((ScreenHandlerProvider)lv).getScreenHandler();
         client.setScreen(lv);
      }

      Screen create(ScreenHandler handler, PlayerInventory playerInventory, Text title);
   }
}
