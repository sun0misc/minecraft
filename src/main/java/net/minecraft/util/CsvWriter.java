package net.minecraft.util;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.Nullable;

public class CsvWriter {
   private static final String CRLF = "\r\n";
   private static final String COMMA = ",";
   private final Writer writer;
   private final int column;

   CsvWriter(Writer writer, List columns) throws IOException {
      this.writer = writer;
      this.column = columns.size();
      this.printRow(columns.stream());
   }

   public static Header makeHeader() {
      return new Header();
   }

   public void printRow(Object... columns) throws IOException {
      if (columns.length != this.column) {
         throw new IllegalArgumentException("Invalid number of columns, expected " + this.column + ", but got " + columns.length);
      } else {
         this.printRow(Stream.of(columns));
      }
   }

   private void printRow(Stream columns) throws IOException {
      Writer var10000 = this.writer;
      Stream var10001 = columns.map(CsvWriter::escape);
      var10000.write((String)var10001.collect(Collectors.joining(",")) + "\r\n");
   }

   private static String escape(@Nullable Object o) {
      return StringEscapeUtils.escapeCsv(o != null ? o.toString() : "[null]");
   }

   public static class Header {
      private final List columns = Lists.newArrayList();

      public Header addColumn(String name) {
         this.columns.add(name);
         return this;
      }

      public CsvWriter startBody(Writer writer) throws IOException {
         return new CsvWriter(writer, this.columns);
      }
   }
}
