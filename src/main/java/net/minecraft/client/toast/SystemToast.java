/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.toast;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SystemToast
implements Toast {
    private static final Identifier TEXTURE = Identifier.method_60656("toast/system");
    private static final int MIN_WIDTH = 200;
    private static final int LINE_HEIGHT = 12;
    private static final int PADDING_Y = 10;
    private final Type type;
    private Text title;
    private List<OrderedText> lines;
    private long startTime;
    private boolean justUpdated;
    private final int width;
    private boolean hidden;

    public SystemToast(Type type, Text title, @Nullable Text description) {
        this(type, title, SystemToast.getTextAsList(description), Math.max(160, 30 + Math.max(MinecraftClient.getInstance().textRenderer.getWidth(title), description == null ? 0 : MinecraftClient.getInstance().textRenderer.getWidth(description))));
    }

    public static SystemToast create(MinecraftClient client, Type type, Text title, Text description) {
        TextRenderer lv = client.textRenderer;
        List<OrderedText> list = lv.wrapLines(description, 200);
        int i = Math.max(200, list.stream().mapToInt(lv::getWidth).max().orElse(200));
        return new SystemToast(type, title, list, i + 30);
    }

    private SystemToast(Type type, Text title, List<OrderedText> lines, int width) {
        this.type = type;
        this.title = title;
        this.lines = lines;
        this.width = width;
    }

    private static ImmutableList<OrderedText> getTextAsList(@Nullable Text text) {
        return text == null ? ImmutableList.of() : ImmutableList.of(text.asOrderedText());
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return 20 + Math.max(this.lines.size(), 1) * 12;
    }

    public void hide() {
        this.hidden = true;
    }

    @Override
    public Toast.Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        int j;
        int i;
        if (this.justUpdated) {
            this.startTime = startTime;
            this.justUpdated = false;
        }
        if ((i = this.getWidth()) == 160 && this.lines.size() <= 1) {
            context.drawGuiTexture(TEXTURE, 0, 0, i, this.getHeight());
        } else {
            j = this.getHeight();
            int k = 28;
            int m = Math.min(4, j - 28);
            this.drawPart(context, i, 0, 0, 28);
            for (int n = 28; n < j - m; n += 10) {
                this.drawPart(context, i, 16, n, Math.min(16, j - n - m));
            }
            this.drawPart(context, i, 32 - m, j - m, m);
        }
        if (this.lines.isEmpty()) {
            context.drawText(manager.getClient().textRenderer, this.title, 18, 12, Colors.YELLOW, false);
        } else {
            context.drawText(manager.getClient().textRenderer, this.title, 18, 7, Colors.YELLOW, false);
            for (j = 0; j < this.lines.size(); ++j) {
                context.drawText(manager.getClient().textRenderer, this.lines.get(j), 18, 18 + j * 12, -1, false);
            }
        }
        double d = (double)this.type.displayDuration * manager.getNotificationDisplayTimeMultiplier();
        long o = startTime - this.startTime;
        return !this.hidden && (double)o < d ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }

    private void drawPart(DrawContext context, int i, int j, int k, int l) {
        int m = j == 0 ? 20 : 5;
        int n = Math.min(60, i - m);
        Identifier lv = TEXTURE;
        context.drawGuiTexture(lv, 160, 32, 0, j, 0, k, m, l);
        for (int o = m; o < i - n; o += 64) {
            context.drawGuiTexture(lv, 160, 32, 32, j, o, k, Math.min(64, i - o - n), l);
        }
        context.drawGuiTexture(lv, 160, 32, 160 - n, j, i - n, k, n, l);
    }

    public void setContent(Text title, @Nullable Text description) {
        this.title = title;
        this.lines = SystemToast.getTextAsList(description);
        this.justUpdated = true;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    public static void add(ToastManager manager, Type type, Text title, @Nullable Text description) {
        manager.add(new SystemToast(type, title, description));
    }

    public static void show(ToastManager manager, Type type, Text title, @Nullable Text description) {
        SystemToast lv = manager.getToast(SystemToast.class, type);
        if (lv == null) {
            SystemToast.add(manager, type, title, description);
        } else {
            lv.setContent(title, description);
        }
    }

    public static void hide(ToastManager manager, Type type) {
        SystemToast lv = manager.getToast(SystemToast.class, type);
        if (lv != null) {
            lv.hide();
        }
    }

    public static void addWorldAccessFailureToast(MinecraftClient client, String worldName) {
        SystemToast.add(client.getToastManager(), Type.WORLD_ACCESS_FAILURE, Text.translatable("selectWorld.access_failure"), Text.literal(worldName));
    }

    public static void addWorldDeleteFailureToast(MinecraftClient client, String worldName) {
        SystemToast.add(client.getToastManager(), Type.WORLD_ACCESS_FAILURE, Text.translatable("selectWorld.delete_failure"), Text.literal(worldName));
    }

    public static void addPackCopyFailure(MinecraftClient client, String directory) {
        SystemToast.add(client.getToastManager(), Type.PACK_COPY_FAILURE, Text.translatable("pack.copyFailure"), Text.literal(directory));
    }

    public static void method_60865(MinecraftClient arg, int i) {
        SystemToast.add(arg.getToastManager(), Type.field_52128, Text.translatable("gui.fileDropFailure.title"), Text.translatable("gui.fileDropFailure.detail", i));
    }

    public static void addLowDiskSpace(MinecraftClient client) {
        SystemToast.show(client.getToastManager(), Type.LOW_DISK_SPACE, Text.translatable("chunk.toast.lowDiskSpace"), Text.translatable("chunk.toast.lowDiskSpace.description"));
    }

    public static void addChunkLoadFailure(MinecraftClient client, ChunkPos pos) {
        SystemToast.show(client.getToastManager(), Type.CHUNK_LOAD_FAILURE, Text.translatable("chunk.toast.loadFailure", pos).formatted(Formatting.RED), Text.translatable("chunk.toast.checkLog"));
    }

    public static void addChunkSaveFailure(MinecraftClient client, ChunkPos pos) {
        SystemToast.show(client.getToastManager(), Type.CHUNK_SAVE_FAILURE, Text.translatable("chunk.toast.saveFailure", pos).formatted(Formatting.RED), Text.translatable("chunk.toast.checkLog"));
    }

    @Override
    public /* synthetic */ Object getType() {
        return this.getType();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Type {
        public static final Type NARRATOR_TOGGLE = new Type();
        public static final Type WORLD_BACKUP = new Type();
        public static final Type PACK_LOAD_FAILURE = new Type();
        public static final Type WORLD_ACCESS_FAILURE = new Type();
        public static final Type PACK_COPY_FAILURE = new Type();
        public static final Type field_52128 = new Type();
        public static final Type PERIODIC_NOTIFICATION = new Type();
        public static final Type LOW_DISK_SPACE = new Type(10000L);
        public static final Type CHUNK_LOAD_FAILURE = new Type();
        public static final Type CHUNK_SAVE_FAILURE = new Type();
        public static final Type UNSECURE_SERVER_WARNING = new Type(10000L);
        final long displayDuration;

        public Type(long displayDuration) {
            this.displayDuration = displayDuration;
        }

        public Type() {
            this(5000L);
        }
    }
}

