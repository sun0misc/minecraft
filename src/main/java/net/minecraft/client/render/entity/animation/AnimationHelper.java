/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.animation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.Keyframe;
import net.minecraft.client.render.entity.animation.Transformation;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class AnimationHelper {
    public static void animate(SinglePartEntityModel<?> model, Animation animation, long runningTime, float scale, Vector3f tempVec) {
        float g = AnimationHelper.getRunningSeconds(animation, runningTime);
        for (Map.Entry<String, List<Transformation>> entry : animation.boneAnimations().entrySet()) {
            Optional<ModelPart> optional = model.getChild(entry.getKey());
            List<Transformation> list = entry.getValue();
            optional.ifPresent(part -> list.forEach(transformation -> {
                Keyframe[] lvs = transformation.keyframes();
                int i = Math.max(0, MathHelper.binarySearch(0, lvs.length, index -> g <= lvs[index].timestamp()) - 1);
                int j = Math.min(lvs.length - 1, i + 1);
                Keyframe lv = lvs[i];
                Keyframe lv2 = lvs[j];
                float h = g - lv.timestamp();
                float k = j != i ? MathHelper.clamp(h / (lv2.timestamp() - lv.timestamp()), 0.0f, 1.0f) : 0.0f;
                lv2.interpolation().apply(tempVec, k, lvs, i, j, scale);
                transformation.target().apply((ModelPart)part, tempVec);
            }));
        }
    }

    private static float getRunningSeconds(Animation animation, long runningTime) {
        float f = (float)runningTime / 1000.0f;
        return animation.looping() ? f % animation.lengthInSeconds() : f;
    }

    public static Vector3f createTranslationalVector(float x, float y, float z) {
        return new Vector3f(x, -y, z);
    }

    public static Vector3f createRotationalVector(float x, float y, float z) {
        return new Vector3f(x * ((float)Math.PI / 180), y * ((float)Math.PI / 180), z * ((float)Math.PI / 180));
    }

    public static Vector3f createScalingVector(double x, double y, double z) {
        return new Vector3f((float)(x - 1.0), (float)(y - 1.0), (float)(z - 1.0));
    }
}

