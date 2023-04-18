package net.minecraft.client.gui.screen.option;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.minecraft.util.Nullables;
import net.minecraft.world.Difficulty;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class OnlineOptionsScreen extends SimpleOptionsScreen {
   @Nullable
   private final SimpleOption difficulty;

   public static OnlineOptionsScreen create(MinecraftClient client, Screen parent, GameOptions gameOptions) {
      List list = Lists.newArrayList();
      list.add(gameOptions.getRealmsNotifications());
      list.add(gameOptions.getAllowServerListing());
      SimpleOption lv = (SimpleOption)Nullables.map(client.world, (world) -> {
         Difficulty lv = world.getDifficulty();
         return new SimpleOption("options.difficulty.online", SimpleOption.emptyTooltip(), (optionText, unit) -> {
            return lv.getTranslatableName();
         }, new SimpleOption.PotentialValuesBasedCallbacks(List.of(Unit.INSTANCE), Codec.EMPTY.codec()), Unit.INSTANCE, (unit) -> {
         });
      });
      if (lv != null) {
         list.add(lv);
      }

      return new OnlineOptionsScreen(parent, gameOptions, (SimpleOption[])list.toArray(new SimpleOption[0]), lv);
   }

   private OnlineOptionsScreen(Screen parent, GameOptions gameOptions, SimpleOption[] options, @Nullable SimpleOption difficulty) {
      super(parent, gameOptions, Text.translatable("options.online.title"), options);
      this.difficulty = difficulty;
   }

   protected void init() {
      super.init();
      ClickableWidget lv;
      if (this.difficulty != null) {
         lv = this.buttonList.getWidgetFor(this.difficulty);
         if (lv != null) {
            lv.active = false;
         }
      }

      lv = this.buttonList.getWidgetFor(this.gameOptions.getTelemetryOptInExtra());
      if (lv != null) {
         lv.active = this.client.isOptionalTelemetryEnabledByApi();
      }

   }
}
