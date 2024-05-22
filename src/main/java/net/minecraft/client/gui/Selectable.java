/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Narratable;
import net.minecraft.client.gui.navigation.Navigable;

@Environment(value=EnvType.CLIENT)
public interface Selectable
extends Navigable,
Narratable {
    public SelectionType getType();

    default public boolean isNarratable() {
        return true;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum SelectionType {
        NONE,
        HOVERED,
        FOCUSED;


        public boolean isFocused() {
            return this == FOCUSED;
        }
    }
}

