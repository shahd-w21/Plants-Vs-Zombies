package pvz.model;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * Base class for all plants placed on the yard grid.
 */
public abstract class Plant extends Characters implements Runnable {
    protected int cost;
    protected ImageView sprite;

    public Plant() {
    }

    public Plant(int cost, double waitingTime, int health) {
        super(health, waitingTime);
        this.cost = cost;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    /** Helper to keep sprite & elementImage references in sync. */
    protected void setSprite(ImageView sprite) {
        this.sprite = sprite;
        this.elementImage = sprite;
    }

    protected ImageView getSprite() {
        return sprite != null ? sprite : elementImage;
    }

    @Override
    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            health = 0;
            disappear(Yard.root);
            synchronized (Yard.grid) {
                int row = getX();
                int col = getY();
                if (row >= 0 && row < Yard.ROWS && col >= 0 && col < Yard.COLUMNS && Yard.grid[row][col] == this) {
                    Yard.grid[row][col] = null;
                }
            }
            synchronized (Yard.plants) {
                Yard.plants.remove(this);
            }
            System.out.println("Plant has died!");
        }
    }

    @Override
    public void appear(Pane root) {
        setAlive(true);
        ImageView node = getSprite();
        Platform.runLater(() -> {
            if (node != null && !root.getChildren().contains(node)) {
                node.setVisible(true);
                root.getChildren().add(node);
                System.out.println("Plant appears.");
            }
        });
    }

    @Override
    public void disappear(Pane root) {
        setAlive(false);
        Thread current = Thread.currentThread();
        if (current != null && !"JavaFX Application Thread".equals(current.getName())) {
            current.interrupt();
        }
        ImageView node = getSprite();
        if (node != null) {
            Platform.runLater(() -> {
                node.setVisible(false);
                root.getChildren().remove(node);
                System.out.println("Plant disappears.");
            });
        }
    }

    @Override
    public abstract void run();
}
