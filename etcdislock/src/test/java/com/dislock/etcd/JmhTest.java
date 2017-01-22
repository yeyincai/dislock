package com.dislock.etcd;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Created by yeyc on 2017/1/22.
 */
public class JmhTest {



    @Benchmark
    @Group("demoTest")
    @GroupThreads(25)
    @BenchmarkMode(Mode.Throughput)
    public void demoTest() {
        JmhArgument.dislock.lock();
        try {
            for (int i = 0; i < 10; i++) {
                System.out.println(Thread.currentThread().getName());
            }
        }finally {
            JmhArgument.dislock.unlock();
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .forks(1)//重复次数
                .warmupIterations(20)//热身迭代次数
                .measurementIterations(20)//测量迭代次数
                .include(JmhTest.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
