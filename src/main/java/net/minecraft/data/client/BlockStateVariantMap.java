package net.minecraft.data.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.state.property.Property;

public abstract class BlockStateVariantMap {
   private final Map variants = Maps.newHashMap();

   protected void register(PropertiesMap condition, List possibleVariants) {
      List list2 = (List)this.variants.put(condition, possibleVariants);
      if (list2 != null) {
         throw new IllegalStateException("Value " + condition + " is already defined");
      }
   }

   Map getVariants() {
      this.checkAllPropertyDefinitions();
      return ImmutableMap.copyOf(this.variants);
   }

   private void checkAllPropertyDefinitions() {
      List list = this.getProperties();
      Stream stream = Stream.of(PropertiesMap.empty());

      Property lv;
      for(Iterator var3 = list.iterator(); var3.hasNext(); stream = stream.flatMap((propertiesMap) -> {
         Stream var10000 = lv.stream();
         Objects.requireNonNull(propertiesMap);
         return var10000.map(propertiesMap::withValue);
      })) {
         lv = (Property)var3.next();
      }

      List list2 = (List)stream.filter((propertiesMap) -> {
         return !this.variants.containsKey(propertiesMap);
      }).collect(Collectors.toList());
      if (!list2.isEmpty()) {
         throw new IllegalStateException("Missing definition for properties: " + list2);
      }
   }

   abstract List getProperties();

   public static SingleProperty create(Property property) {
      return new SingleProperty(property);
   }

   public static DoubleProperty create(Property first, Property second) {
      return new DoubleProperty(first, second);
   }

   public static TripleProperty create(Property first, Property second, Property third) {
      return new TripleProperty(first, second, third);
   }

   public static QuadrupleProperty create(Property first, Property second, Property third, Property fourth) {
      return new QuadrupleProperty(first, second, third, fourth);
   }

   public static QuintupleProperty create(Property first, Property second, Property third, Property fourth, Property fifth) {
      return new QuintupleProperty(first, second, third, fourth, fifth);
   }

   public static class SingleProperty extends BlockStateVariantMap {
      private final Property property;

      SingleProperty(Property property) {
         this.property = property;
      }

      public List getProperties() {
         return ImmutableList.of(this.property);
      }

      public SingleProperty register(Comparable value, List variants) {
         PropertiesMap lv = PropertiesMap.withValues(this.property.createValue(value));
         this.register((PropertiesMap)lv, (List)variants);
         return this;
      }

      public SingleProperty register(Comparable value, BlockStateVariant variant) {
         return this.register(value, Collections.singletonList(variant));
      }

      public BlockStateVariantMap register(Function variantFactory) {
         this.property.getValues().forEach((value) -> {
            this.register(value, (BlockStateVariant)variantFactory.apply(value));
         });
         return this;
      }

      public BlockStateVariantMap registerVariants(Function variantFactory) {
         this.property.getValues().forEach((value) -> {
            this.register(value, (List)variantFactory.apply(value));
         });
         return this;
      }
   }

   public static class DoubleProperty extends BlockStateVariantMap {
      private final Property first;
      private final Property second;

      DoubleProperty(Property first, Property second) {
         this.first = first;
         this.second = second;
      }

      public List getProperties() {
         return ImmutableList.of(this.first, this.second);
      }

      public DoubleProperty register(Comparable firstValue, Comparable secondValue, List variants) {
         PropertiesMap lv = PropertiesMap.withValues(this.first.createValue(firstValue), this.second.createValue(secondValue));
         this.register(lv, variants);
         return this;
      }

      public DoubleProperty register(Comparable firstValue, Comparable secondValue, BlockStateVariant variant) {
         return this.register(firstValue, secondValue, Collections.singletonList(variant));
      }

      public BlockStateVariantMap register(BiFunction variantFactory) {
         this.first.getValues().forEach((firstValue) -> {
            this.second.getValues().forEach((secondValue) -> {
               this.register(firstValue, secondValue, (BlockStateVariant)variantFactory.apply(firstValue, secondValue));
            });
         });
         return this;
      }

      public BlockStateVariantMap registerVariants(BiFunction variantsFactory) {
         this.first.getValues().forEach((firstValue) -> {
            this.second.getValues().forEach((secondValue) -> {
               this.register(firstValue, secondValue, (List)variantsFactory.apply(firstValue, secondValue));
            });
         });
         return this;
      }
   }

   public static class TripleProperty extends BlockStateVariantMap {
      private final Property first;
      private final Property second;
      private final Property third;

      TripleProperty(Property first, Property second, Property third) {
         this.first = first;
         this.second = second;
         this.third = third;
      }

      public List getProperties() {
         return ImmutableList.of(this.first, this.second, this.third);
      }

      public TripleProperty register(Comparable firstValue, Comparable secondValue, Comparable thirdValue, List variants) {
         PropertiesMap lv = PropertiesMap.withValues(this.first.createValue(firstValue), this.second.createValue(secondValue), this.third.createValue(thirdValue));
         this.register(lv, variants);
         return this;
      }

      public TripleProperty register(Comparable firstValue, Comparable secondValue, Comparable thirdValue, BlockStateVariant variant) {
         return this.register(firstValue, secondValue, thirdValue, Collections.singletonList(variant));
      }

      public BlockStateVariantMap register(TriFunction variantFactory) {
         this.first.getValues().forEach((firstValue) -> {
            this.second.getValues().forEach((secondValue) -> {
               this.third.getValues().forEach((thirdValue) -> {
                  this.register(firstValue, secondValue, thirdValue, (BlockStateVariant)variantFactory.apply(firstValue, secondValue, thirdValue));
               });
            });
         });
         return this;
      }

