package br.ufsc.inf.ufsc.lapesd.stopwatch;

import com.google.common.base.Stopwatch;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Sandbox {
    public static void main(String[] args) {
        Set<String> strings = new HashSet<>();
        GCStopwatch ndWatch = GCStopwatch.builder().withoutGC().createStarted();
        Stopwatch watch = Stopwatch.createStarted();
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 100000; j++)
                strings.add(String.valueOf(i * 100000 + j));
            System.out.printf("YGCT: %.3f, OGCT: %.3f\n",
                    PerfHelper.get().getYoungGCTime(TimeUnit.MICROSECONDS)/1000.0,
                    PerfHelper.get().getTenuredGCTime(TimeUnit.MICROSECONDS)/1000.0);
        }
        System.out.printf("watch: %.3f, ndwatch: %.3f\n",
                watch.elapsed(TimeUnit.MICROSECONDS)/1000.0,
                ndWatch.elapsed(TimeUnit.MICROSECONDS)/1000.0);
    }
}
