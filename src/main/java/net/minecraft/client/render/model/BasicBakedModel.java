/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BasicBakedModel
implements BakedModel {
    protected final List<BakedQuad> quads;
    protected final Map<Direction, List<BakedQuad>> faceQuads;
    protected final boolean usesAo;
    protected final boolean hasDepth;
    protected final boolean isSideLit;
    protected final Sprite sprite;
    protected final ModelTransformation transformation;
    protected final ModelOverrideList itemPropertyOverrides;

    public BasicBakedModel(List<BakedQuad> quads, Map<Direction, List<BakedQuad>> faceQuads, boolean usesAo, boolean isSideLit, boolean hasDepth, Sprite sprite, ModelTransformation transformation, ModelOverrideList itemPropertyOverrides) {
        this.quads = quads;
        this.faceQuads = faceQuads;
        this.usesAo = usesAo;
        this.hasDepth = hasDepth;
        this.isSideLit = isSideLit;
        this.sprite = sprite;
        this.transformation = transformation;
        this.itemPropertyOverrides = itemPropertyOverrides;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return face == null ? this.quads : this.faceQuads.get(face);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.usesAo;
    }

    @Override
    public boolean hasDepth() {
        return this.hasDepth;
    }

    @Override
    public boolean isSideLit() {
        return this.isSideLit;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        return this.sprite;
    }

    @Override
    public ModelTransformation getTransformation() {
        return this.transformation;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return this.itemPropertyOverrides;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final List<BakedQuad> quads = Lists.newArrayList();
        private final Map<Direction, List<BakedQuad>> faceQuads = Maps.newEnumMap(Direction.class);
        private final ModelOverrideList itemPropertyOverrides;
        private final boolean usesAo;
        private Sprite particleTexture;
        private final boolean isSideLit;
        private final boolean hasDepth;
        private final ModelTransformation transformation;

        public Builder(JsonUnbakedModel unbakedModel, ModelOverrideList itemPropertyOverrides, boolean hasDepth) {
            this(unbakedModel.useAmbientOcclusion(), unbakedModel.getGuiLight().isSide(), hasDepth, unbakedModel.getTransformations(), itemPropertyOverrides);
        }

        private Builder(boolean usesAo, boolean isSideLit, boolean hasDepth, ModelTransformation transformation, ModelOverrideList itemPropertyOverrides) {
            for (Direction lv : Direction.values()) {
                this.faceQuads.put(lv, Lists.newArrayList());
            }
            this.itemPropertyOverrides = itemPropertyOverrides;
            this.usesAo = usesAo;
            this.isSideLit = isSideLit;
            this.hasDepth = hasDepth;
            this.transformation = transformation;
        }

        public Builder addQuad(Direction side, BakedQuad quad) {
            this.faceQuads.get(side).add(quad);
            return this;
        }

        public Builder addQuad(BakedQuad quad) {
            this.quads.add(quad);
            return this;
        }

        public Builder setParticle(Sprite sprite) {
            this.particleTexture = sprite;
            return this;
        }

        public Builder method_35809() {
            return this;
        }

        public BakedModel build() {
            if (this.particleTexture == null) {
                throw new RuntimeException("Missing particle!");
            }
            return new BasicBakedModel(this.quads, this.faceQuads, this.usesAo, this.isSideLit, this.hasDepth, this.particleTexture, this.transformation, this.itemPropertyOverrides);
        }
    }
}

