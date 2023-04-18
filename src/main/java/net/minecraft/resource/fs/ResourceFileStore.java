package net.minecraft.resource.fs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import org.jetbrains.annotations.Nullable;

class ResourceFileStore extends FileStore {
   private final String name;

   public ResourceFileStore(String name) {
      this.name = name;
   }

   public String name() {
      return this.name;
   }

   public String type() {
      return "index";
   }

   public boolean isReadOnly() {
      return true;
   }

   public long getTotalSpace() {
      return 0L;
   }

   public long getUsableSpace() {
      return 0L;
   }

   public long getUnallocatedSpace() {
      return 0L;
   }

   public boolean supportsFileAttributeView(Class type) {
      return type == BasicFileAttributeView.class;
   }

   public boolean supportsFileAttributeView(String name) {
      return "basic".equals(name);
   }

   @Nullable
   public FileStoreAttributeView getFileStoreAttributeView(Class type) {
      return null;
   }

   public Object getAttribute(String attribute) throws IOException {
      throw new UnsupportedOperationException();
   }
}
