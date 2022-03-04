package com.ustermetrics.ecos;

import org.apache.spark.ml.linalg.DenseMatrix;
import org.apache.spark.ml.linalg.DenseVector;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class EcosSolverTest {

    private static long[] toLongArray(int[] arr) {
        return Arrays.stream(arr).asLongStream().toArray();
    }

    @Test
    void ecosVersionIsNotEmpty() {
        var version = EcosSolver.version();

        System.out.println("Version: " + version);
        assertFalse(version.isEmpty());
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
        var l = 5;
        var q = new long[]{5};
        var nex = 0;

        var As = A.toSparseColMajor();
        var Gs = G.toSparseColMajor();

        var solution = EcosSolver.solve(c.values(), As.values(), toLongArray(As.colPtrs()),
                toLongArray(As.rowIndices()), b.values(), Gs.values(), toLongArray(Gs.colPtrs()),
                toLongArray(Gs.rowIndices()), h.values(), l, q, nex);

        System.out.println(solution);
        assertEquals(0, solution.exitCode());
        var tol = Math.ulp(1.0); // Machine epsilon
        assertArrayEquals(new double[]{0.24879020572078372, 0.049684806182020855, 0.7015249845663684,
                3.5308169265756875e-09, 0.19999999978141014}, solution.solution(), tol);
        assertEquals(-0.07154259763411892, solution.cost(), tol);
    }
}
