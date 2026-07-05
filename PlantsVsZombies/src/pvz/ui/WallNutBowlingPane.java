package pvz.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.animation.AnimationTimer;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import javafx.stage.Stage;

import pvz.model.SoundtrackPlayer;
import pvz.model.LoadingScreen;
import pvz.model.Yard;
import pvz.util.AssetLoader;

/**
 * Dedicated wall-nut bowling pane that reuses the yard constants we just studied to feel like
 * the early levels while still driving a custom conveyor/minigame flow.
 */
public class WallNutBowlingPane extends Pane {
    public static final double WIDTH = 1400;
    public static final double HEIGHT = Yard.HEIGHT;
    private static final double PLAY_AREA_X = 262;
    private static final double PLAY_AREA_Y = 110;
    private static final double PLAY_AREA_WIDTH = 1035;
    private static final double PLAY_AREA_HEIGHT = HEIGHT - PLAY_AREA_Y - 25;
    private static final double RED_LINE_X = 500;

    private static final double CONVEYOR_WIDTH = 480;
    private static final double CONVEYOR_HEIGHT = 74; // shorter to fit inside the black bar
    private static final double CONVEYOR_X = 330;      // shifted right
    private static final double CONVEYOR_Y = 4;        // align vertically
    private static final double CARD_WIDTH = 50;
    private static final double CARD_HEIGHT = 70;
    private static final double CARD_COST = 25;
    private static final double CARD_START_X = CONVEYOR_X + CONVEYOR_WIDTH - CARD_WIDTH - 8;
    private static final double CARD_FINAL_BASE_X = CONVEYOR_X + 22;
    private static final double CARD_FINAL_Y = CONVEYOR_Y + (CONVEYOR_HEIGHT - CARD_HEIGHT) / 2.0;
    private static final double CARD_QUEUE_SPACING = CARD_WIDTH + 8;
    private static final double CARD_SPEED = 140;
    private static final int MAX_CONVEYOR_CARDS = 5;
    private static final double TOTAL_TIME_SECONDS = 240;
    private static final double HOUSE_BOUNDARY_X = PLAY_AREA_X - 40;
    private static final double MOWER_START_X = PLAY_AREA_X - 90;
    private static final double MOWER_SPEED = 520;

    private final Rectangle playAreaBounds;
    private final double laneHeight = PLAY_AREA_HEIGHT / Yard.ROWS;
    private final Label sunLabel;
    private final Label timerLabel;
    private final Timeline cardSpawner;
    private final Timeline conveyorLoop;
    private final AnimationTimer gameLoop;

    private final Pane conveyorLayer = new Pane();
    private final Pane cardLayer = new Pane();
    private final Pane ballLayer = new Pane();
    private final Pane sunLayer = new Pane();
    private final Pane zombieLayer = new Pane();
    private final Pane introLayer = new Pane();
    private final Pane conveyorMask = new Pane();

    private final List<ConveyorCard> conveyorCards = new ArrayList<>();
    private final List<WallnutBall> wallnutBalls = new ArrayList<>();
    private final List<MiniZombie> miniZombies = new ArrayList<>();
    private final List<MiniLawnMower> lawnMowers = new ArrayList<>();
    private final Random random = new Random();
    private final List<ImageView> introZombies = new ArrayList<>();

    private int sunPoints = Yard.SUNCOUNTER;
    private double timeRemaining = TOTAL_TIME_SECONDS;
    private double zombieSpawnTimer = 0;
    private double zombieSpawnInterval = 8.5;
    private static final double MIN_ZOMBIE_INTERVAL = 3.0;
    private double sunSpawnTimer = 0;
    private double sunInterval = randomInterval();
    private boolean gameStarted = false;
    private boolean gameOn = true;
    private boolean hugeWaveShown = false;
    private boolean hugeWaveActive = false;
    private boolean lastMinuteBoost = false;

    private final Runnable onExitAction;
    private final Stage stage;
    private MediaPlayer zombieSpawnMediaPlayer;

