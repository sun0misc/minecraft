/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.report;

import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.report.AbuseReportReasonScreen;
import net.minecraft.client.gui.screen.report.ChatSelectionScreen;
import net.minecraft.client.gui.screen.report.ReportScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.LayoutWidgets;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.session.report.AbuseReport;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.AbuseReportReason;
import net.minecraft.client.session.report.ChatAbuseReport;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Nullables;

@Environment(value=EnvType.CLIENT)
public class ChatReportScreen
extends ReportScreen<ChatAbuseReport.Builder> {
    private static final int BOTTOM_BUTTON_WIDTH = 120;
    private static final Text TITLE_TEXT = Text.translatable("gui.chatReport.title");
    private static final Text SELECT_CHAT_TEXT = Text.translatable("gui.chatReport.select_chat");
    private final DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical().spacing(8);
    private EditBoxWidget commentsBox;
    private ButtonWidget sendButton;
    private ButtonWidget selectChatButton;
    private ButtonWidget selectReasonButton;

    private ChatReportScreen(Screen parent, AbuseReportContext context, ChatAbuseReport.Builder reportBuilder) {
        super(TITLE_TEXT, parent, context, reportBuilder);
    }

    public ChatReportScreen(Screen parent, AbuseReportContext reporter, UUID reportedPlayerUuid) {
        this(parent, reporter, new ChatAbuseReport.Builder(reportedPlayerUuid, reporter.getSender().getLimits()));
    }

    public ChatReportScreen(Screen parent, AbuseReportContext context, ChatAbuseReport report) {
        this(parent, context, new ChatAbuseReport.Builder(report, context.getSender().getLimits()));
    }

    @Override
    protected void init() {
        this.layout.getMainPositioner().alignHorizontalCenter();
        this.layout.add(new TextWidget(this.title, this.textRenderer));
        this.selectChatButton = this.layout.add(ButtonWidget.builder(SELECT_CHAT_TEXT, button -> this.client.setScreen(new ChatSelectionScreen(this, this.context, (ChatAbuseReport.Builder)this.reportBuilder, updatedReportBuilder -> {
            this.reportBuilder = updatedReportBuilder;
            this.onChange();
        }))).width(280).build());
        this.selectReasonButton = ButtonWidget.builder(SELECT_REASON_TEXT, button -> this.client.setScreen(new AbuseReportReasonScreen(this, ((ChatAbuseReport.Builder)this.reportBuilder).getReason(), reason -> {
            ((ChatAbuseReport.Builder)this.reportBuilder).setReason((AbuseReportReason)((Object)((Object)reason)));
            this.onChange();
        }))).width(280).build();
        this.layout.add(LayoutWidgets.createLabeledWidget(this.textRenderer, this.selectReasonButton, OBSERVED_WHAT_TEXT));
        this.commentsBox = this.createCommentsBox(280, this.textRenderer.fontHeight * 8, opinionComments -> {
            ((ChatAbuseReport.Builder)this.reportBuilder).setOpinionComments((String)opinionComments);
            this.onChange();
        });
        this.layout.add(LayoutWidgets.createLabeledWidget(this.textRenderer, this.commentsBox, MORE_COMMENTS_TEXT, positioner -> positioner.marginBottom(12)));
        DirectionalLayoutWidget lv = this.layout.add(DirectionalLayoutWidget.horizontal().spacing(8));
        lv.add(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).width(120).build());
        this.sendButton = lv.add(ButtonWidget.builder(SEND_TEXT, button -> this.trySend()).width(120).build());
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.initTabNavigation();
        this.onChange();
    }

    @Override
    protected void initTabNavigation() {
        this.layout.refreshPositions();
        SimplePositioningWidget.setPos(this.layout, this.getNavigationFocus());
    }

    private void onChange() {
        IntSet intSet = ((ChatAbuseReport.Builder)this.reportBuilder).getSelectedMessages();
        if (intSet.isEmpty()) {
            this.selectChatButton.setMessage(SELECT_CHAT_TEXT);
        } else {
            this.selectChatButton.setMessage(Text.translatable("gui.chatReport.selected_chat", intSet.size()));
        }
        AbuseReportReason lv = ((ChatAbuseReport.Builder)this.reportBuilder).getReason();
        if (lv != null) {
            this.selectReasonButton.setMessage(lv.getText());
        } else {
            this.selectReasonButton.setMessage(SELECT_REASON_TEXT);
        }
        AbuseReport.ValidationError lv2 = ((ChatAbuseReport.Builder)this.reportBuilder).validate();
        this.sendButton.active = lv2 == null;
        this.sendButton.setTooltip(Nullables.map(lv2, AbuseReport.ValidationError::createTooltip));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return this.commentsBox.mouseReleased(mouseX, mouseY, button);
    }
}

