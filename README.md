# GC-aware Stopwatch
This repository contains two useful classes only:
1. `GCStopwatch`: A Guava-like Stopwatch that discounts GC time 
2. `PerfHelper`: A helper class to read JVM performance counters without spawning a jstat process

## WARNINGS
First, think if you really should discount GC time and you will do with those measurements. 
Discounting GC time will give you wall-time performance measurements that with less variability. 
But it will not stop the GC from running! One cannot simply disable the GC on a JVM and completely 
removing the GC time from performance evaluations may not be fair. 

Second, this uses the HotSpot performance counters accessed though jstat and VisualGC plugin 
from visualgc. These performance counters simply count for how much time the GC has run. If you 
use a parallel GC or the Concurrent Mark Sweep GC results will be unreliable and meaningless! 
Results are only reliable with the Serial GC. Enable it with `-XX:+UseSerialGC`.  

Third, granularity of GC measurements are limited by the JVM performance counters tick period. 
Use the `PerfHelper.getTimeMaxError(TimeUnit)` method to get the maximum expected error due to 
tick period.
     
## Usage
This is not published to maven central, so you can either copy-and-paste the two classes, or:

1. `git clone https://github.com/alexishuf/stopwatch`
2. `mvn clean install`
3. Add the following to your pom.xml:
```xml
<dependency>
  <groupId>br.ufsc.inf.ufsc.lapesd.gcstopwatch</groupId>
  <artifactId>gcstopwatch</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

From code, use is similar to a Guava stopwatch. The only difference is that construction is done 
with a builder:

```java
GCStopwatch gcWatch = GCStopwatch.builder().withoutGC().createStarted();
//... do some work ...
System.out.printf("Took: %.3f ms\n", gcWatch.elapsed(TimeUnit.MICROSECONDS)/1000.0);
```

## References
- Quick overview of generations and garbage collectors: [1]
- More in-depth discussion of non-serial collectors: [2]
- jstat documentation: [3] 

[1]: http://www.oracle.com/webfolder/technetwork/tutorials/obe/java/gc01/index.html
[2]: http://www.oracle.com/technetwork/java/gc-tuning-5-138395.html#1.1.%20Types%20of%20Collectors%7Coutline
[3]: https://docs.oracle.com/javase/7/docs/technotes/tools/share/jstat.html