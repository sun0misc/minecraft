/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface ClampedModelPredicateProvider
extends ModelPredicateProvider {
    @Override
    @Deprecated
    default public float call(ItemStack arg, @Nullable ClientWorld arg2, @Nullable LivingEntity arg3, int i) {
        return MathHelper.clamp(this.unclampedCall(arg, arg2, arg3, i), 0.0f, 1.0f);
    }

    public float unclampedCall(ItemStack var1, @Nullable ClientWorld var2, @Nullable LivingEntity var3, int var4);
}

