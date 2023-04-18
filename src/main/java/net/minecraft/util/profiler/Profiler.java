package net.minecraft.util.profiler;

import java.util.function.Supplier;

public interface Profiler {
   String ROOT_NAME = "root";

   void startTick();

   void endTick();

   void push(String location);

   void push(Supplier locationGetter);

   void pop();

   void swap(String location);

   void swap(Supplier locationGetter);

   void markSampleType(SampleType type);

   default void visit(String marker) {
      this.visit((String)marker, 1);
   }

   void visit(String marker, int num);

   default void visit(Supplier markerGetter) {
      this.visit((Supplier)markerGetter, 1);
   }

   void visit(Supplier markerGetter, int num);

   static Profiler union(final Profiler a, final Profiler b) {
      if (a == DummyProfiler.INSTANCE) {
         return b;
      } else {
         return b == DummyProfiler.INSTANCE ? a : new Profiler() {
            public void startTick() {
               a.startTick();
               b.startTick();
            }

            public void endTick() {
               a.endTick();
               b.endTick();
            }

            public void push(String location) {
               a.push(location);
               b.push(location);
            }

            public void push(Supplier locationGetter) {
               a.push(locationGetter);
               b.push(locationGetter);
            }

            public void markSampleType(SampleType type) {
               a.markSampleType(type);
               b.markSampleType(type);
            }

            public void pop() {
               a.pop();
               b.pop();
            }

            public void swap(String location) {
               a.swap(location);
               b.swap(location);
            }

            public void swap(Supplier locationGetter) {
               a.swap(locationGetter);
               b.swap(locationGetter);
            }

            public void visit(String marker, int num) {
               a.visit(marker, num);
               b.visit(marker, num);
            }

            public void visit(Supplier markerGetter, int num) {
               a.visit(markerGetter, num);
               b.visit(markerGetter, num);
            }
         };
      }
   }
}
