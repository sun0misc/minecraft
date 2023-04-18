package net.minecraft.structure;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;

public class WoodlandMansionGenerator {
   public static void addPieces(StructureTemplateManager manager, BlockPos pos, BlockRotation rotation, List pieces, Random random) {
      MansionParameters lv = new MansionParameters(random);
      LayoutGenerator lv2 = new LayoutGenerator(manager, random);
      lv2.generate(pos, rotation, pieces, lv);
   }

   public static void printRandomFloorLayouts(String[] args) {
      Random lv = Random.create();
      long l = lv.nextLong();
      System.out.println("Seed: " + l);
      lv.setSeed(l);
      MansionParameters lv2 = new MansionParameters(lv);
      lv2.printFloorLayouts();
   }

   private static class MansionParameters {
      private static final int SIZE = 11;
      private static final int UNSET = 0;
      private static final int CORRIDOR = 1;
      private static final int ROOM = 2;
      private static final int STAIRCASE = 3;
      private static final int UNUSED = 4;
      private static final int OUTSIDE = 5;
      private static final int SMALL_ROOM_FLAG = 65536;
      private static final int MEDIUM_ROOM_FLAG = 131072;
      private static final int BIG_ROOM_FLAG = 262144;
      private static final int ORIGIN_CELL_FLAG = 1048576;
      private static final int ENTRANCE_CELL_FLAG = 2097152;
      private static final int STAIRCASE_CELL_FLAG = 4194304;
      private static final int CARPET_CELL_FLAG = 8388608;
      private static final int ROOM_SIZE_MASK = 983040;
      private static final int ROOM_ID_MASK = 65535;
      private final Random random;
      final FlagMatrix baseLayout;
      final FlagMatrix thirdFloorLayout;
      final FlagMatrix[] roomFlagsByFloor;
      final int entranceI;
      final int entranceJ;

      public MansionParameters(Random random) {
         this.random = random;
         int i = true;
         this.entranceI = 7;
         this.entranceJ = 4;
         this.baseLayout = new FlagMatrix(SIZE, SIZE, OUTSIDE);
         this.baseLayout.fill(this.entranceI, this.entranceJ, this.entranceI + 1, this.entranceJ + 1, STAIRCASE);
         this.baseLayout.fill(this.entranceI - 1, this.entranceJ, this.entranceI - 1, this.entranceJ + 1, ROOM);
         this.baseLayout.fill(this.entranceI + 2, this.entranceJ - 2, this.entranceI + 3, this.entranceJ + 3, OUTSIDE);
         this.baseLayout.fill(this.entranceI + 1, this.entranceJ - 2, this.entranceI + 1, this.entranceJ - 1, CORRIDOR);
         this.baseLayout.fill(this.entranceI + 1, this.entranceJ + 2, this.entranceI + 1, this.entranceJ + 3, CORRIDOR);
         this.baseLayout.set(this.entranceI - 1, this.entranceJ - 1, CORRIDOR);
         this.baseLayout.set(this.entranceI - 1, this.entranceJ + 2, CORRIDOR);
         this.baseLayout.fill(0, 0, 11, 1, OUTSIDE);
         this.baseLayout.fill(0, 9, 11, 11, OUTSIDE);
         this.layoutCorridor(this.baseLayout, this.entranceI, this.entranceJ - 2, Direction.WEST, 6);
         this.layoutCorridor(this.baseLayout, this.entranceI, this.entranceJ + 3, Direction.WEST, 6);
         this.layoutCorridor(this.baseLayout, this.entranceI - 2, this.entranceJ - 1, Direction.WEST, 3);
         this.layoutCorridor(this.baseLayout, this.entranceI - 2, this.entranceJ + 2, Direction.WEST, 3);

         while(this.adjustLayoutWithRooms(this.baseLayout)) {
         }

         this.roomFlagsByFloor = new FlagMatrix[3];
         this.roomFlagsByFloor[0] = new FlagMatrix(SIZE, SIZE, OUTSIDE);
         this.roomFlagsByFloor[1] = new FlagMatrix(SIZE, SIZE, OUTSIDE);
         this.roomFlagsByFloor[2] = new FlagMatrix(SIZE, SIZE, OUTSIDE);
         this.updateRoomFlags(this.baseLayout, this.roomFlagsByFloor[0]);
         this.updateRoomFlags(this.baseLayout, this.roomFlagsByFloor[1]);
         this.roomFlagsByFloor[0].fill(this.entranceI + 1, this.entranceJ, this.entranceI + 1, this.entranceJ + 1, 8388608);
         this.roomFlagsByFloor[1].fill(this.entranceI + 1, this.entranceJ, this.entranceI + 1, this.entranceJ + 1, 8388608);
         this.thirdFloorLayout = new FlagMatrix(this.baseLayout.n, this.baseLayout.m, OUTSIDE);
         this.layoutThirdFloor();
         this.updateRoomFlags(this.thirdFloorLayout, this.roomFlagsByFloor[2]);
      }

      public static boolean isInsideMansion(FlagMatrix layout, int i, int j) {
         int k = layout.get(i, j);
         return k == CORRIDOR || k == ROOM || k == STAIRCASE || k == UNUSED;
      }

      public boolean isRoomId(FlagMatrix layout, int i, int j, int floor, int roomId) {
         return (this.roomFlagsByFloor[floor].get(i, j) & '\uffff') == roomId;
      }

      @Nullable
      public Direction findConnectedRoomDirection(FlagMatrix layout, int i, int j, int floor, int roomId) {
         Iterator var6 = Direction.Type.HORIZONTAL.iterator();

         Direction lv;
         do {
            if (!var6.hasNext()) {
               return null;
            }

            lv = (Direction)var6.next();
         } while(!this.isRoomId(layout, i + lv.getOffsetX(), j + lv.getOffsetZ(), floor, roomId));

         return lv;
      }

      private void layoutCorridor(FlagMatrix layout, int i, int j, Direction direction, int length) {
         if (length > 0) {
            layout.set(i, j, CORRIDOR);
            layout.update(i + direction.getOffsetX(), j + direction.getOffsetZ(), UNSET, CORRIDOR);

            Direction lv;
            for(int l = 0; l < 8; ++l) {
               lv = Direction.fromHorizontal(this.random.nextInt(4));
               if (lv != direction.getOpposite() && (lv != Direction.EAST || !this.random.nextBoolean())) {
                  int m = i + direction.getOffsetX();
                  int n = j + direction.getOffsetZ();
                  if (layout.get(m + lv.getOffsetX(), n + lv.getOffsetZ()) == 0 && layout.get(m + lv.getOffsetX() * 2, n + lv.getOffsetZ() * 2) == 0) {
                     this.layoutCorridor(layout, i + direction.getOffsetX() + lv.getOffsetX(), j + direction.getOffsetZ() + lv.getOffsetZ(), lv, length - 1);
                     break;
                  }
               }
            }

            Direction lv2 = direction.rotateYClockwise();
            lv = direction.rotateYCounterclockwise();
            layout.update(i + lv2.getOffsetX(), j + lv2.getOffsetZ(), UNSET, ROOM);
            layout.update(i + lv.getOffsetX(), j + lv.getOffsetZ(), UNSET, ROOM);
            layout.update(i + direction.getOffsetX() + lv2.getOffsetX(), j + direction.getOffsetZ() + lv2.getOffsetZ(), UNSET, ROOM);
            layout.update(i + direction.getOffsetX() + lv.getOffsetX(), j + direction.getOffsetZ() + lv.getOffsetZ(), UNSET, ROOM);
            layout.update(i + direction.getOffsetX() * 2, j + direction.getOffsetZ() * 2, UNSET, ROOM);
            layout.update(i + lv2.getOffsetX() * 2, j + lv2.getOffsetZ() * 2, UNSET, ROOM);
            layout.update(i + lv.getOffsetX() * 2, j + lv.getOffsetZ() * 2, UNSET, ROOM);
         }
      }

