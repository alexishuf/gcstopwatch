package br.ufsc.inf.ufsc.lapesd.stopwatch;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class PerfHelper {
    private static PerfHelper instance = null;
    private final Object gc0TimeMon, gc1TimeMon, hrtMon;
    private Method longValue;

    private static File guessToolsJar() {
        return new File(System.getProperty("java.home") + "/../lib/tools.jar");
    }

    public PerfHelper(File toolJar, int pid) throws IOException {
        URL urls[] = {new URL("jar:file:"+toolJar.getCanonicalFile().getAbsolutePath() + "!/")};
        URLClassLoader cl = URLClassLoader.newInstance(urls);
        try {
            Class<?> perfClass = cl.loadClass("sun.misc.Perf");
            Class<?> perfDataBufferClass = cl.loadClass(
                    "sun.jvmstat.perfdata.monitor.v2_0.PerfDataBuffer");
            Class<?> longMonitorClass = cl.loadClass("sun.jvmstat.monitor.LongMonitor");
            longValue = longMonitorClass.getMethod("longValue");

            Method getPerf = perfClass.getMethod("getPerf");
            Object perf = getPerf.invoke(null);

            Method attach = perfClass.getMethod("attach", int.class, String.class);
            ByteBuffer byteBuffer = (ByteBuffer) attach.invoke(perf, pid, "r");

            Constructor<?> pdbCons = perfDataBufferClass.getConstructor(ByteBuffer.class,int.class);
            Object pdb = pdbCons.newInstance(byteBuffer, pid);

            Method findByName = perfDataBufferClass.getMethod("findByName", String.class);
            gc0TimeMon = findByName.invoke(pdb, "sun.gc.collector.0.time");
            gc1TimeMon = findByName.invoke(pdb, "sun.gc.collector.1.time");
            hrtMon = findByName.invoke(pdb, "sun.os.hrt.frequency");
        } catch (ClassNotFoundException | NoSuchMethodException
                | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public PerfHelper(int pid) throws IOException {
        this(guessToolsJar(), pid);
    }

    private PerfHelper() throws IOException {
        this(0);
    }

    private long readLongValue(Object monitor) {
        try {
            return (long) longValue.invoke(monitor);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Cannot read monitor value", e);
        }
    }

    public long getYoungGCTime(TimeUnit timeUnit) {
        double freq = readLongValue(hrtMon);
        long us = Math.round((readLongValue(gc0TimeMon) / freq) * 1000000);
        return timeUnit.convert(us, TimeUnit.MICROSECONDS);
    }

    public long getTenuredGCTime(TimeUnit timeUnit) {
        double freq = readLongValue(hrtMon);
        long us = Math.round((readLongValue(gc1TimeMon) / freq) * 1000000);
        return timeUnit.convert(us, TimeUnit.MICROSECONDS);
    }

    public long getTimeMaxError(TimeUnit timeUnit) {
        long us = (long) Math.ceil((1000000.0 / readLongValue(hrtMon)) / 2);
        return timeUnit.convert(us, TimeUnit.MICROSECONDS);
    }

    public static synchronized PerfHelper get() throws RuntimeException {
        if (instance == null) {
            try {
                instance = new PerfHelper();
            } catch (IOException e) {
                throw new RuntimeException("Cannot access performance counters", e);
            }
        }
        return instance;
    }

}
