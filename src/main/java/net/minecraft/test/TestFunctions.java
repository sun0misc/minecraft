package net.minecraft.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.util.BlockRotation;
import org.jetbrains.annotations.Nullable;

public class TestFunctions {
   private static final Collection TEST_FUNCTIONS = Lists.newArrayList();
   private static final Set TEST_CLASSES = Sets.newHashSet();
   private static final Map BEFORE_BATCH_CONSUMERS = Maps.newHashMap();
   private static final Map AFTER_BATCH_CONSUMERS = Maps.newHashMap();
   private static final Collection FAILED_TEST_FUNCTIONS = Sets.newHashSet();

   public static void register(Class testClass) {
      Arrays.stream(testClass.getDeclaredMethods()).forEach(TestFunctions::register);
   }

   public static void register(Method method) {
      String string = method.getDeclaringClass().getSimpleName();
      GameTest lv = (GameTest)method.getAnnotation(GameTest.class);
      if (lv != null) {
         TEST_FUNCTIONS.add(getTestFunction(method));
         TEST_CLASSES.add(string);
      }

      CustomTestProvider lv2 = (CustomTestProvider)method.getAnnotation(CustomTestProvider.class);
      if (lv2 != null) {
         TEST_FUNCTIONS.addAll(getCustomTestFunctions(method));
         TEST_CLASSES.add(string);
      }

      registerBatchConsumers(method, BeforeBatch.class, BeforeBatch::batchId, BEFORE_BATCH_CONSUMERS);
      registerBatchConsumers(method, AfterBatch.class, AfterBatch::batchId, AFTER_BATCH_CONSUMERS);
   }

   private static void registerBatchConsumers(Method method, Class clazz, Function batchIdFunction, Map batchConsumerMap) {
      Annotation annotation = method.getAnnotation(clazz);
      if (annotation != null) {
         String string = (String)batchIdFunction.apply(annotation);
         Consumer consumer = (Consumer)batchConsumerMap.putIfAbsent(string, getInvoker(method));
         if (consumer != null) {
            throw new RuntimeException("Hey, there should only be one " + clazz + " method per batch. Batch '" + string + "' has more than one!");
         }
      }

   }

   public static Collection getTestFunctions(String testClass) {
      return (Collection)TEST_FUNCTIONS.stream().filter((testFunction) -> {
         return isInClass(testFunction, testClass);
      }).collect(Collectors.toList());
   }

   public static Collection getTestFunctions() {
      return TEST_FUNCTIONS;
   }

   public static Collection getTestClasses() {
      return TEST_CLASSES;
   }

   public static boolean testClassExists(String testClass) {
      return TEST_CLASSES.contains(testClass);
   }

   @Nullable
   public static Consumer getBeforeBatchConsumer(String batchId) {
      return (Consumer)BEFORE_BATCH_CONSUMERS.get(batchId);
   }

   @Nullable
   public static Consumer getAfterBatchConsumer(String batchId) {
      return (Consumer)AFTER_BATCH_CONSUMERS.get(batchId);
   }

   public static Optional getTestFunction(String structurePath) {
      return getTestFunctions().stream().filter((testFunction) -> {
         return testFunction.getTemplatePath().equalsIgnoreCase(structurePath);
      }).findFirst();
   }

   public static TestFunction getTestFunctionOrThrow(String structurePath) {
      Optional optional = getTestFunction(structurePath);
      if (!optional.isPresent()) {
         throw new IllegalArgumentException("Can't find the test function for " + structurePath);
      } else {
         return (TestFunction)optional.get();
      }
   }

   private static Collection getCustomTestFunctions(Method method) {
      try {
         Object object = method.getDeclaringClass().newInstance();
         return (Collection)method.invoke(object);
      } catch (ReflectiveOperationException var2) {
         throw new RuntimeException(var2);
      }
   }

   private static TestFunction getTestFunction(Method method) {
      GameTest lv = (GameTest)method.getAnnotation(GameTest.class);
      String string = method.getDeclaringClass().getSimpleName();
      String string2 = string.toLowerCase();
      String string3 = string2 + "." + method.getName().toLowerCase();
      String string4 = lv.templateName().isEmpty() ? string3 : string2 + "." + lv.templateName();
      String string5 = lv.batchId();
      BlockRotation lv2 = StructureTestUtil.getRotation(lv.rotation());
      return new TestFunction(string5, string3, string4, lv2, lv.tickLimit(), lv.duration(), lv.required(), lv.requiredSuccesses(), lv.maxAttempts(), getInvoker(method));
   }

   private static Consumer getInvoker(Method method) {
      return (args) -> {
         try {
            Object object2 = method.getDeclaringClass().newInstance();
            method.invoke(object2, args);
         } catch (InvocationTargetException var3) {
            if (var3.getCause() instanceof RuntimeException) {
               throw (RuntimeException)var3.getCause();
            } else {
               throw new RuntimeException(var3.getCause());
            }
         } catch (ReflectiveOperationException var4) {
            throw new RuntimeException(var4);
         }
      };
   }

   private static boolean isInClass(TestFunction testFunction, String testClass) {
      return testFunction.getTemplatePath().toLowerCase().startsWith(testClass.toLowerCase() + ".");
   }

   public static Collection getFailedTestFunctions() {
      return FAILED_TEST_FUNCTIONS;
   }

   public static void addFailedTestFunction(TestFunction testFunction) {
      FAILED_TEST_FUNCTIONS.add(testFunction);
   }

   public static void clearFailedTestFunctions() {
      FAILED_TEST_FUNCTIONS.clear();
   }
}