      private boolean adjustLayoutWithRooms(FlagMatrix layout) {
         boolean bl = false;

         for(int i = 0; i < layout.m; ++i) {
            for(int j = 0; j < layout.n; ++j) {
               if (layout.get(j, i) == 0) {
                  int k = 0;
                  k += isInsideMansion(layout, j + 1, i) ? 1 : 0;
                  k += isInsideMansion(layout, j - 1, i) ? 1 : 0;
                  k += isInsideMansion(layout, j, i + 1) ? 1 : 0;
                  k += isInsideMansion(layout, j, i - 1) ? 1 : 0;
                  if (k >= 3) {
                     layout.set(j, i, ROOM);
                     bl = true;
                  } else if (k == 2) {
                     int l = 0;
                     l += isInsideMansion(layout, j + 1, i + 1) ? 1 : 0;
                     l += isInsideMansion(layout, j - 1, i + 1) ? 1 : 0;
                     l += isInsideMansion(layout, j + 1, i - 1) ? 1 : 0;
                     l += isInsideMansion(layout, j - 1, i - 1) ? 1 : 0;
                     if (l <= 1) {
                        layout.set(j, i, ROOM);
                        bl = true;
                     }
                  }
               }
            }
         }

         return bl;
      }

      private void layoutThirdFloor() {
         List list = Lists.newArrayList();
         FlagMatrix lv = this.roomFlagsByFloor[1];

         int j;
         int l;
         for(int i = 0; i < this.thirdFloorLayout.m; ++i) {
            for(j = 0; j < this.thirdFloorLayout.n; ++j) {
               int k = lv.get(j, i);
               l = k & 983040;
               if (l == 131072 && (k & 2097152) == 2097152) {
                  list.add(new Pair(j, i));
               }
            }
         }

         if (list.isEmpty()) {
            this.thirdFloorLayout.fill(0, 0, this.thirdFloorLayout.n, this.thirdFloorLayout.m, OUTSIDE);
         } else {
            Pair lv2 = (Pair)list.get(this.random.nextInt(list.size()));
            j = lv.get((Integer)lv2.getLeft(), (Integer)lv2.getRight());
            lv.set((Integer)lv2.getLeft(), (Integer)lv2.getRight(), j | 4194304);
            Direction lv3 = this.findConnectedRoomDirection(this.baseLayout, (Integer)lv2.getLeft(), (Integer)lv2.getRight(), 1, j & '\uffff');
            l = (Integer)lv2.getLeft() + lv3.getOffsetX();
            int m = (Integer)lv2.getRight() + lv3.getOffsetZ();

            for(int n = 0; n < this.thirdFloorLayout.m; ++n) {
               for(int o = 0; o < this.thirdFloorLayout.n; ++o) {
                  if (!isInsideMansion(this.baseLayout, o, n)) {
                     this.thirdFloorLayout.set(o, n, OUTSIDE);
                  } else if (o == (Integer)lv2.getLeft() && n == (Integer)lv2.getRight()) {
                     this.thirdFloorLayout.set(o, n, STAIRCASE);
                  } else if (o == l && n == m) {
                     this.thirdFloorLayout.set(o, n, STAIRCASE);
                     this.roomFlagsByFloor[2].set(o, n, 8388608);
                  }
               }
            }

            List list2 = Lists.newArrayList();
            Iterator var14 = Direction.Type.HORIZONTAL.iterator();

            while(var14.hasNext()) {
               Direction lv4 = (Direction)var14.next();
               if (this.thirdFloorLayout.get(l + lv4.getOffsetX(), m + lv4.getOffsetZ()) == 0) {
                  list2.add(lv4);
               }
            }

            if (list2.isEmpty()) {
               this.thirdFloorLayout.fill(0, 0, this.thirdFloorLayout.n, this.thirdFloorLayout.m, OUTSIDE);
               lv.set((Integer)lv2.getLeft(), (Integer)lv2.getRight(), j);
            } else {
               Direction lv5 = (Direction)list2.get(this.random.nextInt(list2.size()));
               this.layoutCorridor(this.thirdFloorLayout, l + lv5.getOffsetX(), m + lv5.getOffsetZ(), lv5, 4);

               while(this.adjustLayoutWithRooms(this.thirdFloorLayout)) {
               }

            }
         }
      }

