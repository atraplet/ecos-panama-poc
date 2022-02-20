package com.ustermetrics.ecos.stubs;

import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.SegmentAllocator;
import org.junit.jupiter.api.Test;

import static com.ustermetrics.ecos.stubs.ecos_h.*;
import static jdk.incubator.foreign.CLinker.*;
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static org.junit.jupiter.api.Assertions.*;

class EcosStubsTest {

    @Test
    void ecosVersionIsNotEmpty() {
        var addr = ecos_h.ECOS_ver();
        var version = toJavaString(addr);

        System.out.println("Ecos version: " + version);

        assertFalse(version.isEmpty());
    }

    @Test
    void ecosSolveShouldReturnOptimal() {
        var n = 5;
        var m = 11;
        var p = 0;
        var l = 6;
        var ncones = 1;
        var nex = 0;
        try (var scope = ResourceScope.newConfinedScope()) {
            var allocator = SegmentAllocator.ofScope(scope);
            var Gx = allocator.allocateArray(C_DOUBLE, new double[]{
                    0.416757847405471, 2.136196095668454, 1.793435585194863, -1.,
                    0.056266827226329, -1.640270808404989, 0.841747365656204, -1.,
                    0.416757847405471, 2.136196095668454, 1.793435585194863, -1.,
                    0.056266827226329, -1.640270808404989, 0.841747365656204, -1., -1.
            });
            var Gp = allocator.allocateArray(C_LONG_LONG, new long[]{
                    0, 4, 8, 12, 16, 17
            });
            var Gi = allocator.allocateArray(C_LONG_LONG, new long[]{
                    0, 1, 2, 7, 0, 1, 2, 8, 3, 4, 5, 9, 3, 4, 5, 10, 6
            });
            var q = allocator.allocateArray(C_LONG_LONG, new long[]{
                    5
            });
            var c = allocator.allocateArray(C_DOUBLE, new double[]{
                    0., 0., 0., 0., 1.
            });
            var h = allocator.allocateArray(C_DOUBLE, new double[]{
                    0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0.
            });

            var work = ECOS_setup(n, m, p, l, ncones, q, nex, Gx, Gp, Gi, NULL, NULL, NULL, c, h, NULL);
            var exitFlag = ECOS_solve(work);
            ECOS_cleanup(work, 0);

            assertNotEquals(NULL, work);
            assertEquals(0, exitFlag);
        }
    }
}
