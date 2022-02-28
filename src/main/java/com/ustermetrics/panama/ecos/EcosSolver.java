package com.ustermetrics.panama.ecos;

import com.ustermetrics.panama.ecos.stubs.ecos_h;
import com.ustermetrics.panama.ecos.stubs.pwork;
import com.ustermetrics.panama.ecos.stubs.stats;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.SegmentAllocator;
import org.apache.spark.ml.linalg.DenseVector;
import org.apache.spark.ml.linalg.SparseMatrix;

import java.util.Arrays;

import static com.ustermetrics.panama.ecos.stubs.ecos_h.*;
import static jdk.incubator.foreign.CLinker.*;
import static jdk.incubator.foreign.MemoryAddress.NULL;

public class EcosSolver {

    public record Solution(int exitCode, DenseVector solution, double cost) {
    }

    private static long[] toLongArray(int[] arr) {
        return Arrays.stream(arr).asLongStream().toArray();
    }

    public static String version() {
        var addr = ecos_h.ECOS_ver();

        return toJavaString(addr);
    }

    public static Solution solve(DenseVector c, SparseMatrix A, DenseVector b, SparseMatrix G, DenseVector h, int l,
                                 IntArrayList q, int nex) {
        var n = c.size();
        var m = h.size();
        var p = A.numRows();
        var ncones = q.size();

        int exitCode;
        DenseVector solution = null;
        double cost = Double.NaN;

        try (var sc = ResourceScope.newConfinedScope()) {
            var alloc = SegmentAllocator.arenaAllocator(sc);

            var qSeg = alloc.allocateArray(C_LONG_LONG, toLongArray(q.toIntArray()));
            var GprSeg = alloc.allocateArray(C_DOUBLE, G.values());
            var GjcSeg = alloc.allocateArray(C_LONG_LONG, toLongArray(G.colPtrs()));
            var GirSeg = alloc.allocateArray(C_LONG_LONG, toLongArray(G.rowIndices()));
            var AprSeg = alloc.allocateArray(C_DOUBLE, A.values());
            var AjcSeg = alloc.allocateArray(C_LONG_LONG, toLongArray(A.colPtrs()));
            var AirSeg = alloc.allocateArray(C_LONG_LONG, toLongArray(A.rowIndices()));
            var cSeg = alloc.allocateArray(C_DOUBLE, c.values());
            var hSeg = alloc.allocateArray(C_DOUBLE, h.values());
            var bSeg = alloc.allocateArray(C_DOUBLE, b.values());

            var workAddr = ECOS_setup(n, m, p, l, ncones, qSeg, nex, GprSeg, GjcSeg, GirSeg, AprSeg, AjcSeg, AirSeg,
                    cSeg, hSeg, bSeg);
            if (NULL.equals(workAddr)) {
                throw new IllegalArgumentException("Something went wrong in ECOS_setup()");
            }

            exitCode = (int) ECOS_solve(workAddr);
            if (exitCode == 0) {
                var workSeg = workAddr.asSegment(pwork.sizeof(), sc);
                var xAddr = pwork.x$get(workSeg);
                var xSeg = xAddr.asSegment(C_DOUBLE.byteSize() * n, sc);
                solution = new DenseVector(xSeg.toDoubleArray());

                var infoAddr = pwork.info$get(workSeg);
                var infoSeg = infoAddr.asSegment(stats.sizeof(), sc);
                cost = stats.pcost$get(infoSeg);

                ECOS_cleanup(workAddr, 0);
            }
        }

        return new Solution(exitCode, solution, cost);
    }
}
