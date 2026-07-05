package pvz.model;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import pvz.util.AssetLoader;

/**
 * Handles the classic row-clearing lawn mower behaviour.
 */
public class LawnMower extends Characters {
    private final int row;
    private volatile boolean active;

    public LawnMower(int row) {
        this.row = row;
        this.health = Integer.MAX_VALUE;
        String spritePath = Yard.isPixelArtMode()
            ? "images/pixelart/pixelartzombies/PixelLawnCleanerImage1.png"
            : "images/yard-related/lawnmower.png";
        ImageView view = new ImageView(AssetLoader.loadImage(spritePath));
        view.setFitWidth(90);
        view.setFitHeight(70);
        view.setPreserveRatio(true);
        this.elementImage = view;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void appear(Pane root) {
        if (!root.getChildren().contains(elementImage)) {
            root.getChildren().add(elementImage);
        }
        setAlive(true);
    }

    @Override
    public void disappear(Pane root) {
        Platform.runLater(() -> root.getChildren().remove(elementImage));
        setAlive(false);
    }

    public void activate(AnchorPane root) {
        if (active) return;
        active = true;

        double stopX = Yard.WIDTH + elementImage.getFitWidth();
        final double speed = 8; // pixels per frame (~160px/sec at 20ms frame pacing)

        Platform.runLater(() -> {
            javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (!Yard.gameOn) {
                        stop();
                        disappear(root);
                        return;
                    }

                    elementImage.setLayoutX(elementImage.getLayoutX() + speed);
                    squashZombies();

                    if (elementImage.getLayoutX() >= stopX) {
                        stop();
                        disappear(root);
                    }
                }
            };
            timer.start();
        });
    }

    private void squashZombies() {
        java.util.List<Zombie> snapshot;
        synchronized (Yard.zombies) {
            snapshot = new java.util.ArrayList<>(Yard.zombies);
        }
        snapshot.forEach(zombie -> {
            if (zombie != null && zombie.getElementImage() != null &&
                    elementImage.getBoundsInParent().intersects(zombie.getElementImage().getBoundsInParent())) {
                zombie.takeDamage(Integer.MAX_VALUE);
            }
        });
    }

    @Override
    public void run() {
        // Lawn mowers are animated manually via activate().
    }
}
