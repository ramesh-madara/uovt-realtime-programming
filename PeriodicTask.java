/*
 * IT404041 Real Time Programming - Week 1 Practical
 * Exercise 2: A periodic task - period, jitter and drift
 *
 * Runs a 10 ms periodic task in two ways and measures how accurate it is:
 *   Mode A (relative): sleep(PERIOD) after each job   -> drifts
 *   Mode B (absolute): sleep until the next planned release time -> no drift
 *
 * Compile:  javac PeriodicTask.java
 * Run:      java PeriodicTask
 */
public class PeriodicTask {

    static final long PERIOD_MS = 10;
    static final int CYCLES = 200;

    // Simulated job: about 2 ms of work (e.g. read sensor + compute).
    static void job() {
        long end = System.nanoTime() + 2_000_000;
        while (System.nanoTime() < end) { /* busy work */ }
    }

    // Mode A: sleep for PERIOD after each job (the naive way).
    static long[] runRelative() throws InterruptedException {
        long[] release = new long[CYCLES];
        for (int i = 0; i < CYCLES; i++) {
            release[i] = System.nanoTime();
            job();
            Thread.sleep(PERIOD_MS);          // period = job time + sleep time!
        }
        return release;
    }

    // Mode B: compute each release time from the start time.
    static long[] runAbsolute() throws InterruptedException {
        long[] release = new long[CYCLES];
        long start = System.nanoTime();
        for (int i = 0; i < CYCLES; i++) {
            long planned = start + i * PERIOD_MS * 1_000_000;
            long now = System.nanoTime();
            if (planned > now) Thread.sleep((planned - now) / 1_000_000, (int) ((planned - now) % 1_000_000));
            release[i] = System.nanoTime();
            job();
        }
        return release;
    }

    static void report(String name, long[] release) {
        double min = Double.MAX_VALUE, max = 0, sum = 0;
        for (int i = 1; i < release.length; i++) {
            double p = (release[i] - release[i - 1]) / 1e6;   // actual period in ms
            min = Math.min(min, p); max = Math.max(max, p); sum += p;
        }
        double avg = sum / (release.length - 1);
        double expectedEnd = (release.length - 1) * PERIOD_MS;
        double actualEnd = (release[release.length - 1] - release[0]) / 1e6;
        System.out.println("== " + name + " ==");
        System.out.printf("  period  min %.2f  avg %.2f  max %.2f ms (target %d ms)%n", min, avg, max, PERIOD_MS);
        System.out.printf("  jitter  (max - min)          : %.2f ms%n", max - min);
        System.out.printf("  drift after %d cycles        : %.1f ms%n%n", release.length, actualEnd - expectedEnd);
    }

    public static void main(String[] args) throws InterruptedException {
        report("Mode A: relative sleep", runRelative());
        report("Mode B: absolute release times", runAbsolute());
    }
}