      public BlockStateVariantMap registerVariants(TriFunction variantFactory) {
         this.first.getValues().forEach((firstValue) -> {
            this.second.getValues().forEach((secondValue) -> {
               this.third.getValues().forEach((thirdValue) -> {
                  this.register(firstValue, secondValue, thirdValue, (List)variantFactory.apply(firstValue, secondValue, thirdValue));
               });
            });
         });
         return this;
      }
   }

   public static class QuadrupleProperty extends BlockStateVariantMap {
      private final Property first;
      private final Property second;
      private final Property third;
      private final Property fourth;

      QuadrupleProperty(Property first, Property second, Property third, Property fourth) {
         this.first = first;
         this.second = second;
         this.third = third;
         this.fourth = fourth;
      }

      public List getProperties() {
         return ImmutableList.of(this.first, this.second, this.third, this.fourth);
      }

      public QuadrupleProperty register(Comparable firstValue, Comparable secondValue, Comparable thirdValue, Comparable fourthValue, List variants) {
         PropertiesMap lv = PropertiesMap.withValues(this.first.createValue(firstValue), this.second.createValue(secondValue), this.third.createValue(thirdValue), this.fourth.createValue(fourthValue));
         this.register(lv, variants);
         return this;
      }

      public QuadrupleProperty register(Comparable firstValue, Comparable secondValue, Comparable thirdValue, Comparable fourthValue, BlockStateVariant variant) {
         return this.register(firstValue, secondValue, thirdValue, fourthValue, Collections.singletonList(variant));
      }

      public BlockStateVariantMap register(QuadFunction variantFactory) {
         this.first.getValues().forEach((firstValue) -> {
            this.second.getValues().forEach((secondValue) -> {
               this.third.getValues().forEach((thirdValue) -> {
                  this.fourth.getValues().forEach((fourthValue) -> {
                     this.register(firstValue, secondValue, thirdValue, fourthValue, (BlockStateVariant)variantFactory.apply(firstValue, secondValue, thirdValue, fourthValue));
                  });
               });
            });
         });
         return this;
      }

      public BlockStateVariantMap registerVariants(QuadFunction variantFactory) {
         this.first.getValues().forEach((firstValue) -> {
            this.second.getValues().forEach((secondValue) -> {
               this.third.getValues().forEach((thirdValue) -> {
                  this.fourth.getValues().forEach((fourthValue) -> {
                     this.register(firstValue, secondValue, thirdValue, fourthValue, (List)variantFactory.apply(firstValue, secondValue, thirdValue, fourthValue));
                  });
               });
            });
         });
         return this;
      }
   }

   public static class QuintupleProperty extends BlockStateVariantMap {
      private final Property first;
      private final Property second;
      private final Property third;
      private final Property fourth;
      private final Property fifth;

      QuintupleProperty(Property first, Property second, Property third, Property fourth, Property fifth) {
         this.first = first;
         this.second = second;
         this.third = third;
         this.fourth = fourth;
         this.fifth = fifth;
      }

      public List getProperties() {
         return ImmutableList.of(this.first, this.second, this.third, this.fourth, this.fifth);
      }

      public QuintupleProperty register(Comparable firstValue, Comparable secondValue, Comparable thirdValue, Comparable fourthValue, Comparable fifthValue, List variants) {
         PropertiesMap lv = PropertiesMap.withValues(this.first.createValue(firstValue), this.second.createValue(secondValue), this.third.createValue(thirdValue), this.fourth.createValue(fourthValue), this.fifth.createValue(fifthValue));
         this.register(lv, variants);
         return this;
      }

      public QuintupleProperty register(Comparable firstValue, Comparable secondValue, Comparable thirdValue, Comparable fourthValue, Comparable fifthValue, BlockStateVariant variant) {
         return this.register(firstValue, secondValue, thirdValue, fourthValue, fifthValue, Collections.singletonList(variant));
      }

      public BlockStateVariantMap register(QuintFunction variantFactory) {
         this.first.getValues().forEach((firstValue) -> {
            this.second.getValues().forEach((secondValue) -> {
               this.third.getValues().forEach((thirdValue) -> {
                  this.fourth.getValues().forEach((fourthValue) -> {
                     this.fifth.getValues().forEach((fifthValue) -> {
                        this.register(firstValue, secondValue, thirdValue, fourthValue, fifthValue, (BlockStateVariant)variantFactory.apply(firstValue, secondValue, thirdValue, fourthValue, fifthValue));
                     });
                  });
               });
            });
         });
         return this;
      }

      public BlockStateVariantMap registerVariants(QuintFunction variantFactory) {
         this.first.getValues().forEach((firstValue) -> {
            this.second.getValues().forEach((secondValue) -> {
               this.third.getValues().forEach((thirdValue) -> {
                  this.fourth.getValues().forEach((fourthValue) -> {
                     this.fifth.getValues().forEach((fifthValue) -> {
                        this.register(firstValue, secondValue, thirdValue, fourthValue, fifthValue, (List)variantFactory.apply(firstValue, secondValue, thirdValue, fourthValue, fifthValue));
                     });
                  });
               });
            });
         });
         return this;
      }
   }

   @FunctionalInterface
   public interface QuintFunction {
      Object apply(Object one, Object two, Object three, Object four, Object five);
   }

   @FunctionalInterface
   public interface QuadFunction {
      Object apply(Object one, Object two, Object three, Object four);
   }

   @FunctionalInterface
   public interface TriFunction {
      Object apply(Object one, Object two, Object three);
   }
}
