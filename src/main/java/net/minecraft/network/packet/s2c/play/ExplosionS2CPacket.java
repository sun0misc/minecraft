package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ExplosionS2CPacket implements Packet {
   private final double x;
   private final double y;
   private final double z;
   private final float radius;
   private final List affectedBlocks;
   private final float playerVelocityX;
   private final float playerVelocityY;
   private final float playerVelocityZ;

   public ExplosionS2CPacket(double x, double y, double z, float radius, List affectedBlocks, @Nullable Vec3d playerVelocity) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.radius = radius;
      this.affectedBlocks = Lists.newArrayList(affectedBlocks);
      if (playerVelocity != null) {
         this.playerVelocityX = (float)playerVelocity.x;
         this.playerVelocityY = (float)playerVelocity.y;
         this.playerVelocityZ = (float)playerVelocity.z;
      } else {
         this.playerVelocityX = 0.0F;
         this.playerVelocityY = 0.0F;
         this.playerVelocityZ = 0.0F;
      }

   }

   public ExplosionS2CPacket(PacketByteBuf buf) {
      this.x = buf.readDouble();
      this.y = buf.readDouble();
      this.z = buf.readDouble();
      this.radius = buf.readFloat();
      int i = MathHelper.floor(this.x);
      int j = MathHelper.floor(this.y);
      int k = MathHelper.floor(this.z);
      this.affectedBlocks = buf.readList((buf2) -> {
         int l = buf2.readByte() + i;
         int m = buf2.readByte() + j;
         int n = buf2.readByte() + k;
         return new BlockPos(l, m, n);
      });
      this.playerVelocityX = buf.readFloat();
      this.playerVelocityY = buf.readFloat();
      this.playerVelocityZ = buf.readFloat();
   }

   public void write(PacketByteBuf buf) {
      buf.writeDouble(this.x);
      buf.writeDouble(this.y);
      buf.writeDouble(this.z);
      buf.writeFloat(this.radius);
      int i = MathHelper.floor(this.x);
      int j = MathHelper.floor(this.y);
      int k = MathHelper.floor(this.z);
      buf.writeCollection(this.affectedBlocks, (buf2, pos) -> {
         int l = pos.getX() - i;
         int m = pos.getY() - j;
         int n = pos.getZ() - k;
         buf2.writeByte(l);
         buf2.writeByte(m);
         buf2.writeByte(n);
      });
      buf.writeFloat(this.playerVelocityX);
      buf.writeFloat(this.playerVelocityY);
      buf.writeFloat(this.playerVelocityZ);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onExplosion(this);
   }

   public float getPlayerVelocityX() {
      return this.playerVelocityX;
   }

   public float getPlayerVelocityY() {
      return this.playerVelocityY;
   }

   public float getPlayerVelocityZ() {
      return this.playerVelocityZ;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public float getRadius() {
      return this.radius;
   }

   public List getAffectedBlocks() {
      return this.affectedBlocks;
   }
}
