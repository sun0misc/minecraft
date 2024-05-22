/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft;

import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.util.Colors;

@Environment(value=EnvType.CLIENT)
public class class_9805
implements TooltipComponent {
    private static final int field_52140 = 10;
    private static final int field_52141 = 2;
    private final List<ProfileResult> field_52142;

    public class_9805(class_9806 arg) {
        this.field_52142 = arg.profiles();
    }

    @Override
    public int getHeight() {
        return this.field_52142.size() * 12 + 2;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        int i = 0;
        for (ProfileResult profileResult : this.field_52142) {
            int j = textRenderer.getWidth(profileResult.profile().getName());
            if (j <= i) continue;
            i = j;
        }
        return i + 10 + 6;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        for (int k = 0; k < this.field_52142.size(); ++k) {
            ProfileResult profileResult = this.field_52142.get(k);
            int l = y + 2 + k * 12;
            PlayerSkinDrawer.draw(context, MinecraftClient.getInstance().getSkinProvider().getSkinTextures(profileResult.profile()), x + 2, l, 10);
            context.drawTextWithShadow(textRenderer, profileResult.profile().getName(), x + 10 + 4, l + 2, Colors.WHITE);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record class_9806(List<ProfileResult> profiles) implements TooltipData
    {
    }
}

