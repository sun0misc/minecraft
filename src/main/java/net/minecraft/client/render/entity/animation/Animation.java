/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.animation;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.animation.Transformation;

@Environment(value=EnvType.CLIENT)
public record Animation(float lengthInSeconds, boolean looping, Map<String, List<Transformation>> boneAnimations) {

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final float lengthInSeconds;
        private final Map<String, List<Transformation>> transformations = Maps.newHashMap();
        private boolean looping;

        public static Builder create(float lengthInSeconds) {
            return new Builder(lengthInSeconds);
        }

        private Builder(float lengthInSeconds) {
            this.lengthInSeconds = lengthInSeconds;
        }

        public Builder looping() {
            this.looping = true;
            return this;
        }

        public Builder addBoneAnimation(String name2, Transformation transformation) {
            this.transformations.computeIfAbsent(name2, name -> new ArrayList()).add(transformation);
            return this;
        }

        public Animation build() {
            return new Animation(this.lengthInSeconds, this.looping, this.transformations);
        }
    }
}

