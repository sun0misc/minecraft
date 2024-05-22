/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.tutorial;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.toast.TutorialToast;
import net.minecraft.client.tutorial.FindTreeTutorialStepHandler;
import net.minecraft.client.tutorial.TutorialManager;
import net.minecraft.client.tutorial.TutorialStep;
import net.minecraft.client.tutorial.TutorialStepHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class PunchTreeTutorialStepHandler
implements TutorialStepHandler {
    private static final int DELAY = 600;
    private static final Text TITLE = Text.translatable("tutorial.punch_tree.title");
    private static final Text DESCRIPTION = Text.translatable("tutorial.punch_tree.description", TutorialManager.keyToText("attack"));
    private final TutorialManager manager;
    private TutorialToast toast;
    private int ticks;
    private int punches;

    public PunchTreeTutorialStepHandler(TutorialManager manager) {
        this.manager = manager;
    }

    @Override
    public void tick() {
        ClientPlayerEntity lv;
        ++this.ticks;
        if (!this.manager.isInSurvival()) {
            this.manager.setStep(TutorialStep.NONE);
            return;
        }
        if (this.ticks == 1 && (lv = this.manager.getClient().player) != null) {
            if (lv.getInventory().contains(ItemTags.LOGS)) {
                this.manager.setStep(TutorialStep.CRAFT_PLANKS);
                return;
            }
            if (FindTreeTutorialStepHandler.hasBrokenTreeBlocks(lv)) {
                this.manager.setStep(TutorialStep.CRAFT_PLANKS);
                return;
            }
        }
        if ((this.ticks >= 600 || this.punches > 3) && this.toast == null) {
            this.toast = new TutorialToast(TutorialToast.Type.TREE, TITLE, DESCRIPTION, true);
            this.manager.getClient().getToastManager().add(this.toast);
        }
    }

    @Override
    public void destroy() {
        if (this.toast != null) {
            this.toast.hide();
            this.toast = null;
        }
    }

    @Override
    public void onBlockBreaking(ClientWorld client, BlockPos pos, BlockState state, float progress) {
        boolean bl = state.isIn(BlockTags.LOGS);
        if (bl && progress > 0.0f) {
            if (this.toast != null) {
                this.toast.setProgress(progress);
            }
            if (progress >= 1.0f) {
                this.manager.setStep(TutorialStep.OPEN_INVENTORY);
            }
        } else if (this.toast != null) {
            this.toast.setProgress(0.0f);
        } else if (bl) {
            ++this.punches;
        }
    }

    @Override
    public void onSlotUpdate(ItemStack stack) {
        if (stack.isIn(ItemTags.LOGS)) {
            this.manager.setStep(TutorialStep.CRAFT_PLANKS);
            return;
        }
    }
}

