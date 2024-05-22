/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.report;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
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
import net.minecraft.client.session.report.UsernameAbuseReport;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Nullables;

@Environment(value=EnvType.CLIENT)
public class UsernameReportScreen
extends ReportScreen<UsernameAbuseReport.Builder> {
    private static final int BOTTOM_BUTTON_WIDTH = 120;
    private static final Text TITLE_TEXT = Text.translatable("gui.abuseReport.name.title");
    private final DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical().spacing(8);
    private EditBoxWidget commentsBox;
    private ButtonWidget sendButton;

    private UsernameReportScreen(Screen parent, AbuseReportContext context, UsernameAbuseReport.Builder reportBuilder) {
        super(TITLE_TEXT, parent, context, reportBuilder);
    }

    public UsernameReportScreen(Screen parent, AbuseReportContext context, UUID reportedPlayerUuid, String username) {
        this(parent, context, new UsernameAbuseReport.Builder(reportedPlayerUuid, username, context.getSender().getLimits()));
    }

    public UsernameReportScreen(Screen parent, AbuseReportContext context, UsernameAbuseReport report) {
        this(parent, context, new UsernameAbuseReport.Builder(report, context.getSender().getLimits()));
    }

    @Override
    protected void init() {
        this.layout.getMainPositioner().alignHorizontalCenter();
        this.layout.add(new TextWidget(this.title, this.textRenderer));
        MutableText lv = Text.literal(((UsernameAbuseReport)((UsernameAbuseReport.Builder)this.reportBuilder).getReport()).getUsername()).formatted(Formatting.YELLOW);
        this.layout.add(new TextWidget(Text.translatable("gui.abuseReport.name.reporting", lv), this.textRenderer), positioner -> positioner.alignLeft().margin(0, 8));
        this.commentsBox = this.createCommentsBox(280, this.textRenderer.fontHeight * 8, opinionComments -> {
            ((UsernameAbuseReport.Builder)this.reportBuilder).setOpinionComments((String)opinionComments);
            this.onChange();
        });
        this.layout.add(LayoutWidgets.createLabeledWidget(this.textRenderer, this.commentsBox, MORE_COMMENTS_TEXT, positioner -> positioner.marginBottom(12)));
        DirectionalLayoutWidget lv2 = this.layout.add(DirectionalLayoutWidget.horizontal().spacing(8));
        lv2.add(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).width(120).build());
        this.sendButton = lv2.add(ButtonWidget.builder(SEND_TEXT, button -> this.trySend()).width(120).build());
        this.onChange();
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        this.layout.refreshPositions();
        SimplePositioningWidget.setPos(this.layout, this.getNavigationFocus());
    }

    private void onChange() {
        AbuseReport.ValidationError lv = ((UsernameAbuseReport.Builder)this.reportBuilder).validate();
        this.sendButton.active = lv == null;
        this.sendButton.setTooltip(Nullables.map(lv, AbuseReport.ValidationError::createTooltip));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return this.commentsBox.mouseReleased(mouseX, mouseY, button);
    }
}

