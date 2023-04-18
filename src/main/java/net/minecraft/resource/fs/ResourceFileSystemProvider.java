package net.minecraft.resource.fs;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

class ResourceFileSystemProvider extends FileSystemProvider {
   public static final String SCHEME = "x-mc-link";

   public String getScheme() {
      return "x-mc-link";
   }

   public FileSystem newFileSystem(URI uri, Map env) {
      throw new UnsupportedOperationException();
   }

   public FileSystem getFileSystem(URI uri) {
      throw new UnsupportedOperationException();
   }

   public Path getPath(URI uri) {
      throw new UnsupportedOperationException();
   }

   public SeekableByteChannel newByteChannel(Path path, Set options, FileAttribute... attrs) throws IOException {
      if (!options.contains(StandardOpenOption.CREATE_NEW) && !options.contains(StandardOpenOption.CREATE) && !options.contains(StandardOpenOption.APPEND) && !options.contains(StandardOpenOption.WRITE)) {
         Path path2 = toResourcePath(path).toAbsolutePath().toPath();
         if (path2 == null) {
            throw new NoSuchFileException(path.toString());
         } else {
            return Files.newByteChannel(path2, options, attrs);
         }
      } else {
         throw new UnsupportedOperationException();
      }
   }

   public DirectoryStream newDirectoryStream(Path dir, final DirectoryStream.Filter filter) throws IOException {
      final ResourceFile.Directory lv = toResourcePath(dir).toAbsolutePath().toDirectory();
      if (lv == null) {
         throw new NotDirectoryException(dir.toString());
      } else {
         return new DirectoryStream() {
            public Iterator iterator() {
               return lv.children().values().stream().filter((child) -> {
                  try {
                     return filter.accept(child);
                  } catch (IOException var3) {
                     throw new DirectoryIteratorException(var3);
                  }
               }).map((child) -> {
                  return child;
               }).iterator();
            }

            public void close() {
            }
         };
      }
   }

   public void createDirectory(Path dir, FileAttribute... attrs) {
      throw new ReadOnlyFileSystemException();
   }

   public void delete(Path path) {
      throw new ReadOnlyFileSystemException();
   }

   public void copy(Path source, Path target, CopyOption... options) {
      throw new ReadOnlyFileSystemException();
   }

   public void move(Path source, Path target, CopyOption... options) {
      throw new ReadOnlyFileSystemException();
   }

   public boolean isSameFile(Path path, Path path2) {
      return path instanceof ResourcePath && path2 instanceof ResourcePath && path.equals(path2);
   }

   public boolean isHidden(Path path) {
      return false;
   }

   public FileStore getFileStore(Path path) {
      return toResourcePath(path).getFileSystem().getStore();
   }

   public void checkAccess(Path path, AccessMode... modes) throws IOException {
      if (modes.length == 0 && !toResourcePath(path).isReadable()) {
         throw new NoSuchFileException(path.toString());
      } else {
         AccessMode[] var3 = modes;
         int var4 = modes.length;
         int var5 = 0;

         while(var5 < var4) {
            AccessMode accessMode = var3[var5];
            switch (accessMode) {
               case READ:
                  if (!toResourcePath(path).isReadable()) {
                     throw new NoSuchFileException(path.toString());
                  }
               default:
                  ++var5;
                  break;
               case EXECUTE:
               case WRITE:
                  throw new AccessDeniedException(accessMode.toString());
            }
         }

      }
   }

   @Nullable
   public FileAttributeView getFileAttributeView(Path path, Class type, LinkOption... options) {
      ResourcePath lv = toResourcePath(path);
      return type == BasicFileAttributeView.class ? lv.getAttributeView() : null;
   }

   public BasicFileAttributes readAttributes(Path path, Class type, LinkOption... options) throws IOException {
      ResourcePath lv = toResourcePath(path).toAbsolutePath();
      if (type == BasicFileAttributes.class) {
         return lv.getAttributes();
      } else {
         throw new UnsupportedOperationException("Attributes of type " + type.getName() + " not supported");
      }
   }

   public Map readAttributes(Path path, String attributes, LinkOption... options) {
      throw new UnsupportedOperationException();
   }

   public void setAttribute(Path path, String attribute, Object value, LinkOption... options) {
      throw new ReadOnlyFileSystemException();
   }

   private static ResourcePath toResourcePath(@Nullable Path path) {
      if (path == null) {
         throw new NullPointerException();
      } else if (path instanceof ResourcePath) {
         ResourcePath lv = (ResourcePath)path;
         return lv;
      } else {
         throw new ProviderMismatchException();
      }
   }
}
