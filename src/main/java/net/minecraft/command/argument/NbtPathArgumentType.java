package net.minecraft.command.argument;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NbtPathArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("foo", "foo.bar", "foo[0]", "[0]", "[]", "{foo=bar}");
   public static final SimpleCommandExceptionType INVALID_PATH_NODE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("arguments.nbtpath.node.invalid"));
   public static final SimpleCommandExceptionType TOO_DEEP_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("arguments.nbtpath.too_deep"));
   public static final DynamicCommandExceptionType NOTHING_FOUND_EXCEPTION = new DynamicCommandExceptionType((path) -> {
      return Text.translatable("arguments.nbtpath.nothing_found", path);
   });
   static final DynamicCommandExceptionType EXPECTED_LIST_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return Text.translatable("commands.data.modify.expected_list", object);
   });
   static final DynamicCommandExceptionType INVALID_INDEX_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return Text.translatable("commands.data.modify.invalid_index", object);
   });
   private static final char LEFT_SQUARE_BRACKET = '[';
   private static final char RIGHT_SQUARE_BRACKET = ']';
   private static final char LEFT_CURLY_BRACKET = '{';
   private static final char RIGHT_CURLY_BRACKET = '}';
   private static final char DOUBLE_QUOTE = '"';

   public static NbtPathArgumentType nbtPath() {
      return new NbtPathArgumentType();
   }

   public static NbtPath getNbtPath(CommandContext context, String name) {
      return (NbtPath)context.getArgument(name, NbtPath.class);
   }

   public NbtPath parse(StringReader stringReader) throws CommandSyntaxException {
      List list = Lists.newArrayList();
      int i = stringReader.getCursor();
      Object2IntMap object2IntMap = new Object2IntOpenHashMap();
      boolean bl = true;

      while(stringReader.canRead() && stringReader.peek() != ' ') {
         PathNode lv = parseNode(stringReader, bl);
         list.add(lv);
         object2IntMap.put(lv, stringReader.getCursor() - i);
         bl = false;
         if (stringReader.canRead()) {
            char c = stringReader.peek();
            if (c != ' ' && c != '[' && c != '{') {
               stringReader.expect('.');
            }
         }
      }

      return new NbtPath(stringReader.getString().substring(i, stringReader.getCursor()), (PathNode[])list.toArray(new PathNode[0]), object2IntMap);
   }

   private static PathNode parseNode(StringReader reader, boolean root) throws CommandSyntaxException {
      String string;
      switch (reader.peek()) {
         case '"':
            string = reader.readString();
            return readCompoundChildNode(reader, string);
         case '[':
            reader.skip();
            int i = reader.peek();
            if (i == '{') {
               NbtCompound lv2 = (new StringNbtReader(reader)).parseCompound();
               reader.expect(']');
               return new FilteredListElementNode(lv2);
            } else {
               if (i == ']') {
                  reader.skip();
                  return NbtPathArgumentType.AllListElementNode.INSTANCE;
               }

               int j = reader.readInt();
               reader.expect(']');
               return new IndexedListElementNode(j);
            }
         case '{':
            if (!root) {
               throw INVALID_PATH_NODE_EXCEPTION.createWithContext(reader);
            }

            NbtCompound lv = (new StringNbtReader(reader)).parseCompound();
            return new FilteredRootNode(lv);
         default:
            string = readName(reader);
            return readCompoundChildNode(reader, string);
      }
   }

   private static PathNode readCompoundChildNode(StringReader reader, String name) throws CommandSyntaxException {
      if (reader.canRead() && reader.peek() == '{') {
         NbtCompound lv = (new StringNbtReader(reader)).parseCompound();
         return new FilteredNamedNode(name, lv);
      } else {
         return new NamedNode(name);
      }
   }

   private static String readName(StringReader reader) throws CommandSyntaxException {
      int i = reader.getCursor();

      while(reader.canRead() && isNameCharacter(reader.peek())) {
         reader.skip();
      }

      if (reader.getCursor() == i) {
         throw INVALID_PATH_NODE_EXCEPTION.createWithContext(reader);
      } else {
         return reader.getString().substring(i, reader.getCursor());
      }
   }

   public Collection getExamples() {
      return EXAMPLES;
   }

   private static boolean isNameCharacter(char c) {
      return c != ' ' && c != '"' && c != '[' && c != ']' && c != '.' && c != '{' && c != '}';
   }

   static Predicate getPredicate(NbtCompound filter) {
      return (nbt) -> {
         return NbtHelper.matches(filter, nbt, true);
      };
   }

   // $FF: synthetic method
   public Object parse(StringReader reader) throws CommandSyntaxException {
      return this.parse(reader);
   }

   public static class NbtPath {
      private final String string;
      private final Object2IntMap nodeEndIndices;
      private final PathNode[] nodes;

      public NbtPath(String string, PathNode[] nodes, Object2IntMap nodeEndIndices) {
         this.string = string;
         this.nodes = nodes;
         this.nodeEndIndices = nodeEndIndices;
      }

      public List get(NbtElement element) throws CommandSyntaxException {
         List list = Collections.singletonList(element);
         PathNode[] var3 = this.nodes;
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            PathNode lv = var3[var5];
            list = lv.get(list);
            if (list.isEmpty()) {
               throw this.createNothingFoundException(lv);
            }
         }

         return list;
      }

      public int count(NbtElement element) {
         List list = Collections.singletonList(element);
         PathNode[] var3 = this.nodes;
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            PathNode lv = var3[var5];
            list = lv.get(list);
            if (list.isEmpty()) {
               return 0;
            }
         }

         return list.size();
      }

      private List getTerminals(NbtElement start) throws CommandSyntaxException {
         List list = Collections.singletonList(start);

         for(int i = 0; i < this.nodes.length - 1; ++i) {
            PathNode lv = this.nodes[i];
            int j = i + 1;
            PathNode var10002 = this.nodes[j];
            Objects.requireNonNull(var10002);
            list = lv.getOrInit(list, var10002::init);
            if (list.isEmpty()) {
               throw this.createNothingFoundException(lv);
            }
         }

         return list;
      }

      public List getOrInit(NbtElement element, Supplier source) throws CommandSyntaxException {
         List list = this.getTerminals(element);
         PathNode lv = this.nodes[this.nodes.length - 1];
         return lv.getOrInit(list, source);
      }

      private static int forEach(List elements, Function operation) {
         return (Integer)elements.stream().map(operation).reduce(0, (a, b) -> {
            return a + b;
         });
      }

      public static boolean isTooDeep(NbtElement element, int depth) {
         if (depth >= 512) {
            return true;
         } else {
            Iterator var4;
            if (element instanceof NbtCompound) {
               NbtCompound lv = (NbtCompound)element;
               var4 = lv.getKeys().iterator();

               while(var4.hasNext()) {
                  String string = (String)var4.next();
                  NbtElement lv2 = lv.get(string);
                  if (lv2 != null && isTooDeep(lv2, depth + 1)) {
                     return true;
                  }
               }
            } else if (element instanceof NbtList) {
               NbtList lv3 = (NbtList)element;
               var4 = lv3.iterator();

               while(var4.hasNext()) {
                  NbtElement lv4 = (NbtElement)var4.next();
                  if (isTooDeep(lv4, depth + 1)) {
                     return true;
                  }
               }
            }

            return false;
         }
      }

      public int put(NbtElement element, NbtElement source) throws CommandSyntaxException {
         if (isTooDeep(source, this.getDepth())) {
            throw NbtPathArgumentType.TOO_DEEP_EXCEPTION.create();
         } else {
            NbtElement lv = source.copy();
            List list = this.getTerminals(element);
            if (list.isEmpty()) {
               return 0;
            } else {
               PathNode lv2 = this.nodes[this.nodes.length - 1];
               MutableBoolean mutableBoolean = new MutableBoolean(false);
               return forEach(list, (arg3) -> {
                  return lv2.set(arg3, () -> {
                     if (mutableBoolean.isFalse()) {
                        mutableBoolean.setTrue();
                        return lv;
                     } else {
                        return lv.copy();
                     }
                  });
               });
            }
         }
      }

      private int getDepth() {
         return this.nodes.length;
      }

      public int insert(int index, NbtCompound compound, List elements) throws CommandSyntaxException {
         List list2 = new ArrayList(elements.size());
         Iterator var5 = elements.iterator();

         while(var5.hasNext()) {
            NbtElement lv = (NbtElement)var5.next();
            NbtElement lv2 = lv.copy();
            list2.add(lv2);
            if (isTooDeep(lv2, this.getDepth())) {
               throw NbtPathArgumentType.TOO_DEEP_EXCEPTION.create();
            }
         }

         Collection collection = this.getOrInit(compound, NbtList::new);
         int j = 0;
         boolean bl = false;

         boolean bl2;
         for(Iterator var8 = collection.iterator(); var8.hasNext(); j += bl2 ? 1 : 0) {
            NbtElement lv3 = (NbtElement)var8.next();
            if (!(lv3 instanceof AbstractNbtList)) {
               throw NbtPathArgumentType.EXPECTED_LIST_EXCEPTION.create(lv3);
            }

            AbstractNbtList lv4 = (AbstractNbtList)lv3;
            bl2 = false;
            int k = index < 0 ? lv4.size() + index + 1 : index;
            Iterator var13 = list2.iterator();

            while(var13.hasNext()) {
               NbtElement lv5 = (NbtElement)var13.next();

               try {
                  if (lv4.addElement(k, bl ? lv5.copy() : lv5)) {
                     ++k;
                     bl2 = true;
                  }
               } catch (IndexOutOfBoundsException var16) {
                  throw NbtPathArgumentType.INVALID_INDEX_EXCEPTION.create(k);
               }
            }

            bl = true;
         }

         return j;
      }

      public int remove(NbtElement element) {
         List list = Collections.singletonList(element);

         for(int i = 0; i < this.nodes.length - 1; ++i) {
            list = this.nodes[i].get(list);
         }

         PathNode lv = this.nodes[this.nodes.length - 1];
         Objects.requireNonNull(lv);
         return forEach(list, lv::clear);
      }

      private CommandSyntaxException createNothingFoundException(PathNode node) {
         int i = this.nodeEndIndices.getInt(node);
         return NbtPathArgumentType.NOTHING_FOUND_EXCEPTION.create(this.string.substring(0, i));
      }

      public String toString() {
         return this.string;
      }
   }

   private interface PathNode {
      void get(NbtElement current, List results);

      void getOrInit(NbtElement current, Supplier source, List results);

      NbtElement init();

      int set(NbtElement current, Supplier source);

      int clear(NbtElement current);

      default List get(List elements) {
         return this.process(elements, this::get);
      }

      default List getOrInit(List elements, Supplier supplier) {
         return this.process(elements, (current, results) -> {
            this.getOrInit(current, supplier, results);
         });
      }

      default List process(List elements, BiConsumer action) {
         List list2 = Lists.newArrayList();
         Iterator var4 = elements.iterator();

         while(var4.hasNext()) {
            NbtElement lv = (NbtElement)var4.next();
            action.accept(lv, list2);
         }

         return list2;
      }
   }

   static class FilteredRootNode implements PathNode {
      private final Predicate matcher;

      public FilteredRootNode(NbtCompound filter) {
         this.matcher = NbtPathArgumentType.getPredicate(filter);
      }

      public void get(NbtElement current, List results) {
         if (current instanceof NbtCompound && this.matcher.test(current)) {
            results.add(current);
         }

      }

      public void getOrInit(NbtElement current, Supplier source, List results) {
         this.get(current, results);
      }

      public NbtElement init() {
         return new NbtCompound();
      }

      public int set(NbtElement current, Supplier source) {
         return 0;
      }

      public int clear(NbtElement current) {
         return 0;
      }
   }

   static class FilteredListElementNode implements PathNode {
      private final NbtCompound filter;
      private final Predicate predicate;

      public FilteredListElementNode(NbtCompound filter) {
         this.filter = filter;
         this.predicate = NbtPathArgumentType.getPredicate(filter);
      }

      public void get(NbtElement current, List results) {
         if (current instanceof NbtList lv) {
            Stream var10000 = lv.stream().filter(this.predicate);
            Objects.requireNonNull(results);
            var10000.forEach(results::add);
         }

      }

      public void getOrInit(NbtElement current, Supplier source, List results) {
         MutableBoolean mutableBoolean = new MutableBoolean();
         if (current instanceof NbtList lv) {
            lv.stream().filter(this.predicate).forEach((nbt) -> {
               results.add(nbt);
               mutableBoolean.setTrue();
            });
            if (mutableBoolean.isFalse()) {
               NbtCompound lv2 = this.filter.copy();
               lv.add(lv2);
               results.add(lv2);
            }
         }

      }

      public NbtElement init() {
         return new NbtList();
      }

      public int set(NbtElement current, Supplier source) {
         int i = 0;
         if (current instanceof NbtList lv) {
            int j = lv.size();
            if (j == 0) {
               lv.add((NbtElement)source.get());
               ++i;
            } else {
               for(int k = 0; k < j; ++k) {
                  NbtElement lv2 = lv.get(k);
                  if (this.predicate.test(lv2)) {
                     NbtElement lv3 = (NbtElement)source.get();
                     if (!lv3.equals(lv2) && lv.setElement(k, lv3)) {
                        ++i;
                     }
                  }
               }
            }
         }

         return i;
      }

      public int clear(NbtElement current) {
         int i = 0;
         if (current instanceof NbtList lv) {
            for(int j = lv.size() - 1; j >= 0; --j) {
               if (this.predicate.test(lv.get(j))) {
                  lv.remove(j);
                  ++i;
               }
            }
         }

         return i;
      }
   }

   private static class AllListElementNode implements PathNode {
      public static final AllListElementNode INSTANCE = new AllListElementNode();

      public void get(NbtElement current, List results) {
         if (current instanceof AbstractNbtList) {
            results.addAll((AbstractNbtList)current);
         }

      }

      public void getOrInit(NbtElement current, Supplier source, List results) {
         if (current instanceof AbstractNbtList lv) {
            if (lv.isEmpty()) {
               NbtElement lv2 = (NbtElement)source.get();
               if (lv.addElement(0, lv2)) {
                  results.add(lv2);
               }
            } else {
               results.addAll(lv);
            }
         }

      }

      public NbtElement init() {
         return new NbtList();
      }

      public int set(NbtElement current, Supplier source) {
         if (!(current instanceof AbstractNbtList lv)) {
            return 0;
         } else {
            int i = lv.size();
            if (i == 0) {
               lv.addElement(0, (NbtElement)source.get());
               return 1;
            } else {
               NbtElement lv2 = (NbtElement)source.get();
               Stream var10001 = lv.stream();
               Objects.requireNonNull(lv2);
               int j = i - (int)var10001.filter(lv2::equals).count();
               if (j == 0) {
                  return 0;
               } else {
                  lv.clear();
                  if (!lv.addElement(0, lv2)) {
                     return 0;
                  } else {
                     for(int k = 1; k < i; ++k) {
                        lv.addElement(k, (NbtElement)source.get());
                     }

                     return j;
                  }
               }
            }
         }
      }

      public int clear(NbtElement current) {
         if (current instanceof AbstractNbtList lv) {
            int i = lv.size();
            if (i > 0) {
               lv.clear();
               return i;
            }
         }

         return 0;
      }
   }

   private static class IndexedListElementNode implements PathNode {
      private final int index;

      public IndexedListElementNode(int index) {
         this.index = index;
      }

      public void get(NbtElement current, List results) {
         if (current instanceof AbstractNbtList lv) {
            int i = lv.size();
            int j = this.index < 0 ? i + this.index : this.index;
            if (0 <= j && j < i) {
               results.add((NbtElement)lv.get(j));
            }
         }

      }

      public void getOrInit(NbtElement current, Supplier source, List results) {
         this.get(current, results);
      }

      public NbtElement init() {
         return new NbtList();
      }

      public int set(NbtElement current, Supplier source) {
         if (current instanceof AbstractNbtList lv) {
            int i = lv.size();
            int j = this.index < 0 ? i + this.index : this.index;
            if (0 <= j && j < i) {
               NbtElement lv2 = (NbtElement)lv.get(j);
               NbtElement lv3 = (NbtElement)source.get();
               if (!lv3.equals(lv2) && lv.setElement(j, lv3)) {
                  return 1;
               }
            }
         }

         return 0;
      }

      public int clear(NbtElement current) {
         if (current instanceof AbstractNbtList lv) {
            int i = lv.size();
            int j = this.index < 0 ? i + this.index : this.index;
            if (0 <= j && j < i) {
               lv.remove(j);
               return 1;
            }
         }

         return 0;
      }
   }

   static class FilteredNamedNode implements PathNode {
      private final String name;
      private final NbtCompound filter;
      private final Predicate predicate;

      public FilteredNamedNode(String name, NbtCompound filter) {
         this.name = name;
         this.filter = filter;
         this.predicate = NbtPathArgumentType.getPredicate(filter);
      }

      public void get(NbtElement current, List results) {
         if (current instanceof NbtCompound) {
            NbtElement lv = ((NbtCompound)current).get(this.name);
            if (this.predicate.test(lv)) {
               results.add(lv);
            }
         }

      }

      public void getOrInit(NbtElement current, Supplier source, List results) {
         if (current instanceof NbtCompound lv) {
            NbtElement lv2 = lv.get(this.name);
            if (lv2 == null) {
               NbtElement lv2 = this.filter.copy();
               lv.put(this.name, lv2);
               results.add(lv2);
            } else if (this.predicate.test(lv2)) {
               results.add(lv2);
            }
         }

      }

      public NbtElement init() {
         return new NbtCompound();
      }

      public int set(NbtElement current, Supplier source) {
         if (current instanceof NbtCompound lv) {
            NbtElement lv2 = lv.get(this.name);
            if (this.predicate.test(lv2)) {
               NbtElement lv3 = (NbtElement)source.get();
               if (!lv3.equals(lv2)) {
                  lv.put(this.name, lv3);
                  return 1;
               }
            }
         }

         return 0;
      }

      public int clear(NbtElement current) {
         if (current instanceof NbtCompound lv) {
            NbtElement lv2 = lv.get(this.name);
            if (this.predicate.test(lv2)) {
               lv.remove(this.name);
               return 1;
            }
         }

         return 0;
      }
   }

   private static class NamedNode implements PathNode {
      private final String name;

      public NamedNode(String name) {
         this.name = name;
      }

      public void get(NbtElement current, List results) {
         if (current instanceof NbtCompound) {
            NbtElement lv = ((NbtCompound)current).get(this.name);
            if (lv != null) {
               results.add(lv);
            }
         }

      }

      public void getOrInit(NbtElement current, Supplier source, List results) {
         if (current instanceof NbtCompound lv) {
            NbtElement lv2;
            if (lv.contains(this.name)) {
               lv2 = lv.get(this.name);
            } else {
               lv2 = (NbtElement)source.get();
               lv.put(this.name, lv2);
            }

            results.add(lv2);
         }

      }

      public NbtElement init() {
         return new NbtCompound();
      }

      public int set(NbtElement current, Supplier source) {
         if (current instanceof NbtCompound lv) {
            NbtElement lv2 = (NbtElement)source.get();
            NbtElement lv3 = lv.put(this.name, lv2);
            if (!lv2.equals(lv3)) {
               return 1;
            }
         }

         return 0;
      }

      public int clear(NbtElement current) {
         if (current instanceof NbtCompound lv) {
            if (lv.contains(this.name)) {
               lv.remove(this.name);
               return 1;
            }
         }

         return 0;
      }
   }
}
