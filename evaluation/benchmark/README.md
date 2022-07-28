# Benchmark

Uses the [Java Microbenchmark Harness](https://github.com/openjdk/jmh) to run the
benchmarks and the [JMH Gradle Plugin](https://github.com/melix/jmh-gradle-plugin)
for integration into Gradle.

## Usage

```shell
make bench
```

The report wil be available at `bench/build/results/jmh/results.txt`.
