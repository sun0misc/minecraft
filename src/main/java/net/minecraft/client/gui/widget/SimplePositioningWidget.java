/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.WrapperWidget;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class SimplePositioningWidget
extends WrapperWidget {
    private final List<Element> elements = new ArrayList<Element>();
    private int minHeight;
    private int minWidth;
    private final Positioner mainPositioner = Positioner.create().relative(0.5f, 0.5f);

    public SimplePositioningWidget() {
        this(0, 0, 0, 0);
    }

    public SimplePositioningWidget(int width, int height) {
        this(0, 0, width, height);
    }

    public SimplePositioningWidget(int i, int j, int k, int l) {
        super(i, j, k, l);
        this.setDimensions(k, l);
    }

    public SimplePositioningWidget setDimensions(int minWidth, int minHeight) {
        return this.setMinWidth(minWidth).setMinHeight(minHeight);
    }

    public SimplePositioningWidget setMinHeight(int minWidth) {
        this.minWidth = minWidth;
        return this;
    }

    public SimplePositioningWidget setMinWidth(int minHeight) {
        this.minHeight = minHeight;
        return this;
    }

    public Positioner copyPositioner() {
        return this.mainPositioner.copy();
    }

    public Positioner getMainPositioner() {
        return this.mainPositioner;
    }

    @Override
    public void refreshPositions() {
        super.refreshPositions();
        int i = this.minHeight;
        int j = this.minWidth;
        for (Element lv : this.elements) {
            i = Math.max(i, lv.getWidth());
            j = Math.max(j, lv.getHeight());
        }
        for (Element lv : this.elements) {
            lv.setX(this.getX(), i);
            lv.setY(this.getY(), j);
        }
        this.width = i;
        this.height = j;
    }

    public <T extends Widget> T add(T widget) {
        return this.add(widget, this.copyPositioner());
    }

    public <T extends Widget> T add(T widget, Positioner positioner) {
        this.elements.add(new Element(widget, positioner));
        return widget;
    }

    public <T extends Widget> T add(T widget, Consumer<Positioner> callback) {
        return this.add(widget, Util.make(this.copyPositioner(), callback));
    }

    @Override
    public void forEachElement(Consumer<Widget> consumer) {
        this.elements.forEach(element -> consumer.accept(element.widget));
    }

    public static void setPos(Widget widget, int left, int top, int right, int bottom) {
        SimplePositioningWidget.setPos(widget, left, top, right, bottom, 0.5f, 0.5f);
    }

    public static void setPos(Widget widget, ScreenRect rect) {
        SimplePositioningWidget.setPos(widget, rect.position().x(), rect.position().y(), rect.width(), rect.height());
    }

    public static void setPos(Widget widget, ScreenRect rect, float relativeX, float relativeY) {
        SimplePositioningWidget.setPos(widget, rect.getLeft(), rect.getTop(), rect.width(), rect.height(), relativeX, relativeY);
    }

    public static void setPos(Widget widget, int left, int top, int right, int bottom, float relativeX, float relativeY) {
        SimplePositioningWidget.setPos(left, right, widget.getWidth(), widget::setX, relativeX);
        SimplePositioningWidget.setPos(top, bottom, widget.getHeight(), widget::setY, relativeY);
    }

    public static void setPos(int low, int high, int length, Consumer<Integer> setter, float relative) {
        int l = (int)MathHelper.lerp(relative, 0.0f, (float)(high - length));
        setter.accept(low + l);
    }

    @Environment(value=EnvType.CLIENT)
    static class Element
    extends WrapperWidget.WrappedElement {
        protected Element(Widget arg, Positioner arg2) {
            super(arg, arg2);
        }
    }
}

