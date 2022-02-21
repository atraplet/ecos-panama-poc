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

        assertFalse(version.isEmpty());
    }

    @Test
    void ecosSolveShouldReturnOptimal() {
        var n = 5;
        var m = 11;
        var p = 1;
        var l = 5;
        var ncones = 1;
        var nex = 0;
        try (var scope = ResourceScope.newConfinedScope()) {
            var allocator = SegmentAllocator.arenaAllocator(scope);
            var q = allocator.allocateArray(C_LONG_LONG, new long[]{
                    6
            });
            var Gpr = allocator.allocateArray(C_DOUBLE, new double[]{
                    -1.0, -0.039839731431486176, -0.5591785172661986, 0.3319324258077957, 0.38245161895619884, -1.0,
                    -0.22238007664706236, -0.10469420698987021, -0.7263592669837473, 0.6531424100692084, -1.0,
                    0.3202238157111066, -0.03180753104963039, -0.2431736979577771, 1.1927423460202458, -1.0,
                    -0.15642126285757943, 0.2261450784743907, 0.45028227007139937, 1.4158041698401964,
                    0.15769659397189673, -1.0, 1.0
            });
            var Gjc = allocator.allocateArray(C_LONG_LONG, new long[]{
                    0, 5, 10, 15, 20, 23
            });
            var Gir = allocator.allocateArray(C_LONG_LONG, new long[]{
                    0, 7, 8, 9, 10, 1, 7, 8, 9, 10, 2, 7, 8, 9, 10, 3, 7, 8, 9, 10, 4, 5, 6
            });
            var Apr = allocator.allocateArray(C_DOUBLE, new double[]{
                    1.0, 1.0, 1.0, 1.0
            });
            var Ajc = allocator.allocateArray(C_LONG_LONG, new long[]{
                    0, 1, 2, 3, 4, 4
            });
            var Air = allocator.allocateArray(C_LONG_LONG, new long[]{
                    0, 0, 0, 0
            });
            var c = allocator.allocateArray(C_DOUBLE, new double[]{
                    -0.05, -0.06, -0.08, -0.06, 0.
            });
            var h = allocator.allocateArray(C_DOUBLE, new double[]{
                    0., 0., 0., 0., 0.22, 1., 1., 0., 0., 0., 0.
            });
            var b = allocator.allocateArray(C_DOUBLE, new double[]{
                    1.
            });
            var work = ECOS_setup(n, m, p, l, ncones, q, nex, Gpr, Gjc, Gir, Apr, Ajc, Air, c, h, b);
            var exitFlag = ECOS_solve(work);
            ECOS_cleanup(work, 0);

            assertNotEquals(NULL, work);
            assertEquals(0, exitFlag);
        }
    }
}
