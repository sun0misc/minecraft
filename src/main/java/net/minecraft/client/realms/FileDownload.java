package net.minecraft.client.realms;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.mojang.logging.LogUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.realms.dto.WorldDownload;
import net.minecraft.client.realms.exception.RealmsDefaultUncaughtExceptionHandler;
import net.minecraft.client.realms.gui.screen.RealmsDownloadLatestWorldScreen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class FileDownload {
   static final Logger LOGGER = LogUtils.getLogger();
   volatile boolean cancelled;
   volatile boolean finished;
   volatile boolean error;
   volatile boolean extracting;
   @Nullable
   private volatile File backupFile;
   volatile File resourcePackPath;
   @Nullable
   private volatile HttpGet httpRequest;
   @Nullable
   private Thread currentThread;
   private final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(120000).setConnectTimeout(120000).build();
   private static final String[] INVALID_FILE_NAMES = new String[]{"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

   public long contentLength(String downloadLink) {
      CloseableHttpClient closeableHttpClient = null;
      HttpGet httpGet = null;

      long var5;
      try {
         httpGet = new HttpGet(downloadLink);
         closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
         CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
         var5 = Long.parseLong(closeableHttpResponse.getFirstHeader("Content-Length").getValue());
         return var5;
      } catch (Throwable var16) {
         LOGGER.error("Unable to get content length for download");
         var5 = 0L;
      } finally {
         if (httpGet != null) {
            httpGet.releaseConnection();
         }

         if (closeableHttpClient != null) {
            try {
               closeableHttpClient.close();
            } catch (IOException var15) {
               LOGGER.error("Could not close http client", var15);
            }
         }

      }

      return var5;
   }

   public void downloadWorld(WorldDownload download, String message, RealmsDownloadLatestWorldScreen.DownloadStatus status, LevelStorage storage) {
      if (this.currentThread == null) {
         this.currentThread = new Thread(() -> {
            CloseableHttpClient closeableHttpClient = null;
            boolean var90 = false;

            CloseableHttpResponse httpResponse;
            FileOutputStream outputStream2;
            DownloadCountingOutputStream lv4;
            ResourcePackProgressListener lv5;
            label1398: {
               label1399: {
                  try {
                     var90 = true;
                     this.backupFile = File.createTempFile("backup", ".tar.gz");
                     this.httpRequest = new HttpGet(download.downloadLink);
                     closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
                     httpResponse = closeableHttpClient.execute(this.httpRequest);
                     status.totalBytes = Long.parseLong(httpResponse.getFirstHeader("Content-Length").getValue());
                     if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        outputStream2 = new FileOutputStream(this.backupFile);
                        ProgressListener lv3 = new ProgressListener(message.trim(), this.backupFile, storage, status);
                        lv4 = new DownloadCountingOutputStream(outputStream2);
                        lv4.setListener(lv3);
                        IOUtils.copy(httpResponse.getEntity().getContent(), lv4);
                        var90 = false;
                        break label1399;
                     }

                     this.error = true;
                     this.httpRequest.abort();
                     var90 = false;
                  } catch (Exception var103) {
                     LOGGER.error("Caught exception while downloading: {}", var103.getMessage());
                     this.error = true;
                     var90 = false;
                     break label1398;
                  } finally {
                     if (var90) {
                        this.httpRequest.releaseConnection();
                        if (this.backupFile != null) {
                           this.backupFile.delete();
                        }

                        if (!this.error) {
                           if (!download.resourcePackUrl.isEmpty() && !download.resourcePackHash.isEmpty()) {
                              try {
                                 this.backupFile = File.createTempFile("resources", ".tar.gz");
                                 this.httpRequest = new HttpGet(download.resourcePackUrl);
                                 HttpResponse httpResponse3 = closeableHttpClient.execute(this.httpRequest);
                                 status.totalBytes = Long.parseLong(httpResponse3.getFirstHeader("Content-Length").getValue());
                                 if (httpResponse3.getStatusLine().getStatusCode() != 200) {
                                    this.error = true;
                                    this.httpRequest.abort();
                                    return;
                                 }

                                 OutputStream outputStream3 = new FileOutputStream(this.backupFile);
                                 ResourcePackProgressListener lv6 = new ResourcePackProgressListener(this.backupFile, status, download);
                                 DownloadCountingOutputStream lv7 = new DownloadCountingOutputStream(outputStream3);
                                 lv7.setListener(lv6);
                                 IOUtils.copy(httpResponse3.getEntity().getContent(), lv7);
                              } catch (Exception var95) {
                                 LOGGER.error("Caught exception while downloading: {}", var95.getMessage());
                                 this.error = true;
                              } finally {
                                 this.httpRequest.releaseConnection();
                                 if (this.backupFile != null) {
                                    this.backupFile.delete();
                                 }

                              }
                           } else {
                              this.finished = true;
                           }
                        }

                        if (closeableHttpClient != null) {
                           try {
                              closeableHttpClient.close();
                           } catch (IOException var91) {
                              LOGGER.error("Failed to close Realms download client");
                           }
                        }

                     }
                  }

                  this.httpRequest.releaseConnection();
                  if (this.backupFile != null) {
                     this.backupFile.delete();
                  }

                  if (!this.error) {
                     if (!download.resourcePackUrl.isEmpty() && !download.resourcePackHash.isEmpty()) {
                        try {
                           this.backupFile = File.createTempFile("resources", ".tar.gz");
                           this.httpRequest = new HttpGet(download.resourcePackUrl);
                           HttpResponse httpResponse2 = closeableHttpClient.execute(this.httpRequest);
                           status.totalBytes = Long.parseLong(httpResponse2.getFirstHeader("Content-Length").getValue());
                           if (httpResponse2.getStatusLine().getStatusCode() != 200) {
                              this.error = true;
                              this.httpRequest.abort();
                              return;
                           }

                           OutputStream outputStream = new FileOutputStream(this.backupFile);
                           ResourcePackProgressListener lv = new ResourcePackProgressListener(this.backupFile, status, download);
                           DownloadCountingOutputStream lv2 = new DownloadCountingOutputStream(outputStream);
                           lv2.setListener(lv);
                           IOUtils.copy(httpResponse2.getEntity().getContent(), lv2);
                        } catch (Exception var97) {
                           LOGGER.error("Caught exception while downloading: {}", var97.getMessage());
                           this.error = true;
                        } finally {
                           this.httpRequest.releaseConnection();
                           if (this.backupFile != null) {
                              this.backupFile.delete();
                           }

                        }
                     } else {
                        this.finished = true;
                     }
                  }

                  if (closeableHttpClient != null) {
                     try {
                        closeableHttpClient.close();
                     } catch (IOException var92) {
                        LOGGER.error("Failed to close Realms download client");
                     }
                  }

                  return;
               }

               this.httpRequest.releaseConnection();
               if (this.backupFile != null) {
                  this.backupFile.delete();
               }

               if (!this.error) {
                  if (!download.resourcePackUrl.isEmpty() && !download.resourcePackHash.isEmpty()) {
                     label1343: {
                        try {
                           this.backupFile = File.createTempFile("resources", ".tar.gz");
                           this.httpRequest = new HttpGet(download.resourcePackUrl);
                           httpResponse = closeableHttpClient.execute(this.httpRequest);
                           status.totalBytes = Long.parseLong(httpResponse.getFirstHeader("Content-Length").getValue());
                           if (httpResponse.getStatusLine().getStatusCode() == 200) {
                              outputStream2 = new FileOutputStream(this.backupFile);
                              lv5 = new ResourcePackProgressListener(this.backupFile, status, download);
                              lv4 = new DownloadCountingOutputStream(outputStream2);
                              lv4.setListener(lv5);
                              IOUtils.copy(httpResponse.getEntity().getContent(), lv4);
                              break label1343;
                           }

                           this.error = true;
                           this.httpRequest.abort();
                        } catch (Exception var101) {
                           LOGGER.error("Caught exception while downloading: {}", var101.getMessage());
                           this.error = true;
                           break label1343;
                        } finally {
                           this.httpRequest.releaseConnection();
                           if (this.backupFile != null) {
                              this.backupFile.delete();
                           }

                        }

                        return;
                     }
                  } else {
                     this.finished = true;
                  }
               }

               if (closeableHttpClient != null) {
                  try {
                     closeableHttpClient.close();
                  } catch (IOException var94) {
                     LOGGER.error("Failed to close Realms download client");
                  }

                  return;
               }

               return;
            }

            this.httpRequest.releaseConnection();
            if (this.backupFile != null) {
               this.backupFile.delete();
            }

            if (!this.error) {
               if (!download.resourcePackUrl.isEmpty() && !download.resourcePackHash.isEmpty()) {
                  try {
                     this.backupFile = File.createTempFile("resources", ".tar.gz");
                     this.httpRequest = new HttpGet(download.resourcePackUrl);
                     httpResponse = closeableHttpClient.execute(this.httpRequest);
                     status.totalBytes = Long.parseLong(httpResponse.getFirstHeader("Content-Length").getValue());
                     if (httpResponse.getStatusLine().getStatusCode() != 200) {
                        this.error = true;
                        this.httpRequest.abort();
                        return;
                     }

                     outputStream2 = new FileOutputStream(this.backupFile);
                     lv5 = new ResourcePackProgressListener(this.backupFile, status, download);
                     lv4 = new DownloadCountingOutputStream(outputStream2);
                     lv4.setListener(lv5);
                     IOUtils.copy(httpResponse.getEntity().getContent(), lv4);
                  } catch (Exception var99) {
                     LOGGER.error("Caught exception while downloading: {}", var99.getMessage());
                     this.error = true;
                  } finally {
                     this.httpRequest.releaseConnection();
                     if (this.backupFile != null) {
                        this.backupFile.delete();
                     }

                  }
               } else {
                  this.finished = true;
               }
            }

            if (closeableHttpClient != null) {
               try {
                  closeableHttpClient.close();
               } catch (IOException var93) {
                  LOGGER.error("Failed to close Realms download client");
               }
            }

         });
         this.currentThread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
         this.currentThread.start();
      }
   }

   public void cancel() {
      if (this.httpRequest != null) {
         this.httpRequest.abort();
      }

      if (this.backupFile != null) {
         this.backupFile.delete();
      }

      this.cancelled = true;
   }

   public boolean isFinished() {
      return this.finished;
   }

   public boolean isError() {
      return this.error;
   }

   public boolean isExtracting() {
      return this.extracting;
   }

   public static String findAvailableFolderName(String folder) {
      folder = folder.replaceAll("[\\./\"]", "_");
      String[] var1 = INVALID_FILE_NAMES;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         String string2 = var1[var3];
         if (folder.equalsIgnoreCase(string2)) {
            folder = "_" + folder + "_";
         }
      }

      return folder;
   }

   void untarGzipArchive(String name, @Nullable File archive, LevelStorage storage) throws IOException {
      Pattern pattern = Pattern.compile(".*-([0-9]+)$");
      int i = 1;
      char[] var7 = SharedConstants.INVALID_CHARS_LEVEL_NAME;
      int var8 = var7.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         char c = var7[var9];
         name = name.replace(c, '_');
      }

      if (StringUtils.isEmpty(name)) {
         name = "Realm";
      }

      name = findAvailableFolderName(name);

      try {
         Iterator var47 = storage.getLevelList().iterator();

         while(var47.hasNext()) {
            LevelStorage.LevelSave lv = (LevelStorage.LevelSave)var47.next();
            String string2 = lv.getRootPath();
            if (string2.toLowerCase(Locale.ROOT).startsWith(name.toLowerCase(Locale.ROOT))) {
               Matcher matcher = pattern.matcher(string2);
               if (matcher.matches()) {
                  int j = Integer.parseInt(matcher.group(1));
                  if (j > i) {
                     i = j;
                  }
               } else {
                  ++i;
               }
            }
         }
      } catch (Exception var46) {
         LOGGER.error("Error getting level list", var46);
         this.error = true;
         return;
      }

      String string3;
      if (storage.isLevelNameValid(name) && i <= 1) {
         string3 = name;
      } else {
         string3 = name + (i == 1 ? "" : "-" + i);
         if (!storage.isLevelNameValid(string3)) {
            boolean bl = false;

            while(!bl) {
               ++i;
               string3 = name + (i == 1 ? "" : "-" + i);
               if (storage.isLevelNameValid(string3)) {
                  bl = true;
               }
            }
         }
      }

      TarArchiveInputStream tarArchiveInputStream = null;
      File file2 = new File(MinecraftClient.getInstance().runDirectory.getAbsolutePath(), "saves");
      boolean var32 = false;

      LevelStorage.Session lv2;
      Path path;
      label454: {
         try {
            var32 = true;
            file2.mkdir();
            tarArchiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(archive))));

            for(TarArchiveEntry tarArchiveEntry = tarArchiveInputStream.getNextTarEntry(); tarArchiveEntry != null; tarArchiveEntry = tarArchiveInputStream.getNextTarEntry()) {
               File file3 = new File(file2, tarArchiveEntry.getName().replace("world", string3));
               if (tarArchiveEntry.isDirectory()) {
                  file3.mkdirs();
               } else {
                  file3.createNewFile();
                  FileOutputStream fileOutputStream = new FileOutputStream(file3);

                  try {
                     IOUtils.copy(tarArchiveInputStream, fileOutputStream);
                  } catch (Throwable var37) {
                     try {
                        fileOutputStream.close();
                     } catch (Throwable var35) {
                        var37.addSuppressed(var35);
                     }

                     throw var37;
                  }

                  fileOutputStream.close();
               }
            }

            var32 = false;
            break label454;
         } catch (Exception var44) {
            LOGGER.error("Error extracting world", var44);
            this.error = true;
            var32 = false;
         } finally {
            if (var32) {
               if (tarArchiveInputStream != null) {
                  tarArchiveInputStream.close();
               }

               if (archive != null) {
                  archive.delete();
               }

               try {
                  LevelStorage.Session lv3 = storage.createSession(string3);

                  try {
                     lv3.save(string3.trim());
                     Path path2 = lv3.getDirectory(WorldSavePath.LEVEL_DAT);
                     readNbtFile(path2.toFile());
                  } catch (Throwable var38) {
                     if (lv3 != null) {
                        try {
                           lv3.close();
                        } catch (Throwable var33) {
                           var38.addSuppressed(var33);
                        }
                     }

                     throw var38;
                  }

                  if (lv3 != null) {
                     lv3.close();
                  }
               } catch (IOException var39) {
                  LOGGER.error("Failed to rename unpacked realms level {}", string3, var39);
               }

               this.resourcePackPath = new File(file2, string3 + File.separator + "resources.zip");
            }
         }

         if (tarArchiveInputStream != null) {
            tarArchiveInputStream.close();
         }

         if (archive != null) {
            archive.delete();
         }

         try {
            lv2 = storage.createSession(string3);

            try {
               lv2.save(string3.trim());
               path = lv2.getDirectory(WorldSavePath.LEVEL_DAT);
               readNbtFile(path.toFile());
            } catch (Throwable var40) {
               if (lv2 != null) {
                  try {
                     lv2.close();
                  } catch (Throwable var34) {
                     var40.addSuppressed(var34);
                  }
               }

               throw var40;
            }

            if (lv2 != null) {
               lv2.close();
            }
         } catch (IOException var41) {
            LOGGER.error("Failed to rename unpacked realms level {}", string3, var41);
         }

         this.resourcePackPath = new File(file2, string3 + File.separator + "resources.zip");
         return;
      }

      if (tarArchiveInputStream != null) {
         tarArchiveInputStream.close();
      }

      if (archive != null) {
         archive.delete();
      }

      try {
         lv2 = storage.createSession(string3);

         try {
            lv2.save(string3.trim());
            path = lv2.getDirectory(WorldSavePath.LEVEL_DAT);
            readNbtFile(path.toFile());
         } catch (Throwable var42) {
            if (lv2 != null) {
               try {
                  lv2.close();
               } catch (Throwable var36) {
                  var42.addSuppressed(var36);
               }
            }

            throw var42;
         }

         if (lv2 != null) {
            lv2.close();
         }
      } catch (IOException var43) {
         LOGGER.error("Failed to rename unpacked realms level {}", string3, var43);
      }

      this.resourcePackPath = new File(file2, string3 + File.separator + "resources.zip");
   }

   private static void readNbtFile(File file) {
      if (file.exists()) {
         try {
            NbtCompound lv = NbtIo.readCompressed(file);
            NbtCompound lv2 = lv.getCompound("Data");
            lv2.remove("Player");
            NbtIo.writeCompressed(lv, file);
         } catch (Exception var3) {
            var3.printStackTrace();
         }
      }

   }

   @Environment(EnvType.CLIENT)
   private class ResourcePackProgressListener implements ActionListener {
      private final File tempFile;
      private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;
      private final WorldDownload worldDownload;

      ResourcePackProgressListener(File tempFile, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus, WorldDownload worldDownload) {
         this.tempFile = tempFile;
         this.downloadStatus = downloadStatus;
         this.worldDownload = worldDownload;
      }

      public void actionPerformed(ActionEvent e) {
         this.downloadStatus.bytesWritten = ((DownloadCountingOutputStream)e.getSource()).getByteCount();
         if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled) {
            try {
               String string = Hashing.sha1().hashBytes(Files.toByteArray(this.tempFile)).toString();
               if (string.equals(this.worldDownload.resourcePackHash)) {
                  FileUtils.copyFile(this.tempFile, FileDownload.this.resourcePackPath);
                  FileDownload.this.finished = true;
               } else {
                  FileDownload.LOGGER.error("Resourcepack had wrong hash (expected {}, found {}). Deleting it.", this.worldDownload.resourcePackHash, string);
                  FileUtils.deleteQuietly(this.tempFile);
                  FileDownload.this.error = true;
               }
            } catch (IOException var3) {
               FileDownload.LOGGER.error("Error copying resourcepack file: {}", var3.getMessage());
               FileDownload.this.error = true;
            }
         }

      }
   }

   @Environment(EnvType.CLIENT)
   private static class DownloadCountingOutputStream extends CountingOutputStream {
      @Nullable
      private ActionListener listener;

      public DownloadCountingOutputStream(OutputStream stream) {
         super(stream);
      }

      public void setListener(ActionListener listener) {
         this.listener = listener;
      }

      protected void afterWrite(int n) throws IOException {
         super.afterWrite(n);
         if (this.listener != null) {
            this.listener.actionPerformed(new ActionEvent(this, 0, (String)null));
         }

      }
   }

   @Environment(EnvType.CLIENT)
   class ProgressListener implements ActionListener {
      private final String worldName;
      private final File tempFile;
      private final LevelStorage levelStorageSource;
      private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;

      ProgressListener(String worldName, File tempFile, LevelStorage levelStorageSource, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus) {
         this.worldName = worldName;
         this.tempFile = tempFile;
         this.levelStorageSource = levelStorageSource;
         this.downloadStatus = downloadStatus;
      }

      public void actionPerformed(ActionEvent e) {
         this.downloadStatus.bytesWritten = ((DownloadCountingOutputStream)e.getSource()).getByteCount();
         if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled && !FileDownload.this.error) {
            try {
               FileDownload.this.extracting = true;
               FileDownload.this.untarGzipArchive(this.worldName, this.tempFile, this.levelStorageSource);
            } catch (IOException var3) {
               FileDownload.LOGGER.error("Error extracting archive", var3);
               FileDownload.this.error = true;
            }
         }

      }
   }
}
