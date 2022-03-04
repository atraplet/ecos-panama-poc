package com.ustermetrics.ecos.stubs;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.SegmentAllocator;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodType;

import static com.ustermetrics.ecos.stubs.ecos_h.*;
import static jdk.incubator.foreign.CLinker.*;
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static org.junit.jupiter.api.Assertions.*;

class EcosStubsTest {

    @Test
    void panamaSmokeTest() throws Throwable {
        var os = System.getProperty("os.name");
        var name = os.startsWith("Windows") ? "_getpid" : "getpid";

        var linker = CLinker.getInstance();
        var lookup = CLinker.systemLookup();
        var symbol = lookup.lookup(name).orElseThrow();
        var type = MethodType.methodType(int.class);
        var function = FunctionDescriptor.of(CLinker.C_INT);
        var getpid = linker.downcallHandle(symbol, type, function);

        var pid = (int) getpid.invokeExact();

        assertTrue(pid > 0);
    }

    @Test
    void ecosVersionIsNotEmpty() {
        var addr = ecos_h.ECOS_ver();
        var ver = toJavaString(addr);

        assertFalse(ver.isEmpty());
    }

    @Test
    void ecosSolveShouldReturnOptimalSolution() {
        var n = 5;
        var m = 10;
        var p = 1;
        var l = 5;
        var ncones = 1;
        var nex = 0;

        try (var sc = ResourceScope.newConfinedScope()) {
            var alloc = SegmentAllocator.arenaAllocator(sc);

            var q = alloc.allocateArray(C_LONG_LONG, new long[]{5});
            var Gpr = alloc.allocateArray(C_DOUBLE, new double[]{-1., -0.15, -1., -0.02, -0.198997487421324, -1.,
                    -0.1, -0.16583123951776996, -0.158113883008419, -1., -0.15, -0.10552897060221729,
                    -0.17392527130926083, -0.16159714218895202, 1., -1.});
            var Gjc = alloc.allocateArray(C_LONG_LONG, new long[]{0, 2, 5, 9, 14, 16});
            var Gir = alloc.allocateArray(C_LONG_LONG, new long[]{0, 6, 1, 6, 7, 2, 6, 7, 8, 3, 6, 7, 8, 9, 4, 5});
            var Apr = alloc.allocateArray(C_DOUBLE, new double[]{1., 1., 1., 1.});
            var Ajc = alloc.allocateArray(C_LONG_LONG, new long[]{0, 1, 2, 3, 4, 4});
            var Air = alloc.allocateArray(C_LONG_LONG, new long[]{0, 0, 0, 0});
            var c = alloc.allocateArray(C_DOUBLE, new double[]{-0.05, -0.06, -0.08, -0.06, 0.});
            var h = alloc.allocateArray(C_DOUBLE, new double[]{0., 0., 0., 0., 0.2, 0., 0., 0., 0.,
                    0.});
            var b = alloc.allocateArray(C_DOUBLE, new double[]{1.});

            var workAddr = ECOS_setup(n, m, p, l, ncones, q, nex, Gpr, Gjc, Gir, Apr, Ajc, Air, c, h, b);
            assertNotEquals(NULL, workAddr);

            var exitCode = ECOS_solve(workAddr);
            assertEquals(0, exitCode);

            var workSeg = workAddr.asSegment(pwork.sizeof(), sc);
            var xAddr = pwork.x$get(workSeg);
            var xSeg = xAddr.asSegment(C_DOUBLE.byteSize() * n, sc);
            var x = xSeg.toDoubleArray();
            var tol = Math.ulp(1.0); // Machine epsilon
            assertArrayEquals(new double[]{0.24879020572078372, 0.049684806182020855, 0.7015249845663684,
                    3.5308169265756875e-09, 0.19999999978141014}, x, tol);

            var infoAddr = pwork.info$get(workSeg);
            var infoSeg = infoAddr.asSegment(stats.sizeof(), sc);
            var pcost = stats.pcost$get(infoSeg);
            assertEquals(-0.07154259763411892, pcost, tol);

            ECOS_cleanup(workAddr, 0);
        }
    }
}
