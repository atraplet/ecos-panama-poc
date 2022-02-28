package com.ustermetrics.panama.ecos.stubs;

import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.SegmentAllocator;
import org.apache.spark.ml.linalg.DenseMatrix;
import org.apache.spark.ml.linalg.DenseVector;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.ustermetrics.panama.ecos.stubs.ecos_h.*;
import static jdk.incubator.foreign.CLinker.*;
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static org.junit.jupiter.api.Assertions.*;

class EcosStubsTest {

    private static long[] toLongArray(int[] arr) {
        return Arrays.stream(arr).asLongStream().toArray();
    }

    @Test
    void ecosVersionIsNotEmpty() {
        var addr = ecos_h.ECOS_ver();
        var ver = toJavaString(addr);

        assertFalse(ver.isEmpty());
    }

    @Test
    void ecosSolveShouldReturnOptimalSolution() {
        var c = new DenseVector(new double[]{-0.05, -0.06, -0.08, -0.06, 0.});
        var A = new DenseMatrix(1, 5, new double[]{1., 1., 1., 1., 0.});
        var b = new DenseVector(new double[]{1.});
        var G = new DenseMatrix(10, 5, new double[]{
                -1., 0., 0., 0., 0.,
                0., -1., 0., 0., 0.,
                0., 0., -1., 0., 0.,
                0., 0., 0., -1., 0.,
                0., 0., 0., 0., 1.,
                0., 0., 0., 0., -1.,
                -0.15, -0.02, -0.1, -0.15, 0.,
                0., -0.198997487421324, -0.16583123951776996, -0.10552897060221729, 0.,
                0., 0., -0.158113883008419, -0.17392527130926083, 0.,
                0., 0., 0., -0.16159714218895202, 0.
        }, true);
        var h = new DenseVector(new double[]{0., 0., 0., 0., 0.2, 0., 0., 0., 0., 0.});

        var n = c.size();
        var m = h.size();
        var p = A.numRows();
        var l = 5;
        var q = new long[]{5};
        var ncones = q.length;
        var nex = 0;

        var GS = G.toSparseColMajor();
        var AS = A.toSparseColMajor();

        try (var sc = ResourceScope.newConfinedScope()) {
            var alloc = SegmentAllocator.arenaAllocator(sc);
            var qSeg = alloc.allocateArray(C_LONG_LONG, q);
            var GprSeg = alloc.allocateArray(C_DOUBLE, GS.values());
            var GjcSeg = alloc.allocateArray(C_LONG_LONG, toLongArray(GS.colPtrs()));
            var GirSeg = alloc.allocateArray(C_LONG_LONG, toLongArray(GS.rowIndices()));
            var AprSeg = alloc.allocateArray(C_DOUBLE, AS.values());
            var AjcSeg = alloc.allocateArray(C_LONG_LONG, toLongArray(AS.colPtrs()));
            var AirSeg = alloc.allocateArray(C_LONG_LONG, toLongArray(AS.rowIndices()));
            var cSeg = alloc.allocateArray(C_DOUBLE, c.values());
            var hSeg = alloc.allocateArray(C_DOUBLE, h.values());
            var bSeg = alloc.allocateArray(C_DOUBLE, b.values());

            var workAddr = ECOS_setup(n, m, p, l, ncones, qSeg, nex, GprSeg, GjcSeg, GirSeg, AprSeg,
                    AjcSeg, AirSeg, cSeg, hSeg, bSeg);
            assertNotEquals(NULL, workAddr);

            var exitCode = ECOS_solve(workAddr);
            assertEquals(0, exitCode);

            var workSeg = workAddr.asSegment(pwork.sizeof(), sc);
            var xAddr = pwork.x$get(workSeg);
            var xSeg = xAddr.asSegment(C_DOUBLE.byteSize() * n, sc);
            var x = xSeg.toDoubleArray();
            var tol = 2.220446e-16; // Normal machine epsilon
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
