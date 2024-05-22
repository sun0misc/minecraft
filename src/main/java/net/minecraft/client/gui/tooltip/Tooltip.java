/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.tooltip;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Narratable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class Tooltip
implements Narratable {
    private static final int ROW_LENGTH = 170;
    private final Text content;
    @Nullable
    private List<OrderedText> lines;
    @Nullable
    private Language language;
    @Nullable
    private final Text narration;

    private Tooltip(Text content, @Nullable Text narration) {
        this.content = content;
        this.narration = narration;
    }

    public static Tooltip of(Text content, @Nullable Text narration) {
        return new Tooltip(content, narration);
    }

    public static Tooltip of(Text content) {
        return new Tooltip(content, content);
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        if (this.narration != null) {
            builder.put(NarrationPart.HINT, this.narration);
        }
    }

    public List<OrderedText> getLines(MinecraftClient client) {
        Language lv = Language.getInstance();
        if (this.lines == null || lv != this.language) {
            this.lines = Tooltip.wrapLines(client, this.content);
            this.language = lv;
        }
        return this.lines;
    }

    public static List<OrderedText> wrapLines(MinecraftClient client, Text text) {
        return client.textRenderer.wrapLines(text, 170);
    }
}

