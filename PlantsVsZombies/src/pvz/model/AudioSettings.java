package pvz.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javafx.scene.media.MediaPlayer;

/**
 * Central audio preferences for menu and in-game music.
 */
public final class AudioSettings {
    private static boolean musicEnabled = true;
    private static double musicVolume = 0.2;
    private static final Set<MediaPlayer> trackedPlayers = new HashSet<>();

    private AudioSettings() { }

    public static synchronized boolean isMusicEnabled() {
        return musicEnabled;
    }

    public static synchronized double getMusicVolume() {
        return musicVolume;
    }

    public static synchronized void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        applyToTrackedPlayers();
    }

    public static synchronized void setMusicVolume(double volume) {
        musicVolume = clamp(volume, 0.0, 1.0);
        applyToTrackedPlayers();
    }

    public static synchronized void register(MediaPlayer player) {
        if (player == null) return;
        trackedPlayers.add(player);
        apply(player);
    }

    public static synchronized void unregister(MediaPlayer player) {
        if (player == null) return;
        trackedPlayers.remove(player);
    }

    public static synchronized void apply(MediaPlayer player) {
        if (player == null) return;
        player.setMute(!musicEnabled);
        player.setVolume(musicEnabled ? musicVolume : 0.0);
    }

    public static synchronized Set<MediaPlayer> getTrackedPlayers() {
        return Collections.unmodifiableSet(trackedPlayers);
    }

    private static synchronized void applyToTrackedPlayers() {
        for (MediaPlayer player : trackedPlayers) {
            apply(player);
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