    public WallNutBowlingPane(Stage stage, Runnable onExitAction) {
        this.stage = stage;
        this.onExitAction = onExitAction;
        setPrefSize(WIDTH, HEIGHT);
        setupBackground();

        playAreaBounds = new Rectangle(PLAY_AREA_X, PLAY_AREA_Y, PLAY_AREA_WIDTH, PLAY_AREA_HEIGHT);
        playAreaBounds.setStroke(Color.TRANSPARENT);
        playAreaBounds.setFill(Color.TRANSPARENT);
        playAreaBounds.setMouseTransparent(true);
        getChildren().add(playAreaBounds);

        conveyorLoop = setupConveyorBelts();

        conveyorLayer.setPickOnBounds(false);
        cardLayer.setPickOnBounds(false);
        ballLayer.setPickOnBounds(false);
        sunLayer.setPickOnBounds(false);
        zombieLayer.setPickOnBounds(false);
        introLayer.setMouseTransparent(false);
        introLayer.setPickOnBounds(false);
        getChildren().addAll(zombieLayer, ballLayer, sunLayer, conveyorLayer, cardLayer, introLayer);

        sunLabel = createLabel("Sun: " + sunPoints, WIDTH - 200, 18);
        timerLabel = createLabel(formatTime((int) timeRemaining), WIDTH - 360, 18);
        sunLabel.setStyle("-fx-background-color: rgba(0,0,0,0.55); -fx-padding: 6px 12px; -fx-background-radius: 8;");
        timerLabel.setStyle("-fx-background-color: rgba(0,0,0,0.55); -fx-padding: 6px 12px; -fx-background-radius: 8;");
        getChildren().addAll(sunLabel, timerLabel);

        cardSpawner = createCardSpawner();
        gameLoop = createGameLoop();
        SoundtrackPlayer.stopTrack();
        SoundtrackPlayer.playInGametrack1();
        playIntroSequence();
        spawnLawnMowers();
    }

    private void startGameplay() {
        if (gameStarted) {
            return;
        }
        gameStarted = true;
        cardSpawner.play();
        gameLoop.start();
    }

    private void playIntroSequence() {
        showStaticZombies();
        playSoundEffect("/pvz/music/zombies arrive.mp3", 0.3);
        zoomAndReveal();
    }

    private void spawnLawnMowers() {
        lawnMowers.clear();
        for (int r = 0; r < Yard.ROWS; r++) {
            MiniLawnMower mower = new MiniLawnMower(r);
            lawnMowers.add(mower);
            ballLayer.getChildren().add(mower.view);
        }
    }

    private void readySetPlant() {
        Rectangle overlay = new Rectangle(WIDTH, HEIGHT);
        overlay.setFill(Color.BLACK);
        overlay.setOpacity(0.5);

        ImageView readyImage = createIntroImage("/pvz/images/others/Ready.png");
        ImageView setImage = createIntroImage("/pvz/images/others/Set.png");
        ImageView plantImage = createIntroImage("/pvz/images/others/Plant.png");
        setImage.setVisible(false);
        plantImage.setVisible(false);
        introLayer.getChildren().addAll(overlay, readyImage, setImage, plantImage);

        PauseTransition readyPause = new PauseTransition(Duration.seconds(1.5));
        readyPause.setOnFinished(event -> {
            readyImage.setVisible(false);
            setImage.setVisible(true);

            PauseTransition setPause = new PauseTransition(Duration.seconds(1.5));
            setPause.setOnFinished(setEvent -> {
                setImage.setVisible(false);
                plantImage.setVisible(true);

                PauseTransition plantPause = new PauseTransition(Duration.seconds(1.5));
                plantPause.setOnFinished(goEvent -> {
                    Timeline shakeTimeline = new Timeline();
                    double baseX = plantImage.getLayoutX();
                    double baseY = plantImage.getLayoutY();
                    for (int i = 0; i < 20; i++) {
                        double randomX = random.nextDouble() * 20 - 10;
                        double randomY = random.nextDouble() * 20 - 10;
                        double randomScale = 1 + random.nextDouble() * 0.2;
                        double randomAngle = random.nextDouble() * 20 - 10;
                        KeyFrame keyFrame = new KeyFrame(
                                Duration.millis(i * 50),
                                new KeyValue(plantImage.layoutXProperty(), baseX + randomX),
                                new KeyValue(plantImage.layoutYProperty(), baseY + randomY),
                                new KeyValue(plantImage.scaleXProperty(), randomScale),
                                new KeyValue(plantImage.scaleYProperty(), randomScale),
                                new KeyValue(plantImage.rotateProperty(), randomAngle)
                        );
                        shakeTimeline.getKeyFrames().add(keyFrame);
                    }
                    shakeTimeline.setOnFinished(shakeEvent -> cleanupIntro(overlay, readyImage, setImage, plantImage));
                    shakeTimeline.play();
                });
                plantPause.play();
            });
            setPause.play();
        });
        readyPause.play();
    }

