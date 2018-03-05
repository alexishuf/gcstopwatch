package br.ufsc.inf.ufsc.lapesd.stopwatch;

import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public class GCStopwatch {
    private final Stopwatch delegate;
    private final boolean withoutGCTenured, withoutGCYoung;
    private long oldGCTenuredUs = 0, oldGCYoungUs = 0;
    private long stopTime = -1;

    @SuppressWarnings("WeakerAccess")
    protected GCStopwatch(Stopwatch delegate, boolean withoutGCTenured, boolean withoutGCYoung) {
        this.delegate = delegate;
        this.withoutGCTenured = withoutGCTenured;
        this.withoutGCYoung = withoutGCYoung;

    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public static class Builder {
        private boolean withoutGCTenured = false, withoutGCYoung = false;

        public Builder withoutGC() {
            withoutGCYoung();
            withoutGCTenured();
            return this;
        }

        public Builder withoutGCYoung() {
            withoutGCYoung = true;
            return this;
        }

        public Builder withoutGCTenured() {
            withoutGCTenured = true;
            return this;
        }

        public GCStopwatch createStarted() {
            return new GCStopwatch(Stopwatch.createStarted(), withoutGCTenured, withoutGCYoung);
        }

        public GCStopwatch createUnstarted() {
            return new GCStopwatch(Stopwatch.createUnstarted(), withoutGCTenured, withoutGCYoung);
        }
    }

    public static Builder builder() {
        return new Builder();
    }


    public GCStopwatch reset() {
        delegate.reset();
        oldGCTenuredUs = 0;
        oldGCYoungUs = 0;
        stopTime = -1;
        return this;
    }

    public GCStopwatch start() {
        delegate.start();
        return this;
    }

    public GCStopwatch stop() {
        delegate.stop();
        stopTime = elapsed(TimeUnit.MICROSECONDS);
        return this;
    }

    public long elapsed(TimeUnit timeUnit) {
        if (stopTime > 0)
            return timeUnit.convert(stopTime, TimeUnit.MICROSECONDS);

        long wallTime = delegate.elapsed(TimeUnit.MICROSECONDS);
        if (withoutGCTenured) {
            long newGCTenuredUs = PerfHelper.get().getTenuredGCTime(TimeUnit.MICROSECONDS);
            wallTime -= newGCTenuredUs - oldGCTenuredUs;
            oldGCTenuredUs = newGCTenuredUs;
        }
        if (withoutGCYoung) {
            long newGCYoungUs = PerfHelper.get().getYoungGCTime(TimeUnit.MICROSECONDS);
            wallTime -= newGCYoungUs - oldGCYoungUs;
            oldGCYoungUs = newGCYoungUs;
        }
        return timeUnit.convert(wallTime, TimeUnit.MICROSECONDS);
    }
}