      private void updateRoomFlags(FlagMatrix layout, FlagMatrix roomFlags) {
         ObjectArrayList objectArrayList = new ObjectArrayList();

         int i;
         for(i = 0; i < layout.m; ++i) {
            for(int j = 0; j < layout.n; ++j) {
               if (layout.get(j, i) == ROOM) {
                  objectArrayList.add(new Pair(j, i));
               }
            }
         }

         Util.shuffle(objectArrayList, this.random);
         i = 10;
         ObjectListIterator var19 = objectArrayList.iterator();

         while(true) {
            int k;
            int l;
            do {
               if (!var19.hasNext()) {
                  return;
               }

               Pair lv = (Pair)var19.next();
               k = (Integer)lv.getLeft();
               l = (Integer)lv.getRight();
            } while(roomFlags.get(k, l) != 0);

            int m = k;
            int n = k;
            int o = l;
            int p = l;
            int q = 65536;
            if (roomFlags.get(k + 1, l) == 0 && roomFlags.get(k, l + 1) == 0 && roomFlags.get(k + 1, l + 1) == 0 && layout.get(k + 1, l) == ROOM && layout.get(k, l + 1) == ROOM && layout.get(k + 1, l + 1) == ROOM) {
               n = k + 1;
               p = l + 1;
               q = 262144;
            } else if (roomFlags.get(k - 1, l) == 0 && roomFlags.get(k, l + 1) == 0 && roomFlags.get(k - 1, l + 1) == 0 && layout.get(k - 1, l) == ROOM && layout.get(k, l + 1) == ROOM && layout.get(k - 1, l + 1) == ROOM) {
               m = k - 1;
               p = l + 1;
               q = 262144;
            } else if (roomFlags.get(k - 1, l) == 0 && roomFlags.get(k, l - 1) == 0 && roomFlags.get(k - 1, l - 1) == 0 && layout.get(k - 1, l) == ROOM && layout.get(k, l - 1) == ROOM && layout.get(k - 1, l - 1) == ROOM) {
               m = k - 1;
               o = l - 1;
               q = 262144;
            } else if (roomFlags.get(k + 1, l) == 0 && layout.get(k + 1, l) == ROOM) {
               n = k + 1;
               q = 131072;
            } else if (roomFlags.get(k, l + 1) == 0 && layout.get(k, l + 1) == ROOM) {
               p = l + 1;
               q = 131072;
            } else if (roomFlags.get(k - 1, l) == 0 && layout.get(k - 1, l) == ROOM) {
               m = k - 1;
               q = 131072;
            } else if (roomFlags.get(k, l - 1) == 0 && layout.get(k, l - 1) == ROOM) {
               o = l - 1;
               q = 131072;
            }

            int r = this.random.nextBoolean() ? m : n;
            int s = this.random.nextBoolean() ? o : p;
            int t = 2097152;
            if (!layout.anyMatchAround(r, s, CORRIDOR)) {
               r = r == m ? n : m;
               s = s == o ? p : o;
               if (!layout.anyMatchAround(r, s, CORRIDOR)) {
                  s = s == o ? p : o;
                  if (!layout.anyMatchAround(r, s, CORRIDOR)) {
                     r = r == m ? n : m;
                     s = s == o ? p : o;
                     if (!layout.anyMatchAround(r, s, CORRIDOR)) {
                        t = 0;
                        r = m;
                        s = o;
                     }
                  }
               }
            }

            for(int u = o; u <= p; ++u) {
               for(int v = m; v <= n; ++v) {
                  if (v == r && u == s) {
                     roomFlags.set(v, u, 1048576 | t | q | i);
                  } else {
                     roomFlags.set(v, u, q | i);
                  }
               }
            }

            ++i;
         }
      }

      public void printFloorLayouts() {
         for(int i = 0; i < 2; ++i) {
            FlagMatrix lv = i == 0 ? this.baseLayout : this.thirdFloorLayout;

            for(int j = 0; j < lv.m; ++j) {
               for(int k = 0; k < lv.n; ++k) {
                  int l = lv.get(k, j);
                  if (l == CORRIDOR) {
                     System.out.print("+");
                  } else if (l == UNUSED) {
                     System.out.print("x");
                  } else if (l == ROOM) {
                     System.out.print("X");
                  } else if (l == STAIRCASE) {
                     System.out.print("O");
                  } else if (l == OUTSIDE) {
                     System.out.print("#");
                  } else {
                     System.out.print(" ");
                  }
               }

               System.out.println("");
            }

            System.out.println("");
         }

      }
   }

   static class LayoutGenerator {
      private final StructureTemplateManager manager;
      private final Random random;
      private int entranceI;
      private int entranceJ;

      public LayoutGenerator(StructureTemplateManager manager, Random random) {
         this.manager = manager;
         this.random = random;
      }

