package net.minecraft.network;

import io.netty.util.AttributeKey;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.packet.BundlePacket;
import net.minecraft.network.packet.BundleSplitterPacket;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;

public interface PacketBundleHandler {
   AttributeKey KEY = AttributeKey.valueOf("bundler");
   int MAX_PACKETS = 4096;
   PacketBundleHandler NOOP = new PacketBundleHandler() {
      public void forEachPacket(Packet packet, Consumer consumer) {
         consumer.accept(packet);
      }

      @Nullable
      public Bundler createBundler(Packet splitter) {
         return null;
      }
   };

   static PacketBundleHandler create(final Class bundlePacketType, final Function bundleFunction, final BundleSplitterPacket splitter) {
      return new PacketBundleHandler() {
         public void forEachPacket(Packet packet, Consumer consumer) {
            if (packet.getClass() == bundlePacketType) {
               BundlePacket lv = (BundlePacket)packet;
               consumer.accept(splitter);
               lv.getPackets().forEach(consumer);
               consumer.accept(splitter);
            } else {
               consumer.accept(packet);
            }

         }

         @Nullable
         public Bundler createBundler(Packet splitterx) {
            return splitterx == splitter ? new Bundler() {
               private final List packets = new ArrayList();

               @Nullable
               public Packet add(Packet packet) {
                  if (packet == splitter) {
                     return (Packet)bundleFunction.apply(this.packets);
                  } else if (this.packets.size() >= 4096) {
                     throw new IllegalStateException("Too many packets in a bundle");
                  } else {
                     this.packets.add(packet);
                     return null;
                  }
               }
            } : null;
         }
      };
   }

   void forEachPacket(Packet packet, Consumer consumer);

   @Nullable
   Bundler createBundler(Packet splitter);

   public interface BundlerGetter {
      PacketBundleHandler getBundler(NetworkSide side);
   }

   public interface Bundler {
      @Nullable
      Packet add(Packet packet);
   }
}
