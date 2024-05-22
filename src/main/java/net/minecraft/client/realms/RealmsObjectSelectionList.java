/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms;

import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;

@Environment(value=EnvType.CLIENT)
public abstract class RealmsObjectSelectionList<E extends AlwaysSelectedEntryListWidget.Entry<E>>
extends AlwaysSelectedEntryListWidget<E> {
    protected RealmsObjectSelectionList(int width, int height, int top, int bottom) {
        super(MinecraftClient.getInstance(), width, height, top, bottom);
    }

    public void setSelectedItem(int index) {
        if (index == -1) {
            this.setSelected(null);
        } else if (super.getEntryCount() != 0) {
            this.setSelected((AlwaysSelectedEntryListWidget.Entry)this.getEntry(index));
        }
    }

    @Override
    public void setSelected(int index) {
        this.setSelectedItem(index);
    }

    @Override
    public int getMaxPosition() {
        return 0;
    }

    @Override
    public int getRowWidth() {
        return (int)((double)this.width * 0.6);
    }

    @Override
    public void replaceEntries(Collection<E> newEntries) {
        super.replaceEntries(newEntries);
    }

    @Override
    public int getEntryCount() {
        return super.getEntryCount();
    }

    @Override
    public int getRowTop(int index) {
        return super.getRowTop(index);
    }

    @Override
    public int getRowLeft() {
        return super.getRowLeft();
    }

    @Override
    public int addEntry(E arg) {
        return super.addEntry(arg);
    }

    public void clear() {
        this.clearEntries();
    }

    @Override
    public /* synthetic */ int addEntry(EntryListWidget.Entry entry) {
        return this.addEntry((E)((AlwaysSelectedEntryListWidget.Entry)entry));
    }
}