      public void generate(BlockPos pos, BlockRotation rotation, List pieces, MansionParameters parameters) {
         GenerationPiece lv = new GenerationPiece();
         lv.position = pos;
         lv.rotation = rotation;
         lv.template = "wall_flat";
         GenerationPiece lv2 = new GenerationPiece();
         this.addEntrance(pieces, lv);
         lv2.position = lv.position.up(8);
         lv2.rotation = lv.rotation;
         lv2.template = "wall_window";
         if (!pieces.isEmpty()) {
         }

         FlagMatrix lv3 = parameters.baseLayout;
         FlagMatrix lv4 = parameters.thirdFloorLayout;
         this.entranceI = parameters.entranceI + 1;
         this.entranceJ = parameters.entranceJ + 1;
         int i = parameters.entranceI + 1;
         int j = parameters.entranceJ;
         this.addOuterWall(pieces, lv, lv3, Direction.SOUTH, this.entranceI, this.entranceJ, i, j);
         this.addOuterWall(pieces, lv2, lv3, Direction.SOUTH, this.entranceI, this.entranceJ, i, j);
         GenerationPiece lv5 = new GenerationPiece();
         lv5.position = lv.position.up(19);
         lv5.rotation = lv.rotation;
         lv5.template = "wall_window";
         boolean bl = false;

         int l;
         for(int k = 0; k < lv4.m && !bl; ++k) {
            for(l = lv4.n - 1; l >= 0 && !bl; --l) {
               if (WoodlandMansionGenerator.MansionParameters.isInsideMansion(lv4, l, k)) {
                  lv5.position = lv5.position.offset(rotation.rotate(Direction.SOUTH), 8 + (k - this.entranceJ) * 8);
                  lv5.position = lv5.position.offset(rotation.rotate(Direction.EAST), (l - this.entranceI) * 8);
                  this.addWallPiece(pieces, lv5);
                  this.addOuterWall(pieces, lv5, lv4, Direction.SOUTH, l, k, l, k);
                  bl = true;
               }
            }
         }

         this.addRoof(pieces, pos.up(16), rotation, lv3, lv4);
         this.addRoof(pieces, pos.up(27), rotation, lv4, (FlagMatrix)null);
         if (!pieces.isEmpty()) {
         }

         RoomPool[] lvs = new RoomPool[]{new FirstFloorRoomPool(), new SecondFloorRoomPool(), new ThirdFloorRoomPool()};

         for(l = 0; l < 3; ++l) {
            BlockPos lv6 = pos.up(8 * l + (l == 2 ? 3 : 0));
            FlagMatrix lv7 = parameters.roomFlagsByFloor[l];
            FlagMatrix lv8 = l == 2 ? lv4 : lv3;
            String string = l == 0 ? "carpet_south_1" : "carpet_south_2";
            String string2 = l == 0 ? "carpet_west_1" : "carpet_west_2";

            for(int m = 0; m < lv8.m; ++m) {
               for(int n = 0; n < lv8.n; ++n) {
                  if (lv8.get(n, m) == WoodlandMansionGenerator.MansionParameters.CORRIDOR) {
                     BlockPos lv9 = lv6.offset(rotation.rotate(Direction.SOUTH), 8 + (m - this.entranceJ) * 8);
                     lv9 = lv9.offset(rotation.rotate(Direction.EAST), (n - this.entranceI) * 8);
                     pieces.add(new Piece(this.manager, "corridor_floor", lv9, rotation));
                     if (lv8.get(n, m - 1) == WoodlandMansionGenerator.MansionParameters.CORRIDOR || (lv7.get(n, m - 1) & 8388608) == 8388608) {
                        pieces.add(new Piece(this.manager, "carpet_north", lv9.offset((Direction)rotation.rotate(Direction.EAST), 1).up(), rotation));
                     }

                     if (lv8.get(n + 1, m) == WoodlandMansionGenerator.MansionParameters.CORRIDOR || (lv7.get(n + 1, m) & 8388608) == 8388608) {
                        pieces.add(new Piece(this.manager, "carpet_east", lv9.offset((Direction)rotation.rotate(Direction.SOUTH), 1).offset((Direction)rotation.rotate(Direction.EAST), 5).up(), rotation));
                     }

                     if (lv8.get(n, m + 1) == WoodlandMansionGenerator.MansionParameters.CORRIDOR || (lv7.get(n, m + 1) & 8388608) == 8388608) {
                        pieces.add(new Piece(this.manager, string, lv9.offset((Direction)rotation.rotate(Direction.SOUTH), 5).offset((Direction)rotation.rotate(Direction.WEST), 1), rotation));
                     }

                     if (lv8.get(n - 1, m) == WoodlandMansionGenerator.MansionParameters.CORRIDOR || (lv7.get(n - 1, m) & 8388608) == 8388608) {
                        pieces.add(new Piece(this.manager, string2, lv9.offset((Direction)rotation.rotate(Direction.WEST), 1).offset((Direction)rotation.rotate(Direction.NORTH), 1), rotation));
                     }
                  }
               }
            }

            String string3 = l == 0 ? "indoors_wall_1" : "indoors_wall_2";
            String string4 = l == 0 ? "indoors_door_1" : "indoors_door_2";
            List list2 = Lists.newArrayList();

            for(int o = 0; o < lv8.m; ++o) {
               for(int p = 0; p < lv8.n; ++p) {
                  boolean bl2 = l == 2 && lv8.get(p, o) == WoodlandMansionGenerator.MansionParameters.STAIRCASE;
                  if (lv8.get(p, o) == WoodlandMansionGenerator.MansionParameters.ROOM || bl2) {
                     int q = lv7.get(p, o);
                     int r = q & 983040;
                     int s = q & '\uffff';
                     bl2 = bl2 && (q & 8388608) == 8388608;
                     list2.clear();
                     if ((q & 2097152) == 2097152) {
                        Iterator var29 = Direction.Type.HORIZONTAL.iterator();

                        while(var29.hasNext()) {
                           Direction lv10 = (Direction)var29.next();
                           if (lv8.get(p + lv10.getOffsetX(), o + lv10.getOffsetZ()) == WoodlandMansionGenerator.MansionParameters.CORRIDOR) {
                              list2.add(lv10);
                           }
                        }
                     }

                     Direction lv11 = null;
                     if (!list2.isEmpty()) {
                        lv11 = (Direction)list2.get(this.random.nextInt(list2.size()));
                     } else if ((q & 1048576) == 1048576) {
                        lv11 = Direction.UP;
                     }

                     BlockPos lv12 = lv6.offset(rotation.rotate(Direction.SOUTH), 8 + (o - this.entranceJ) * 8);
                     lv12 = lv12.offset(rotation.rotate(Direction.EAST), -1 + (p - this.entranceI) * 8);
                     if (WoodlandMansionGenerator.MansionParameters.isInsideMansion(lv8, p - 1, o) && !parameters.isRoomId(lv8, p - 1, o, l, s)) {
                        pieces.add(new Piece(this.manager, lv11 == Direction.WEST ? string4 : string3, lv12, rotation));
                     }

                     BlockPos lv13;
                     if (lv8.get(p + 1, o) == WoodlandMansionGenerator.MansionParameters.CORRIDOR && !bl2) {
                        lv13 = lv12.offset((Direction)rotation.rotate(Direction.EAST), 8);
                        pieces.add(new Piece(this.manager, lv11 == Direction.EAST ? string4 : string3, lv13, rotation));
                     }

                     if (WoodlandMansionGenerator.MansionParameters.isInsideMansion(lv8, p, o + 1) && !parameters.isRoomId(lv8, p, o + 1, l, s)) {
                        lv13 = lv12.offset((Direction)rotation.rotate(Direction.SOUTH), 7);
                        lv13 = lv13.offset((Direction)rotation.rotate(Direction.EAST), 7);
                        pieces.add(new Piece(this.manager, lv11 == Direction.SOUTH ? string4 : string3, lv13, rotation.rotate(BlockRotation.CLOCKWISE_90)));
                     }

                     if (lv8.get(p, o - 1) == WoodlandMansionGenerator.MansionParameters.CORRIDOR && !bl2) {
                        lv13 = lv12.offset((Direction)rotation.rotate(Direction.NORTH), 1);
                        lv13 = lv13.offset((Direction)rotation.rotate(Direction.EAST), 7);
                        pieces.add(new Piece(this.manager, lv11 == Direction.NORTH ? string4 : string3, lv13, rotation.rotate(BlockRotation.CLOCKWISE_90)));
                     }

                     if (r == 65536) {
                        this.addSmallRoom(pieces, lv12, rotation, lv11, lvs[l]);
                     } else {
                        Direction lv14;
                        if (r == 131072 && lv11 != null) {
                           lv14 = parameters.findConnectedRoomDirection(lv8, p, o, l, s);
                           boolean bl3 = (q & 4194304) == 4194304;
                           this.addMediumRoom(pieces, lv12, rotation, lv14, lv11, lvs[l], bl3);
                        } else if (r == 262144 && lv11 != null && lv11 != Direction.UP) {
                           lv14 = lv11.rotateYClockwise();
                           if (!parameters.isRoomId(lv8, p + lv14.getOffsetX(), o + lv14.getOffsetZ(), l, s)) {
                              lv14 = lv14.getOpposite();
                           }

                           this.addBigRoom(pieces, lv12, rotation, lv14, lv11, lvs[l]);
                        } else if (r == 262144 && lv11 == Direction.UP) {
                           this.addBigSecretRoom(pieces, lv12, rotation, lvs[l]);
                        }
                     }
                  }
               }
            }
         }

      }

