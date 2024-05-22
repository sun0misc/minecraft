/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class EntityAttributesS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, EntityAttributesS2CPacket> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, EntityAttributesS2CPacket::getEntityId, Entry.CODEC.collect(PacketCodecs.toList()), EntityAttributesS2CPacket::getEntries, EntityAttributesS2CPacket::new);
    private final int entityId;
    private final List<Entry> entries;

    public EntityAttributesS2CPacket(int entityId, Collection<EntityAttributeInstance> attributes) {
        this.entityId = entityId;
        this.entries = Lists.newArrayList();
        for (EntityAttributeInstance lv : attributes) {
            this.entries.add(new Entry(lv.getAttribute(), lv.getBaseValue(), lv.getModifiers()));
        }
    }

    private EntityAttributesS2CPacket(int entityId, List<Entry> attributes) {
        this.entityId = entityId;
        this.entries = attributes;
    }

    @Override
    public PacketType<EntityAttributesS2CPacket> getPacketId() {
        return PlayPackets.UPDATE_ATTRIBUTES;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onEntityAttributes(this);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public List<Entry> getEntries() {
        return this.entries;
    }

    public record Entry(RegistryEntry<EntityAttribute> attribute, double base, Collection<EntityAttributeModifier> modifiers) {
        public static final PacketCodec<ByteBuf, EntityAttributeModifier> MODIFIER_CODEC = PacketCodec.tuple(Identifier.PACKET_CODEC, EntityAttributeModifier::uuid, PacketCodecs.DOUBLE, EntityAttributeModifier::value, EntityAttributeModifier.Operation.PACKET_CODEC, EntityAttributeModifier::operation, EntityAttributeModifier::new);
        public static final PacketCodec<RegistryByteBuf, Entry> CODEC = PacketCodec.tuple(EntityAttribute.PACKET_CODEC, Entry::attribute, PacketCodecs.DOUBLE, Entry::base, MODIFIER_CODEC.collect(PacketCodecs.toCollection(ArrayList::new)), Entry::modifiers, Entry::new);
    }
}

