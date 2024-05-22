/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface GameTest {
    public int tickLimit() default 100;

    public String batchId() default "defaultBatch";

    public boolean skyAccess() default false;

    public int rotation() default 0;

    public boolean required() default true;

    public boolean manualOnly() default false;

    public String templateName() default "";

    public long duration() default 0L;

    public int maxAttempts() default 1;

    public int requiredSuccesses() default 1;
}

