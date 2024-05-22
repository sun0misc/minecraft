/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.packet.c2s.play;

import java.util.function.Function;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class PlayerInteractEntityC2SPacket
implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, PlayerInteractEntityC2SPacket> CODEC = Packet.createCodec(PlayerInteractEntityC2SPacket::write, PlayerInteractEntityC2SPacket::new);
    private final int entityId;
    private final InteractTypeHandler type;
    private final boolean playerSneaking;
    static final InteractTypeHandler ATTACK = new InteractTypeHandler(){

        @Override
        public InteractType getType() {
            return InteractType.ATTACK;
        }

        @Override
        public void handle(Handler handler) {
            handler.attack();
        }

        @Override
        public void write(PacketByteBuf buf) {
        }
    };

    private PlayerInteractEntityC2SPacket(int entityId, boolean playerSneaking, InteractTypeHandler type) {
        this.entityId = entityId;
        this.type = type;
        this.playerSneaking = playerSneaking;
    }

    public static PlayerInteractEntityC2SPacket attack(Entity entity, boolean playerSneaking) {
        return new PlayerInteractEntityC2SPacket(entity.getId(), playerSneaking, ATTACK);
    }

    public static PlayerInteractEntityC2SPacket interact(Entity entity, boolean playerSneaking, Hand hand) {
        return new PlayerInteractEntityC2SPacket(entity.getId(), playerSneaking, new InteractHandler(hand));
    }

    public static PlayerInteractEntityC2SPacket interactAt(Entity entity, boolean playerSneaking, Hand hand, Vec3d pos) {
        return new PlayerInteractEntityC2SPacket(entity.getId(), playerSneaking, new InteractAtHandler(hand, pos));
    }

    private PlayerInteractEntityC2SPacket(PacketByteBuf buf) {
        this.entityId = buf.readVarInt();
        InteractType lv = buf.readEnumConstant(InteractType.class);
        this.type = lv.handlerGetter.apply(buf);
        this.playerSneaking = buf.readBoolean();
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeEnumConstant(this.type.getType());
        this.type.write(buf);
        buf.writeBoolean(this.playerSneaking);
    }

    @Override
    public PacketType<PlayerInteractEntityC2SPacket> getPacketId() {
        return PlayPackets.INTERACT;
    }

    @Override
    public void apply(ServerPlayPacketListener arg) {
        arg.onPlayerInteractEntity(this);
    }

    @Nullable
    public Entity getEntity(ServerWorld world) {
        return world.getDragonPart(this.entityId);
    }

    public boolean isPlayerSneaking() {
        return this.playerSneaking;
    }

    public void handle(Handler handler) {
        this.type.handle(handler);
    }

    static interface InteractTypeHandler {
        public InteractType getType();

        public void handle(Handler var1);

        public void write(PacketByteBuf var1);
    }

    static class InteractHandler
    implements InteractTypeHandler {
        private final Hand hand;

        InteractHandler(Hand hand) {
            this.hand = hand;
        }

        private InteractHandler(PacketByteBuf buf) {
            this.hand = buf.readEnumConstant(Hand.class);
        }

        @Override
        public InteractType getType() {
            return InteractType.INTERACT;
        }

        @Override
        public void handle(Handler handler) {
            handler.interact(this.hand);
        }

        @Override
        public void write(PacketByteBuf buf) {
            buf.writeEnumConstant(this.hand);
        }
    }

    static class InteractAtHandler
    implements InteractTypeHandler {
        private final Hand hand;
        private final Vec3d pos;

        InteractAtHandler(Hand hand, Vec3d pos) {
            this.hand = hand;
            this.pos = pos;
        }

        private InteractAtHandler(PacketByteBuf buf) {
            this.pos = new Vec3d(buf.readFloat(), buf.readFloat(), buf.readFloat());
            this.hand = buf.readEnumConstant(Hand.class);
        }

        @Override
        public InteractType getType() {
            return InteractType.INTERACT_AT;
        }

        @Override
        public void handle(Handler handler) {
            handler.interactAt(this.hand, this.pos);
        }

        @Override
        public void write(PacketByteBuf buf) {
            buf.writeFloat((float)this.pos.x);
            buf.writeFloat((float)this.pos.y);
            buf.writeFloat((float)this.pos.z);
            buf.writeEnumConstant(this.hand);
        }
    }

    static enum InteractType {
        INTERACT(InteractHandler::new),
        ATTACK(buf -> ATTACK),
        INTERACT_AT(InteractAtHandler::new);

        final Function<PacketByteBuf, InteractTypeHandler> handlerGetter;

        private InteractType(Function<PacketByteBuf, InteractTypeHandler> handlerGetter) {
            this.handlerGetter = handlerGetter;
        }
    }

    public static interface Handler {
        public void interact(Hand var1);

        public void interactAt(Hand var1, Vec3d var2);

        public void attack();
    }
}

