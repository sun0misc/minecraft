/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.enums;

import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.vault.VaultConfig;
import net.minecraft.block.vault.VaultServerData;
import net.minecraft.block.vault.VaultSharedData;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldEvents;

public enum VaultState implements StringIdentifiable
{
    INACTIVE("inactive", Light.HALF_LIT){

        @Override
        protected void onChangedTo(ServerWorld world, BlockPos pos, VaultConfig config, VaultSharedData sharedData, boolean ominous) {
            sharedData.setDisplayItem(ItemStack.EMPTY);
            world.syncWorldEvent(WorldEvents.VAULT_DEACTIVATES, pos, ominous ? 1 : 0);
        }
    }
    ,
    ACTIVE("active", Light.LIT){

        @Override
        protected void onChangedTo(ServerWorld world, BlockPos pos, VaultConfig config, VaultSharedData sharedData, boolean ominous) {
            if (!sharedData.hasDisplayItem()) {
                VaultBlockEntity.Server.updateDisplayItem(world, this, config, sharedData, pos);
            }
            world.syncWorldEvent(WorldEvents.VAULT_ACTIVATES, pos, ominous ? 1 : 0);
        }
    }
    ,
    UNLOCKING("unlocking", Light.LIT){

        @Override
        protected void onChangedTo(ServerWorld world, BlockPos pos, VaultConfig config, VaultSharedData sharedData, boolean ominous) {
            world.playSound(null, pos, SoundEvents.BLOCK_VAULT_INSERT_ITEM, SoundCategory.BLOCKS);
        }
    }
    ,
    EJECTING("ejecting", Light.LIT){

        @Override
        protected void onChangedTo(ServerWorld world, BlockPos pos, VaultConfig config, VaultSharedData sharedData, boolean ominous) {
            world.playSound(null, pos, SoundEvents.BLOCK_VAULT_OPEN_SHUTTER, SoundCategory.BLOCKS);
        }

        @Override
        protected void onChangedFrom(ServerWorld world, BlockPos pos, VaultConfig config, VaultSharedData sharedData) {
            world.playSound(null, pos, SoundEvents.BLOCK_VAULT_CLOSE_SHUTTER, SoundCategory.BLOCKS);
        }
    };

    private static final int field_48903 = 20;
    private static final int field_48904 = 20;
    private static final int field_48905 = 20;
    private static final int field_48906 = 20;
    private final String id;
    private final Light light;

    VaultState(String id, Light light) {
        this.id = id;
        this.light = light;
    }

    @Override
    public String asString() {
        return this.id;
    }

    public int getLuminance() {
        return this.light.luminance;
    }

    public VaultState update(ServerWorld world, BlockPos pos, VaultConfig config, VaultServerData serverData, VaultSharedData sharedData) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> VaultState.updateActiveState(world, pos, config, serverData, sharedData, config.activationRange());
            case 1 -> VaultState.updateActiveState(world, pos, config, serverData, sharedData, config.deactivationRange());
            case 2 -> {
                serverData.setStateUpdatingResumeTime(world.getTime() + 20L);
                yield EJECTING;
            }
            case 3 -> {
                if (serverData.getItemsToEject().isEmpty()) {
                    serverData.finishEjecting();
                    yield VaultState.updateActiveState(world, pos, config, serverData, sharedData, config.deactivationRange());
                }
                float f = serverData.getEjectSoundPitchModifier();
                this.ejectItem(world, pos, serverData.getItemToEject(), f);
                sharedData.setDisplayItem(serverData.getItemToDisplay());
                boolean bl = serverData.getItemsToEject().isEmpty();
                int i = bl ? 20 : 20;
                serverData.setStateUpdatingResumeTime(world.getTime() + (long)i);
                yield EJECTING;
            }
        };
    }

    private static VaultState updateActiveState(ServerWorld world, BlockPos pos, VaultConfig config, VaultServerData serverData, VaultSharedData sharedData, double radius) {
        sharedData.updateConnectedPlayers(world, pos, serverData, config, radius);
        serverData.setStateUpdatingResumeTime(world.getTime() + 20L);
        return sharedData.hasConnectedPlayers() ? ACTIVE : INACTIVE;
    }

    public void onStateChange(ServerWorld world, BlockPos pos, VaultState newState, VaultConfig config, VaultSharedData sharedData, boolean ominous) {
        this.onChangedFrom(world, pos, config, sharedData);
        newState.onChangedTo(world, pos, config, sharedData, ominous);
    }

    protected void onChangedTo(ServerWorld world, BlockPos pos, VaultConfig config, VaultSharedData sharedData, boolean ominous) {
    }

    protected void onChangedFrom(ServerWorld world, BlockPos pos, VaultConfig config, VaultSharedData sharedData) {
    }

    private void ejectItem(ServerWorld world, BlockPos pos, ItemStack stack, float pitchModifier) {
        ItemDispenserBehavior.spawnItem(world, stack, 2, Direction.UP, Vec3d.ofBottomCenter(pos).offset(Direction.UP, 1.2));
        world.syncWorldEvent(WorldEvents.VAULT_EJECTS_ITEM, pos, 0);
        world.playSound(null, pos, SoundEvents.BLOCK_VAULT_EJECT_ITEM, SoundCategory.BLOCKS, 1.0f, 0.8f + 0.4f * pitchModifier);
    }

    static enum Light {
        HALF_LIT(6),
        LIT(12);

        final int luminance;

        private Light(int luminance) {
            this.luminance = luminance;
        }
    }
}

