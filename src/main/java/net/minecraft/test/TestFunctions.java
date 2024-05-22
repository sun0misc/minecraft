/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.StructureTestUtil;
import net.minecraft.test.TestFunction;
import net.minecraft.util.BlockRotation;

public class TestFunctions {
    private static final Collection<TestFunction> TEST_FUNCTIONS = Lists.newArrayList();
    private static final Set<String> TEST_CLASSES = Sets.newHashSet();
    private static final Map<String, Consumer<ServerWorld>> BEFORE_BATCH_CONSUMERS = Maps.newHashMap();
    private static final Map<String, Consumer<ServerWorld>> AFTER_BATCH_CONSUMERS = Maps.newHashMap();
    private static final Set<TestFunction> FAILED_TEST_FUNCTIONS = Sets.newHashSet();

    public static void register(Class<?> testClass) {
        Arrays.stream(testClass.getDeclaredMethods()).sorted(Comparator.comparing(Method::getName)).forEach(TestFunctions::register);
    }

    public static void register(Method method) {
        CustomTestProvider lv2;
        String string = method.getDeclaringClass().getSimpleName();
        GameTest lv = method.getAnnotation(GameTest.class);
        if (lv != null) {
            TEST_FUNCTIONS.add(TestFunctions.getTestFunction(method));
            TEST_CLASSES.add(string);
        }
        if ((lv2 = method.getAnnotation(CustomTestProvider.class)) != null) {
            TEST_FUNCTIONS.addAll(TestFunctions.getCustomTestFunctions(method));
            TEST_CLASSES.add(string);
        }
        TestFunctions.registerBatchConsumers(method, BeforeBatch.class, BeforeBatch::batchId, BEFORE_BATCH_CONSUMERS);
        TestFunctions.registerBatchConsumers(method, AfterBatch.class, AfterBatch::batchId, AFTER_BATCH_CONSUMERS);
    }

    private static <T extends Annotation> void registerBatchConsumers(Method method, Class<T> clazz, Function<T, String> batchIdFunction, Map<String, Consumer<ServerWorld>> batchConsumerMap) {
        String string;
        Consumer<?> consumer;
        T annotation = method.getAnnotation(clazz);
        if (annotation != null && (consumer = batchConsumerMap.putIfAbsent(string = batchIdFunction.apply(annotation), TestFunctions.getInvoker(method))) != null) {
            throw new RuntimeException("Hey, there should only be one " + String.valueOf(clazz) + " method per batch. Batch '" + string + "' has more than one!");
        }
    }

    public static Stream<TestFunction> getTestFunctions(String testClass) {
        return TEST_FUNCTIONS.stream().filter(testFunction -> TestFunctions.isInClass(testFunction, testClass));
    }

    public static Collection<TestFunction> getTestFunctions() {
        return TEST_FUNCTIONS;
    }

    public static Collection<String> getTestClasses() {
        return TEST_CLASSES;
    }

    public static boolean testClassExists(String testClass) {
        return TEST_CLASSES.contains(testClass);
    }

    public static Consumer<ServerWorld> getBeforeBatchConsumer(String batchId) {
        return BEFORE_BATCH_CONSUMERS.getOrDefault(batchId, world -> {});
    }

    public static Consumer<ServerWorld> getAfterBatchConsumer(String batchId) {
        return AFTER_BATCH_CONSUMERS.getOrDefault(batchId, world -> {});
    }

    public static Optional<TestFunction> getTestFunction(String structurePath) {
        return TestFunctions.getTestFunctions().stream().filter(testFunction -> testFunction.templatePath().equalsIgnoreCase(structurePath)).findFirst();
    }

    public static TestFunction getTestFunctionOrThrow(String structurePath) {
        Optional<TestFunction> optional = TestFunctions.getTestFunction(structurePath);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Can't find the test function for " + structurePath);
        }
        return optional.get();
    }

    private static Collection<TestFunction> getCustomTestFunctions(Method method) {
        try {
            Object object = method.getDeclaringClass().newInstance();
            return (Collection)method.invoke(object, new Object[0]);
        } catch (ReflectiveOperationException reflectiveOperationException) {
            throw new RuntimeException(reflectiveOperationException);
        }
    }

    private static TestFunction getTestFunction(Method method) {
        GameTest lv = method.getAnnotation(GameTest.class);
        String string = method.getDeclaringClass().getSimpleName();
        String string2 = string.toLowerCase();
        String string3 = string2 + "." + method.getName().toLowerCase();
        String string4 = lv.templateName().isEmpty() ? string3 : string2 + "." + lv.templateName();
        String string5 = lv.batchId();
        BlockRotation lv2 = StructureTestUtil.getRotation(lv.rotation());
        return new TestFunction(string5, string3, string4, lv2, lv.tickLimit(), lv.duration(), lv.required(), lv.manualOnly(), lv.requiredSuccesses(), lv.maxAttempts(), lv.skyAccess(), TestFunctions.getInvoker(method));
    }

    private static Consumer<?> getInvoker(Method method) {
        return args -> {
            try {
                Object object2 = method.getDeclaringClass().newInstance();
                method.invoke(object2, args);
            } catch (InvocationTargetException invocationTargetException) {
                if (invocationTargetException.getCause() instanceof RuntimeException) {
                    throw (RuntimeException)invocationTargetException.getCause();
                }
                throw new RuntimeException(invocationTargetException.getCause());
            } catch (ReflectiveOperationException reflectiveOperationException) {
                throw new RuntimeException(reflectiveOperationException);
            }
        };
    }

    private static boolean isInClass(TestFunction testFunction, String testClass) {
        return testFunction.templatePath().toLowerCase().startsWith(testClass.toLowerCase() + ".");
    }

    public static Stream<TestFunction> getFailedTestFunctions() {
        return FAILED_TEST_FUNCTIONS.stream();
    }

    public static void addFailedTestFunction(TestFunction testFunction) {
        FAILED_TEST_FUNCTIONS.add(testFunction);
    }

    public static void clearFailedTestFunctions() {
        FAILED_TEST_FUNCTIONS.clear();
    }
}

