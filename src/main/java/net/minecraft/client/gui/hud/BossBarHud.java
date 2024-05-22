/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class BossBarHud {
    private static final int WIDTH = 182;
    private static final int HEIGHT = 5;
    private static final Identifier[] BACKGROUND_TEXTURES = new Identifier[]{Identifier.method_60656("boss_bar/pink_background"), Identifier.method_60656("boss_bar/blue_background"), Identifier.method_60656("boss_bar/red_background"), Identifier.method_60656("boss_bar/green_background"), Identifier.method_60656("boss_bar/yellow_background"), Identifier.method_60656("boss_bar/purple_background"), Identifier.method_60656("boss_bar/white_background")};
    private static final Identifier[] PROGRESS_TEXTURES = new Identifier[]{Identifier.method_60656("boss_bar/pink_progress"), Identifier.method_60656("boss_bar/blue_progress"), Identifier.method_60656("boss_bar/red_progress"), Identifier.method_60656("boss_bar/green_progress"), Identifier.method_60656("boss_bar/yellow_progress"), Identifier.method_60656("boss_bar/purple_progress"), Identifier.method_60656("boss_bar/white_progress")};
    private static final Identifier[] NOTCHED_BACKGROUND_TEXTURES = new Identifier[]{Identifier.method_60656("boss_bar/notched_6_background"), Identifier.method_60656("boss_bar/notched_10_background"), Identifier.method_60656("boss_bar/notched_12_background"), Identifier.method_60656("boss_bar/notched_20_background")};
    private static final Identifier[] NOTCHED_PROGRESS_TEXTURES = new Identifier[]{Identifier.method_60656("boss_bar/notched_6_progress"), Identifier.method_60656("boss_bar/notched_10_progress"), Identifier.method_60656("boss_bar/notched_12_progress"), Identifier.method_60656("boss_bar/notched_20_progress")};
    private final MinecraftClient client;
    final Map<UUID, ClientBossBar> bossBars = Maps.newLinkedHashMap();

    public BossBarHud(MinecraftClient client) {
        this.client = client;
    }

    public void render(DrawContext context) {
        if (this.bossBars.isEmpty()) {
            return;
        }
        this.client.getProfiler().push("bossHealth");
        int i = context.getScaledWindowWidth();
        int j = 12;
        for (ClientBossBar lv : this.bossBars.values()) {
            int k = i / 2 - 91;
            int l = j;
            this.renderBossBar(context, k, l, lv);
            Text lv2 = lv.getName();
            int m = this.client.textRenderer.getWidth(lv2);
            int n = i / 2 - m / 2;
            int o = l - 9;
            context.drawTextWithShadow(this.client.textRenderer, lv2, n, o, 0xFFFFFF);
            if ((j += 10 + this.client.textRenderer.fontHeight) < context.getScaledWindowHeight() / 3) continue;
            break;
        }
        this.client.getProfiler().pop();
    }

    private void renderBossBar(DrawContext context, int x, int y, BossBar bossBar) {
        this.renderBossBar(context, x, y, bossBar, 182, BACKGROUND_TEXTURES, NOTCHED_BACKGROUND_TEXTURES);
        int k = MathHelper.lerpPositive(bossBar.getPercent(), 0, 182);
        if (k > 0) {
            this.renderBossBar(context, x, y, bossBar, k, PROGRESS_TEXTURES, NOTCHED_PROGRESS_TEXTURES);
        }
    }

    private void renderBossBar(DrawContext context, int x, int y, BossBar bossBar, int width, Identifier[] textures, Identifier[] notchedTextures) {
        RenderSystem.enableBlend();
        context.drawGuiTexture(textures[bossBar.getColor().ordinal()], 182, 5, 0, 0, x, y, width, 5);
        if (bossBar.getStyle() != BossBar.Style.PROGRESS) {
            context.drawGuiTexture(notchedTextures[bossBar.getStyle().ordinal() - 1], 182, 5, 0, 0, x, y, width, 5);
        }
        RenderSystem.disableBlend();
    }

    public void handlePacket(BossBarS2CPacket packet) {
        packet.accept(new BossBarS2CPacket.Consumer(){

            @Override
            public void add(UUID uuid, Text name, float percent, BossBar.Color color, BossBar.Style style, boolean darkenSky, boolean dragonMusic, boolean thickenFog) {
                BossBarHud.this.bossBars.put(uuid, new ClientBossBar(uuid, name, percent, color, style, darkenSky, dragonMusic, thickenFog));
            }

            @Override
            public void remove(UUID uuid) {
                BossBarHud.this.bossBars.remove(uuid);
            }

            @Override
            public void updateProgress(UUID uuid, float percent) {
                BossBarHud.this.bossBars.get(uuid).setPercent(percent);
            }

            @Override
            public void updateName(UUID uuid, Text name) {
                BossBarHud.this.bossBars.get(uuid).setName(name);
            }

            @Override
            public void updateStyle(UUID id, BossBar.Color color, BossBar.Style style) {
                ClientBossBar lv = BossBarHud.this.bossBars.get(id);
                lv.setColor(color);
                lv.setStyle(style);
            }

            @Override
            public void updateProperties(UUID uuid, boolean darkenSky, boolean dragonMusic, boolean thickenFog) {
                ClientBossBar lv = BossBarHud.this.bossBars.get(uuid);
                lv.setDarkenSky(darkenSky);
                lv.setDragonMusic(dragonMusic);
                lv.setThickenFog(thickenFog);
            }
        });
    }

    public void clear() {
        this.bossBars.clear();
    }

    public boolean shouldPlayDragonMusic() {
        if (!this.bossBars.isEmpty()) {
            for (BossBar bossBar : this.bossBars.values()) {
                if (!bossBar.hasDragonMusic()) continue;
                return true;
            }
        }
        return false;
    }

    public boolean shouldDarkenSky() {
        if (!this.bossBars.isEmpty()) {
            for (BossBar bossBar : this.bossBars.values()) {
                if (!bossBar.shouldDarkenSky()) continue;
                return true;
            }
        }
        return false;
    }

    public boolean shouldThickenFog() {
        if (!this.bossBars.isEmpty()) {
            for (BossBar bossBar : this.bossBars.values()) {
                if (!bossBar.shouldThickenFog()) continue;
                return true;
            }
        }
        return false;
    }
}

