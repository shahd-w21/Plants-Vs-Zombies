package pvz.ui;

import java.util.Objects;

import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import pvz.model.AudioSettings;

public class GameMenuPane extends StackPane {

    public interface Handler {
        void onPlay();
        void onOptions();
        void onMore();
        void onLogout();
        void onDeleteAccount();
        void onExit();
        void onWalnutMinigame();
        default void onLevelSelected(int level) {}
    }

    private final Image baseImg;
    private final Image hoverPlay;
    private final Image hoverOptions;
    private final Image hoverMore;
    private final Image hoverExit;
    private final Image playMenuImg;
    private final Image minigamesImg;
    private final Image walnutIconImg;
    private final Image optionsBgImg;

    private final ImageView view;
    private final Pane baseLayer;
    private final Pane overlayLayer;
    private final ImageView overlayImage;
    private final Label usernameLabel;
    private final Pane minigamesLayer;
    private final ImageView minigamesView;
    private final ImageView walnutIconView;
    private final ImageView optionsBgView;
    private final Pane optionsLayer;
    private final CheckBox musicToggle;
    private final Slider musicSlider;
    private final Label musicValueLabel;

    private Rectangle2D rPlay;
    private Rectangle2D rOptions;
    private Rectangle2D rMore;
    private Rectangle2D rLogout;
    private Rectangle2D rDelete;
    private Rectangle2D rExit;
    private Rectangle2D rExit2;
    private Rectangle2D rBackToMenu;
    private Rectangle2D rWalnutSlot;

    private boolean showingLevelOverlay;
    private boolean showingMinigamesOverlay;
    private boolean showingOptionsOverlay;

    private Handler handler;
    private String playerUsername;

    private double usernameX = 55;
    private double usernameY = 120;
    private double usernameSize = 18;
    private Color usernameColor = Color.WHITE;
    private double minigamesViewWidth;
    private double minigamesViewHeight;
    private static final double MINIGAMES_WIDTH = 640;
    private static final double MINIGAMES_HEIGHT = 440;

