package com.ustermetrics.ecos;

import com.ustermetrics.ecos.stubs.ecos_h;
import com.ustermetrics.ecos.stubs.pwork;
import com.ustermetrics.ecos.stubs.stats;
import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.SegmentAllocator;

import static com.ustermetrics.ecos.stubs.ecos_h.*;
import static jdk.incubator.foreign.CLinker.*;
import static jdk.incubator.foreign.MemoryAddress.NULL;

public class EcosSolver {

    public record Solution(long exitCode, double[] solution, double cost) {
    }

    public static String version() {
        var addr = ecos_h.ECOS_ver();

        return toJavaString(addr);
    }

    public static Solution solve(double[] c, double[] Apr, long[] Ajc, long[] Air, double[] b, double[] Gpr,
                                 long[] Gjc, long[] Gir, double[] h, long l, long[] q, long nex) {
        var n = c.length;
        var m = h.length;
        var p = b.length;
        var ncones = q.length;

        long exitCode;
        double[] solution = null;
        double cost = Double.NaN;

        try (var sc = ResourceScope.newConfinedScope()) {
            var alloc = SegmentAllocator.arenaAllocator(sc);

            var qSeg = alloc.allocateArray(C_LONG_LONG, q);
            var GprSeg = alloc.allocateArray(C_DOUBLE, Gpr);
            var GjcSeg = alloc.allocateArray(C_LONG_LONG, Gjc);
            var GirSeg = alloc.allocateArray(C_LONG_LONG, Gir);
            var AprSeg = alloc.allocateArray(C_DOUBLE, Apr);
            var AjcSeg = alloc.allocateArray(C_LONG_LONG, Ajc);
            var AirSeg = alloc.allocateArray(C_LONG_LONG, Air);
            var cSeg = alloc.allocateArray(C_DOUBLE, c);
            var hSeg = alloc.allocateArray(C_DOUBLE, h);
            var bSeg = alloc.allocateArray(C_DOUBLE, b);

            var workAddr = ECOS_setup(n, m, p, l, ncones, qSeg, nex, GprSeg, GjcSeg, GirSeg, AprSeg, AjcSeg, AirSeg,
                    cSeg, hSeg, bSeg);
            if (NULL.equals(workAddr)) {
                throw new IllegalArgumentException("Something went wrong in ECOS_setup()");
            }

            exitCode = ECOS_solve(workAddr);
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
