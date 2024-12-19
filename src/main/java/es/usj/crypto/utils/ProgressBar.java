package es.usj.crypto.utils;

import java.time.Duration;
import java.time.Instant;

public class ProgressBar {
    private long currentProgress;
    private long totalProgress;
    private Instant startTime;
    private long lastRemainingTime; // Keeps track of the last remaining time for smoothing

    public long get() {
        return currentProgress;
    }

    public ProgressBar(int totalProgress) {
        this((long) totalProgress);
    }

    public ProgressBar(long totalProgress) {
        this.currentProgress = 0;
        this.totalProgress = totalProgress;
        this.startTime = Instant.now();
        this.lastRemainingTime = 0;
    }

    public synchronized void add(int increment) {
        add((long) increment);
    }

    public synchronized void add(long increment) {
        currentProgress = Math.min(currentProgress + increment, totalProgress);
        showProgressBar();
    }

    public void showProgressBar() {
        int barLength = 50;
        int progress = (int) ((double) currentProgress / totalProgress * barLength);
        StringBuilder bar = new StringBuilder("[");

        for (int i = 0; i < barLength; i++) {
            bar.append(i < progress ? "#" : " ");
        }
        bar.append("]");

        double percentage = ((double) currentProgress / totalProgress) * 100;
        String elapsedTime = getElapsedTime();
        String eta = getETA();

        String progressString = String.format("\r%s %.2f%% (%d/%d) Elapsed: %s ETA: %s",
                bar, percentage, currentProgress, totalProgress, elapsedTime, eta);

        System.out.print(progressString);
        System.out.flush();

        if (currentProgress == totalProgress) {
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

    private String getETA() {
        if (currentProgress == 0) {
            return "--:--:--"; // ETA cannot be calculated at 0 progress
        }

        // Calculate elapsed time
        Duration elapsed = Duration.between(startTime, Instant.now());
        long elapsedMillis = elapsed.toMillis();

        // Estimate total time based on progress
        long estimatedTotalMillis = (long) ((double) elapsedMillis / currentProgress * totalProgress);
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

    public void reset(int size) {
        this.currentProgress = 0;
        this.totalProgress = size;
        this.startTime = Instant.now();
        this.lastRemainingTime = 0;
    }
}
