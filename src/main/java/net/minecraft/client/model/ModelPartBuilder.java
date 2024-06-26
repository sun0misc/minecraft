/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelCuboidData;
import net.minecraft.util.math.Direction;

@Environment(value=EnvType.CLIENT)
public class ModelPartBuilder {
    private static final Set<Direction> ALL_DIRECTIONS = EnumSet.allOf(Direction.class);
    private final List<ModelCuboidData> cuboidData = Lists.newArrayList();
    private int textureX;
    private int textureY;
    private boolean mirror;

    public ModelPartBuilder uv(int textureX, int textureY) {
        this.textureX = textureX;
        this.textureY = textureY;
        return this;
    }

    public ModelPartBuilder mirrored() {
        return this.mirrored(true);
    }

    public ModelPartBuilder mirrored(boolean mirror) {
        this.mirror = mirror;
        return this;
    }

    public ModelPartBuilder cuboid(String name, float offsetX, float offsetY, float offsetZ, int sizeX, int sizeY, int sizeZ, Dilation extra, int textureX, int textureY) {
        this.uv(textureX, textureY);
        this.cuboidData.add(new ModelCuboidData(name, this.textureX, this.textureY, offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ, extra, this.mirror, 1.0f, 1.0f, ALL_DIRECTIONS));
        return this;
    }

    public ModelPartBuilder cuboid(String name, float offsetX, float offsetY, float offsetZ, int sizeX, int sizeY, int sizeZ, int textureX, int textureY) {
        this.uv(textureX, textureY);
        this.cuboidData.add(new ModelCuboidData(name, this.textureX, this.textureY, offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ, Dilation.NONE, this.mirror, 1.0f, 1.0f, ALL_DIRECTIONS));
        return this;
    }

    public ModelPartBuilder cuboid(float offsetX, float offsetY, float offsetZ, float sizeX, float sizeY, float sizeZ) {
        this.cuboidData.add(new ModelCuboidData(null, this.textureX, this.textureY, offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ, Dilation.NONE, this.mirror, 1.0f, 1.0f, ALL_DIRECTIONS));
        return this;
    }

    public ModelPartBuilder cuboid(float offsetX, float offsetY, float offsetZ, float sizeX, float sizeY, float sizeZ, Set<Direction> directions) {
        this.cuboidData.add(new ModelCuboidData(null, this.textureX, this.textureY, offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ, Dilation.NONE, this.mirror, 1.0f, 1.0f, directions));
        return this;
    }

    public ModelPartBuilder cuboid(String name, float offsetX, float offsetY, float offsetZ, float sizeX, float sizeY, float sizeZ) {
        this.cuboidData.add(new ModelCuboidData(name, this.textureX, this.textureY, offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ, Dilation.NONE, this.mirror, 1.0f, 1.0f, ALL_DIRECTIONS));
        return this;
    }

    public ModelPartBuilder cuboid(String name, float offsetX, float offsetY, float offsetZ, float sizeX, float sizeY, float sizeZ, Dilation extra) {
        this.cuboidData.add(new ModelCuboidData(name, this.textureX, this.textureY, offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ, extra, this.mirror, 1.0f, 1.0f, ALL_DIRECTIONS));
        return this;
    }

    public ModelPartBuilder cuboid(float offsetX, float offsetY, float offsetZ, float sizeX, float sizeY, float sizeZ, boolean mirror) {
        this.cuboidData.add(new ModelCuboidData(null, this.textureX, this.textureY, offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ, Dilation.NONE, mirror, 1.0f, 1.0f, ALL_DIRECTIONS));
        return this;
    }

    public ModelPartBuilder cuboid(float offsetX, float offsetY, float offsetZ, float sizeX, float sizeY, float sizeZ, Dilation extra, float textureScaleX, float textureScaleY) {
        this.cuboidData.add(new ModelCuboidData(null, this.textureX, this.textureY, offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ, extra, this.mirror, textureScaleX, textureScaleY, ALL_DIRECTIONS));
        return this;
    }

    public ModelPartBuilder cuboid(float offsetX, float offsetY, float offsetZ, float sizeX, float sizeY, float sizeZ, Dilation extra) {
        this.cuboidData.add(new ModelCuboidData(null, this.textureX, this.textureY, offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ, extra, this.mirror, 1.0f, 1.0f, ALL_DIRECTIONS));
        return this;
    }

    public List<ModelCuboidData> build() {
        return ImmutableList.copyOf(this.cuboidData);
    }

    public static ModelPartBuilder create() {
        return new ModelPartBuilder();
    }
}

