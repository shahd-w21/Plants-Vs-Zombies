package pvz.model;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.ArrayList;

import pvz.util.AssetLoader;

public class Cherry extends Plant {
    public Cherry() {
        super(150, 7, 150);
    }

    public Cherry(int x, int y) {
        this();
        this.x = x;
        this.y = y;
        String spritePath = Yard.isPixelArtMode()
            ? "images/pixelart/pixelartplants/pixelCherry.png"
            : "images/plants/cherry.png";
        ImageView sprite = new ImageView(AssetLoader.loadImage(spritePath));
        sprite.setFitWidth(90);
        sprite.setFitHeight(90);
        sprite.setPreserveRatio(true);
        sprite.setLayoutX(x - sprite.getFitWidth() / 2.0);
        sprite.setLayoutY(y - sprite.getFitHeight() / 2.0);
        setSprite(sprite);
    }

    @Override
    public void run() {
        try {
            // Small fuse before exploding
            Thread.sleep(1000);
            triggerExplosion();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void triggerExplosion() {
        ImageView node = getSprite();
        if (node != null) {
            ScaleTransition grow = new ScaleTransition(Duration.millis(250), node);
            grow.setToX(1.6);
            grow.setToY(1.6);
            grow.setOnFinished(evt -> {
                damageNearbyZombies(170, 500);
                disappear(Yard.root);
            });
            Platform.runLater(grow::play);
        } else {
            damageNearbyZombies(170, 500);
            Platform.runLater(() -> disappear(Yard.root));
        }
    }

    private void damageNearbyZombies(double radius, int damage) {
        ArrayList<Zombie> snapshot;
        synchronized (Yard.zombies) {
            snapshot = new ArrayList<>(Yard.zombies);
        }

        ImageView node = getSprite();
        double centerX = node != null ? node.getLayoutX() + node.getFitWidth() / 2.0 : x;
        double centerY = node != null ? node.getLayoutY() + node.getFitHeight() / 2.0 : y;

        for (Zombie zombie : snapshot) {
            if (zombie == null || !zombie.isAlive() || zombie.getElementImage() == null) {
                continue;
            }
            ImageView zombieView = zombie.getElementImage();
            double dx = (zombieView.getLayoutX() + zombieView.getFitWidth() / 2.0) - centerX;
            double dy = (zombieView.getLayoutY() + zombieView.getFitHeight() / 2.0) - centerY;
            if (Math.hypot(dx, dy) <= radius) {
                zombie.takeDamage(damage);
            }
        }
    }
}
