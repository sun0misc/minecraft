/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.LayoutWidgets;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.gui.screen.RealmsCreateWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.gui.screen.RealmsResetNormalWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.realms.gui.screen.ResetWorldInfo;
import net.minecraft.client.realms.task.CreatingSnapshotWorldTask;
import net.minecraft.client.realms.task.WorldCreationTask;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;

@Environment(value=EnvType.CLIENT)
public class RealmsCreateRealmScreen
extends RealmsScreen {
    private static final Text TITLE_TEXT = Text.translatable("mco.selectServer.create");
    private static final Text WORLD_NAME_TEXT = Text.translatable("mco.configure.world.name");
    private static final Text WORLD_DESCRIPTION_TEXT = Text.translatable("mco.configure.world.description");
    private static final int field_45243 = 10;
    private static final int field_45244 = 210;
    private final RealmsMainScreen parent;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private TextFieldWidget nameBox;
    private TextFieldWidget descriptionBox;
    private final Runnable worldCreator;

    public RealmsCreateRealmScreen(RealmsMainScreen parent, RealmsServer realmsServer) {
        super(TITLE_TEXT);
        this.parent = parent;
        this.worldCreator = () -> this.createWorld(realmsServer);
    }

    public RealmsCreateRealmScreen(RealmsMainScreen parent, long parentId) {
        super(TITLE_TEXT);
        this.parent = parent;
        this.worldCreator = () -> this.createSnapshotWorld(parentId);
    }

    @Override
    public void init() {
        this.layout.addHeader(this.title, this.textRenderer);
        DirectionalLayoutWidget lv = this.layout.addBody(DirectionalLayoutWidget.vertical()).spacing(10);
        ButtonWidget lv2 = ButtonWidget.builder(ScreenTexts.CONTINUE, button -> this.worldCreator.run()).build();
        lv2.active = false;
        this.nameBox = new TextFieldWidget(this.textRenderer, 210, 20, WORLD_NAME_TEXT);
        this.nameBox.setChangedListener(name -> {
            arg.active = !StringHelper.isBlank(name);
        });
        this.descriptionBox = new TextFieldWidget(this.textRenderer, 210, 20, WORLD_DESCRIPTION_TEXT);
        lv.add(LayoutWidgets.createLabeledWidget(this.textRenderer, this.nameBox, WORLD_NAME_TEXT));
        lv.add(LayoutWidgets.createLabeledWidget(this.textRenderer, this.descriptionBox, WORLD_DESCRIPTION_TEXT));
        DirectionalLayoutWidget lv3 = this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(10));
        lv3.add(lv2);
        lv3.add(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).build());
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.initTabNavigation();
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.nameBox);
    }

    @Override
    protected void initTabNavigation() {
        this.layout.refreshPositions();
    }

    private void createWorld(RealmsServer realmsServer) {
        WorldCreationTask lv = new WorldCreationTask(realmsServer.id, this.nameBox.getText(), this.descriptionBox.getText());
        RealmsCreateWorldScreen lv2 = RealmsCreateWorldScreen.newRealm(this, realmsServer, lv, () -> this.client.execute(() -> {
            RealmsMainScreen.resetServerList();
            this.client.setScreen(this.parent);
        }));
        this.client.setScreen(lv2);
    }

    private void createSnapshotWorld(long parentId) {
        RealmsResetNormalWorldScreen lv = new RealmsResetNormalWorldScreen(info -> {
            if (info == null) {
                this.client.setScreen(this);
                return;
            }
            this.client.setScreen(new RealmsLongRunningMcoTaskScreen(this, new CreatingSnapshotWorldTask(this.parent, parentId, (ResetWorldInfo)info, this.nameBox.getText(), this.descriptionBox.getText())));
        }, TITLE_TEXT);
        this.client.setScreen(lv);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}

