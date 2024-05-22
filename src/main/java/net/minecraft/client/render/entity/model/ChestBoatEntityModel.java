/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BoatEntityModel;

@Environment(value=EnvType.CLIENT)
public class ChestBoatEntityModel
extends BoatEntityModel {
    private static final String CHEST_BOTTOM = "chest_bottom";
    private static final String CHEST_LID = "chest_lid";
    private static final String CHEST_LOCK = "chest_lock";

    public ChestBoatEntityModel(ModelPart arg) {
        super(arg);
    }

    @Override
    protected ImmutableList.Builder<ModelPart> getParts(ModelPart root) {
        ImmutableList.Builder<ModelPart> builder = super.getParts(root);
        builder.add((Object)root.getChild(CHEST_BOTTOM));
        builder.add((Object)root.getChild(CHEST_LID));
        builder.add((Object)root.getChild(CHEST_LOCK));
        return builder;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        BoatEntityModel.addParts(lv2);
        lv2.addChild(CHEST_BOTTOM, ModelPartBuilder.create().uv(0, 76).cuboid(0.0f, 0.0f, 0.0f, 12.0f, 8.0f, 12.0f), ModelTransform.of(-2.0f, -5.0f, -6.0f, 0.0f, -1.5707964f, 0.0f));
        lv2.addChild(CHEST_LID, ModelPartBuilder.create().uv(0, 59).cuboid(0.0f, 0.0f, 0.0f, 12.0f, 4.0f, 12.0f), ModelTransform.of(-2.0f, -9.0f, -6.0f, 0.0f, -1.5707964f, 0.0f));
        lv2.addChild(CHEST_LOCK, ModelPartBuilder.create().uv(0, 59).cuboid(0.0f, 0.0f, 0.0f, 2.0f, 4.0f, 1.0f), ModelTransform.of(-1.0f, -6.0f, -1.0f, 0.0f, -1.5707964f, 0.0f));
        return TexturedModelData.of(lv, 128, 128);
    }
}

