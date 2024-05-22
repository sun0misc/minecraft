/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

public class CraftRequestC2SPacket
implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, CraftRequestC2SPacket> CODEC = Packet.createCodec(CraftRequestC2SPacket::write, CraftRequestC2SPacket::new);
    private final int syncId;
    private final Identifier recipeId;
    private final boolean craftAll;

    public CraftRequestC2SPacket(int syncId, RecipeEntry<?> recipe, boolean craftAll) {
        this.syncId = syncId;
        this.recipeId = recipe.id();
        this.craftAll = craftAll;
    }

    private CraftRequestC2SPacket(PacketByteBuf buf) {
        this.syncId = buf.readByte();
        this.recipeId = buf.readIdentifier();
        this.craftAll = buf.readBoolean();
    }

    private void write(PacketByteBuf buf) {
        buf.writeByte(this.syncId);
        buf.writeIdentifier(this.recipeId);
        buf.writeBoolean(this.craftAll);
    }

    @Override
    public PacketType<CraftRequestC2SPacket> getPacketId() {
        return PlayPackets.PLACE_RECIPE;
    }

    @Override
    public void apply(ServerPlayPacketListener arg) {
        arg.onCraftRequest(this);
    }

    public int getSyncId() {
        return this.syncId;
    }

    public Identifier getRecipeId() {
        return this.recipeId;
    }

    public boolean shouldCraftAll() {
        return this.craftAll;
    }
}

