/*
 * This source file was generated by the Gradle 'init' task
 */
package io.github.gmodena.simd;

import jdk.incubator.vector.*;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

@BenchmarkMode(Mode.AverageTime)
@State(Scope.Thread)
@Measurement(iterations = 1)
public class Runner {
    @State(Scope.Thread)
    public static class BenchmarkState {
        Random rand = new Random(42);

        int maxRecords = 10_000_000;
    }

    @BenchmarkMode(Mode.AverageTime)
    @Measurement(iterations = 1)
    public float[] initArray(BenchmarkState state) {
        float[] array = new float[state.maxRecords];
        Arrays.fill(array, state.rand.nextFloat());
        return array;
    }

    static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    void mul(float[] array, float by) {
        for (int i = 0; i < array.length; i++) {
            array[i] *= by;
        }
    }

    void vectorizedMul(float[] array, float by) {
        int i = 0;
        VectorSpecies<Float> species = FloatVector.SPECIES_PREFERRED;
        int bound = species.loopBound(array.length);
        FloatVector byVector = FloatVector.broadcast(species, by);
        for (; i < bound; i += species.length()) {
            FloatVector vec = FloatVector.fromArray(species, array, i);
            FloatVector multiplied = vec.mul(byVector);
            multiplied.intoArray(array, i);
        }
        for (; i < array.length; i++) {
            array[i] *= by;
        }
    }

    void dot(float[] va, float[] vb) {
        float sum = 0;
        for (int i = 0; i < va.length; i++)
        {
            sum += va[i] * vb[i];
        }
    }

    void vectorizedDot(float[] va, float[] vb) {
        VectorSpecies<Float> species = FloatVector.SPECIES_PREFERRED;
        int i = 0;
        float sum = 0.0f;
        for (; i < va.length - species.length(); i += species.length()) {
            FloatVector a = FloatVector.fromArray(species, va, i);
            FloatVector b = FloatVector.fromArray(species, vb, i);
            sum += a.mul(b).reduceLanes(VectorOperators.ADD);
        }
        // Remaining elements that are left out of SPECIES_PREFERRED
        // chunks.
        for (; i < va.length; i++) {
            sum += va[i] * vb[i];
        }
    }

    @Benchmark
    public void benchMul() {
        BenchmarkState state = new BenchmarkState();
        float[] array = initArray(state);
        mul(array, 3.14f);
    }

    @Benchmark
    public void benchVectorizedMul() {
        BenchmarkState state = new BenchmarkState();
        float[] array = initArray(state);
        vectorizedMul(array, 3.14f);
    }

    @Benchmark
    public void benchDot() {
        BenchmarkState state = new BenchmarkState();
        float[] va = initArray(state);
        float[] vb = initArray(state);

        dot(va, vb);
    }

    @Benchmark
    public void benchVectorizedDot() {
        BenchmarkState state = new BenchmarkState();
        float[] va = initArray(state);
        float[] vb = initArray(state);

        vectorizedDot(va, vb);
    }

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(args);
    }
}
