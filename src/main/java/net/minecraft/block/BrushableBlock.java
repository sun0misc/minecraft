/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.LandingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class BrushableBlock
extends BlockWithEntity
implements LandingBlock {
    public static final MapCodec<BrushableBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Registries.BLOCK.getCodec().fieldOf("turns_into")).forGetter(BrushableBlock::getBaseBlock), ((MapCodec)Registries.SOUND_EVENT.getCodec().fieldOf("brush_sound")).forGetter(BrushableBlock::getBrushingSound), ((MapCodec)Registries.SOUND_EVENT.getCodec().fieldOf("brush_comleted_sound")).forGetter(BrushableBlock::getBrushingCompleteSound), BrushableBlock.createSettingsCodec()).apply((Applicative<BrushableBlock, ?>)instance, BrushableBlock::new));
    private static final IntProperty DUSTED = Properties.DUSTED;
    public static final int field_42773 = 2;
    private final Block baseBlock;
    private final SoundEvent brushingSound;
    private final SoundEvent brushingCompleteSound;

    public MapCodec<BrushableBlock> getCodec() {
        return CODEC;
    }

    public BrushableBlock(Block baseBlock, SoundEvent brushingSound, SoundEvent brushingCompleteSound, AbstractBlock.Settings settings) {
        super(settings);
        this.baseBlock = baseBlock;
        this.brushingSound = brushingSound;
        this.brushingCompleteSound = brushingCompleteSound;
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(DUSTED, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DUSTED);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleBlockTick(pos, this, 2);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        world.scheduleBlockTick(pos, this, 2);
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BrushableBlockEntity) {
            BrushableBlockEntity lv = (BrushableBlockEntity)blockEntity;
            lv.scheduledTick();
        }
        if (!FallingBlock.canFallThrough(world.getBlockState(pos.down())) || pos.getY() < world.getBottomY()) {
            return;
        }
        FallingBlockEntity lv2 = FallingBlockEntity.spawnFromBlock(world, pos, state);
        lv2.setDestroyedOnLanding();
    }

    @Override
    public void onDestroyedOnLanding(World world, BlockPos pos, FallingBlockEntity fallingBlockEntity) {
        Vec3d lv = fallingBlockEntity.getBoundingBox().getCenter();
        world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, BlockPos.ofFloored(lv), Block.getRawIdFromState(fallingBlockEntity.getBlockState()));
        world.emitGameEvent((Entity)fallingBlockEntity, GameEvent.BLOCK_DESTROY, lv);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        BlockPos lv;
        if (random.nextInt(16) == 0 && FallingBlock.canFallThrough(world.getBlockState(lv = pos.down()))) {
            double d = (double)pos.getX() + random.nextDouble();
            double e = (double)pos.getY() - 0.05;
            double f = (double)pos.getZ() + random.nextDouble();
            world.addParticle(new BlockStateParticleEffect(ParticleTypes.FALLING_DUST, state), d, e, f, 0.0, 0.0, 0.0);
        }
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BrushableBlockEntity(pos, state);
    }

    public Block getBaseBlock() {
        return this.baseBlock;
    }

    public SoundEvent getBrushingSound() {
        return this.brushingSound;
    }

    public SoundEvent getBrushingCompleteSound() {
        return this.brushingCompleteSound;
    }
}

