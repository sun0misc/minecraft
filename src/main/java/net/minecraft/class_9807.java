/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_9782;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class class_9807
extends Screen {
    private static final int field_52143 = 310;
    private static final int field_52144 = 25;
    private static final Text field_52145 = Text.translatable("menu.server_links.title");
    private final Screen field_52146;
    @Nullable
    private class_9808 field_52147;
    final ThreePartsLayoutWidget field_52148 = new ThreePartsLayoutWidget(this);
    final class_9782 field_52149;

    public class_9807(Screen arg, class_9782 arg2) {
        super(field_52145);
        this.field_52146 = arg;
        this.field_52149 = arg2;
    }

    @Override
    protected void init() {
        this.field_52148.addHeader(this.title, this.textRenderer);
        this.field_52147 = this.field_52148.addBody(new class_9808(this.client, this.width, this));
        this.field_52148.addFooter(ButtonWidget.builder(ScreenTexts.BACK, arg -> this.close()).width(200).build());
        this.field_52148.forEachChild(arg2 -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(arg2);
        });
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        this.field_52148.refreshPositions();
        if (this.field_52147 != null) {
            this.field_52147.position(this.width, this.field_52148);
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.field_52146);
    }

    @Environment(value=EnvType.CLIENT)
    static class class_9808
    extends ElementListWidget<class_9809> {
        public class_9808(MinecraftClient arg, int i, class_9807 arg22) {
            super(arg, i, arg22.field_52148.getContentHeight(), arg22.field_52148.getHeaderHeight(), 25);
            arg22.field_52149.entries().forEach(arg2 -> this.addEntry(new class_9809(arg22, (class_9782.class_9783)arg2)));
        }

        @Override
        public int getRowWidth() {
            return 310;
        }

        @Override
        public void position(int width, ThreePartsLayoutWidget layout) {
            super.position(width, layout);
            int j = width / 2 - 155;
            this.children().forEach(arg -> arg.field_52150.setX(j));
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class class_9809
    extends ElementListWidget.Entry<class_9809> {
        final ClickableWidget field_52150;

        class_9809(Screen arg, class_9782.class_9783 arg2) {
            this.field_52150 = ButtonWidget.builder(arg2.method_60662(), ConfirmLinkScreen.method_60867(arg, arg2.url(), false)).width(310).build();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.field_52150.setY(y);
            this.field_52150.render(context, mouseX, mouseY, tickDelta);
        }

        @Override
        public List<? extends Element> children() {
            return List.of(this.field_52150);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of(this.field_52150);
        }
    }
}

