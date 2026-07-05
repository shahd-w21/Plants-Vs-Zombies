package pvz.model;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import pvz.util.AssetLoader;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public abstract class Zombie extends Characters implements Runnable {

    protected int attackPower;
    protected double speed;
    private double baseSpeed;
    private volatile boolean isAttacking = false;
    private volatile boolean slowed = false;
    private PauseTransition slowReset;

    public Zombie() {
    }

    public Zombie(int attackPower, double speed, int health) {
        this.attackPower = attackPower;
        this.speed = speed;
        this.baseSpeed = speed;
        this.health = health;
    }

    public boolean isSlowed() {
        return slowed;
    }

    public void setSlowed(boolean slowed) {
        this.slowed = slowed;
    }

    public double getBaseSpeed() {
        return baseSpeed;
    }

    public void setBaseSpeed(double baseSpeed) {
        this.baseSpeed = baseSpeed;
        this.speed = baseSpeed;
    }

    public double getAttackPower() {
        return attackPower;
    }

    public void setAttackPower(int attackPower) {
        this.attackPower = attackPower;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public boolean isColliding(ImageView object) {
        double margin = 50;
        var zombieBounds = elementImage.getBoundsInParent();
        var objectBounds = object.getBoundsInParent();
        var adjustedZombieBounds = new javafx.geometry.BoundingBox(
                zombieBounds.getMinX() + margin,
                zombieBounds.getMinY() + margin,
                zombieBounds.getWidth() - 2 * margin,
                zombieBounds.getHeight() - 2 * margin
        );
        return adjustedZombieBounds.intersects(objectBounds);
    }

    public Plant checkForPlantCollision() {
        synchronized (Yard.plants) {
            for (Plant plant : Yard.plants) {
                if (isColliding(plant.elementImage)) {
                    return plant;
                }
            }
        }
        return null;
    }

    public synchronized void move() {
        if (!isAlive() || isAttacking) {
            return;
        }
        if (!Yard.gameOn) {
            return;
        }
        Plant targetPlant = checkForPlantCollision();
        if (targetPlant != null && targetPlant.isAlive()) {
            attack(targetPlant);
        } else {
            Platform.runLater(() -> {
                elementImage.setLayoutX(elementImage.getLayoutX() - speed);
            });
            if (elementImage.getLayoutX() <= -elementImage.getFitWidth()) {
                Yard.gameOver();
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public synchronized void applySlow(double factor, long durationMs) {
        if (factor <= 0 || factor >= 1) {
            factor = 0.5; // default slow factor
        }
        if (durationMs <= 0) {
            durationMs = 3000; // default slow time in ms
        }

        double slowedSpeed = baseSpeed * factor;
        setSlowed(true);
        setSpeed(slowedSpeed);

        if (slowReset != null) {
            slowReset.stop();
        }

        PauseTransition reset = new PauseTransition(Duration.millis(durationMs));
        reset.setOnFinished(event -> {
            setSpeed(baseSpeed);
            setSlowed(false);
        });
        slowReset = reset;
        Platform.runLater(reset::play);
    }

    @Override
    public void takeDamage(int damage) {
        health -= damage;
        System.out.println("Zombie takes damage: " + damage + " Health: " + health);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(0.5);
        elementImage.setEffect(colorAdjust);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(200),
                        new KeyValue(colorAdjust.brightnessProperty(), 0)
                )
        );
        timeline.setCycleCount(1);
        timeline.play();
        if (health <= 0) {
            setAlive(false);
            synchronized (Yard.zombies) {
                Yard.zombies.remove(this);
            }
            Platform.runLater(() -> {
                disappear(Yard.root);
            });
        }
    }

    private void attack(Plant targetPlant) {
        if (isAttacking) {
            return;
        }
        isAttacking = true;
        zombieEatingAudio();
        double originalSpeed = this.getSpeed();
        setSpeed(0);
        if (this instanceof FootballZombie) {
            elementImage.setImage(AssetLoader.loadImage("images/zombies/FootballZombieAttack.gif"));
            elementImage.setFitWidth(120);
            elementImage.setFitHeight(125);
            elementImage.setPreserveRatio(true);
        } else if (this instanceof ConeZombie) {
            elementImage.setImage(AssetLoader.loadImage("images/zombies/ConeheadZombieAttack.gif"));
            elementImage.setFitHeight(155);
            elementImage.setFitWidth(134);
            elementImage.setPreserveRatio(true);
        } else if (this instanceof DefaultZombie) {
            elementImage.setImage(AssetLoader.loadImage("images/zombies/ZombieAttack.gif"));
            elementImage.setFitHeight(155);
            elementImage.setFitWidth(134);
            elementImage.setPreserveRatio(true);
        } else if (this instanceof HelmetZombie) {
            elementImage.setImage(AssetLoader.loadImage("images/zombies/BucketheadZombieAttack.gif"));
            elementImage.setFitHeight(155);
            elementImage.setFitWidth(134);
            elementImage.setPreserveRatio(true);
        }
        Thread attackThread = new Thread(() -> {
            try {
                while (isAlive() && targetPlant.isAlive() && isColliding(targetPlant.elementImage)) {
                    targetPlant.takeDamage(attackPower);
                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                isAttacking = false;
                Platform.runLater(() -> {
                    if (this instanceof FootballZombie && this.isAlive()) {
                        elementImage.setImage(AssetLoader.loadImage("images/zombies/FootballZombie.gif"));
                        elementImage.setFitWidth(120);
                        elementImage.setFitHeight(125);
                        elementImage.setPreserveRatio(true);
                    } else if (this instanceof DefaultZombie && this.isAlive()) {
                        elementImage.setImage(AssetLoader.loadImage("images/zombies/Zombie.gif"));
                        elementImage.setFitHeight(155);
                        elementImage.setFitWidth(134);
                        elementImage.setPreserveRatio(true);
                    } else if (this instanceof ConeZombie && this.isAlive()) {
                        elementImage.setImage(AssetLoader.loadImage("images/zombies/ConeZombie.gif"));
                        elementImage.setFitHeight(155);
                        elementImage.setFitWidth(134);
                        elementImage.setPreserveRatio(true);
                    } else if (this instanceof HelmetZombie && this.isAlive()) {
                        elementImage.setImage(AssetLoader.loadImage("images/zombies/BucketheadZombie.gif"));
                        elementImage.setFitHeight(155);
                        elementImage.setFitWidth(134);
                        elementImage.setPreserveRatio(true);
                    }
                });
                setSpeed(originalSpeed);
            }
        });
        attackThread.setDaemon(true);
        attackThread.start();
    }

    @Override
    public void appear(Pane root) {
        // Default no-op implementation. Specific spawning uses appear(root, x, y).
    }

    public void appear(Pane root, int x, int y) {
        Platform.runLater(() -> {
            elementImage.setLayoutX(x);
            elementImage.setLayoutY(y);
            root.getChildren().add(elementImage);
        });
        setAlive(true);
    }

    @Override
    public void disappear(Pane root) {
        if (this instanceof FootballZombie) {
            elementImage.setImage(AssetLoader.loadImage("images/zombies/FootballZombieDie.gif"));
            elementImage.setFitWidth(134);
            elementImage.setFitHeight(150);
            elementImage.setPreserveRatio(true);
        } else {
            elementImage.setImage(AssetLoader.loadImage("images/zombies/ZombieDie.gif"));
            elementImage.setFitHeight(155);
            elementImage.setFitWidth(134);
            elementImage.setPreserveRatio(true);
        }
        double gifDurationInSeconds = (this instanceof FootballZombie) ? 0.8 : 1.6;
        PauseTransition pause = new PauseTransition(Duration.seconds(gifDurationInSeconds));
        pause.setOnFinished(event -> {
            root.getChildren().remove(elementImage);
        });
        pause.play();
    }

    public void zombieEatingAudio() {
        try {
            String path = getClass().getResource("/pvz/music/zombie eating.mp3").toExternalForm();
            Media media = new Media(path);
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(0.3);
            javafx.application.Platform.runLater(() -> mediaPlayer.play());
        } catch (Exception e) {
            System.out.println("Error playing zombie eating sound: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        while (isAlive() && !Thread.currentThread().isInterrupted()) {
            move();
        }
    }
}