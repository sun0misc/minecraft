package net.minecraft.world.level.storage;

import java.nio.file.Path;
import net.minecraft.GameVersion;
import net.minecraft.SharedConstants;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;
import net.minecraft.world.GameMode;
import net.minecraft.world.level.LevelInfo;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class LevelSummary implements Comparable {
   private final LevelInfo levelInfo;
   private final SaveVersionInfo versionInfo;
   private final String name;
   private final boolean requiresConversion;
   private final boolean locked;
   private final boolean experimental;
   private final Path iconPath;
   @Nullable
   private Text details;

   public LevelSummary(LevelInfo levelInfo, SaveVersionInfo versionInfo, String name, boolean requiresConversion, boolean locked, boolean experimental, Path iconPath) {
      this.levelInfo = levelInfo;
      this.versionInfo = versionInfo;
      this.name = name;
      this.locked = locked;
      this.experimental = experimental;
      this.iconPath = iconPath;
      this.requiresConversion = requiresConversion;
   }

   public String getName() {
      return this.name;
   }

   public String getDisplayName() {
      return StringUtils.isEmpty(this.levelInfo.getLevelName()) ? this.name : this.levelInfo.getLevelName();
   }

   public Path getIconPath() {
      return this.iconPath;
   }

   public boolean requiresConversion() {
      return this.requiresConversion;
   }

   public boolean isExperimental() {
      return this.experimental;
   }

   public long getLastPlayed() {
      return this.versionInfo.getLastPlayed();
   }

   public int compareTo(LevelSummary arg) {
      if (this.versionInfo.getLastPlayed() < arg.versionInfo.getLastPlayed()) {
         return 1;
      } else {
         return this.versionInfo.getLastPlayed() > arg.versionInfo.getLastPlayed() ? -1 : this.name.compareTo(arg.name);
      }
   }

   public LevelInfo getLevelInfo() {
      return this.levelInfo;
   }

   public GameMode getGameMode() {
      return this.levelInfo.getGameMode();
   }

   public boolean isHardcore() {
      return this.levelInfo.isHardcore();
   }

   public boolean hasCheats() {
      return this.levelInfo.areCommandsAllowed();
   }

   public MutableText getVersion() {
      return StringHelper.isEmpty(this.versionInfo.getVersionName()) ? Text.translatable("selectWorld.versionUnknown") : Text.literal(this.versionInfo.getVersionName());
   }

   public SaveVersionInfo getVersionInfo() {
      return this.versionInfo;
   }

   public boolean isDifferentVersion() {
      return this.isFutureLevel() || !SharedConstants.getGameVersion().isStable() && !this.versionInfo.isStable() || this.getConversionWarning().promptsBackup();
   }

   public boolean isFutureLevel() {
      return this.versionInfo.getVersion().getId() > SharedConstants.getGameVersion().getSaveVersion().getId();
   }

   public ConversionWarning getConversionWarning() {
      GameVersion lv = SharedConstants.getGameVersion();
      int i = lv.getSaveVersion().getId();
      int j = this.versionInfo.getVersion().getId();
      if (!lv.isStable() && j < i) {
         return LevelSummary.ConversionWarning.UPGRADE_TO_SNAPSHOT;
      } else {
         return j > i ? LevelSummary.ConversionWarning.DOWNGRADE : LevelSummary.ConversionWarning.NONE;
      }
   }

   public boolean isLocked() {
      return this.locked;
   }

   public boolean isUnavailable() {
      if (!this.isLocked() && !this.requiresConversion()) {
         return !this.isVersionAvailable();
      } else {
         return true;
      }
   }

   public boolean isVersionAvailable() {
      return SharedConstants.getGameVersion().getSaveVersion().isAvailableTo(this.versionInfo.getVersion());
   }

   public Text getDetails() {
      if (this.details == null) {
         this.details = this.createDetails();
      }

      return this.details;
   }

   private Text createDetails() {
      if (this.isLocked()) {
         return Text.translatable("selectWorld.locked").formatted(Formatting.RED);
      } else if (this.requiresConversion()) {
         return Text.translatable("selectWorld.conversion").formatted(Formatting.RED);
      } else if (!this.isVersionAvailable()) {
         return Text.translatable("selectWorld.incompatible_series").formatted(Formatting.RED);
      } else {
         MutableText lv = this.isHardcore() ? Text.empty().append((Text)Text.translatable("gameMode.hardcore").styled((style) -> {
            return style.withColor(-65536);
         })) : Text.translatable("gameMode." + this.getGameMode().getName());
         if (this.hasCheats()) {
            lv.append(", ").append((Text)Text.translatable("selectWorld.cheats"));
         }

         if (this.isExperimental()) {
            lv.append(", ").append((Text)Text.translatable("selectWorld.experimental").formatted(Formatting.YELLOW));
         }

         MutableText lv2 = this.getVersion();
         MutableText lv3 = Text.literal(", ").append((Text)Text.translatable("selectWorld.version")).append(ScreenTexts.SPACE);
         if (this.isDifferentVersion()) {
            lv3.append((Text)lv2.formatted(this.isFutureLevel() ? Formatting.RED : Formatting.ITALIC));
         } else {
            lv3.append((Text)lv2);
         }

         lv.append((Text)lv3);
         return lv;
      }
   }

   // $FF: synthetic method
   public int compareTo(Object other) {
      return this.compareTo((LevelSummary)other);
   }

   public static enum ConversionWarning {
      NONE(false, false, ""),
      DOWNGRADE(true, true, "downgrade"),
      UPGRADE_TO_SNAPSHOT(true, false, "snapshot");

      private final boolean backup;
      private final boolean boldRedFormatting;
      private final String translationKeySuffix;

      private ConversionWarning(boolean backup, boolean boldRedFormatting, String translationKeySuffix) {
         this.backup = backup;
         this.boldRedFormatting = boldRedFormatting;
         this.translationKeySuffix = translationKeySuffix;
      }

      public boolean promptsBackup() {
         return this.backup;
      }

      public boolean needsBoldRedFormatting() {
         return this.boldRedFormatting;
      }

      public String getTranslationKeySuffix() {
         return this.translationKeySuffix;
      }

      // $FF: synthetic method
      private static ConversionWarning[] method_36792() {
         return new ConversionWarning[]{NONE, DOWNGRADE, UPGRADE_TO_SNAPSHOT};
      }
   }
}
