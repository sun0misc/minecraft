/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.toast;

import com.google.common.collect.Queues;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ToastManager {
    private static final int SPACES = 5;
    private static final int ALL_OCCUPIED = -1;
    final MinecraftClient client;
    private final List<Entry<?>> visibleEntries = new ArrayList();
    private final BitSet occupiedSpaces = new BitSet(5);
    private final Deque<Toast> toastQueue = Queues.newArrayDeque();

    public ToastManager(MinecraftClient client) {
        this.client = client;
    }

    public void draw(DrawContext context) {
        if (this.client.options.hudHidden) {
            return;
        }
        int i = context.getScaledWindowWidth();
        this.visibleEntries.removeIf(visibleEntry -> {
            if (visibleEntry != null && visibleEntry.draw(i, context)) {
                this.occupiedSpaces.clear(visibleEntry.topIndex, visibleEntry.topIndex + visibleEntry.requiredSpaceCount);
                return true;
            }
            return false;
        });
        if (!this.toastQueue.isEmpty() && this.getEmptySpaceCount() > 0) {
            this.toastQueue.removeIf(toast -> {
                int i = toast.getRequiredSpaceCount();
                int j = this.getTopIndex(i);
                if (j != -1) {
                    this.visibleEntries.add(new Entry(this, toast, j, i));
                    this.occupiedSpaces.set(j, j + i);
                    return true;
                }
                return false;
            });
        }
    }

    private int getTopIndex(int requiredSpaces) {
        if (this.getEmptySpaceCount() >= requiredSpaces) {
            int j = 0;
            for (int k = 0; k < 5; ++k) {
                if (this.occupiedSpaces.get(k)) {
                    j = 0;
                    continue;
                }
                if (++j != requiredSpaces) continue;
                return k + 1 - j;
            }
        }
        return -1;
    }

    private int getEmptySpaceCount() {
        return 5 - this.occupiedSpaces.cardinality();
    }

    @Nullable
    public <T extends Toast> T getToast(Class<? extends T> toastClass, Object type) {
        for (Entry<?> lv : this.visibleEntries) {
            if (lv == null || !toastClass.isAssignableFrom(lv.getInstance().getClass()) || !lv.getInstance().getType().equals(type)) continue;
            return (T)lv.getInstance();
        }
        for (Toast lv2 : this.toastQueue) {
            if (!toastClass.isAssignableFrom(lv2.getClass()) || !lv2.getType().equals(type)) continue;
            return (T)lv2;
        }
        return null;
    }

    public void clear() {
        this.occupiedSpaces.clear();
        this.visibleEntries.clear();
        this.toastQueue.clear();
    }

    public void add(Toast toast) {
        this.toastQueue.add(toast);
    }

    public MinecraftClient getClient() {
        return this.client;
    }

    public double getNotificationDisplayTimeMultiplier() {
        return this.client.options.getNotificationDisplayTime().getValue();
    }

    @Environment(value=EnvType.CLIENT)
    class Entry<T extends Toast> {
        private static final long DISAPPEAR_TIME = 600L;
        private final T instance;
        final int topIndex;
        final int requiredSpaceCount;
        private long startTime = -1L;
        private long showTime = -1L;
        private Toast.Visibility visibility = Toast.Visibility.SHOW;
        final /* synthetic */ ToastManager field_2245;

        /*
         * WARNING - Possible parameter corruption
         */
        Entry(T instance, int topIndex, int requiredSpaceCount) {
            this.field_2245 = (ToastManager)arg;
            this.instance = instance;
            this.topIndex = topIndex;
            this.requiredSpaceCount = requiredSpaceCount;
        }

        public T getInstance() {
            return this.instance;
        }

        private float getDisappearProgress(long time) {
            float f = MathHelper.clamp((float)(time - this.startTime) / 600.0f, 0.0f, 1.0f);
            f *= f;
            if (this.visibility == Toast.Visibility.HIDE) {
                return 1.0f - f;
            }
            return f;
        }

        public boolean draw(int x, DrawContext context) {
            long l = Util.getMeasuringTimeMs();
            if (this.startTime == -1L) {
                this.startTime = l;
                this.visibility.playSound(this.field_2245.client.getSoundManager());
            }
            if (this.visibility == Toast.Visibility.SHOW && l - this.startTime <= 600L) {
                this.showTime = l;
            }
            context.getMatrices().push();
            context.getMatrices().translate((float)x - (float)this.instance.getWidth() * this.getDisappearProgress(l), this.topIndex * 32, 800.0f);
            Toast.Visibility lv = this.instance.draw(context, this.field_2245, l - this.showTime);
            context.getMatrices().pop();
            if (lv != this.visibility) {
                this.startTime = l - (long)((int)((1.0f - this.getDisappearProgress(l)) * 600.0f));
                this.visibility = lv;
                this.visibility.playSound(this.field_2245.client.getSoundManager());
            }
            return this.visibility == Toast.Visibility.HIDE && l - this.startTime > 600L;
        }
    }
}

