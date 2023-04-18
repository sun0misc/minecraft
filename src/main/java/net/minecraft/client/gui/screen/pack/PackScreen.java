package net.minecraft.client.gui.screen.pack;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class PackScreen extends Screen {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_32395 = 200;
   private static final Text DROP_INFO;
   private static final Text FOLDER_INFO;
   private static final int field_32396 = 20;
   private static final Identifier UNKNOWN_PACK;
   private final ResourcePackOrganizer organizer;
   @Nullable
   private DirectoryWatcher directoryWatcher;
   private long refreshTimeout;
   private PackListWidget availablePackList;
   private PackListWidget selectedPackList;
   private final Path file;
   private ButtonWidget doneButton;
   private final Map iconTextures = Maps.newHashMap();

   public PackScreen(ResourcePackManager resourcePackManager, Consumer applier, Path file, Text title) {
      super(title);
      this.organizer = new ResourcePackOrganizer(this::updatePackLists, this::getPackIconTexture, resourcePackManager, applier);
      this.file = file;
      this.directoryWatcher = PackScreen.DirectoryWatcher.create(file);
   }

   public void close() {
      this.organizer.apply();
      this.closeDirectoryWatcher();
   }

   private void closeDirectoryWatcher() {
      if (this.directoryWatcher != null) {
         try {
            this.directoryWatcher.close();
            this.directoryWatcher = null;
         } catch (Exception var2) {
         }
      }

   }

   protected void init() {
      this.availablePackList = new PackListWidget(this.client, this, 200, this.height, Text.translatable("pack.available.title"));
      this.availablePackList.setLeftPos(this.width / 2 - 4 - 200);
      this.addSelectableChild(this.availablePackList);
      this.selectedPackList = new PackListWidget(this.client, this, 200, this.height, Text.translatable("pack.selected.title"));
      this.selectedPackList.setLeftPos(this.width / 2 + 4);
      this.addSelectableChild(this.selectedPackList);
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("pack.openFolder"), (button) -> {
         Util.getOperatingSystem().open(this.file.toUri());
      }).dimensions(this.width / 2 - 154, this.height - 48, 150, 20).tooltip(Tooltip.of(FOLDER_INFO)).build());
      this.doneButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
         this.close();
      }).dimensions(this.width / 2 + 4, this.height - 48, 150, 20).build());
      this.refresh();
   }

   public void tick() {
      if (this.directoryWatcher != null) {
         try {
            if (this.directoryWatcher.pollForChange()) {
               this.refreshTimeout = 20L;
            }
         } catch (IOException var2) {
            LOGGER.warn("Failed to poll for directory {} changes, stopping", this.file);
            this.closeDirectoryWatcher();
         }
      }

      if (this.refreshTimeout > 0L && --this.refreshTimeout == 0L) {
         this.refresh();
      }

   }

   private void updatePackLists() {
      this.updatePackList(this.selectedPackList, this.organizer.getEnabledPacks());
      this.updatePackList(this.availablePackList, this.organizer.getDisabledPacks());
      this.doneButton.active = !this.selectedPackList.children().isEmpty();
   }

   private void updatePackList(PackListWidget widget, Stream packs) {
      widget.children().clear();
      PackListWidget.ResourcePackEntry lv = (PackListWidget.ResourcePackEntry)widget.getSelectedOrNull();
      String string = lv == null ? "" : lv.getName();
      widget.setSelected((EntryListWidget.Entry)null);
      packs.forEach((pack) -> {
         PackListWidget.ResourcePackEntry lv = new PackListWidget.ResourcePackEntry(this.client, widget, pack);
         widget.children().add(lv);
         if (pack.getName().equals(string)) {
            widget.setSelected(lv);
         }

      });
   }

   public void switchFocusedList(PackListWidget listWidget) {
      PackListWidget lv = this.selectedPackList == listWidget ? this.availablePackList : this.selectedPackList;
      this.switchFocus(GuiNavigationPath.of((Element)lv.getFirst(), (ParentElement[])(lv, this)));
   }

   public void clearSelection() {
      this.selectedPackList.setSelected((EntryListWidget.Entry)null);
      this.availablePackList.setSelected((EntryListWidget.Entry)null);
   }

   private void refresh() {
      this.organizer.refresh();
      this.updatePackLists();
      this.refreshTimeout = 0L;
      this.iconTextures.clear();
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackgroundTexture(matrices);
      this.availablePackList.render(matrices, mouseX, mouseY, delta);
      this.selectedPackList.render(matrices, mouseX, mouseY, delta);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);
      drawCenteredTextWithShadow(matrices, this.textRenderer, DROP_INFO, this.width / 2, 20, 16777215);
      super.render(matrices, mouseX, mouseY, delta);
   }

   protected static void copyPacks(MinecraftClient client, List srcPaths, Path destPath) {
      MutableBoolean mutableBoolean = new MutableBoolean();
      srcPaths.forEach((src) -> {
         try {
            Stream stream = Files.walk(src);

            try {
               stream.forEach((toCopy) -> {
                  try {
                     Util.relativeCopy(src.getParent(), destPath, toCopy);
                  } catch (IOException var5) {
                     LOGGER.warn("Failed to copy datapack file  from {} to {}", new Object[]{toCopy, destPath, var5});
                     mutableBoolean.setTrue();
                  }

               });
            } catch (Throwable var7) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (stream != null) {
               stream.close();
            }
         } catch (IOException var8) {
            LOGGER.warn("Failed to copy datapack file from {} to {}", src, destPath);
            mutableBoolean.setTrue();
         }

      });
      if (mutableBoolean.isTrue()) {
         SystemToast.addPackCopyFailure(client, destPath.toString());
      }

   }

   public void filesDragged(List paths) {
      String string = (String)paths.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", "));
      this.client.setScreen(new ConfirmScreen((confirmed) -> {
         if (confirmed) {
            copyPacks(this.client, paths, this.file);
            this.refresh();
         }

         this.client.setScreen(this);
      }, Text.translatable("pack.dropConfirm"), Text.literal(string)));
   }

   private Identifier loadPackIcon(TextureManager textureManager, ResourcePackProfile resourcePackProfile) {
      try {
         ResourcePack lv = resourcePackProfile.createResourcePack();

         Identifier var15;
         label70: {
            Identifier var9;
            try {
               InputSupplier lv2 = lv.openRoot("pack.png");
               if (lv2 == null) {
                  var15 = UNKNOWN_PACK;
                  break label70;
               }

               String string = resourcePackProfile.getName();
               String var10003 = Util.replaceInvalidChars(string, Identifier::isPathCharacterValid);
               Identifier lv3 = new Identifier("minecraft", "pack/" + var10003 + "/" + Hashing.sha1().hashUnencodedChars(string) + "/icon");
               InputStream inputStream = (InputStream)lv2.get();

               try {
                  NativeImage lv4 = NativeImage.read(inputStream);
                  textureManager.registerTexture(lv3, new NativeImageBackedTexture(lv4));
                  var9 = lv3;
               } catch (Throwable var12) {
                  if (inputStream != null) {
                     try {
                        inputStream.close();
                     } catch (Throwable var11) {
                        var12.addSuppressed(var11);
                     }
                  }

                  throw var12;
               }

               if (inputStream != null) {
                  inputStream.close();
               }
            } catch (Throwable var13) {
               if (lv != null) {
                  try {
                     lv.close();
                  } catch (Throwable var10) {
                     var13.addSuppressed(var10);
                  }
               }

               throw var13;
            }

            if (lv != null) {
               lv.close();
            }

            return var9;
         }

         if (lv != null) {
            lv.close();
         }

         return var15;
      } catch (Exception var14) {
         LOGGER.warn("Failed to load icon from pack {}", resourcePackProfile.getName(), var14);
         return UNKNOWN_PACK;
      }
   }

   private Identifier getPackIconTexture(ResourcePackProfile resourcePackProfile) {
      return (Identifier)this.iconTextures.computeIfAbsent(resourcePackProfile.getName(), (profileName) -> {
         return this.loadPackIcon(this.client.getTextureManager(), resourcePackProfile);
      });
   }

   static {
      DROP_INFO = Text.translatable("pack.dropInfo").formatted(Formatting.GRAY);
      FOLDER_INFO = Text.translatable("pack.folderInfo");
      UNKNOWN_PACK = new Identifier("textures/misc/unknown_pack.png");
   }

   @Environment(EnvType.CLIENT)
   private static class DirectoryWatcher implements AutoCloseable {
      private final WatchService watchService;
      private final Path path;

      public DirectoryWatcher(Path path) throws IOException {
         this.path = path;
         this.watchService = path.getFileSystem().newWatchService();

         try {
            this.watchDirectory(path);
            DirectoryStream directoryStream = Files.newDirectoryStream(path);

            try {
               Iterator var3 = directoryStream.iterator();

               while(var3.hasNext()) {
                  Path path2 = (Path)var3.next();
                  if (Files.isDirectory(path2, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
                     this.watchDirectory(path2);
                  }
               }
            } catch (Throwable var6) {
               if (directoryStream != null) {
                  try {
                     directoryStream.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (directoryStream != null) {
               directoryStream.close();
            }

         } catch (Exception var7) {
            this.watchService.close();
            throw var7;
         }
      }

      @Nullable
      public static DirectoryWatcher create(Path path) {
         try {
            return new DirectoryWatcher(path);
         } catch (IOException var2) {
            PackScreen.LOGGER.warn("Failed to initialize pack directory {} monitoring", path, var2);
            return null;
         }
      }

      private void watchDirectory(Path path) throws IOException {
         path.register(this.watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
      }

      public boolean pollForChange() throws IOException {
         boolean bl = false;

         WatchKey watchKey;
         while((watchKey = this.watchService.poll()) != null) {
            List list = watchKey.pollEvents();
            Iterator var4 = list.iterator();

            while(var4.hasNext()) {
               WatchEvent watchEvent = (WatchEvent)var4.next();
               bl = true;
               if (watchKey.watchable() == this.path && watchEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                  Path path = this.path.resolve((Path)watchEvent.context());
                  if (Files.isDirectory(path, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
                     this.watchDirectory(path);
                  }
               }
            }

            watchKey.reset();
         }

         return bl;
      }

      public void close() throws IOException {
         this.watchService.close();
      }
   }
}
