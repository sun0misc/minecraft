/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BubbleColumnBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

@Environment(value=EnvType.CLIENT)
public class BubbleColumnSoundPlayer
implements ClientPlayerTickable {
    private final ClientPlayerEntity player;
    private boolean hasPlayedForCurrentColumn;
    private boolean firstTick = true;

    public BubbleColumnSoundPlayer(ClientPlayerEntity player) {
        this.player = player;
    }

    @Override
    public void tick() {
        World lv = this.player.getWorld();
        BlockState lv2 = lv.getStatesInBoxIfLoaded(this.player.getBoundingBox().expand(0.0, -0.4f, 0.0).contract(1.0E-6)).filter(state -> state.isOf(Blocks.BUBBLE_COLUMN)).findFirst().orElse(null);
        if (lv2 != null) {
            if (!this.hasPlayedForCurrentColumn && !this.firstTick && lv2.isOf(Blocks.BUBBLE_COLUMN) && !this.player.isSpectator()) {
                boolean bl = lv2.get(BubbleColumnBlock.DRAG);
                if (bl) {
                    this.player.playSound(SoundEvents.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 1.0f, 1.0f);
                } else {
                    this.player.playSound(SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE, 1.0f, 1.0f);
                }
            }
            this.hasPlayedForCurrentColumn = true;
        } else {
            this.hasPlayedForCurrentColumn = false;
        }
        this.firstTick = false;
    }
}

