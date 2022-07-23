# Benchmark

## Usage

```shell
make bench
```

The report wil be available at `bench/build/results/jmh/results.txt`.

## Notes

Uses [Java Microbenchmark Harness](https://github.com/openjdk/jmh) to run the
benchmarks and [JMH Gradle Plugin](https://github.com/melix/jmh-gradle-plugin)
for integration into Gradle.
