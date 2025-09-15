package mm.gui;

import javafx.animation.AnimationTimer;
import java.util.ArrayList;
import java.util.List;

public class FPSManager {
    private static FPSManager instance;
    private double targetFPS = 60.0;
    private AnimationTimer fpsLimiter;
    private long lastFrameTime = 0;
    private final long OPTIMAL_TIME = 1_000_000_000L; // 1 Sekunde in Nanosekunden
    private final List<SettingsObserver> observers;

    private FPSManager() {
        observers = new ArrayList<>();
        setupFPSLimiter();
    }

    public static FPSManager getInstance() {
        if (instance == null) {
            instance = new FPSManager();
        }
        return instance;
    }

    public void addObserver(SettingsObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(SettingsObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (SettingsObserver observer : observers) {
            observer.onFPSChanged(targetFPS);
        }
    }

    public void setTargetFPS(double fps) {
        if (fps < 30.0 || fps > 144.0) {
            throw new IllegalArgumentException("FPS must be between 30 and 144");
        }
        if (this.targetFPS != fps) {
            this.targetFPS = fps;
            notifyObservers();
        }
    }

    public double getTargetFPS() {
        return targetFPS;
    }

    private void setupFPSLimiter() {
        fpsLimiter = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastFrameTime == 0) {
                    lastFrameTime = now;
                    return;
                }

                long targetFrameTime = (long) (OPTIMAL_TIME / targetFPS);
                long elapsedNanos = now - lastFrameTime;

                if (elapsedNanos < targetFrameTime) {
                    // Warte bis zum nächsten Frame
                    return;
                }

                lastFrameTime = now;
            }
        };
    }

    public void startFPSLimiter() {
        if (fpsLimiter != null) {
            fpsLimiter.start();
        }
    }

    public void stopFPSLimiter() {
        if (fpsLimiter != null) {
            fpsLimiter.stop();
        }
    }
} 