    private void gameOver() {
        if (!gameOn) return;
        gameOn = false;
        dispose();

        playSoundEffect("/pvz/music/level loss.mp3", 0.4);

        Rectangle overlay = new Rectangle(WIDTH, HEIGHT);
        overlay.setFill(Color.BLACK);
        overlay.setOpacity(0.6);

        ImageView gameOverImage = new ImageView(AssetLoader.loadImage("/pvz/images/others/ZombiesWin.png"));
        gameOverImage.setPreserveRatio(true);
        gameOverImage.setFitWidth(520);
        gameOverImage.setFitHeight(407);
        gameOverImage.setLayoutX((WIDTH - gameOverImage.getFitWidth()) / 2);
        gameOverImage.setLayoutY((HEIGHT - gameOverImage.getFitHeight()) / 2);

        getChildren().addAll(overlay, gameOverImage);

        ScaleTransition zoom = new ScaleTransition(Duration.seconds(3), gameOverImage);
        zoom.setFromX(0.5);
        zoom.setFromY(0.5);
        zoom.setToX(1.0);
        zoom.setToY(1.0);
        zoom.setInterpolator(Interpolator.EASE_BOTH);

        TranslateTransition shake = new TranslateTransition(Duration.seconds(0.1), gameOverImage);
        shake.setByX(10);
        shake.setByY(10);
        shake.setAutoReverse(true);
        shake.setCycleCount(30);

        ParallelTransition animation = new ParallelTransition(zoom, shake);
        PauseTransition pause = new PauseTransition(Duration.seconds(4));
        SequentialTransition seq = new SequentialTransition(animation, pause);
        seq.setOnFinished(evt -> showLoadingThenExit());
        seq.play();
    }

    private void gameWin() {
        if (!gameOn) return;
        gameOn = false;
        dispose();

        playSoundEffect("/pvz/music/level win.mp3", 0.5);

        Rectangle overlay = new Rectangle(WIDTH, HEIGHT);
        overlay.setFill(Color.BLACK);
        overlay.setOpacity(0.6);

        ImageView gameWinImage = new ImageView(AssetLoader.loadImage("/pvz/images/others/PlantsWin.png"));
        gameWinImage.setPreserveRatio(true);
        gameWinImage.setFitWidth(762);
        gameWinImage.setFitHeight(276);
        gameWinImage.setLayoutX((WIDTH - gameWinImage.getFitWidth()) / 2);
        gameWinImage.setLayoutY((HEIGHT - gameWinImage.getFitHeight()) / 2);

        getChildren().addAll(overlay, gameWinImage);

        ScaleTransition zoom = new ScaleTransition(Duration.seconds(3), gameWinImage);
        zoom.setFromX(0.5);
        zoom.setFromY(0.5);
        zoom.setToX(1.0);
        zoom.setToY(1.0);
        zoom.setInterpolator(Interpolator.EASE_BOTH);

        TranslateTransition shake = new TranslateTransition(Duration.seconds(0.1), gameWinImage);
        shake.setByX(10);
        shake.setByY(10);
        shake.setAutoReverse(true);
        shake.setCycleCount(30);

        ParallelTransition animation = new ParallelTransition(zoom, shake);
        PauseTransition pause = new PauseTransition(Duration.seconds(4));
        SequentialTransition seq = new SequentialTransition(animation, pause);
        seq.setOnFinished(evt -> showLoadingThenExit());
        seq.play();
    }

    private void showLoadingThenExit() {
        if (stage != null) {
            LoadingScreen.show(stage, () -> Platform.runLater(this::exit));
        } else {
            exit();
        }
    }

    private void showHugeWave() {
        hugeWaveActive = true;
        playSoundEffect("/pvz/music/zombie wave.mp3", 0.6);
        ImageView waveImage = new ImageView(AssetLoader.loadImage("/pvz/images/others/HugeWave.gif"));
        waveImage.setFitWidth(500);
        waveImage.setFitHeight(120);
        waveImage.setLayoutX(WIDTH / 2 - waveImage.getFitWidth() / 2);
        waveImage.setLayoutY(HEIGHT / 2 - waveImage.getFitHeight() / 2);
        introLayer.getChildren().add(waveImage);
        PauseTransition wavePause = new PauseTransition(Duration.seconds(2.5));
        wavePause.setOnFinished(event -> {
            introLayer.getChildren().remove(waveImage);
            hugeWaveActive = false;
            lastMinuteBoost = true;
        });
        wavePause.play();
    }

