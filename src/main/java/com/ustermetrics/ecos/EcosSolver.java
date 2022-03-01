package com.ustermetrics.ecos;

import com.ustermetrics.ecos.stubs.ecos_h;
import com.ustermetrics.ecos.stubs.pwork;
import com.ustermetrics.ecos.stubs.stats;
import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.SegmentAllocator;

import java.util.Arrays;

import static com.ustermetrics.ecos.stubs.ecos_h.*;
import static jdk.incubator.foreign.CLinker.*;
import static jdk.incubator.foreign.MemoryAddress.NULL;

public class EcosSolver {

    public record Solution(int exitCode, double[] solution, double cost) {
    }

    private static long[] toLongArray(int[] arr) {
        return Arrays.stream(arr).asLongStream().toArray();
    }

    public static String version() {
        var addr = ecos_h.ECOS_ver();

        return toJavaString(addr);
    }

    public static Solution solve(double[] c, double[] Apr, int[] Ajc, int[] Air, double[] b, double[] Gpr, int[] Gjc,
                                 int[] Gir, double[] h, int l, int[] q, int nex) {
        var n = c.length;
        var m = h.length;
        var p = b.length;
        var ncones = q.length;

        int exitCode;
        double[] solution = null;
        double cost = Double.NaN;

        try (var sc = ResourceScope.newConfinedScope()) {
            var alloc = SegmentAllocator.arenaAllocator(sc);

            var qSeg = alloc.allocateArray(C_LONG_LONG, toLongArray(q));
            var GprSeg = alloc.allocateArray(C_DOUBLE, Gpr);
            var GjcSeg = alloc.allocateArray(C_LONG_LONG, toLongArray(Gjc));
            var GirSeg = alloc.allocateArray(C_LONG_LONG, toLongArray(Gir));
            var AprSeg = alloc.allocateArray(C_DOUBLE, Apr);
            var AjcSeg = alloc.allocateArray(C_LONG_LONG, toLongArray(Ajc));
            var AirSeg = alloc.allocateArray(C_LONG_LONG, toLongArray(Air));
            var cSeg = alloc.allocateArray(C_DOUBLE, c);
            var hSeg = alloc.allocateArray(C_DOUBLE, h);
            var bSeg = alloc.allocateArray(C_DOUBLE, b);

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
                solution = xSeg.toDoubleArray();

                var infoAddr = pwork.info$get(workSeg);
                var infoSeg = infoAddr.asSegment(stats.sizeof(), sc);
                cost = stats.pcost$get(infoSeg);

                ECOS_cleanup(workAddr, 0);
            }
        }

        return new Solution(exitCode, solution, cost);
    }
}
