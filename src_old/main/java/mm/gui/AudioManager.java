package mm.gui;

import javafx.scene.media.MediaPlayer;
import java.util.ArrayList;
import java.util.List;

public class AudioManager {
    private static AudioManager instance;
    private double masterVolume = 0.5; // 0.0 bis 1.0
    private boolean isMuted = false;
    private final List<MediaPlayer> activeMediaPlayers;
    private final List<SettingsObserver> observers;

    private AudioManager() {
        activeMediaPlayers = new ArrayList<>();
        observers = new ArrayList<>();
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
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
        double effectiveVolume = isMuted ? 0.0 : masterVolume;
        for (SettingsObserver observer : observers) {
            observer.onVolumeChanged(effectiveVolume);
        }
    }

    public void setMasterVolume(double volume) {
        if (volume < 0.0 || volume > 1.0) {
            throw new IllegalArgumentException("Volume must be between 0.0 and 1.0");
        }
        if (this.masterVolume != volume) {
            this.masterVolume = volume;
            updateMediaPlayerVolumes();
            notifyObservers();
        }
    }

    public double getMasterVolume() {
        return masterVolume;
    }

    public void setMuted(boolean muted) {
        if (this.isMuted != muted) {
            this.isMuted = muted;
            updateMediaPlayerVolumes();
            notifyObservers();
        }
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void registerMediaPlayer(MediaPlayer mediaPlayer) {
        activeMediaPlayers.add(mediaPlayer);
        updateMediaPlayerVolume(mediaPlayer);
    }

    public void unregisterMediaPlayer(MediaPlayer mediaPlayer) {
        activeMediaPlayers.remove(mediaPlayer);
    }

    private void updateMediaPlayerVolumes() {
        for (MediaPlayer player : activeMediaPlayers) {
            updateMediaPlayerVolume(player);
        }
    }

    private void updateMediaPlayerVolume(MediaPlayer player) {
        player.setVolume(isMuted ? 0.0 : masterVolume);
    }
} 