      private void addOuterWall(List pieces, GenerationPiece wallPiece, FlagMatrix layout, Direction direction, int startI, int startJ, int endI, int endJ) {
         int m = startI;
         int n = startJ;
         Direction lv = direction;

         do {
            if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, m + direction.getOffsetX(), n + direction.getOffsetZ())) {
               this.turnLeft(pieces, wallPiece);
               direction = direction.rotateYClockwise();
               if (m != endI || n != endJ || lv != direction) {
                  this.addWallPiece(pieces, wallPiece);
               }
            } else if (WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, m + direction.getOffsetX(), n + direction.getOffsetZ()) && WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, m + direction.getOffsetX() + direction.rotateYCounterclockwise().getOffsetX(), n + direction.getOffsetZ() + direction.rotateYCounterclockwise().getOffsetZ())) {
               this.turnRight(pieces, wallPiece);
               m += direction.getOffsetX();
               n += direction.getOffsetZ();
               direction = direction.rotateYCounterclockwise();
            } else {
               m += direction.getOffsetX();
               n += direction.getOffsetZ();
               if (m != endI || n != endJ || lv != direction) {
                  this.addWallPiece(pieces, wallPiece);
               }
            }
         } while(m != endI || n != endJ || lv != direction);

      }

      private void addRoof(List pieces, BlockPos pos, BlockRotation rotation, FlagMatrix layout, @Nullable FlagMatrix nextFloorLayout) {
         int i;
         int j;
         BlockPos lv;
         boolean bl;
         BlockPos lv2;
         for(i = 0; i < layout.m; ++i) {
            for(j = 0; j < layout.n; ++j) {
               lv = pos.offset(rotation.rotate(Direction.SOUTH), 8 + (i - this.entranceJ) * 8);
               lv = lv.offset(rotation.rotate(Direction.EAST), (j - this.entranceI) * 8);
               bl = nextFloorLayout != null && WoodlandMansionGenerator.MansionParameters.isInsideMansion(nextFloorLayout, j, i);
               if (WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j, i) && !bl) {
                  pieces.add(new Piece(this.manager, "roof", lv.up(3), rotation));
                  if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j + 1, i)) {
                     lv2 = lv.offset((Direction)rotation.rotate(Direction.EAST), 6);
                     pieces.add(new Piece(this.manager, "roof_front", lv2, rotation));
                  }

                  if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j - 1, i)) {
                     lv2 = lv.offset((Direction)rotation.rotate(Direction.EAST), 0);
                     lv2 = lv2.offset((Direction)rotation.rotate(Direction.SOUTH), 7);
                     pieces.add(new Piece(this.manager, "roof_front", lv2, rotation.rotate(BlockRotation.CLOCKWISE_180)));
                  }

                  if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j, i - 1)) {
                     lv2 = lv.offset((Direction)rotation.rotate(Direction.WEST), 1);
                     pieces.add(new Piece(this.manager, "roof_front", lv2, rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90)));
                  }

                  if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j, i + 1)) {
                     lv2 = lv.offset((Direction)rotation.rotate(Direction.EAST), 6);
                     lv2 = lv2.offset((Direction)rotation.rotate(Direction.SOUTH), 6);
                     pieces.add(new Piece(this.manager, "roof_front", lv2, rotation.rotate(BlockRotation.CLOCKWISE_90)));
                  }
               }
            }
         }

         if (nextFloorLayout != null) {
            for(i = 0; i < layout.m; ++i) {
               for(j = 0; j < layout.n; ++j) {
                  lv = pos.offset(rotation.rotate(Direction.SOUTH), 8 + (i - this.entranceJ) * 8);
                  lv = lv.offset(rotation.rotate(Direction.EAST), (j - this.entranceI) * 8);
                  bl = WoodlandMansionGenerator.MansionParameters.isInsideMansion(nextFloorLayout, j, i);
                  if (WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j, i) && bl) {
                     if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j + 1, i)) {
                        lv2 = lv.offset((Direction)rotation.rotate(Direction.EAST), 7);
                        pieces.add(new Piece(this.manager, "small_wall", lv2, rotation));
                     }

                     if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j - 1, i)) {
                        lv2 = lv.offset((Direction)rotation.rotate(Direction.WEST), 1);
                        lv2 = lv2.offset((Direction)rotation.rotate(Direction.SOUTH), 6);
                        pieces.add(new Piece(this.manager, "small_wall", lv2, rotation.rotate(BlockRotation.CLOCKWISE_180)));
                     }

                     if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j, i - 1)) {
                        lv2 = lv.offset((Direction)rotation.rotate(Direction.WEST), 0);
                        lv2 = lv2.offset((Direction)rotation.rotate(Direction.NORTH), 1);
                        pieces.add(new Piece(this.manager, "small_wall", lv2, rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90)));
                     }

                     if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j, i + 1)) {
                        lv2 = lv.offset((Direction)rotation.rotate(Direction.EAST), 6);
                        lv2 = lv2.offset((Direction)rotation.rotate(Direction.SOUTH), 7);
                        pieces.add(new Piece(this.manager, "small_wall", lv2, rotation.rotate(BlockRotation.CLOCKWISE_90)));
                     }

                     if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j + 1, i)) {
                        if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j, i - 1)) {
                           lv2 = lv.offset((Direction)rotation.rotate(Direction.EAST), 7);
                           lv2 = lv2.offset((Direction)rotation.rotate(Direction.NORTH), 2);
                           pieces.add(new Piece(this.manager, "small_wall_corner", lv2, rotation));
                        }

                        if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j, i + 1)) {
                           lv2 = lv.offset((Direction)rotation.rotate(Direction.EAST), 8);
                           lv2 = lv2.offset((Direction)rotation.rotate(Direction.SOUTH), 7);
                           pieces.add(new Piece(this.manager, "small_wall_corner", lv2, rotation.rotate(BlockRotation.CLOCKWISE_90)));
                        }
                     }

                     if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j - 1, i)) {
                        if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j, i - 1)) {
                           lv2 = lv.offset((Direction)rotation.rotate(Direction.WEST), 2);
                           lv2 = lv2.offset((Direction)rotation.rotate(Direction.NORTH), 1);
                           pieces.add(new Piece(this.manager, "small_wall_corner", lv2, rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90)));
                        }

                        if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j, i + 1)) {
                           lv2 = lv.offset((Direction)rotation.rotate(Direction.WEST), 1);
                           lv2 = lv2.offset((Direction)rotation.rotate(Direction.SOUTH), 8);
                           pieces.add(new Piece(this.manager, "small_wall_corner", lv2, rotation.rotate(BlockRotation.CLOCKWISE_180)));
                        }
                     }
                  }
               }
            }
         }

         for(i = 0; i < layout.m; ++i) {
            for(j = 0; j < layout.n; ++j) {
               lv = pos.offset(rotation.rotate(Direction.SOUTH), 8 + (i - this.entranceJ) * 8);
               lv = lv.offset(rotation.rotate(Direction.EAST), (j - this.entranceI) * 8);
               bl = nextFloorLayout != null && WoodlandMansionGenerator.MansionParameters.isInsideMansion(nextFloorLayout, j, i);
               if (WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j, i) && !bl) {
                  BlockPos lv3;
                  if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j + 1, i)) {
                     lv2 = lv.offset((Direction)rotation.rotate(Direction.EAST), 6);
                     if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j, i + 1)) {
                        lv3 = lv2.offset((Direction)rotation.rotate(Direction.SOUTH), 6);
                        pieces.add(new Piece(this.manager, "roof_corner", lv3, rotation));
                     } else if (WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j + 1, i + 1)) {
                        lv3 = lv2.offset((Direction)rotation.rotate(Direction.SOUTH), 5);
                        pieces.add(new Piece(this.manager, "roof_inner_corner", lv3, rotation));
                     }

                     if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j, i - 1)) {
                        pieces.add(new Piece(this.manager, "roof_corner", lv2, rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90)));
                     } else if (WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j + 1, i - 1)) {
                        lv3 = lv.offset((Direction)rotation.rotate(Direction.EAST), 9);
                        lv3 = lv3.offset((Direction)rotation.rotate(Direction.NORTH), 2);
                        pieces.add(new Piece(this.manager, "roof_inner_corner", lv3, rotation.rotate(BlockRotation.CLOCKWISE_90)));
                     }
                  }

                  if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j - 1, i)) {
                     lv2 = lv.offset((Direction)rotation.rotate(Direction.EAST), 0);
                     lv2 = lv2.offset((Direction)rotation.rotate(Direction.SOUTH), 0);
                     if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j, i + 1)) {
                        lv3 = lv2.offset((Direction)rotation.rotate(Direction.SOUTH), 6);
                        pieces.add(new Piece(this.manager, "roof_corner", lv3, rotation.rotate(BlockRotation.CLOCKWISE_90)));
                     } else if (WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j - 1, i + 1)) {
                        lv3 = lv2.offset((Direction)rotation.rotate(Direction.SOUTH), 8);
                        lv3 = lv3.offset((Direction)rotation.rotate(Direction.WEST), 3);
                        pieces.add(new Piece(this.manager, "roof_inner_corner", lv3, rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90)));
                     }

                     if (!WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j, i - 1)) {
                        pieces.add(new Piece(this.manager, "roof_corner", lv2, rotation.rotate(BlockRotation.CLOCKWISE_180)));
                     } else if (WoodlandMansionGenerator.MansionParameters.isInsideMansion(layout, j - 1, i - 1)) {
                        lv3 = lv2.offset((Direction)rotation.rotate(Direction.SOUTH), 1);
                        pieces.add(new Piece(this.manager, "roof_inner_corner", lv3, rotation.rotate(BlockRotation.CLOCKWISE_180)));
                     }
                  }
               }
            }
         }

      }

      private void addEntrance(List pieces, GenerationPiece wallPiece) {
         Direction lv = wallPiece.rotation.rotate(Direction.WEST);
         pieces.add(new Piece(this.manager, "entrance", wallPiece.position.offset((Direction)lv, 9), wallPiece.rotation));
         wallPiece.position = wallPiece.position.offset((Direction)wallPiece.rotation.rotate(Direction.SOUTH), 16);
      }

      private void addWallPiece(List pieces, GenerationPiece wallPiece) {
         pieces.add(new Piece(this.manager, wallPiece.template, wallPiece.position.offset((Direction)wallPiece.rotation.rotate(Direction.EAST), 7), wallPiece.rotation));
         wallPiece.position = wallPiece.position.offset((Direction)wallPiece.rotation.rotate(Direction.SOUTH), 8);
      }

      private void turnLeft(List pieces, GenerationPiece wallPiece) {
         wallPiece.position = wallPiece.position.offset((Direction)wallPiece.rotation.rotate(Direction.SOUTH), -1);
         pieces.add(new Piece(this.manager, "wall_corner", wallPiece.position, wallPiece.rotation));
         wallPiece.position = wallPiece.position.offset((Direction)wallPiece.rotation.rotate(Direction.SOUTH), -7);
         wallPiece.position = wallPiece.position.offset((Direction)wallPiece.rotation.rotate(Direction.WEST), -6);
         wallPiece.rotation = wallPiece.rotation.rotate(BlockRotation.CLOCKWISE_90);
      }

      private void turnRight(List pieces, GenerationPiece wallPiece) {
         wallPiece.position = wallPiece.position.offset((Direction)wallPiece.rotation.rotate(Direction.SOUTH), 6);
         wallPiece.position = wallPiece.position.offset((Direction)wallPiece.rotation.rotate(Direction.EAST), 8);
         wallPiece.rotation = wallPiece.rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90);
      }

      private void addSmallRoom(List pieces, BlockPos pos, BlockRotation rotation, Direction direction, RoomPool pool) {
         BlockRotation lv = BlockRotation.NONE;
         String string = pool.getSmallRoom(this.random);
         if (direction != Direction.EAST) {
            if (direction == Direction.NORTH) {
               lv = lv.rotate(BlockRotation.COUNTERCLOCKWISE_90);
            } else if (direction == Direction.WEST) {
               lv = lv.rotate(BlockRotation.CLOCKWISE_180);
            } else if (direction == Direction.SOUTH) {
               lv = lv.rotate(BlockRotation.CLOCKWISE_90);
            } else {
               string = pool.getSmallSecretRoom(this.random);
            }
         }

         BlockPos lv2 = StructureTemplate.applyTransformedOffset(new BlockPos(1, 0, 0), BlockMirror.NONE, lv, 7, 7);
         lv = lv.rotate(rotation);
         lv2 = lv2.rotate(rotation);
         BlockPos lv3 = pos.add(lv2.getX(), 0, lv2.getZ());
         pieces.add(new Piece(this.manager, string, lv3, lv));
      }

      private void addMediumRoom(List pieces, BlockPos pos, BlockRotation rotation, Direction connectedRoomDirection, Direction entranceDirection, RoomPool pool, boolean staircase) {
         BlockPos lv;
         if (entranceDirection == Direction.EAST && connectedRoomDirection == Direction.SOUTH) {
            lv = pos.offset((Direction)rotation.rotate(Direction.EAST), 1);
            pieces.add(new Piece(this.manager, pool.getMediumFunctionalRoom(this.random, staircase), lv, rotation));
         } else if (entranceDirection == Direction.EAST && connectedRoomDirection == Direction.NORTH) {
            lv = pos.offset((Direction)rotation.rotate(Direction.EAST), 1);
            lv = lv.offset((Direction)rotation.rotate(Direction.SOUTH), 6);
            pieces.add(new Piece(this.manager, pool.getMediumFunctionalRoom(this.random, staircase), lv, rotation, BlockMirror.LEFT_RIGHT));
         } else if (entranceDirection == Direction.WEST && connectedRoomDirection == Direction.NORTH) {
            lv = pos.offset((Direction)rotation.rotate(Direction.EAST), 7);
            lv = lv.offset((Direction)rotation.rotate(Direction.SOUTH), 6);
            pieces.add(new Piece(this.manager, pool.getMediumFunctionalRoom(this.random, staircase), lv, rotation.rotate(BlockRotation.CLOCKWISE_180)));
         } else if (entranceDirection == Direction.WEST && connectedRoomDirection == Direction.SOUTH) {
            lv = pos.offset((Direction)rotation.rotate(Direction.EAST), 7);
            pieces.add(new Piece(this.manager, pool.getMediumFunctionalRoom(this.random, staircase), lv, rotation, BlockMirror.FRONT_BACK));
         } else if (entranceDirection == Direction.SOUTH && connectedRoomDirection == Direction.EAST) {
            lv = pos.offset((Direction)rotation.rotate(Direction.EAST), 1);
            pieces.add(new Piece(this.manager, pool.getMediumFunctionalRoom(this.random, staircase), lv, rotation.rotate(BlockRotation.CLOCKWISE_90), BlockMirror.LEFT_RIGHT));
         } else if (entranceDirection == Direction.SOUTH && connectedRoomDirection == Direction.WEST) {
            lv = pos.offset((Direction)rotation.rotate(Direction.EAST), 7);
            pieces.add(new Piece(this.manager, pool.getMediumFunctionalRoom(this.random, staircase), lv, rotation.rotate(BlockRotation.CLOCKWISE_90)));
         } else if (entranceDirection == Direction.NORTH && connectedRoomDirection == Direction.WEST) {
            lv = pos.offset((Direction)rotation.rotate(Direction.EAST), 7);
            lv = lv.offset((Direction)rotation.rotate(Direction.SOUTH), 6);
            pieces.add(new Piece(this.manager, pool.getMediumFunctionalRoom(this.random, staircase), lv, rotation.rotate(BlockRotation.CLOCKWISE_90), BlockMirror.FRONT_BACK));
         } else if (entranceDirection == Direction.NORTH && connectedRoomDirection == Direction.EAST) {
            lv = pos.offset((Direction)rotation.rotate(Direction.EAST), 1);
            lv = lv.offset((Direction)rotation.rotate(Direction.SOUTH), 6);
            pieces.add(new Piece(this.manager, pool.getMediumFunctionalRoom(this.random, staircase), lv, rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90)));
         } else if (entranceDirection == Direction.SOUTH && connectedRoomDirection == Direction.NORTH) {
            lv = pos.offset((Direction)rotation.rotate(Direction.EAST), 1);
            lv = lv.offset((Direction)rotation.rotate(Direction.NORTH), 8);
            pieces.add(new Piece(this.manager, pool.getMediumGenericRoom(this.random, staircase), lv, rotation));
         } else if (entranceDirection == Direction.NORTH && connectedRoomDirection == Direction.SOUTH) {
            lv = pos.offset((Direction)rotation.rotate(Direction.EAST), 7);
            lv = lv.offset((Direction)rotation.rotate(Direction.SOUTH), 14);
            pieces.add(new Piece(this.manager, pool.getMediumGenericRoom(this.random, staircase), lv, rotation.rotate(BlockRotation.CLOCKWISE_180)));
         } else if (entranceDirection == Direction.WEST && connectedRoomDirection == Direction.EAST) {
            lv = pos.offset((Direction)rotation.rotate(Direction.EAST), 15);
            pieces.add(new Piece(this.manager, pool.getMediumGenericRoom(this.random, staircase), lv, rotation.rotate(BlockRotation.CLOCKWISE_90)));
         } else if (entranceDirection == Direction.EAST && connectedRoomDirection == Direction.WEST) {
            lv = pos.offset((Direction)rotation.rotate(Direction.WEST), 7);
            lv = lv.offset((Direction)rotation.rotate(Direction.SOUTH), 6);
            pieces.add(new Piece(this.manager, pool.getMediumGenericRoom(this.random, staircase), lv, rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90)));
         } else if (entranceDirection == Direction.UP && connectedRoomDirection == Direction.EAST) {
            lv = pos.offset((Direction)rotation.rotate(Direction.EAST), 15);
            pieces.add(new Piece(this.manager, pool.getMediumSecretRoom(this.random), lv, rotation.rotate(BlockRotation.CLOCKWISE_90)));
         } else if (entranceDirection == Direction.UP && connectedRoomDirection == Direction.SOUTH) {
            lv = pos.offset((Direction)rotation.rotate(Direction.EAST), 1);
            lv = lv.offset((Direction)rotation.rotate(Direction.NORTH), 0);
            pieces.add(new Piece(this.manager, pool.getMediumSecretRoom(this.random), lv, rotation));
         }

      }

      private void addBigRoom(List pieces, BlockPos pos, BlockRotation rotation, Direction connectedRoomDirection, Direction entranceDirection, RoomPool pool) {
         int i = 0;
         int j = 0;
         BlockRotation lv = rotation;
         BlockMirror lv2 = BlockMirror.NONE;
         if (entranceDirection == Direction.EAST && connectedRoomDirection == Direction.SOUTH) {
            i = -7;
         } else if (entranceDirection == Direction.EAST && connectedRoomDirection == Direction.NORTH) {
            i = -7;
            j = 6;
            lv2 = BlockMirror.LEFT_RIGHT;
         } else if (entranceDirection == Direction.NORTH && connectedRoomDirection == Direction.EAST) {
            i = 1;
            j = 14;
            lv = rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90);
         } else if (entranceDirection == Direction.NORTH && connectedRoomDirection == Direction.WEST) {
            i = 7;
            j = 14;
            lv = rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90);
            lv2 = BlockMirror.LEFT_RIGHT;
         } else if (entranceDirection == Direction.SOUTH && connectedRoomDirection == Direction.WEST) {
            i = 7;
            j = -8;
            lv = rotation.rotate(BlockRotation.CLOCKWISE_90);
         } else if (entranceDirection == Direction.SOUTH && connectedRoomDirection == Direction.EAST) {
            i = 1;
            j = -8;
            lv = rotation.rotate(BlockRotation.CLOCKWISE_90);
            lv2 = BlockMirror.LEFT_RIGHT;
         } else if (entranceDirection == Direction.WEST && connectedRoomDirection == Direction.NORTH) {
            i = 15;
            j = 6;
            lv = rotation.rotate(BlockRotation.CLOCKWISE_180);
         } else if (entranceDirection == Direction.WEST && connectedRoomDirection == Direction.SOUTH) {
            i = 15;
            lv2 = BlockMirror.FRONT_BACK;
         }

         BlockPos lv3 = pos.offset((Direction)rotation.rotate(Direction.EAST), i);
         lv3 = lv3.offset((Direction)rotation.rotate(Direction.SOUTH), j);
         pieces.add(new Piece(this.manager, pool.getBigRoom(this.random), lv3, lv, lv2));
      }

      private void addBigSecretRoom(List pieces, BlockPos pos, BlockRotation rotation, RoomPool pool) {
         BlockPos lv = pos.offset((Direction)rotation.rotate(Direction.EAST), 1);
         pieces.add(new Piece(this.manager, pool.getBigSecretRoom(this.random), lv, rotation, BlockMirror.NONE));
      }
   }

   static class ThirdFloorRoomPool extends SecondFloorRoomPool {
   }

   private static class SecondFloorRoomPool extends RoomPool {
      SecondFloorRoomPool() {
      }

      public String getSmallRoom(Random random) {
         int var10000 = random.nextInt(4);
         return "1x1_b" + (var10000 + 1);
      }

      public String getSmallSecretRoom(Random random) {
         int var10000 = random.nextInt(4);
         return "1x1_as" + (var10000 + 1);
      }

      public String getMediumFunctionalRoom(Random random, boolean staircase) {
         if (staircase) {
            return "1x2_c_stairs";
         } else {
            int var10000 = random.nextInt(4);
            return "1x2_c" + (var10000 + 1);
         }
      }

      public String getMediumGenericRoom(Random random, boolean staircase) {
         if (staircase) {
            return "1x2_d_stairs";
         } else {
            int var10000 = random.nextInt(5);
            return "1x2_d" + (var10000 + 1);
         }
      }

      public String getMediumSecretRoom(Random random) {
         int var10000 = random.nextInt(1);
         return "1x2_se" + (var10000 + 1);
      }

      public String getBigRoom(Random random) {
         int var10000 = random.nextInt(5);
         return "2x2_b" + (var10000 + 1);
      }

      public String getBigSecretRoom(Random random) {
         return "2x2_s1";
      }
   }

   private static class FirstFloorRoomPool extends RoomPool {
      FirstFloorRoomPool() {
      }

      public String getSmallRoom(Random random) {
         int var10000 = random.nextInt(5);
         return "1x1_a" + (var10000 + 1);
      }

      public String getSmallSecretRoom(Random random) {
         int var10000 = random.nextInt(4);
         return "1x1_as" + (var10000 + 1);
      }

      public String getMediumFunctionalRoom(Random random, boolean staircase) {
         int var10000 = random.nextInt(9);
         return "1x2_a" + (var10000 + 1);
      }

      public String getMediumGenericRoom(Random random, boolean staircase) {
         int var10000 = random.nextInt(5);
         return "1x2_b" + (var10000 + 1);
      }

      public String getMediumSecretRoom(Random random) {
         int var10000 = random.nextInt(2);
         return "1x2_s" + (var10000 + 1);
      }

      public String getBigRoom(Random random) {
         int var10000 = random.nextInt(4);
         return "2x2_a" + (var10000 + 1);
      }

      public String getBigSecretRoom(Random random) {
         return "2x2_s1";
      }
   }

   private abstract static class RoomPool {
      RoomPool() {
      }

      public abstract String getSmallRoom(Random random);

      public abstract String getSmallSecretRoom(Random random);

      public abstract String getMediumFunctionalRoom(Random random, boolean staircase);

      public abstract String getMediumGenericRoom(Random random, boolean staircase);

      public abstract String getMediumSecretRoom(Random random);

      public abstract String getBigRoom(Random random);

      public abstract String getBigSecretRoom(Random random);
   }

   private static class FlagMatrix {
      private final int[][] array;
      final int n;
      final int m;
      private final int fallback;

      public FlagMatrix(int n, int m, int fallback) {
         this.n = n;
         this.m = m;
         this.fallback = fallback;
         this.array = new int[n][m];
      }

      public void set(int i, int j, int value) {
         if (i >= 0 && i < this.n && j >= 0 && j < this.m) {
            this.array[i][j] = value;
         }

      }

      public void fill(int i0, int j0, int i1, int j1, int value) {
         for(int n = j0; n <= j1; ++n) {
            for(int o = i0; o <= i1; ++o) {
               this.set(o, n, value);
            }
         }

      }

      public int get(int i, int j) {
         return i >= 0 && i < this.n && j >= 0 && j < this.m ? this.array[i][j] : this.fallback;
      }

      public void update(int i, int j, int expected, int newValue) {
         if (this.get(i, j) == expected) {
            this.set(i, j, newValue);
         }

      }

      public boolean anyMatchAround(int i, int j, int value) {
         return this.get(i - 1, j) == value || this.get(i + 1, j) == value || this.get(i, j + 1) == value || this.get(i, j - 1) == value;
      }
   }

   private static class GenerationPiece {
      public BlockRotation rotation;
      public BlockPos position;
      public String template;

      GenerationPiece() {
      }
   }

   public static class Piece extends SimpleStructurePiece {
      public Piece(StructureTemplateManager manager, String template, BlockPos pos, BlockRotation rotation) {
         this(manager, template, pos, rotation, BlockMirror.NONE);
      }

      public Piece(StructureTemplateManager manager, String template, BlockPos pos, BlockRotation rotation, BlockMirror mirror) {
         super(StructurePieceType.WOODLAND_MANSION, 0, manager, getId(template), template, createPlacementData(mirror, rotation), pos);
      }

      public Piece(StructureTemplateManager manager, NbtCompound nbt) {
         super(StructurePieceType.WOODLAND_MANSION, nbt, manager, (id) -> {
            return createPlacementData(BlockMirror.valueOf(nbt.getString("Mi")), BlockRotation.valueOf(nbt.getString("Rot")));
         });
      }

      protected Identifier getId() {
         return getId(this.templateIdString);
      }

      private static Identifier getId(String identifier) {
         return new Identifier("woodland_mansion/" + identifier);
      }

      private static StructurePlacementData createPlacementData(BlockMirror mirror, BlockRotation rotation) {
         return (new StructurePlacementData()).setIgnoreEntities(true).setRotation(rotation).setMirror(mirror).addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
         super.writeNbt(context, nbt);
         nbt.putString("Rot", this.placementData.getRotation().name());
         nbt.putString("Mi", this.placementData.getMirror().name());
      }

      protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world, Random random, BlockBox boundingBox) {
         if (metadata.startsWith("Chest")) {
            BlockRotation lv = this.placementData.getRotation();
            BlockState lv2 = Blocks.CHEST.getDefaultState();
            if ("ChestWest".equals(metadata)) {
               lv2 = (BlockState)lv2.with(ChestBlock.FACING, lv.rotate(Direction.WEST));
            } else if ("ChestEast".equals(metadata)) {
               lv2 = (BlockState)lv2.with(ChestBlock.FACING, lv.rotate(Direction.EAST));
            } else if ("ChestSouth".equals(metadata)) {
               lv2 = (BlockState)lv2.with(ChestBlock.FACING, lv.rotate(Direction.SOUTH));
            } else if ("ChestNorth".equals(metadata)) {
               lv2 = (BlockState)lv2.with(ChestBlock.FACING, lv.rotate(Direction.NORTH));
            }

            this.addChest(world, boundingBox, random, pos, LootTables.WOODLAND_MANSION_CHEST, lv2);
         } else {
            List list = new ArrayList();
            label60:
            switch (metadata) {
               case "Mage":
                  list.add((MobEntity)EntityType.EVOKER.create(world.toServerWorld()));
                  break;
               case "Warrior":
                  list.add((MobEntity)EntityType.VINDICATOR.create(world.toServerWorld()));
                  break;
               case "Group of Allays":
                  int i = world.getRandom().nextInt(3) + 1;
                  int j = 0;

                  while(true) {
                     if (j >= i) {
                        break label60;
                     }

                     list.add((MobEntity)EntityType.ALLAY.create(world.toServerWorld()));
                     ++j;
                  }
               default:
                  return;
            }

            Iterator var7 = list.iterator();

            while(var7.hasNext()) {
               MobEntity lv3 = (MobEntity)var7.next();
               if (lv3 != null) {
                  lv3.setPersistent();
                  lv3.refreshPositionAndAngles(pos, 0.0F, 0.0F);
                  lv3.initialize(world, world.getLocalDifficulty(lv3.getBlockPos()), SpawnReason.STRUCTURE, (EntityData)null, (NbtCompound)null);
                  world.spawnEntityAndPassengers(lv3);
                  world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
               }
            }
         }

      }
   }
}
