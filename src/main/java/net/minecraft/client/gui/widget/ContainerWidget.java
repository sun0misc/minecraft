/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class ContainerWidget
extends ClickableWidget
implements ParentElement {
    @Nullable
    private Element focusedElement;
    private boolean dragging;

    public ContainerWidget(int i, int j, int k, int l, Text arg) {
        super(i, j, k, l, arg);
    }

    @Override
    public final boolean isDragging() {
        return this.dragging;
    }

    @Override
    public final void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    @Override
    @Nullable
    public Element getFocused() {
        return this.focusedElement;
    }

    @Override
    public void setFocused(@Nullable Element focused) {
        if (this.focusedElement != null) {
            this.focusedElement.setFocused(false);
        }
        if (focused != null) {
            focused.setFocused(true);
        }
        this.focusedElement = focused;
    }

    @Override
    @Nullable
    public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        return ParentElement.super.getNavigationPath(navigation);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return ParentElement.super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return ParentElement.super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return ParentElement.super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean isFocused() {
        return ParentElement.super.isFocused();
    }

    @Override
    public void setFocused(boolean focused) {
        ParentElement.super.setFocused(focused);
    }
}

