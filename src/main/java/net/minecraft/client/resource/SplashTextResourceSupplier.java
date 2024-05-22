/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.resource;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.session.Session;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SplashTextResourceSupplier
extends SinglePreparationResourceReloader<List<String>> {
    private static final Identifier RESOURCE_ID = Identifier.method_60656("texts/splashes.txt");
    private static final Random RANDOM = Random.create();
    private final List<String> splashTexts = Lists.newArrayList();
    private final Session session;

    public SplashTextResourceSupplier(Session session) {
        this.session = session;
    }

    @Override
    protected List<String> prepare(ResourceManager arg, Profiler arg2) {
        List<String> list;
        block8: {
            BufferedReader bufferedReader = MinecraftClient.getInstance().getResourceManager().openAsReader(RESOURCE_ID);
            try {
                list = bufferedReader.lines().map(String::trim).filter(splashText -> splashText.hashCode() != 125780783).collect(Collectors.toList());
                if (bufferedReader == null) break block8;
            } catch (Throwable throwable) {
                try {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (IOException iOException) {
                    return Collections.emptyList();
                }
            }
            bufferedReader.close();
        }
        return list;
    }

    @Override
    protected void apply(List<String> list, ResourceManager arg, Profiler arg2) {
        this.splashTexts.clear();
        this.splashTexts.addAll(list);
    }

    @Nullable
    public SplashTextRenderer get() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (calendar.get(2) + 1 == 12 && calendar.get(5) == 24) {
            return SplashTextRenderer.MERRY_X_MAS_;
        }
        if (calendar.get(2) + 1 == 1 && calendar.get(5) == 1) {
            return SplashTextRenderer.HAPPY_NEW_YEAR_;
        }
        if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31) {
            return SplashTextRenderer.OOOOO_O_O_OOOOO__SPOOKY_;
        }
        if (this.splashTexts.isEmpty()) {
            return null;
        }
        if (this.session != null && RANDOM.nextInt(this.splashTexts.size()) == 42) {
            return new SplashTextRenderer(this.session.getUsername().toUpperCase(Locale.ROOT) + " IS YOU");
        }
        return new SplashTextRenderer(this.splashTexts.get(RANDOM.nextInt(this.splashTexts.size())));
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager manager, Profiler profiler) {
        return this.prepare(manager, profiler);
    }
}

