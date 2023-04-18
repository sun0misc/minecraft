package net.minecraft.item;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FireworkRocketItem extends Item {
   public static final byte[] FLIGHT_VALUES = new byte[]{1, 2, 3};
   public static final String FIREWORKS_KEY = "Fireworks";
   public static final String EXPLOSION_KEY = "Explosion";
   public static final String EXPLOSIONS_KEY = "Explosions";
   public static final String FLIGHT_KEY = "Flight";
   public static final String TYPE_KEY = "Type";
   public static final String TRAIL_KEY = "Trail";
   public static final String FLICKER_KEY = "Flicker";
   public static final String COLORS_KEY = "Colors";
   public static final String FADE_COLORS_KEY = "FadeColors";
   public static final double OFFSET_POS_MULTIPLIER = 0.15;

   public FireworkRocketItem(Item.Settings arg) {
      super(arg);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World lv = context.getWorld();
      if (!lv.isClient) {
         ItemStack lv2 = context.getStack();
         Vec3d lv3 = context.getHitPos();
         Direction lv4 = context.getSide();
         FireworkRocketEntity lv5 = new FireworkRocketEntity(lv, context.getPlayer(), lv3.x + (double)lv4.getOffsetX() * 0.15, lv3.y + (double)lv4.getOffsetY() * 0.15, lv3.z + (double)lv4.getOffsetZ() * 0.15, lv2);
         lv.spawnEntity(lv5);
         lv2.decrement(1);
      }

      return ActionResult.success(lv.isClient);
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      if (user.isFallFlying()) {
         ItemStack lv = user.getStackInHand(hand);
         if (!world.isClient) {
            FireworkRocketEntity lv2 = new FireworkRocketEntity(world, lv, user);
            world.spawnEntity(lv2);
            if (!user.getAbilities().creativeMode) {
               lv.decrement(1);
            }

            user.incrementStat(Stats.USED.getOrCreateStat(this));
         }

         return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
      } else {
         return TypedActionResult.pass(user.getStackInHand(hand));
      }
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      NbtCompound lv = stack.getSubNbt("Fireworks");
      if (lv != null) {
         if (lv.contains("Flight", NbtElement.NUMBER_TYPE)) {
            tooltip.add(Text.translatable("item.minecraft.firework_rocket.flight").append(ScreenTexts.SPACE).append(String.valueOf(lv.getByte("Flight"))).formatted(Formatting.GRAY));
         }

         NbtList lv2 = lv.getList("Explosions", NbtElement.COMPOUND_TYPE);
         if (!lv2.isEmpty()) {
            for(int i = 0; i < lv2.size(); ++i) {
               NbtCompound lv3 = lv2.getCompound(i);
               List list2 = Lists.newArrayList();
               FireworkStarItem.appendFireworkTooltip(lv3, list2);
               if (!list2.isEmpty()) {
                  for(int j = 1; j < list2.size(); ++j) {
                     list2.set(j, Text.literal("  ").append((Text)list2.get(j)).formatted(Formatting.GRAY));
                  }

                  tooltip.addAll(list2);
               }
            }
         }

      }
   }

   public static void setFlight(ItemStack stack, byte flight) {
      stack.getOrCreateSubNbt("Fireworks").putByte("Flight", flight);
   }

   public ItemStack getDefaultStack() {
      ItemStack lv = new ItemStack(this);
      setFlight(lv, (byte)1);
      return lv;
   }

   public static enum Type {
      SMALL_BALL(0, "small_ball"),
      LARGE_BALL(1, "large_ball"),
      STAR(2, "star"),
      CREEPER(3, "creeper"),
      BURST(4, "burst");

      private static final IntFunction BY_ID = ValueLists.createIdToValueFunction(Type::getId, values(), (ValueLists.OutOfBoundsHandling)ValueLists.OutOfBoundsHandling.ZERO);
      private final int id;
      private final String name;

      private Type(int id, String name) {
         this.id = id;
         this.name = name;
      }

      public int getId() {
         return this.id;
      }

      public String getName() {
         return this.name;
      }

      public static Type byId(int id) {
         return (Type)BY_ID.apply(id);
      }

      // $FF: synthetic method
      private static Type[] method_36677() {
         return new Type[]{SMALL_BALL, LARGE_BALL, STAR, CREEPER, BURST};
      }
   }
}
