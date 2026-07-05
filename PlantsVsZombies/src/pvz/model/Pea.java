package pvz.model;

import java.io.Serializable;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import pvz.util.AssetLoader;

/**
 * Projectile fired by peashooters. Handles movement, collisions, and TorchWood buffs.
 */
public class Pea extends Characters implements Serializable, Runnable {
    private static final long MOVE_DELAY_MS = 3;
    private static final double MOVE_PIXELS = 1.0;

    private Plant parent;
    protected int damage;
    private final Image normalPeaImage;
    private final Image firePeaImage;
    private boolean firePeaActive;
    private volatile boolean cleanedUp;
    private boolean slowEffect;
    private double slowFactor = 0.5;
    private long slowDurationMs = 3000;
    private ColorAdjust tintEffect;

    public Pea(int damage, Plant parent) {
        this.damage = damage;
        this.parent = parent;
        this.health = 1;
        this.normalPeaImage = AssetLoader.loadImage("images/others/pea.png");
        this.firePeaImage = loadImageOrFallback("images/projectiles/firePea.gif", normalPeaImage);
        ImageView view = new ImageView(normalPeaImage);
        view.setFitWidth(30);
        view.setFitHeight(30);
        view.setPreserveRatio(true);
        this.elementImage = view;
    }

    @Override
    public void appear(Pane root) {
        Platform.runLater(() -> {
            if (elementImage != null && !root.getChildren().contains(elementImage)) {
                root.getChildren().add(elementImage);
            }
            setAlive(true);
        });
    }

    @Override
    public void disappear(Pane root) {
        Platform.runLater(() -> {
            if (elementImage != null) {
                root.getChildren().remove(elementImage);
            }
            setAlive(false);
        });
    }

    @Override
    public void run() {
        try {
            if (!Yard.gameOn || Yard.timeLeft <= 0) {
                cleanup();
                return;
            }
            Thread.sleep(20); // allow sprite to appear before moving
            while (Yard.gameOn && isAlive() && withinBounds()) {
                Platform.runLater(() -> {
                    changePeaToFirePea();
                    elementImage.setLayoutX(elementImage.getLayoutX() + MOVE_PIXELS);
                });
                Zombie target = checkForZombieCollision();
                if (target != null) {
                    if (slowEffect) {
                        target.applySlow(slowFactor, slowDurationMs);
                    }
                    target.takeDamage(damage);
                    peaHitsZombieAudio();
                    break;
                }
                Thread.sleep(MOVE_DELAY_MS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            cleanup();
        }
    }

    private boolean withinBounds() {
        return parent != null && parent.isAlive() && elementImage.getLayoutX() < Yard.WIDTH;
    }

    private void cleanup() {
        if (cleanedUp) {
            return;
        }
        cleanedUp = true;
        disappear(Yard.root);
        synchronized (Yard.peas) {
            Yard.peas.remove(this);
        }
        setAlive(false);
    }

    private void changePeaToFirePea() {
        if (firePeaActive || elementImage == null) {
            return;
        }
        synchronized (Yard.plants) {
            for (Plant plant : Yard.plants) {
                if (!(plant instanceof TorchWood) || !plant.isAlive() || plant.getElementImage() == null) {
                    continue;
                }

                // Prefer a lane-based check so peas ignite even if sprites do not perfectly overlap.
                boolean sameRow = parent != null && plant.getX() == parent.getX();
                double peaLeft = elementImage.getLayoutX();
                double peaRight = peaLeft + elementImage.getFitWidth();
                double torchLeft = plant.getElementImage().getLayoutX();
                double torchRight = torchLeft + plant.getElementImage().getFitWidth();
                boolean crossesTorch = peaRight >= torchLeft && peaLeft <= torchRight;

                if (sameRow && crossesTorch) {
                    ignitePea();
                    return;
                }

                // Fallback to bounds intersection in case rows were not set properly.
                if (elementImage.getBoundsInParent().intersects(plant.getElementImage().getBoundsInParent())) {
                    ignitePea();
                    return;
                }
            }
        }
    }

    private void ignitePea() {
        firePeaActive = true;
        slowEffect = false; // torchwood burns away ice slowing
        damage = 25; // Buffed but not overpowered

        // Tint the standard pea orange instead of swapping to a fire gif
        setTintHue(0.08); // ~orange hue shift
        Platform.runLater(() -> {
            elementImage.setImage(normalPeaImage);
            elementImage.setPreserveRatio(true);
            elementImage.setFitWidth(34);
            elementImage.setFitHeight(34);
        });
        firePeaAudio();
    }

    private synchronized Zombie checkForZombieCollision() {
        synchronized (Yard.zombies) {
            for (Zombie zombie : Yard.zombies) {
                if (zombie == null || !zombie.isAlive() || zombie.getElementImage() == null) {
                    continue;
                }
                if (elementImage.getBoundsInParent().intersects(zombie.getElementImage().getBoundsInParent())) {
                    return zombie;
                }
            }
        }
        return null;
    }

    public void peaHitsZombieAudio() {
        playAudio("/pvz/music/pea hits zombie.mp3", 0.3);
    }

    public void firePeaAudio() {
        playAudio("/pvz/music/fire pea.mp3", 0.3);
    }

    private void playAudio(String resourcePath, double volume) {
        try {
            var resource = getClass().getResource(resourcePath);
            if (resource == null) {
                return;
            }
            Media media = new Media(resource.toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(volume);
            mediaPlayer.play();
        } catch (Exception e) {
            System.out.println("Error playing pea audio: " + e.getMessage());
        }
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public void enableSlowEffect(double factor, long durationMs) {
        this.slowEffect = true;
        if (factor > 0 && factor < 1) {
            this.slowFactor = factor;
        }
        if (durationMs > 0) {
            this.slowDurationMs = durationMs;
        }
    }

    public void setTintHue(double hue) {
        ColorAdjust adjust = new ColorAdjust();
        adjust.setHue(hue);
        adjust.setSaturation(0.6);
        adjust.setBrightness(0.1);
        this.tintEffect = adjust;
        Platform.runLater(() -> elementImage.setEffect(adjust));
    }

    public void shot(Zombie zombie) {
        this.x += 5;
        if (zombie.getX() == this.x) {
            zombie.takeDamage(this.damage);
        }
    }

    @Override
    public void takeDamage(int damage) {
        // Peas are removed on impact; no separate damage handling needed.
    }

    private Image loadImageOrFallback(String path, Image fallback) {
        try {
            return AssetLoader.loadImage(path);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}
