/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public final class EntityModelLayer {
    private final Identifier id;
    private final String name;

    public EntityModelLayer(Identifier id, String name) {
        this.id = id;
        this.name = name;
    }

    public Identifier getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof EntityModelLayer) {
            EntityModelLayer lv = (EntityModelLayer)o;
            return this.id.equals(lv.id) && this.name.equals(lv.name);
        }
        return false;
    }

    public int hashCode() {
        int i = this.id.hashCode();
        i = 31 * i + this.name.hashCode();
        return i;
    }

    public String toString() {
        return String.valueOf(this.id) + "#" + this.name;
    }
}