    public GameMenuPane(String username) {
        this.playerUsername = username;

        baseImg = load("/pvz/images/menu/main_bg.png");
        hoverPlay = load("/pvz/images/menu/hover_play.png");
        hoverOptions = load("/pvz/images/menu/hover_options.png");
        hoverMore = load("/pvz/images/menu/hover_more.png");
        hoverExit = load("/pvz/images/menu/hover_exit.png");
        playMenuImg = load("/pvz/images/menu/playmenu_bg.png");
        minigamesImg = load("/pvz/images/Wall-nutBawling/MinigamesMenue.png");
        walnutIconImg = load("/pvz/images/Wall-nutBawling/walnut_icon_250_rounded.png");
        optionsBgImg = load("/pvz/images/menu/options_bg.png");

        if (baseImg == null || hoverPlay == null || hoverOptions == null
            || hoverMore == null || hoverExit == null) {
            throw new IllegalStateException("Missing game menu images under /pvz/images/menu");
        }

        rPlay    = rect(0.52, 0.33, 0.36, 0.12);  // Play button
        rOptions = rect(0.51, 0.47, 0.35, 0.10);  // Options button
        rExit    = rect(0.52, 0.59, 0.32, 0.10);  // Exit (top)
        rMore    = rect(0.48, 0.75, 0.13, 0.12);  // More button
        rLogout  = rect(0.71, 0.80, 0.08, 0.08);  // Logout button
        rDelete  = rect(0.81, 0.84, 0.06, 0.08);  // Delete button
        rExit2   = rect(0.90, 0.82, 0.06, 0.02);  // Exit (bottom)

        baseLayer = new Pane();
        baseLayer.setPrefSize(800, 598);

        view = new ImageView(baseImg);
        view.setPreserveRatio(true);
        view.setFitWidth(800);
        view.setFitHeight(598);
        baseLayer.getChildren().add(view);

        usernameLabel = new Label(playerUsername);
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, usernameSize));
        usernameLabel.setTextFill(usernameColor);
        usernameLabel.setLayoutX(usernameX);
        usernameLabel.setLayoutY(usernameY - usernameSize);
        baseLayer.getChildren().add(usernameLabel);

        overlayLayer = new Pane();
        overlayLayer.setPrefSize(800, 598);
        overlayLayer.setVisible(false);
        overlayLayer.setMouseTransparent(true);

        Rectangle dimmer = new Rectangle(800, 598);
        dimmer.setFill(new Color(0, 0, 0, 0.75));
        overlayLayer.getChildren().add(dimmer);

        overlayImage = playMenuImg == null ? null : new ImageView(playMenuImg);
        if (overlayImage != null) {
            overlayImage.setLayoutX(120);
            overlayImage.setLayoutY(100);
            overlayImage.setFitWidth(600);
            overlayImage.setFitHeight(400);
            overlayImage.setPreserveRatio(false);
            overlayLayer.getChildren().add(overlayImage);
        }

        minigamesLayer = new Pane();
        minigamesLayer.setPrefSize(800, 598);
        minigamesLayer.setVisible(false);
        minigamesLayer.setMouseTransparent(true);
        minigamesLayer.setPickOnBounds(true);

        minigamesView = new ImageView();
        minigamesView.setPreserveRatio(false);
        minigamesView.setFitWidth(MINIGAMES_WIDTH);
        minigamesView.setFitHeight(MINIGAMES_HEIGHT);
        minigamesView.setLayoutX((800 - MINIGAMES_WIDTH) / 2);
        minigamesView.setLayoutY((598 - MINIGAMES_HEIGHT) / 2);
        if (minigamesImg != null) {
            minigamesView.setImage(minigamesImg);
        }
        minigamesLayer.getChildren().add(minigamesView);
        minigamesViewWidth = MINIGAMES_WIDTH;
        minigamesViewHeight = MINIGAMES_HEIGHT;

        rBackToMenu = rect(0.04, 0.89, 0.11, 0.08);
        rWalnutSlot = rect(0.01, 0.17, 0.21, 0.33);

        walnutIconView = new ImageView();
        if (walnutIconImg != null) {
            walnutIconView.setImage(walnutIconImg);
            walnutIconView.setPreserveRatio(true);
            walnutIconView.setFitWidth(120);
            walnutIconView.setFitHeight(120);
            positionWalnutIcon();
            walnutIconView.setOnMouseClicked(e -> {
                if (handler != null) {
                    handler.onWalnutMinigame();
                }
                hideMinigamesOverlay();
            });
            minigamesLayer.getChildren().add(walnutIconView);
        } else {
            walnutIconView.setVisible(false);
        }

        minigamesLayer.setOnMouseMoved(e -> {
            if (!showingMinigamesOverlay) return;
            if (containsMinigamesHotspot(rBackToMenu, e.getX(), e.getY()) ||
                containsMinigamesHotspot(rWalnutSlot, e.getX(), e.getY())) {
                setCursor(Cursor.HAND);
            } else {
                setCursor(Cursor.DEFAULT);
            }
        });
        minigamesLayer.setOnMouseExited(e -> setCursor(Cursor.DEFAULT));
        minigamesLayer.setOnMouseClicked(e -> {
            if (!showingMinigamesOverlay) return;
            if (containsMinigamesHotspot(rBackToMenu, e.getX(), e.getY())) {
                hideMinigamesOverlay();
            }
        });

        optionsLayer = new Pane();
        optionsLayer.setPrefSize(800, 598);
        optionsLayer.setVisible(false);
        optionsLayer.setMouseTransparent(true);
        optionsLayer.setPickOnBounds(true);

        optionsBgView = optionsBgImg == null ? null : new ImageView(optionsBgImg);
        if (optionsBgView != null) {
            optionsBgView.setFitWidth(800);
            optionsBgView.setFitHeight(598);
            optionsBgView.setPreserveRatio(false);
            optionsBgView.setSmooth(true);
            optionsLayer.getChildren().add(optionsBgView);
        } else {
            Rectangle fallback = new Rectangle(800, 598);
            fallback.setFill(new Color(0, 0, 0, 0.75));
            optionsLayer.getChildren().add(fallback);
            System.err.println("Missing options background image at /pvz/images/menu/options_bg.png; using fallback overlay.");
        }

        Pane card = new Pane();
        card.setPrefSize(520, 320);
        card.setLayoutX((800 - 520) / 2.0);
        card.setLayoutY((598 - 320) / 2.0 - 10);
        card.setStyle("-fx-background-color: rgba(0,0,0,0.35); -fx-background-radius: 14; " +
                      "-fx-border-color: rgba(255,255,255,0.15); -fx-border-radius: 14; -fx-border-width: 1;");

        Label optionsTitle = new Label("Audio");
        optionsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        optionsTitle.setTextFill(Color.WHITE);
        optionsTitle.setLayoutX(24);
        optionsTitle.setLayoutY(18);
        card.getChildren().add(optionsTitle);

        musicToggle = new CheckBox("Enable music");
        musicToggle.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 16));
        musicToggle.setTextFill(Color.WHITE);
        musicToggle.setLayoutX(24);
        musicToggle.setLayoutY(70);

        Label volumeLabel = new Label("Music volume");
        volumeLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 16));
        volumeLabel.setTextFill(Color.WHITE);
        volumeLabel.setLayoutX(24);
        volumeLabel.setLayoutY(120);

        musicSlider = new Slider(0, 1, AudioSettings.getMusicVolume());
        musicSlider.setShowTickMarks(true);
        musicSlider.setShowTickLabels(true);
        musicSlider.setBlockIncrement(0.05);
        musicSlider.setMajorTickUnit(0.25);
        musicSlider.setMinorTickCount(4);
        musicSlider.setLayoutX(24);
        musicSlider.setLayoutY(150);
        musicSlider.setPrefWidth(260);
        musicSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            AudioSettings.setMusicVolume(newVal.doubleValue());
            updateMusicValueLabel();
        });

        musicToggle.setOnAction(e -> {
            AudioSettings.setMusicEnabled(musicToggle.isSelected());
            musicSlider.setDisable(!musicToggle.isSelected());
            updateMusicValueLabel();
        });

        musicValueLabel = new Label();
        musicValueLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        musicValueLabel.setTextFill(Color.LIGHTGRAY);
        musicValueLabel.setLayoutX(300);
        musicValueLabel.setLayoutY(147);


        card.getChildren().addAll(musicToggle, volumeLabel, musicSlider, musicValueLabel);
        optionsLayer.getChildren().addAll(card);

        optionsLayer.setOnMouseClicked(e -> {
            double localX = e.getX() - card.getLayoutX();
            double localY = e.getY() - card.getLayoutY();
            if (localX < 0 || localY < 0 || localX > card.getPrefWidth() || localY > card.getPrefHeight()) {
                hideOptionsOverlay();
            }
        });
        card.setOnMouseClicked(e -> e.consume());

        configureLevelButtons();

        getChildren().addAll(baseLayer, overlayLayer, minigamesLayer, optionsLayer);

        setPrefSize(800, 598);
        setMinSize(800, 598);
        setMaxSize(800, 598);

        view.setOnMouseMoved(e -> {
            if (showingLevelOverlay) {
                setCursor(Cursor.HAND);
                return;
            }
            if (showingMinigamesOverlay) {
                setCursor(Cursor.DEFAULT);
                return;
            }
            if (showingOptionsOverlay) {
                setCursor(Cursor.DEFAULT);
                return;
            }
            int which = whichHotspot(e.getX(), e.getY());
            switch (which) {
                case 1 -> { view.setImage(hoverPlay); setCursor(Cursor.HAND); }
                case 2 -> { view.setImage(hoverOptions); setCursor(Cursor.HAND); }
                case 3 -> { view.setImage(hoverMore); setCursor(Cursor.HAND); }
                case 4 -> { view.setImage(hoverExit); setCursor(Cursor.HAND); }
                case 5, 6, 7 -> { view.setImage(baseImg); setCursor(Cursor.HAND); }
                default -> { view.setImage(baseImg); setCursor(Cursor.DEFAULT); }
            }
        });

        view.setOnMouseExited(e -> {
            if (showingLevelOverlay || showingMinigamesOverlay || showingOptionsOverlay) {
                return;
            }
            view.setImage(baseImg);
            setCursor(Cursor.DEFAULT);
        });

        view.setOnMouseClicked(e -> {
            if (handler == null) {
                return;
            }

            if (showingLevelOverlay) {
                return;
            }

            if (showingMinigamesOverlay) {
                return;
            }

            if (showingOptionsOverlay) {
                return;
            }

            int which = whichHotspot(e.getX(), e.getY());
            switch (which) {
                case 1 -> {
                    showLevelOverlay();
                    handler.onPlay();
                }
                case 2 -> {
                    showOptionsOverlay();
                    handler.onOptions();
                }
                case 3 -> {
                    showMinigamesOverlay();
                    handler.onMore();
                }
                case 4 -> handler.onExit();
                case 5 -> handler.onLogout();
                case 6 -> handler.onDeleteAccount();
                case 7 -> handler.onExit();
                default -> { }
            }
        });
    }

    private void updateUsernameLabel() {
        usernameLabel.setText(playerUsername);
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, usernameSize));
        usernameLabel.setTextFill(usernameColor);
        usernameLabel.setLayoutX(usernameX);
        usernameLabel.setLayoutY(usernameY - usernameSize);
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setHotspotsNormalized(Rectangle2D play, Rectangle2D options, Rectangle2D more,
                                      Rectangle2D logout, Rectangle2D delete, Rectangle2D exit) {
        this.rPlay = Objects.requireNonNull(play);
        this.rOptions = Objects.requireNonNull(options);
        this.rMore = Objects.requireNonNull(more);
        this.rLogout = Objects.requireNonNull(logout);
        this.rDelete = Objects.requireNonNull(delete);
        this.rExit = Objects.requireNonNull(exit);
    }

    public void setUsernamePosition(double x, double y, double size, Color color) {
        this.usernameX = x;
        this.usernameY = y;
        this.usernameSize = size;
        this.usernameColor = color;
        updateUsernameLabel();
    }

    private boolean containsNormalized(Rectangle2D r, double x, double y) {
        double iw = view.getBoundsInParent().getWidth();
        double ih = view.getBoundsInParent().getHeight();

        double rx = r.getMinX() * iw;
        double ry = r.getMinY() * ih;
        double rw = r.getWidth() * iw;
        double rh = r.getHeight() * ih;

        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    private int whichHotspot(double x, double y) {
        if (containsNormalized(rPlay, x, y)) return 1;
        if (containsNormalized(rOptions, x, y)) return 2;
        if (containsNormalized(rMore, x, y)) return 3;
        if (containsNormalized(rExit, x, y)) return 4;
        if (containsNormalized(rLogout, x, y)) return 5;
        if (containsNormalized(rDelete, x, y)) return 6;
        if (containsNormalized(rExit2, x, y)) return 7;
        return 0;
    }

    private boolean containsMinigamesHotspot(Rectangle2D r, double x, double y) {
        if (r == null) {
            return false;
        }
        double vx = minigamesView.getLayoutX();
        double vy = minigamesView.getLayoutY();
        double rw = r.getWidth() * minigamesViewWidth;
        double rh = r.getHeight() * minigamesViewHeight;
        double rx = vx + r.getMinX() * minigamesViewWidth;
        double ry = vy + r.getMinY() * minigamesViewHeight;
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    private void positionWalnutIcon() {
        if (walnutIconImg == null) {
            return;
        }
        double slotX = minigamesView.getLayoutX() + rWalnutSlot.getMinX() * minigamesViewWidth;
        double slotY = minigamesView.getLayoutY() + rWalnutSlot.getMinY() * minigamesViewHeight;
        double slotW = rWalnutSlot.getWidth() * minigamesViewWidth;
        double slotH = rWalnutSlot.getHeight() * minigamesViewHeight;
        double x = slotX + (slotW - walnutIconView.getFitWidth()) / 2;
        double y = slotY + (slotH - walnutIconView.getFitHeight()) / 2;
        walnutIconView.setLayoutX(x);
        walnutIconView.setLayoutY(y);
    }

    private Image load(String path) {
        var in = getClass().getResourceAsStream(path);
        return in == null ? null : new Image(in);
    }

    private Rectangle2D rect(double x, double y, double w, double h) {
        return new Rectangle2D(x, y, w, h);
    }

    private void showLevelOverlay() {
        showingLevelOverlay = true;
        if (showingMinigamesOverlay) {
            hideMinigamesOverlay();
        }
        overlayLayer.setVisible(true);
        overlayLayer.setMouseTransparent(false);
        view.setMouseTransparent(true);
        setCursor(Cursor.DEFAULT);
    }

    private void hideOverlay() {
        showingLevelOverlay = false;
        overlayLayer.setVisible(false);
        overlayLayer.setMouseTransparent(true);
        view.setMouseTransparent(false);
        setCursor(Cursor.DEFAULT);
    }

    private void showMinigamesOverlay() {
        if (showingMinigamesOverlay) {
            return;
        }
        if (minigamesImg == null) {
            System.err.println("Missing MinigamesMenue asset. Please add /pvz/images/menu/MinigamesMenue.png");
            return;
        }
        if (showingLevelOverlay) {
            hideOverlay();
        }
        showingMinigamesOverlay = true;
        minigamesLayer.setVisible(true);
        minigamesLayer.setMouseTransparent(false);
        view.setMouseTransparent(true);
        setCursor(Cursor.DEFAULT);
    }

    private void hideMinigamesOverlay() {
        showingMinigamesOverlay = false;
        minigamesLayer.setVisible(false);
        minigamesLayer.setMouseTransparent(true);
        if (!showingLevelOverlay && !showingOptionsOverlay) {
            view.setMouseTransparent(false);
        }
        setCursor(Cursor.DEFAULT);
    }

    private void showOptionsOverlay() {
        if (showingOptionsOverlay) {
            return;
        }
        if (showingLevelOverlay) {
            hideOverlay();
        }
        if (showingMinigamesOverlay) {
            hideMinigamesOverlay();
        }
        refreshOptionsUI();
        showingOptionsOverlay = true;
        optionsLayer.setVisible(true);
        optionsLayer.setMouseTransparent(false);
        view.setMouseTransparent(true);
        setCursor(Cursor.DEFAULT);
    }

    private void hideOptionsOverlay() {
        showingOptionsOverlay = false;
        optionsLayer.setVisible(false);
        optionsLayer.setMouseTransparent(true);
        if (!showingLevelOverlay && !showingMinigamesOverlay) {
            view.setMouseTransparent(false);
        }
        setCursor(Cursor.DEFAULT);
    }

    private void refreshOptionsUI() {
        musicToggle.setSelected(AudioSettings.isMusicEnabled());
        musicSlider.setValue(AudioSettings.getMusicVolume());
        musicSlider.setDisable(!AudioSettings.isMusicEnabled());
        updateMusicValueLabel();
    }

    private void updateMusicValueLabel() {
        if (!AudioSettings.isMusicEnabled()) {
            musicValueLabel.setText("Muted");
            return;
        }
        double pct = Math.round(musicSlider.getValue() * 100);
        musicValueLabel.setText((int) pct + "%");
    }

    private void configureLevelButtons() {
        double overlayX = overlayImage != null ? overlayImage.getLayoutX() : 120;
        double overlayY = overlayImage != null ? overlayImage.getLayoutY() : 100;

        double buttonWidth = 90;
        double buttonHeight = 90;
        double spacing = 10;
        double firstX = overlayX + 60;
        double rowY = overlayY + 40;

        // Show levels 1-5 (level 4 is infinite mode)
        for (int i = 0; i < 5; i++) {
            double x = firstX + i * (buttonWidth + spacing);
            double[] bounds = {x, rowY, buttonWidth, buttonHeight};
            addLevelButton(bounds, i + 1);
        }

        double[] backBounds = {overlayX + 250, overlayY + 325, 140, 70};
        addLevelButton(backBounds, 0);
    }

    private void addLevelButton(double[] bounds, int levelNumber) {
        Button button = new Button(levelNumber == 0 ? "BACK" : "L" + levelNumber);
        button.setLayoutX(bounds[0]);
        button.setLayoutY(bounds[1]);
        button.setPrefWidth(bounds[2]);
        button.setPrefHeight(bounds[3]);
        button.setFont(Font.font("Arial", FontWeight.BOLD, levelNumber == 0 ? 18 : 16));
        button.setTextFill(Color.TRANSPARENT);
        button.setOpacity(0.0); // keep hitbox but hide visuals
        button.setFocusTraversable(false);
        button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        button.setCursor(Cursor.HAND);
        button.setOnAction(e -> {
            if (levelNumber == 0) {
                hideOverlay();
            } else {
                handleLevelSelection(levelNumber);
            }
        });
        overlayLayer.getChildren().add(button);
    }

    private String debugColorForLevel(int levelNumber) {
        return switch (levelNumber) {
            case 1 -> "rgba(255, 165, 0, 0.55)";
            case 2 -> "rgba(144, 238, 144, 0.55)";
            case 3 -> "rgba(100, 149, 237, 0.55)";
            case 4 -> "rgba(64, 224, 208, 0.55)";
            case 5 -> "rgba(255, 105, 180, 0.55)";
            default -> "rgba(128, 128, 128, 0.55)";
        };
    }

    private void handleLevelSelection(int levelNumber) {
        hideOverlay();
        if (handler != null) {
            handler.onLevelSelected(levelNumber);
        }
    }

    
}
