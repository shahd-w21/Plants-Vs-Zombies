package pvz.model;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import pvz.util.AssetLoader;

public class IcedPeashooter extends Plant {
    public IcedPeashooter() {
        super(175, 20, 100);
    }

    public IcedPeashooter(int x, int y) {
        this();
        this.x = x;
        this.y = y;
        String spritePath = Yard.isPixelArtMode()
            ? "images/pixelart/pixelartplants/pixelIcePeaShooter.gif"
            : "images/plants/icedpeashooter.gif";
        ImageView sprite = new ImageView(AssetLoader.loadImage(spritePath));
        sprite.setFitWidth(90);
        sprite.setFitHeight(85);
        sprite.setPreserveRatio(true);
        sprite.setLayoutX((x - sprite.getFitWidth() / 2) + 5);
        sprite.setLayoutY((y - sprite.getFitHeight() / 2) - 25);
        setSprite(sprite);
    }

    @Override
    public void run() {
        while (isAlive() && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(3500);
                if (!isAlive() || Thread.currentThread().isInterrupted()) {
                    break;
                }

                if (!hasZombieInLane()) {
                    continue;
                }

                Platform.runLater(() -> {
                    if (!isAlive()) {
                        return;
                    }
                    Pea projectile = new Pea(15, this);
                    projectile.enableSlowEffect(0.5, 4000);
                    projectile.setTintHue(0.5); // shift pea color toward light blue for ice shots
                    projectile.getElementImage().setLayoutX(getSprite().getLayoutX() + 65);
                    projectile.getElementImage().setLayoutY(getSprite().getLayoutY() + 31);
                    projectile.appear(Yard.root);
                    Yard.peas.add(projectile);
                    Thread projectileThread = new Thread(projectile);
                    projectileThread.setDaemon(true);
                    projectileThread.start();
                    playShootSound();
                });
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                System.out.println("IcedPeashooter interrupted: " + ex.getMessage());
            }
        }
        Platform.runLater(() -> disappear(Yard.root));
        System.out.println("IcedPeashooter stopped.");
    }

    private boolean hasZombieInLane() {
        ImageView sprite = getSprite();
        if (sprite == null) {
            return false;
        }

        double shooterY = sprite.getLayoutY();
        double shooterX = sprite.getLayoutX();
        final double laneTolerance = 45.0; // pixels

        synchronized (Yard.zombies) {
            for (Zombie zombie : Yard.zombies) {
                if (zombie == null || !zombie.isAlive()) {
                    continue;
                }
                ImageView zombieView = zombie.getElementImage();
                if (zombieView == null) {
                    continue;
                }
                double zombieY = zombieView.getLayoutY();
                double zombieX = zombieView.getLayoutX();
                if (Math.abs(zombieY - shooterY) <= laneTolerance && zombieX > shooterX) {
                    return true;
                }
            }
        }
        return false;
    }

    private void playShootSound() {
        try {
            String audioPath = getClass().getResource("/pvz/music/peashooter-shoot.mp3").toExternalForm();
            Media sound = new Media(audioPath);
            MediaPlayer player = new MediaPlayer(sound);
            player.setVolume(0.3);
            player.play();
        } catch (Exception ex) {
            System.out.println("Audio error: " + ex.getMessage());
        }
    }
}
