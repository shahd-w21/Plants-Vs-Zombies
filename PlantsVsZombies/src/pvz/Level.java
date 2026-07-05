package pvz;

import java.io.Serializable;

import javafx.scene.Scene;
import javafx.stage.Stage;
import pvz.model.LoadingScreen;
import pvz.model.SoundtrackPlayer;
import pvz.model.Yard;

/**
 * Represents a single level selection within the game menu. In addition to the
 * level number, this class stores simple metadata (like the intended duration)
 * and owns the {@link Yard} instance representing the actual gameplay scene.
 */
public class Level implements Serializable {

    private static final long serialVersionUID = 1L;

    private int levelNumber;
    private int durationInSeconds;
    private Yard currentYard;

    public Level(int levelNumber) {
        this(levelNumber, defaultDurationFor(levelNumber));
    }

    public Level(int levelNumber, int durationInSeconds) {
        this.levelNumber = levelNumber;
        this.durationInSeconds = durationInSeconds;
    }

    private static int defaultDurationFor(int levelNumber) {
        return switch (levelNumber) {
            case 2 -> 150;
            case 3 -> 160; 
            case 5 -> 150; 
            default -> 130; 
        };
    }

    /**
     * Creates and displays the {@link Yard} associated with this level on the
     * provided JavaFX {@link Stage}. The yard thread is started automatically.
     */
    public Yard startLevel(Stage stage) {
        playLevelSoundtrack();

        currentYard = new Yard(this, stage);

        LoadingScreen.show(stage, () -> {
            Scene yardScene = new Scene(currentYard.root, Yard.WIDTH, Yard.HEIGHT);
            stage.setScene(yardScene);
            stage.setTitle("PvZ - Level " + levelNumber);
            stage.setResizable(false);
            currentYard.start();
        });
        return currentYard;
    }

    private void playLevelSoundtrack() {
        SoundtrackPlayer.stopTrack();
        switch (levelNumber) {
            case 2 -> SoundtrackPlayer.playInGametrack2();
            case 3 -> SoundtrackPlayer.playInGametrack3();
            default -> SoundtrackPlayer.playInGametrack1();
        }
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(int durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public Yard getCurrentYard() {
        return currentYard;
    }

    public void setCurrentYard(Yard currentYard) {
        this.currentYard = currentYard;
    }
}
