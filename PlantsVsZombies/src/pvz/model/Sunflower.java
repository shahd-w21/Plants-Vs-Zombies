package pvz.model;

import javafx.animation.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

import pvz.util.AssetLoader;

public class Sunflower extends Plant {
    private boolean sunReady = false;

    public Sunflower() {
        super(50, 25, 50);
    }

    public Sunflower(int posX, int posY) {
        this();
        super.x = posX;
        super.y = posY;
        String spritePath = Yard.isPixelArtMode()
            ? "images/pixelart/pixelartplants/PixelSunFlower.gif"
            : "images/plants/sunflower.gif";
        ImageView sprite = new ImageView(AssetLoader.loadImage(spritePath));
        sprite.setFitWidth(73);
        sprite.setFitHeight(70);
        sprite.setPreserveRatio(true);
        sprite.setLayoutX((posX - sprite.getFitWidth() / 2) + 5);
        sprite.setLayoutY((posY - sprite.getFitHeight() / 2) - 15);
        setSprite(sprite);
    }

    @Override
    public void takeDamage(int amount) {
        super.takeDamage(amount);
        System.out.println("Sunflower damaged: " + amount + " HP left: " + this.health);
    }

    @Override
    public void run() {
    }

    public void startSunProduction(AnchorPane root) {
        Timeline sunTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0), event -> createSun(root)),
                new KeyFrame(Duration.seconds(10), event -> createSun(root))
        );
        sunTimeline.setCycleCount(Timeline.INDEFINITE);
        sunTimeline.play();
    }

    public void beginSunProduction(AnchorPane root) {
        startSunProduction(root);
    }

    private void createSun(AnchorPane root) {
        if (this.isAlive()) {
            if (sunReady) return;
            sunReady = true;
            ColorAdjust adjust = new ColorAdjust();
            adjust.setBrightness(0.5);
            sprite.setEffect(adjust);
            Timeline effectTimeline = new Timeline(
                    new KeyFrame(Duration.millis(800), new KeyValue(adjust.brightnessProperty(), 0))
            );
            effectTimeline.setCycleCount(1);
            effectTimeline.play();
            Sun sun = new Sun((int) sprite.getLayoutX(), (int) sprite.getLayoutY());
            root.getChildren().add(sun.getElementImage());
            Path path = new Path();
            MoveTo start = new MoveTo(sprite.getLayoutX() + 40, sprite.getLayoutY());
            ArcTo curve = new ArcTo();
            curve.setX(sprite.getLayoutX() + 70);
            curve.setY(sprite.getLayoutY() + 60);
            curve.setRadiusX(20);
            curve.setRadiusY(20);
            curve.setSweepFlag(true);
            path.getElements().addAll(start, curve);
            PathTransition transition = new PathTransition();
            transition.setDuration(Duration.seconds(1));
            transition.setPath(path);
            transition.setNode(sun.getElementImage());
            transition.setCycleCount(1);
            transition.setOnFinished(event -> {
                sun.getElementImage().setOnMouseClicked(clickEvent -> {
                    sun.sunCollectedAudio();
                    Yard.sunCounter += 25;
                    Yard.label.setText(String.valueOf(Yard.sunCounter));
                    root.getChildren().remove(sun.getElementImage());
                    sunReady = false;
                });
                PauseTransition removeDelay = new PauseTransition(Duration.seconds(8));
                removeDelay.setOnFinished(e -> {
                    root.getChildren().remove(sun.getElementImage());
                    sunReady = false;
                });
                removeDelay.play();
            });
            transition.play();
        }
    }
}
