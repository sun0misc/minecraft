/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.World;

public class MarkerEntity
extends Entity {
    private static final String DATA_KEY = "data";
    private NbtCompound data = new NbtCompound();

    public MarkerEntity(EntityType<?> arg, World arg2) {
        super(arg, arg2);
        this.noClip = true;
    }

    @Override
    public void tick() {
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.data = nbt.getCompound(DATA_KEY);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.put(DATA_KEY, this.data.copy());
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        throw new IllegalStateException("Markers should never be sent");
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return false;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
    }

    @Override
    public PistonBehavior getPistonBehavior() {
        return PistonBehavior.IGNORE;
    }

    @Override
    public boolean canAvoidTraps() {
        return true;
    }
}

