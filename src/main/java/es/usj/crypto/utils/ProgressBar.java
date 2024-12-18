package es.usj.crypto.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class ProgressBar {
    private final AtomicLong currentProgress;
    private final long totalProgress;
    private final Instant startTime;
    private long lastRemainingTime; // Keeps track of the last remaining time for smoothing

    public long get() {
        return currentProgress.get();
    }

    public ProgressBar(int totalProgress) {
        this((long) totalProgress);
    }

    public ProgressBar(long totalProgress) {
        this.currentProgress = new AtomicLong(0);
        this.totalProgress = totalProgress;
        this.startTime = Instant.now();
        this.lastRemainingTime = 0;
    }

    public void add(int increment) {
        add((long) increment);
    }

    public void add(long increment) {
        currentProgress.addAndGet(increment);
        showProgressBar();
    }

    public void showProgressBar() {
        long progress = currentProgress.get();
        int barLength = 50;
        int progressBar = (int) ((double) progress / totalProgress * barLength);
        StringBuilder bar = new StringBuilder("[");

        for (int i = 0; i < barLength; i++) {
            bar.append(i < progressBar ? "#" : " ");
        }
        bar.append("]");

        double percentage = ((double) progress / totalProgress) * 100;
        String elapsedTime = getElapsedTime();
        String eta = getETA(progress);

        String progressString = String.format("\r%s %.2f%% (%d/%d) Elapsed: %s ETA: %s",
                bar, percentage, progress, totalProgress, elapsedTime, eta);

        System.out.print(progressString);
        System.out.flush();

        if (progress == totalProgress) {
            System.out.println(); // Move to next line when complete
        }
    }

    private String getElapsedTime() {
        Duration elapsed = Duration.between(startTime, Instant.now());
        long hours = elapsed.toHours();
        long minutes = elapsed.toMinutesPart();
        long seconds = elapsed.toSecondsPart();

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String getETA(long progress) {
        if (progress == 0) {
            return "--:--:--"; // ETA cannot be calculated at 0 progress
        }

        // Calculate elapsed time
        Duration elapsed = Duration.between(startTime, Instant.now());
        long elapsedMillis = elapsed.toMillis();

        // Estimate total time based on progress
        long estimatedTotalMillis = (long) ((double) elapsedMillis / progress * totalProgress);
        long remainingMillis = Math.max(estimatedTotalMillis - elapsedMillis, 0);

        // Smooth the remaining time to avoid fluctuations
        if (lastRemainingTime == 0) {
            lastRemainingTime = remainingMillis; // Initialize for the first calculation
        } else {
            // Apply smoothing (e.g., exponential moving average)
            lastRemainingTime = (long) (0.8 * lastRemainingTime + 0.2 * remainingMillis);
        }

        // Convert remainingMillis to hours, minutes, and seconds
        long hours = (lastRemainingTime / (1000 * 60 * 60)) % 24;
        long minutes = (lastRemainingTime / (1000 * 60)) % 60;
        long seconds = (lastRemainingTime / 1000) % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void update(int processed) {
        this.currentProgress.set(processed);
    }
}