    private void zoomAndReveal() {
        PauseTransition initialFreeze = new PauseTransition(Duration.seconds(2));

        Timeline zoomToRight = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(scaleXProperty(), 1),
                        new KeyValue(scaleYProperty(), 1),
                        new KeyValue(layoutXProperty(), 0)
                ),
                new KeyFrame(Duration.seconds(1),
                        new KeyValue(scaleXProperty(), 1.5),
                        new KeyValue(scaleYProperty(), 1.5),
                        new KeyValue(layoutXProperty(), -WIDTH * 0.25)
                )
        );

        Timeline panLeft = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(layoutXProperty(), -WIDTH * 0.25)
                ),
                new KeyFrame(Duration.seconds(2),
                        new KeyValue(layoutXProperty(), WIDTH * 0.25)
                )
        );

        PauseTransition hold = new PauseTransition(Duration.seconds(4.5));
        panLeft.setOnFinished(evt -> hideStaticZombies());

        Timeline zoomOut = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(scaleXProperty(), 1.5),
                        new KeyValue(scaleYProperty(), 1.5),
                        new KeyValue(layoutXProperty(), WIDTH * 0.25)
                ),
                new KeyFrame(Duration.seconds(2),
                        new KeyValue(scaleXProperty(), 1),
                        new KeyValue(scaleYProperty(), 1),
                        new KeyValue(layoutXProperty(), 0)
                )
        );

        SequentialTransition sequence = new SequentialTransition(initialFreeze, zoomToRight, hold, panLeft, zoomOut);
        sequence.setOnFinished(evt -> readySetPlant());
        sequence.play();
    }

    private void cleanupIntro(Rectangle overlay, ImageView readyImage, ImageView setImage, ImageView plantImage) {
        introLayer.getChildren().removeAll(overlay, readyImage, setImage, plantImage);
        hideStaticZombies();
        startGameplay();
    }

    private void showStaticZombies() {
        hideStaticZombies();
        for (int i = 0; i < 8; i++) {
            ImageView zombie = new ImageView(AssetLoader.loadImage("/pvz/images/yardStaticZombies/" + i + ".gif"));
            zombie.setFitWidth(127);
            zombie.setFitHeight(164);
            zombie.setPreserveRatio(true);
            if (i % 2 == 0) {
                zombie.setLayoutX(1100);
            } else {
                zombie.setLayoutX(1210);
            }
            int yOffset = switch (i) {
                case 0 -> 130;
                case 1 -> 170;
                case 2 -> 210;
                case 3 -> 250;
                case 4 -> 290;
                case 5 -> 330;
                case 6 -> 370;
                default -> 410;
            };
            zombie.setLayoutY(yOffset);
            introZombies.add(zombie);
            introLayer.getChildren().add(zombie);
        }
    }

    private void hideStaticZombies() {
        for (ImageView zombie : introZombies) {
            introLayer.getChildren().remove(zombie);
        }
        introZombies.clear();
    }

    private ImageView createIntroImage(String path) {
        ImageView image = new ImageView(AssetLoader.loadImage(path));
        image.setFitWidth(400);
        image.setFitHeight(130);
        image.setPreserveRatio(true);
        image.setLayoutX((WIDTH - image.getFitWidth()) / 2 + 60);
        image.setLayoutY(HEIGHT / 2 - image.getFitHeight() / 2);
        return image;
    }

    private void setupBackground() {
        ImageView background = new ImageView(AssetLoader.loadImage("/pvz/images/Wall-nutBawling/background1.png"));
        background.setFitWidth(WIDTH);
        background.setFitHeight(HEIGHT);
        background.setPreserveRatio(false);
        background.setMouseTransparent(true);
        getChildren().add(0, background);
    }

    private Timeline setupConveyorBelts() {
        Image conveyorImage = AssetLoader.loadImage("/pvz/images/Wall-nutBawling/conveyor.gif");
        ImageView beltA = new ImageView(conveyorImage);
        ImageView beltB = new ImageView(conveyorImage);
        beltA.setFitWidth(CONVEYOR_WIDTH);
        beltA.setFitHeight(CONVEYOR_HEIGHT);
        beltB.setFitWidth(CONVEYOR_WIDTH);
        beltB.setFitHeight(CONVEYOR_HEIGHT);
        beltA.setLayoutX(0);
        beltA.setLayoutY(0);
        beltB.setLayoutX(CONVEYOR_WIDTH);
        beltB.setLayoutY(0);

        conveyorMask.setLayoutX(CONVEYOR_X);
        conveyorMask.setLayoutY(CONVEYOR_Y);
        conveyorMask.setPrefSize(CONVEYOR_WIDTH, CONVEYOR_HEIGHT);
        conveyorMask.setClip(new Rectangle(CONVEYOR_WIDTH, CONVEYOR_HEIGHT));
        conveyorMask.getChildren().setAll(beltA, beltB);
        conveyorLayer.getChildren().add(conveyorMask);

        Timeline loop = new Timeline(
                new KeyFrame(Duration.ZERO, evt -> {
                    beltA.setTranslateX(0);
                    beltB.setTranslateX(0);
                }),
                new KeyFrame(Duration.seconds(2.8), evt -> {
                    beltA.setTranslateX(-CONVEYOR_WIDTH);
                    beltB.setTranslateX(-CONVEYOR_WIDTH);
                })
        );
        loop.setCycleCount(Timeline.INDEFINITE);
        loop.play();
        return loop;
    }

    private Timeline createCardSpawner() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2.5), evt -> spawnConveyorCard()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setDelay(Duration.seconds(2.5));
        return timeline;
    }

    private void spawnConveyorCard() {
        if (conveyorCards.size() >= MAX_CONVEYOR_CARDS) {
            return;
        }
        ConveyorCard card = new ConveyorCard();
        cardLayer.getChildren().add(card.view);
        card.view.toFront();
        conveyorCards.add(card);
        reassignCardSlots();
    }

    private AnimationTimer createGameLoop() {
        return new AnimationTimer() {
            private long last = 0;

            @Override
            public void handle(long now) {
                if (last == 0) {
                    last = now;
                    return;
                }
                double delta = (now - last) / 1_000_000_000.0;
                last = now;
                updateConveyorCards(delta);
                updateBalls(delta);
                updateZombies(delta);
                updateLawnMowers(delta);
                maybeSpawnZombie(delta);
                maybeSpawnSun(delta);
                tickTimer(delta);
            }
        };
    }

    private void updateBalls(double delta) {
        Iterator<WallnutBall> iter = wallnutBalls.iterator();
        while (iter.hasNext()) {
            WallnutBall ball = iter.next();
            if (!ball.update(delta)) {
                ballLayer.getChildren().remove(ball.view);
                iter.remove();
            }
        }
    }

    private void updateConveyorCards(double delta) {
        for (ConveyorCard card : conveyorCards) {
            card.update(delta);
        }
    }

    private void updateZombies(double delta) {
        Iterator<MiniZombie> iter = miniZombies.iterator();
        while (iter.hasNext()) {
            MiniZombie zombie = iter.next();
            if (!zombie.update(delta)) {
                iter.remove();
            }
        }
    }

    private void updateLawnMowers(double delta) {
        for (MiniLawnMower mower : lawnMowers) {
            mower.update(delta);
        }
    }

    private void maybeSpawnZombie(double delta) {
        if (!gameOn) return;
        if (hugeWaveActive) return;
        zombieSpawnTimer += delta;
        if (zombieSpawnTimer >= zombieSpawnInterval) {
            spawnMiniZombie();
            if (timeRemaining < 90 && random.nextDouble() < 0.45) {
                spawnMiniZombie();
            } else if (random.nextDouble() < 0.25) {
                spawnMiniZombie();
            }
            if (lastMinuteBoost && random.nextDouble() < 0.6) {
                spawnMiniZombie();
            }
            playRandomZombieSpawnSound();
            zombieSpawnTimer = 0;
            zombieSpawnInterval = Math.max(MIN_ZOMBIE_INTERVAL, zombieSpawnInterval - (lastMinuteBoost ? 0.2 : 0.12));
        }
    }

    private void maybeSpawnSun(double delta) {
        if (!gameOn) return;
        sunSpawnTimer += delta;
        if (sunSpawnTimer >= sunInterval) {
            dropSunFromSky();
            sunSpawnTimer = 0;
            sunInterval = randomInterval();
        }
    }

    private void tickTimer(double delta) {
        if (!gameOn) return;
        timeRemaining -= delta;
        if (!hugeWaveShown && timeRemaining <= 60) {
            hugeWaveShown = true;
            showHugeWave();
        }
        if (timeRemaining <= 0) {
            gameWin();
            return;
        }
        timerLabel.setText(formatTime((int) Math.ceil(timeRemaining)));
    }

    private void dropSunFromSky() {
        ImageView sun = createSunImage();
        double startX = PLAY_AREA_X + 40 + random.nextDouble() * (PLAY_AREA_WIDTH - 80);
        sun.setLayoutX(startX);
        sun.setLayoutY(PLAY_AREA_Y - 60);
        TranslateTransition fall = new TranslateTransition(Duration.seconds(4), sun);
        fall.setToY(PLAY_AREA_Y + laneHeight * 0.5);
        fall.setInterpolator(Interpolator.EASE_IN);
        fall.setOnFinished(evt -> sunLayer.getChildren().remove(sun));
        fall.play();
    }

    private void spawnMiniZombie() {
        int row = random.nextInt(Yard.ROWS);
        ZombieType[] values = ZombieType.values();
        ZombieType type = values[random.nextInt(values.length)];
        double startX = PLAY_AREA_X + PLAY_AREA_WIDTH + 60;
        miniZombies.add(new MiniZombie(type, row, startX));
    }

    private double randomInterval() {
        return 4 + random.nextDouble() * 3;
    }

    private ImageView createSunImage() {
        ImageView sun = new ImageView(AssetLoader.loadImage("/pvz/images/others/sun.png"));
        sun.setFitWidth(40);
        sun.setFitHeight(40);
        sun.setCursor(Cursor.HAND);
        sun.setOnMouseClicked(evt -> collectSun(sun, 25));
        sunLayer.getChildren().add(sun);
        return sun;
    }

    private void collectSun(ImageView sun, int value) {
        sunPoints += value;
        updateSunLabel();
        playSoundEffect("/pvz/music/sun pickup.mp3", 0.3);
        sunLayer.getChildren().remove(sun);
    }

    private Label createLabel(String text, double x, double y) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        label.setTextFill(Color.WHITE);
        label.setLayoutX(x);
        label.setLayoutY(y);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private void updateSunLabel() {
        sunLabel.setText("Sun: " + sunPoints);
    }

    private ImageView createCardImage() {
        ImageView view = new ImageView(AssetLoader.loadImage("/pvz/images/Wall-nutBawling/wallnut_card_25_50x70.png"));
        view.setFitWidth(CARD_WIDTH);
        view.setFitHeight(CARD_HEIGHT);
        return view;
    }

    private void spawnSunFromDrop(double x, double y) {
        ImageView sun = createSunImage();
        sun.setLayoutX(x);
        sun.setLayoutY(y - 30);
    }

    private boolean isValidDrop(Point2D dropPoint) {
        return playAreaBounds.contains(dropPoint) && dropPoint.getX() <= RED_LINE_X;
    }

    private int getRowForY(double y) {
        int row = (int) ((y - PLAY_AREA_Y) / laneHeight);
        if (row < 0) row = 0;
        if (row >= Yard.ROWS) row = Yard.ROWS - 1;
        return row;
    }

    private void exit() {
        dispose();
        if (onExitAction != null) {
            onExitAction.run();
        }
    }

    private void dispose() {
        if (cardSpawner != null) cardSpawner.stop();
        if (conveyorLoop != null) conveyorLoop.stop();
        if (gameLoop != null) gameLoop.stop();
        if (zombieSpawnMediaPlayer != null) {
            zombieSpawnMediaPlayer.stop();
            zombieSpawnMediaPlayer = null;
        }
        SoundtrackPlayer.stopTrack();
        gameOn = false;
    }

    private String resolveMediaPath(String resourcePath) {
        try {
            var url = getClass().getResource(resourcePath);
            if (url != null) {
                return url.toExternalForm();
            }
            Path fallback = Paths.get("src" + resourcePath);
            if (Files.exists(fallback)) {
                return fallback.toUri().toString();
            }
            Path binFallback = Paths.get("bin" + resourcePath);
            if (Files.exists(binFallback)) {
                return binFallback.toUri().toString();
            }
        } catch (Exception e) {
            System.out.println("Failed to resolve media path for " + resourcePath + " : " + e.getMessage());
        }
        System.out.println("Missing media: " + resourcePath);
        return null;
    }

    private void reassignCardSlots() {
        for (int i = 0; i < conveyorCards.size(); i++) {
            conveyorCards.get(i).setSlot(i);
        }
    }

    private void removeCard(ConveyorCard card) {
        conveyorCards.remove(card);
        cardLayer.getChildren().remove(card.view);
        reassignCardSlots();
    }

    private void playRandomZombieSpawnSound() {
        try {
            String[] audioPaths = {
                    getClass().getResource("/pvz/music/zombie s1.mp3").toExternalForm(),
                    getClass().getResource("/pvz/music/zombie s2.mp3").toExternalForm(),
                    getClass().getResource("/pvz/music/zombie s3.mp3").toExternalForm()
            };
            int randomIndex = random.nextInt(audioPaths.length);
            Media media = new Media(audioPaths[randomIndex]);
            zombieSpawnMediaPlayer = new MediaPlayer(media);
            zombieSpawnMediaPlayer.setVolume(0.3);
            zombieSpawnMediaPlayer.play();
        } catch (Exception e) {
            System.out.println("Error playing zombie spawn sound: " + e.getMessage());
        }
    }

    private void playSoundEffect(String resourcePath, double volume) {
        try {
            String fullPath = resolveMediaPath(resourcePath);
            if (fullPath == null) {
                System.out.println("Sound missing: " + resourcePath);
                return;
            }
            Media media = new Media(fullPath);
            MediaPlayer player = new MediaPlayer(media);
            player.setVolume(volume);
            player.setOnError(() -> System.out.println("Sound error for " + resourcePath + ": " + player.getError()));
            player.play();
        } catch (Exception e) {
            System.out.println("Error playing sound effect: " + e.getMessage() + " for " + resourcePath);
        }
    }

    private void playImpactSound(boolean secondary) {
        String clip = secondary ? "/pvz/music/bowlingimpact2.mp3" : "/pvz/music/bowlingimpact.mp3";
        playSoundEffect(clip, 0.8);
    }

    private class ConveyorCard {
        final ImageView view;
        final DropShadow glow = new DropShadow(14, Color.rgb(255, 215, 0, 0.9));
        boolean ready = false;
        int slotIndex = -1;
        double targetX;
        double dragOffsetX;
        double dragOffsetY;

        ConveyorCard() {
            view = createCardImage();
            view.setLayoutX(CARD_START_X);
            view.setLayoutY(CARD_FINAL_Y);
            view.setCursor(Cursor.DEFAULT);

            view.setOnMousePressed(evt -> {
                if (!ready || sunPoints < CARD_COST) {
                    return;
                }
                dragOffsetX = evt.getSceneX() - view.getLayoutX();
                dragOffsetY = evt.getSceneY() - view.getLayoutY();
            });

            view.setOnMouseDragged(evt -> {
                if (!ready) {
                    return;
                }
                Point2D local = sceneToLocal(evt.getSceneX(), evt.getSceneY());
                view.setLayoutX(local.getX() - dragOffsetX);
                view.setLayoutY(local.getY() - dragOffsetY);
            });

            view.setOnMouseReleased(evt -> {
                if (!ready) {
                    resetPosition();
                    return;
                }
                Point2D dropPoint = sceneToLocal(evt.getSceneX(), evt.getSceneY());
                if (sunPoints >= CARD_COST && isValidDrop(dropPoint)) {
                    sunPoints -= CARD_COST;
                    updateSunLabel();
                    int row = getRowForY(dropPoint.getY());
                    wallnutBalls.add(new WallnutBall(Math.max(PLAY_AREA_X, dropPoint.getX() - 25), row));
                    spawnSunFromDrop(dropPoint.getX() + 20, dropPoint.getY());
                    removeCard(this);
                } else {
                    resetPosition();
                }
            });
        }

        void setSlot(int slot) {
            slotIndex = slot;
            targetX = CARD_FINAL_BASE_X + slot * CARD_QUEUE_SPACING;
            ready = false;
            view.setEffect(null);
            view.setCursor(Cursor.DEFAULT);
        }

        void update(double delta) {
            if (ready) {
                return;
            }
            double nextX = view.getLayoutX() - CARD_SPEED * delta;
            if (nextX <= targetX) {
                view.setLayoutX(targetX);
                ready = true;
                view.setCursor(Cursor.HAND);
                view.setEffect(glow);
            } else {
                view.setLayoutX(nextX);
            }
        }

        private void resetPosition() {
            view.setLayoutX(CARD_START_X);
            view.setLayoutY(CARD_FINAL_Y);
            ready = false;
            view.setEffect(null);
            view.setCursor(Cursor.DEFAULT);
            if (slotIndex >= 0) {
                targetX = CARD_FINAL_BASE_X + slotIndex * CARD_QUEUE_SPACING;
            }
        }
    }

    private class WallnutBall {
        final ImageView view;
        final int row;
        private double vx = 260;
        private double vy = 0;
        private boolean deflected = false;
        private final List<MiniZombie> hitZombies = new ArrayList<>();

        WallnutBall(double x, int row) {
            this.row = row;
            Image sprite = AssetLoader.loadImage("/pvz/images/Wall-nutBawling/walnut_roll_smooth_loop_faster.gif");
            view = new ImageView(sprite);
            view.setFitWidth(60);
            view.setFitHeight(60);
            double laneCenter = PLAY_AREA_Y + row * laneHeight + (laneHeight - view.getFitHeight()) / 2.0;
            view.setLayoutX(x);
            view.setLayoutY(laneCenter);
            ballLayer.getChildren().add(view);
        }

        boolean update(double delta) {
            view.setLayoutX(view.getLayoutX() + vx * delta);
            view.setLayoutY(view.getLayoutY() + vy * delta);

            // clamp to play area vertically
            double minY = PLAY_AREA_Y;
            double maxY = PLAY_AREA_Y + PLAY_AREA_HEIGHT - view.getFitHeight();
            if (view.getLayoutY() < minY) view.setLayoutY(minY);
            if (view.getLayoutY() > maxY) view.setLayoutY(maxY);

            if (view.getLayoutX() > PLAY_AREA_X + PLAY_AREA_WIDTH + 120) {
                return false;
            }

            for (MiniZombie zombie : miniZombies) {
                if (!zombie.isAlive() || hitZombies.contains(zombie)) {
                    continue;
                }

                if (view.getBoundsInParent().intersects(zombie.view.getBoundsInParent())) {
                    hitZombies.add(zombie);
                    zombie.hit();

                    if (!deflected) {
                        deflected = true;
                        double angle = random.nextBoolean() ? 1 : -1;
                        vy = angle * 140;
                        playImpactSound(false);
                    } else {
                        playImpactSound(true);
                    }
                }
            }
            return true;
        }
    }

    private class MiniZombie {
        final ImageView view;
        final int row;
        final double speed;
        private final ZombieType type;
        private int remainingHits;
        private boolean alive = true;

        MiniZombie(ZombieType type, int row, double startX) {
            this.type = type;
            this.row = row;
            this.remainingHits = type.hits;
            Image sprite = AssetLoader.loadImage(type.imagePath);
            this.view = new ImageView(sprite);
            this.view.setFitWidth(90);
            this.view.setFitHeight(110);
            double laneCenter = PLAY_AREA_Y + row * laneHeight + (laneHeight - view.getFitHeight()) / 2.0;
            this.view.setLayoutX(startX);
            this.view.setLayoutY(laneCenter);
            this.speed = type.speed;
            zombieLayer.getChildren().add(this.view);
        }

        boolean update(double delta) {
            if (!alive || !gameOn) return false;

            view.setLayoutX(view.getLayoutX() - speed * delta);

            // Check lawn mower collision
            for (MiniLawnMower mower : lawnMowers) {
                if (mower.row == row && mower.isAlive() && mower.intersects(view)) {
                    mower.activate();
                    takeLawnMowerHit();
                    return false;
                }
            }

            // Check house breach if mower already spent
            if (view.getLayoutX() <= HOUSE_BOUNDARY_X) {
                boolean mowerSpent = lawnMowers.stream().anyMatch(m -> m.row == row && !m.isAlive());
                if (mowerSpent) {
                    gameOver();
                    return false;
                }
            }

            if (view.getLayoutX() + view.getFitWidth() < PLAY_AREA_X - 120) {
                alive = false;
                removeView();
                return false;
            }
            return true;
        }

        void hit() {
            if (!alive) return;
            remainingHits--;
            flashHit();
            if (remainingHits <= 0) {
                alive = false;
                playDeathAnimation();
            }
        }

        private void flashHit() {
            ColorAdjust adjust = new ColorAdjust();
            adjust.setBrightness(0.6);
            view.setEffect(adjust);
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(200), new KeyValue(adjust.brightnessProperty(), 0))
            );
            timeline.setCycleCount(1);
            timeline.play();
        }

        private void playDeathAnimation() {
            String deathPath = type == ZombieType.FOOTBALL
                ? "/pvz/images/zombies/FootballZombieDie.gif"
                : "/pvz/images/zombies/ZombieDie.gif";
            view.setImage(AssetLoader.loadImage(deathPath));
            view.setFitWidth(type == ZombieType.FOOTBALL ? 134 : 134);
            view.setFitHeight(type == ZombieType.FOOTBALL ? 150 : 155);
            view.setPreserveRatio(true);

            double duration = type == ZombieType.FOOTBALL ? 0.8 : 1.6;
            PauseTransition pause = new PauseTransition(Duration.seconds(duration));
            pause.setOnFinished(evt -> zombieLayer.getChildren().remove(view));
            pause.play();
        }

        private void takeLawnMowerHit() {
            if (!alive) return;
            alive = false;
            playDeathAnimation();
        }

        private void removeView() {
            zombieLayer.getChildren().remove(view);
        }

        boolean isAlive() {
            return alive;
        }
    }

    private class MiniLawnMower {
        final int row;
        final ImageView view;
        private boolean active = false;
        private boolean alive = true;

        MiniLawnMower(int row) {
            this.row = row;
            this.view = new ImageView(AssetLoader.loadImage("/pvz/images/yard-related/lawnmower.png"));
            view.setFitWidth(90);
            view.setFitHeight(70);
            view.setPreserveRatio(true);
            double laneCenter = PLAY_AREA_Y + row * laneHeight + (laneHeight - view.getFitHeight()) / 2.0;
            view.setLayoutX(MOWER_START_X);
            view.setLayoutY(laneCenter);
        }

        boolean isAlive() {
            return alive;
        }

        boolean intersects(ImageView zombieView) {
            return view.getBoundsInParent().intersects(zombieView.getBoundsInParent());
        }

        void activate() {
            if (!alive || active) return;
            active = true;
            playSoundEffect("/pvz/music/card selected.mp3", 0.6);
        }

        void update(double delta) {
            if (!alive || !active || !gameOn) return;
            view.setLayoutX(view.getLayoutX() + MOWER_SPEED * delta);

            // squash zombies in this lane
            for (MiniZombie zombie : new ArrayList<>(miniZombies)) {
                if (zombie.row == row && zombie.isAlive() && intersects(zombie.view)) {
                    zombie.takeLawnMowerHit();
                }
            }

            if (view.getLayoutX() > PLAY_AREA_X + PLAY_AREA_WIDTH + 200) {
                alive = false;
            }
        }
    }

    private enum ZombieType {
        DEFAULT("/pvz/images/zombies/Zombie.gif", 1, 120),
        CONE("/pvz/images/zombies/ConeZombie.gif", 2, 110),
        BUCKET("/pvz/images/zombies/BucketheadZombie.gif", 2, 105),
        FOOTBALL("/pvz/images/zombies/FootballZombie.gif", 3, 160);

        final String imagePath;
        final int hits;
        final double speed;

        ZombieType(String imagePath, int hits, double speed) {
            this.imagePath = imagePath;
            this.hits = hits;
            this.speed = speed;
        }
    }

    private String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("Time: %02d:%02d", mins, secs);
    }
}
