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
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.collection.Weighting;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class WeightedBakedModel
implements BakedModel {
    private final int totalWeight;
    private final List<Weighted.Present<BakedModel>> models;
    private final BakedModel defaultModel;

    public WeightedBakedModel(List<Weighted.Present<BakedModel>> models) {
        this.models = models;
        this.totalWeight = Weighting.getWeightSum(models);
        this.defaultModel = models.get(0).data();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return Weighting.getAt(this.models, Math.abs((int)random.nextLong()) % this.totalWeight).map(arg4 -> ((BakedModel)arg4.data()).getQuads(state, face, random)).orElse(Collections.emptyList());
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.defaultModel.useAmbientOcclusion();
    }

    @Override
    public boolean hasDepth() {
        return this.defaultModel.hasDepth();
    }

    @Override
    public boolean isSideLit() {
        return this.defaultModel.isSideLit();
    }

    @Override
    public boolean isBuiltin() {
        return this.defaultModel.isBuiltin();
    }

    @Override
    public Sprite getParticleSprite() {
        return this.defaultModel.getParticleSprite();
    }

    @Override
    public ModelTransformation getTransformation() {
        return this.defaultModel.getTransformation();
    }

    @Override
    public ModelOverrideList getOverrides() {
        return this.defaultModel.getOverrides();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final List<Weighted.Present<BakedModel>> models = Lists.newArrayList();

        public Builder add(@Nullable BakedModel model, int weight) {
            if (model != null) {
                this.models.add(Weighted.of(model, weight));
            }
            return this;
        }

        @Nullable
        public BakedModel build() {
            if (this.models.isEmpty()) {
                return null;
            }
            if (this.models.size() == 1) {
                return this.models.get(0).data();
            }
            return new WeightedBakedModel(this.models);
        }
    }
}

