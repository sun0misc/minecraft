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
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AbstractZombieModel;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;

@Environment(value=EnvType.CLIENT)
public class ZombieEntityModel<T extends ZombieEntity>
extends AbstractZombieModel<T> {
    public ZombieEntityModel(ModelPart arg) {
        super(arg);
    }

    @Override
    public boolean isAttacking(T arg) {
        return ((MobEntity)arg).isAttacking();
    }
}

