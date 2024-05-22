/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.world;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.time.Instant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.NoticeScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.nbt.NbtCrashException;
import net.minecraft.nbt.NbtException;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RecoverWorldScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_46863 = 25;
    private static final Text TITLE_TEXT = Text.translatable("recover_world.title").formatted(Formatting.BOLD);
    private static final Text BUG_TRACKER_TEXT = Text.translatable("recover_world.bug_tracker");
    private static final Text RESTORE_TEXT = Text.translatable("recover_world.restore");
    private static final Text NO_FALLBACK_TEXT = Text.translatable("recover_world.no_fallback");
    private static final Text DONE_TITLE_TEXT = Text.translatable("recover_world.done.title");
    private static final Text DONE_SUCCESS_TEXT = Text.translatable("recover_world.done.success");
    private static final Text DONE_FAILED_TEXT = Text.translatable("recover_world.done.failed");
    private static final Text ISSUE_NONE_TEXT = Text.translatable("recover_world.issue.none").formatted(Formatting.GREEN);
    private static final Text MISSING_FILE_TEXT = Text.translatable("recover_world.issue.missing_file").formatted(Formatting.RED);
    private final BooleanConsumer callback;
    private final DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical().spacing(8);
    private final Text message;
    private final MultilineTextWidget messageWidget;
    private final MultilineTextWidget exceptionWidget;
    private final LevelStorage.Session session;

    public RecoverWorldScreen(MinecraftClient client, BooleanConsumer callback, LevelStorage.Session session) {
        super(TITLE_TEXT);
        this.callback = callback;
        this.message = Text.translatable("recover_world.message", Text.literal(session.getDirectoryName()).formatted(Formatting.GRAY));
        this.messageWidget = new MultilineTextWidget(this.message, client.textRenderer);
        this.session = session;
        Exception exception = this.getLoadingException(session, false);
        Exception exception2 = this.getLoadingException(session, true);
        MutableText lv = Text.empty().append(this.toText(session, false, exception)).append("\n").append(this.toText(session, true, exception2));
        this.exceptionWidget = new MultilineTextWidget(lv, client.textRenderer);
        boolean bl = exception != null && exception2 == null;
        this.layout.getMainPositioner().alignHorizontalCenter();
        this.layout.add(new TextWidget(this.title, client.textRenderer));
        this.layout.add(this.messageWidget.setCentered(true));
        this.layout.add(this.exceptionWidget);
        DirectionalLayoutWidget lv2 = DirectionalLayoutWidget.horizontal().spacing(5);
        lv2.add(ButtonWidget.builder(BUG_TRACKER_TEXT, ConfirmLinkScreen.opening(this, "https://aka.ms/snapshotbugs?ref=game")).size(120, 20).build());
        lv2.add(ButtonWidget.builder((Text)RecoverWorldScreen.RESTORE_TEXT, (ButtonWidget.PressAction)(ButtonWidget.PressAction)LambdaMetafactory.metafactory(null, null, null, (Lnet/minecraft/client/gui/widget/ButtonWidget;)V, method_54586(net.minecraft.client.MinecraftClient net.minecraft.client.gui.widget.ButtonWidget ), (Lnet/minecraft/client/gui/widget/ButtonWidget;)V)((RecoverWorldScreen)this, (MinecraftClient)client)).size((int)120, (int)20).tooltip((Tooltip)(bl ? null : Tooltip.of((Text)RecoverWorldScreen.NO_FALLBACK_TEXT))).build()).active = bl;
        this.layout.add(lv2);
        this.layout.add(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).size(120, 20).build());
        this.layout.forEachChild(this::addDrawableChild);
    }

    private void tryRestore(MinecraftClient client) {
        Exception exception = this.getLoadingException(this.session, false);
        Exception exception2 = this.getLoadingException(this.session, true);
        if (exception == null || exception2 != null) {
            LOGGER.error("Failed to recover world, files not as expected. level.dat: {}, level.dat_old: {}", (Object)(exception != null ? exception.getMessage() : "no issues"), (Object)(exception2 != null ? exception2.getMessage() : "no issues"));
            client.setScreen(new NoticeScreen(() -> this.callback.accept(false), DONE_TITLE_TEXT, DONE_FAILED_TEXT));
            return;
        }
        client.setScreenAndRender(new MessageScreen(Text.translatable("recover_world.restoring")));
        EditWorldScreen.backupLevel(this.session);
        if (this.session.tryRestoreBackup()) {
            client.setScreen(new ConfirmScreen(this.callback, DONE_TITLE_TEXT, DONE_SUCCESS_TEXT, ScreenTexts.CONTINUE, ScreenTexts.BACK));
        } else {
            client.setScreen(new NoticeScreen(() -> this.callback.accept(false), DONE_TITLE_TEXT, DONE_FAILED_TEXT));
        }
    }

    private Text toText(LevelStorage.Session session, boolean old, @Nullable Exception exception) {
        if (old && exception instanceof FileNotFoundException) {
            return Text.empty();
        }
        MutableText lv = Text.empty();
        Instant instant = session.getLastModifiedTime(old);
        MutableText lv2 = instant != null ? Text.literal(WorldListWidget.DATE_FORMAT.format(instant)) : Text.translatable("recover_world.state_entry.unknown");
        lv.append(Text.translatable("recover_world.state_entry", lv2.formatted(Formatting.GRAY)));
        if (exception == null) {
            lv.append(ISSUE_NONE_TEXT);
        } else if (exception instanceof FileNotFoundException) {
            lv.append(MISSING_FILE_TEXT);
        } else if (exception instanceof NbtCrashException) {
            lv.append(Text.literal(exception.getCause().toString()).formatted(Formatting.RED));
        } else {
            lv.append(Text.literal(exception.toString()).formatted(Formatting.RED));
        }
        return lv;
    }

    @Nullable
    private Exception getLoadingException(LevelStorage.Session session, boolean old) {
        try {
            if (!old) {
                session.getLevelSummary(session.readLevelProperties());
            } else {
                session.getLevelSummary(session.readOldLevelProperties());
            }
        } catch (IOException | NbtCrashException | NbtException exception) {
            return exception;
        }
        return null;
    }

    @Override
    protected void init() {
        super.init();
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        this.exceptionWidget.setMaxWidth(this.width - 50);
        this.messageWidget.setMaxWidth(this.width - 50);
        this.layout.refreshPositions();
        SimplePositioningWidget.setPos(this.layout, this.getNavigationFocus());
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinSentences(super.getNarratedTitle(), this.message);
    }

    @Override
    public void close() {
        this.callback.accept(false);
    }

    private /* synthetic */ void method_54586(MinecraftClient arg, ButtonWidget arg2) {
        this.tryRestore(arg);
    }
}

